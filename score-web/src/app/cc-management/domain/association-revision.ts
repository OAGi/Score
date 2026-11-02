import {AsccSummary, BccSummary} from './core-component-node';

type AssociationSummary = AsccSummary | BccSummary;

export function findPreviousAssociation(
    type: 'ASCC',
    manifestId: number,
    previousManifestId: number,
    previousAssociations: AssociationSummary[]): AsccSummary | null;
export function findPreviousAssociation(
    type: 'BCC',
    manifestId: number,
    previousManifestId: number,
    previousAssociations: AssociationSummary[]): BccSummary | null;
export function findPreviousAssociation(
    type: 'ASCC' | 'BCC',
    manifestId: number,
    previousManifestId: number,
    previousAssociations: AssociationSummary[]): AssociationSummary | null {
  if (!previousAssociations) {
    return null;
  }

  if (type === 'ASCC') {
    const previousAssociation = previousManifestId == null ? null : previousAssociations.find((association): association is AsccSummary =>
        'asccManifestId' in association && association.asccManifestId === previousManifestId);
    return previousAssociation || previousAssociations.find((association): association is AsccSummary =>
        'asccManifestId' in association && (
            association.nextAsccManifestId === manifestId ||
            (association.nextAsccManifestId == null && association.asccManifestId === manifestId)
        )) || null;
  }

  const previousAssociation = previousManifestId == null ? null : previousAssociations.find((association): association is BccSummary =>
      'bccManifestId' in association && association.bccManifestId === previousManifestId);
  return previousAssociation || previousAssociations.find((association): association is BccSummary =>
      'bccManifestId' in association && (
          association.nextBccManifestId === manifestId ||
          (association.nextBccManifestId == null && association.bccManifestId === manifestId)
      )) || null;
}
