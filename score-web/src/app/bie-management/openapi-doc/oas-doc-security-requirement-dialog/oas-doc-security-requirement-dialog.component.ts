import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {
  OasSecurityRequirement,
  OasSecurityRequirementScheme,
  OasSecurityScheme
} from '../domain/openapi-doc';

export interface OasDocSecurityRequirementDialogData {
  title: string;
  securitySchemes: OasSecurityScheme[];
  securityRequirements: OasSecurityRequirement[];
  securityOverridden?: boolean;
  allowInherit?: boolean;
}

/**
 * Issue #1729: edits a `security` array (a list of Security Requirement Objects). Each requirement is an
 * OR alternative; the schemes within a requirement are ANDed. A requirement may be marked anonymous ({}).
 * For operations the dialog also exposes Inherit / No security (public, []) / Custom. oauth2 scopes are
 * chosen from the scheme's declared flow scopes; openIdConnect scopes are free text (provider-defined);
 * apiKey/http carry no scopes (emitted as []).
 */
@Component({
  standalone: false,
  selector: 'score-oas-doc-security-requirement-dialog',
  templateUrl: './oas-doc-security-requirement-dialog.component.html',
  styleUrls: ['./oas-doc-security-requirement-dialog.component.css']
})
export class OasDocSecurityRequirementDialogComponent {

  securitySchemes: OasSecurityScheme[] = [];
  requirements: OasSecurityRequirement[] = [];
  mode: 'inherit' | 'none' | 'custom' = 'custom';

  constructor(
    private dialogRef: MatDialogRef<OasDocSecurityRequirementDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: OasDocSecurityRequirementDialogData) {
    this.securitySchemes = data.securitySchemes || [];
    this.requirements = JSON.parse(JSON.stringify(data.securityRequirements || []));
    this.requirements.forEach(r => {
      if (!r.schemes) {
        r.schemes = [];
      }
    });
    // Resolve the initial mode. For an operation (allowInherit): not overridden => inherit; overridden
    // with NO requirements => 'none' (Public, security: []); overridden with requirements => custom.
    // The root editor (no inherit/public modes) is always custom.
    if (!data.allowInherit) {
      this.mode = 'custom';
    } else if (!data.securityOverridden) {
      this.mode = 'inherit';
    } else if (this.requirements.length === 0) {
      this.mode = 'none';
    } else {
      this.mode = 'custom';
    }
    if (this.mode === 'custom') {
      this.ensureEditableRequirement();
    }
  }

  // ----- requirements (OR alternatives) -----
  addRequirement(): void {
    // Default a new alternative to a scheme not already used as a sole single-scheme requirement, so two
    // identical alternatives are not created by default when distinct ones are still possible.
    const usedSole = new Set<string>(this.requirements
      .filter(r => !r.anonymous && (r.schemes || []).length === 1 && !!r.schemes[0].schemeName)
      .map(r => r.schemes[0].schemeName));
    const distinct = this.securitySchemes.find(s => !usedSole.has(s.schemeName));
    const schemeName = distinct ? distinct.schemeName
      : (this.securitySchemes[0] ? this.securitySchemes[0].schemeName : undefined);
    this.requirements.push({anonymous: false, schemes: [this.newSchemeEntry(schemeName)]});
  }

  // A normalized signature of a requirement so identical OR alternatives can be detected regardless of
  // scheme/scope ordering. Incomplete (no scheme chosen, not anonymous) requirements get no signature.
  private requirementSignature(requirement: OasSecurityRequirement): string {
    if (requirement.anonymous) {
      return 'anon';
    }
    const schemes = (requirement.schemes || [])
      .filter(s => !!s.schemeName)
      .map(s => {
        const scopes = (this.supportsScopes(s.schemeName) ? (s.scopes || []) : []).slice().sort();
        return s.schemeName + '' + scopes.join('');
      })
      .sort();
    return schemes.length === 0 ? '' : 'set:' + schemes.join('');
  }

  // Index of the earlier OR alternative this one exactly duplicates, or -1. Only complete requirements
  // (anonymous, or with at least one chosen scheme) are considered.
  duplicateOf(index: number): number {
    const sig = this.requirementSignature(this.requirements[index]);
    if (!sig) {
      return -1;
    }
    for (let i = 0; i < index; i++) {
      if (this.requirementSignature(this.requirements[i]) === sig) {
        return i;
      }
    }
    return -1;
  }

  isDuplicate(index: number): boolean {
    return this.duplicateOf(index) >= 0;
  }

  private hasDuplicates(): boolean {
    return this.requirements.some((_, i) => this.duplicateOf(i) >= 0);
  }

  removeRequirement(index: number): void {
    this.requirements.splice(index, 1);
  }

  toggleAnonymous(requirement: OasSecurityRequirement): void {
    if (requirement.anonymous) {
      requirement.schemes = [];
    } else if (!requirement.schemes || requirement.schemes.length === 0) {
      requirement.schemes = [this.newSchemeEntry()];
    }
  }

  onModeChange(mode: 'inherit' | 'none' | 'custom'): void {
    if (mode === 'custom') {
      this.ensureEditableRequirement();
    }
  }

