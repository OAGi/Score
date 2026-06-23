import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {BieViewOrderEntry, BieViewOrderUpdateEntry} from './bie-view-order';

/**
 * Reads/writes the instance-level sibling view order (Issue #1638). The weights are fetched LAZILY,
 * one view-parent ACC at a time, as each ACC node is expanded in the Model Browser or BIE editor
 * (group/choice flattening happens on the client, so only it knows a child's view parent), and
 * applied on the frontend when flattening the sibling list. NOT used for generated output or the CC
 * editor. The data shapes and the pure ordering logic live in {@link ./bie-view-order}.
 */
@Injectable({providedIn: 'root'})
export class BieViewOrderService {
  private http = inject(HttpClient);

  /** The view-order weights stored under the view-parent {@code accManifestId} (empty if none). */
  getViewOrder(accManifestId: number): Observable<BieViewOrderEntry[]> {
    return this.http.get<BieViewOrderEntry[]>('/api/bie-view-order/acc/' + accManifestId);
  }

  /** Upsert the affected child weights under the view parent {@code accManifestId} (developer-only). */
  updateViewOrder(accManifestId: number, entries: BieViewOrderUpdateEntry[]): Observable<void> {
    return this.http.put<void>('/api/bie-view-order/acc/' + accManifestId, {entries});
  }

  /** Remove every child weight stored directly under the view parent {@code accManifestId} (developer-only). */
  resetViewOrder(accManifestId: number): Observable<void> {
    return this.http.delete<void>('/api/bie-view-order/acc/' + accManifestId);
  }
}
