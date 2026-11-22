/**
 * What the search integrity checks (`*.integrity.spec.ts`) need to reach the real data: a running backend and the
 * database behind it, which the checks ask directly with SQL (independently of the backend) to know what a search has
 * to find. The checks are skipped unless `SEARCH_INTEGRITY_BACKEND` is set, e.g.
 *
 *   SEARCH_INTEGRITY_BACKEND=http://127.0.0.1:9011 SEARCH_INTEGRITY_DOCKER=beautiful_bassi npx vitest run integrity
 *
 * `SEARCH_INTEGRITY_DOCKER` names the docker container that runs MariaDB (database `oagi`, user and password `oagi`);
 * `SEARCH_INTEGRITY_USER` and `SEARCH_INTEGRITY_PASSWORD` are the application login (default `oagis`).
 */
import {execFileSync} from 'node:child_process';

export const BACKEND = process.env['SEARCH_INTEGRITY_BACKEND'];
export const ENABLED = !!BACKEND;

const CONTAINER = process.env['SEARCH_INTEGRITY_DOCKER'] ?? 'beautiful_bassi';

/** Rows of an SQL query as arrays of strings (NULL is the string `NULL`). */
export function sql(query: string): string[][] {
  const out = execFileSync('docker', ['exec', '-i', CONTAINER, 'mariadb', '-uoagi', '-poagi', 'oagi', '--default-character-set=utf8mb3',
    '-N', '-B', '--quick', '-e', query], {encoding: 'utf8', maxBuffer: 1 << 30});
  return out.split('\n').filter(line => line.length > 0).map(line => line.split('\t'));
}

/** One number the query returns. */
export function sqlNumber(query: string): number {
  return Number(sql(query)[0][0]);
}

/** An SQL string literal (the input is only what the checks generate from the data, but quotes must hold). */
export function lit(value: string): string {
  return '\'' + value.replace(/\\/g, '\\\\').replace(/'/g, '\'\'') + '\'';
}

let cookie: string | undefined;

async function login(): Promise<string> {
  if (!cookie) {
    const response = await fetch(BACKEND + '/login', {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: new URLSearchParams({
        username: process.env['SEARCH_INTEGRITY_USER'] ?? 'oagis',
        password: process.env['SEARCH_INTEGRITY_PASSWORD'] ?? 'oagis'
      })
    });
    if (!response.ok) {
      throw new Error('The login failed: ' + response.status);
    }
    cookie = response.headers.getSetCookie().map(c => c.split(';')[0]).join('; ');
  }
  return cookie;
}

/** A request to the backend as the logged-in user. */
export async function backend<T>(path: string, body?: unknown): Promise<T> {
  const response = await fetch(BACKEND + path, {
    method: body === undefined ? 'GET' : 'POST',
    headers: {Cookie: await login(), ...(body === undefined ? {} : {'Content-Type': 'application/json'})},
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  if (!response.ok) {
    throw new Error(path + ' answered ' + response.status + ': ' + (await response.text()).substring(0, 300));
  }
  return response.json() as Promise<T>;
}