  // ----- scheme entries (AND members of one requirement) -----
  newSchemeEntry(schemeName?: string): OasSecurityRequirementScheme {
    const name = schemeName !== undefined
      ? schemeName
      : (this.securitySchemes[0] ? this.securitySchemes[0].schemeName : undefined);
    return {schemeName: name, scopes: []};
  }

  // Scheme names already used by other AND entries of this requirement. A Security Requirement Object is
  // a map keyed by scheme name, so each scheme may appear at most once within one requirement.
  private usedSchemeNames(requirement: OasSecurityRequirement, except?: OasSecurityRequirementScheme): Set<string> {
    const used = new Set<string>();
    (requirement.schemes || []).forEach(s => {
      if (s !== except && s.schemeName) {
        used.add(s.schemeName);
      }
    });
    return used;
  }

  private firstUnusedScheme(requirement: OasSecurityRequirement): string | undefined {
    const used = this.usedSchemeNames(requirement);
    const scheme = this.securitySchemes.find(s => !used.has(s.schemeName));
    return scheme ? scheme.schemeName : undefined;
  }

  // True when another AND entry in the same requirement already uses this scheme (so its option is
  // disabled in the dropdown — no duplicate schemes within one requirement).
  isSchemeTaken(requirement: OasSecurityRequirement, entry: OasSecurityRequirementScheme, schemeName: string): boolean {
    return this.usedSchemeNames(requirement, entry).has(schemeName);
  }

  // AND only makes sense across DISTINCT schemes, so adding is allowed only while an unused scheme remains.
  canAddSchemeEntry(requirement: OasSecurityRequirement): boolean {
    return !requirement.anonymous && this.firstUnusedScheme(requirement) !== undefined;
  }

  addSchemeEntry(requirement: OasSecurityRequirement): void {
    if (!requirement.schemes) {
      requirement.schemes = [];
    }
    const unused = this.firstUnusedScheme(requirement);
    if (unused === undefined) {
      return;
    }
    requirement.schemes.push(this.newSchemeEntry(unused));
  }

  removeSchemeEntry(requirement: OasSecurityRequirement, index: number): void {
    requirement.schemes.splice(index, 1);
    if (requirement.schemes.length === 0) {
      requirement.schemes.push(this.newSchemeEntry());
    }
  }

  onSchemeChange(entry: OasSecurityRequirementScheme): void {
    entry.scopes = [];
  }

  // ----- scope helpers -----
  private findScheme(schemeName: string): OasSecurityScheme {
    return this.securitySchemes.find(s => s.schemeName === schemeName);
  }

  isOAuth2(schemeName: string): boolean {
    const s = this.findScheme(schemeName);
    return !!s && s.type === 'oauth2';
  }

  isOidc(schemeName: string): boolean {
    const s = this.findScheme(schemeName);
    return !!s && s.type === 'openIdConnect';
  }

  supportsScopes(schemeName: string): boolean {
    return this.isOAuth2(schemeName) || this.isOidc(schemeName);
  }

  // oauth2 options = the scheme's declared flow scopes, unioned with any already-selected scopes so a
  // saved scope is never silently dropped from the multiselect.
  oauth2ScopeOptions(entry: OasSecurityRequirementScheme): string[] {
    const scheme = this.findScheme(entry.schemeName);
    const options = new Set<string>();
    if (scheme && scheme.type === 'oauth2') {
      (scheme.flows || []).forEach(flow => (flow.scopes || []).forEach(scope => {
        if (scope.scopeName) {
          options.add(scope.scopeName);
        }
      }));
    }
    (entry.scopes || []).forEach(s => {
      if (s) {
        options.add(s);
      }
    });
    return Array.from(options);
  }

  freeTextScopes(entry: OasSecurityRequirementScheme): string {
    return (entry.scopes || []).join(', ');
  }

  setFreeTextScopes(entry: OasSecurityRequirementScheme, value: string): void {
    entry.scopes = (value || '').split(',').map(v => v.trim()).filter(v => !!v);
  }

  // ----- apply / validity -----
  canApply(): boolean {
    if (this.mode !== 'custom') {
      return true;
    }
    if (this.data.allowInherit && this.requirements.length === 0) {
      return false;
    }
    if (this.hasDuplicates()) {
      return false;
    }
    return this.requirements.every(req =>
      req.anonymous || (req.schemes || []).some(s => !!s.schemeName));
  }

  apply(): void {
    if (this.mode === 'inherit') {
      this.dialogRef.close({securityOverridden: false, securityRequirements: []});
      return;
    }
    if (this.mode === 'none') {
      this.dialogRef.close({securityOverridden: true, securityRequirements: []});
      return;
    }
    const securityRequirements: OasSecurityRequirement[] = this.requirements
      .map(req => {
        if (req.anonymous) {
          return {anonymous: true, schemes: []};
        }
        return {
          anonymous: false,
          schemes: (req.schemes || [])
            .filter(s => !!s.schemeName)
            .map(s => ({
              schemeName: s.schemeName,
              scopes: this.supportsScopes(s.schemeName) ? (s.scopes || []) : []
            }))
        };
      })
      .filter(req => req.anonymous || req.schemes.length > 0);
    this.dialogRef.close({securityOverridden: true, securityRequirements});
  }

  cancel(): void {
    this.dialogRef.close();
  }

  private ensureEditableRequirement(): void {
    if (this.securitySchemes.length > 0 && this.requirements.length === 0) {
      this.addRequirement();
    }
  }
}
