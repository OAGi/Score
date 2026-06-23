import {defineConfig, devices} from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  reporter: 'list',
  use: {
    // "localhost" resolves to both 127.0.0.1 and ::1, so reuseExistingServer below matches an already
    // running `ng serve` regardless of which it bound (it binds ::1 by default), instead of spawning a
    // redundant 127.0.0.1 instance — and the API e2e specs default to the same host.
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry'
  },
  webServer: {
    command: 'npm start -- --host localhost --port 4200',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env.CI
  },
  projects: [
    {
      name: 'chromium',
      use: {...devices['Desktop Chrome']}
    }
  ]
});
