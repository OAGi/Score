import {NamespaceSummary} from '../../namespace-management/domain/namespace';

/** The namespaces a detail panel offers: the current one, plus the standard ones to a developer and the rest to anyone else. */
export function selectableNamespaces(namespaces: NamespaceSummary[], isDeveloper: boolean, namespaceId?: number): NamespaceSummary[] {
  return namespaces.filter(e => {
    if (!!namespaceId && e.namespaceId === namespaceId) {
      return true;
    }
    return isDeveloper ? e.standard : !e.standard;
  });
}
