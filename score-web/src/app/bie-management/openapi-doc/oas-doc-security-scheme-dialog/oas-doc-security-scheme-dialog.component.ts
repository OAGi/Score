import {Component, Inject, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {OasOAuthFlow, OasSecurityScheme} from '../domain/openapi-doc';

/**
 * Issue #1729: dialog to add or edit a single OpenAPI Security Scheme. For oauth2 schemes it embeds a
 * full OAuth Flows Object editor (multiple flows, each with its own URLs and scopes). The dialog edits
 * a working copy and returns it on save; the caller persists the whole document.
 */
@Component({
  standalone: false,
  selector: 'score-oas-doc-security-scheme-dialog',
  templateUrl: './oas-doc-security-scheme-dialog.component.html',
  styleUrls: ['./oas-doc-security-scheme-dialog.component.css']
})
export class OasDocSecuritySchemeDialogComponent {
  private dialogRef = inject(MatDialogRef<OasDocSecuritySchemeDialogComponent>);

  scheme: OasSecurityScheme;
  isNew: boolean;
  existingNames: string[];   // other schemes' names, for uniqueness

  // Remembers the fields entered for each type so switching type away and back restores them,
  // instead of losing the data and re-seeding defaults.
  private typeFieldCache: { [type: string]: Partial<OasSecurityScheme> } = {};
  private previousType: string;

  securitySchemeTypes = [
    {value: 'apiKey', label: 'API Key'},
    {value: 'http', label: 'HTTP'},
    {value: 'oauth2', label: 'OAuth 2.0'},
    {value: 'openIdConnect', label: 'OpenID Connect'}
  ];
  // deviceAuthorization is an OpenAPI 3.2+ flow; the document targets 3.0.3, so it is not offered here
  // (the generator also gates it on version >= 3.2). Re-add when 3.2 output is supported.
  flowTypes = [
    {value: 'authorizationCode', label: 'Authorization Code'},
    {value: 'implicit', label: 'Implicit'},
    {value: 'password', label: 'Password'},
    {value: 'clientCredentials', label: 'Client Credentials'}
  ];

  constructor(@Inject(MAT_DIALOG_DATA) data: any) {
    this.scheme = data.scheme || ({type: 'apiKey'} as OasSecurityScheme);
    this.isNew = !!data.isNew;
    this.existingNames = data.existingNames || [];
    if (!this.scheme.type) {
      this.scheme.type = 'apiKey';
    }
    this.previousType = this.scheme.type;
    this.ensureTypeDefaults(false);
  }

  // Seed example defaults for the current type (and the Scheme Name when blank).
  ensureTypeDefaults(resetName: boolean) {
    const s = this.scheme;
    if (s.type === 'apiKey') {
      if (!s.apiKeyIn) {
        s.apiKeyIn = 'header';
      }
      if (!s.apiKeyName) {
        s.apiKeyName = 'X-API-Key';
      }
    } else if (s.type === 'http') {
      if (!s.httpScheme) {
        s.httpScheme = 'bearer';
      }
    } else if (s.type === 'openIdConnect') {
      if (!s.openIdConnectUrl) {
        s.openIdConnectUrl = 'https://example.com/.well-known/openid-configuration';
      }
    } else if (s.type === 'oauth2') {
      if (!s.flows || s.flows.length === 0) {
        s.flows = [this.newFlow('authorizationCode')];
      }
    }
    if (resetName || !s.schemeName) {
      s.schemeName = this.defaultSchemeName();
    }
  }

  onTypeChange() {
    const s = this.scheme;
    // Remember what was entered for the type we're leaving.
    this.typeFieldCache[this.previousType] = {
      schemeName: s.schemeName,
      apiKeyIn: s.apiKeyIn,
      apiKeyName: s.apiKeyName,
      httpScheme: s.httpScheme,
      bearerFormat: s.bearerFormat,
      openIdConnectUrl: s.openIdConnectUrl,
      flows: s.flows
    };
    // Clear all type-specific fields.
    s.apiKeyIn = undefined;
    s.apiKeyName = undefined;
    s.httpScheme = undefined;
    s.bearerFormat = undefined;
    s.openIdConnectUrl = undefined;
    s.flows = undefined;

    const cached = this.typeFieldCache[s.type];
    if (cached) {
      // Returning to a type we already edited: restore exactly what was there.
      s.schemeName = cached.schemeName;
      s.apiKeyIn = cached.apiKeyIn;
      s.apiKeyName = cached.apiKeyName;
      s.httpScheme = cached.httpScheme;
      s.bearerFormat = cached.bearerFormat;
      s.openIdConnectUrl = cached.openIdConnectUrl;
      s.flows = cached.flows;
    } else {
      // First time on this type: seed example defaults + the default Scheme Name.
      this.ensureTypeDefaults(true);
    }
    this.previousType = s.type;
  }

  onHttpSchemeChange() {
    if (this.scheme.httpScheme !== 'bearer') {
      this.scheme.bearerFormat = undefined;
    }
    this.scheme.schemeName = this.defaultSchemeName();
  }

  defaultSchemeName(): string {
    const s = this.scheme;
    const base = s.type === 'apiKey' ? 'ApiKeyAuth'
      : s.type === 'http' ? (s.httpScheme === 'basic' ? 'BasicAuth' : 'BearerAuth')
        : s.type === 'openIdConnect' ? 'OpenID'
          : 'OAuth2';
    const used = new Set(this.existingNames);
    if (!used.has(base)) {
      return base;
    }
    let i = 2;
    while (used.has(base + i)) {
      i++;
    }
    return base + i;
  }

  // ----- oauth2 flows -----
  newFlow(flowType: string): OasOAuthFlow {
    const flow: OasOAuthFlow = {
      flowType,
      scopes: [
        {scopeName: 'read', description: 'Grants read access'},
        {scopeName: 'write', description: 'Grants write access'},
        {scopeName: 'admin', description: 'Grants access to admin operations'}
      ]
    };
    if (this.needsAuthUrl(flowType)) {
      flow.authorizationUrl = 'https://example.com/oauth/authorize';
    }
    if (this.needsTokenUrl(flowType)) {
      flow.tokenUrl = 'https://example.com/oauth/token';
    }
    if (this.needsDeviceUrl(flowType)) {
      flow.deviceAuthorizationUrl = 'https://example.com/oauth/device';
    }
    return flow;
  }

  addFlow() {
    if (!this.scheme.flows) {
      this.scheme.flows = [];
    }
    const unused = this.flowTypes.map(t => t.value).find(v => !this.scheme.flows.some(f => f.flowType === v));
    this.scheme.flows.push(this.newFlow(unused || 'authorizationCode'));
  }

  removeFlow(index: number) {
    this.scheme.flows.splice(index, 1);
  }

  onFlowTypeChange(flow: OasOAuthFlow) {
    if (!this.needsAuthUrl(flow.flowType)) {
      flow.authorizationUrl = undefined;
    } else if (!flow.authorizationUrl) {
      flow.authorizationUrl = 'https://example.com/oauth/authorize';
    }
    if (!this.needsTokenUrl(flow.flowType)) {
      flow.tokenUrl = undefined;
    } else if (!flow.tokenUrl) {
      flow.tokenUrl = 'https://example.com/oauth/token';
    }
    if (!this.needsDeviceUrl(flow.flowType)) {
      flow.deviceAuthorizationUrl = undefined;
    } else if (!flow.deviceAuthorizationUrl) {
      flow.deviceAuthorizationUrl = 'https://example.com/oauth/device';
    }
  }

  addScope(flow: OasOAuthFlow) {
    if (!flow.scopes) {
      flow.scopes = [];
    }
    flow.scopes.push({scopeName: '', description: ''});
  }

  removeScope(flow: OasOAuthFlow, index: number) {
    flow.scopes.splice(index, 1);
  }

  needsAuthUrl(flowType: string): boolean {
    return flowType === 'implicit' || flowType === 'authorizationCode';
  }

  needsTokenUrl(flowType: string): boolean {
    return flowType === 'password' || flowType === 'clientCredentials'
      || flowType === 'authorizationCode' || flowType === 'deviceAuthorization';
  }

  needsDeviceUrl(flowType: string): boolean {
    return flowType === 'deviceAuthorization';
  }

  get isValid(): boolean {
    const s = this.scheme;
    const name = (s.schemeName || '').trim();
    if (!name || this.existingNames.includes(name)) {
      return false;
    }
    if (!s.type) {
      return false;
    }
    if (s.type === 'apiKey') {
      return !!s.apiKeyIn && !!s.apiKeyName;
    }
    if (s.type === 'http') {
      return !!s.httpScheme;
    }
    if (s.type === 'openIdConnect') {
      return !!s.openIdConnectUrl;
    }
    if (s.type === 'oauth2') {
      // An oauth2 scheme must declare at least one flow (otherwise it generates a useless `flows: {}`).
      if (!s.flows || s.flows.length === 0) {
        return false;
      }
      const types = new Set<string>();
      for (const f of s.flows) {
        if (!f.flowType || types.has(f.flowType)) {
          return false;
        }
        types.add(f.flowType);
        if (this.needsAuthUrl(f.flowType) && !f.authorizationUrl) {
          return false;
        }
        if (this.needsTokenUrl(f.flowType) && !f.tokenUrl) {
          return false;
        }
        if (this.needsDeviceUrl(f.flowType) && !f.deviceAuthorizationUrl) {
          return false;
        }
        if (f.scopes) {
          for (const sc of f.scopes) {
            if (!sc.scopeName || !sc.scopeName.trim()) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  cancel() {
    this.dialogRef.close(null);
  }

  save() {
    if (this.isValid) {
      this.dialogRef.close(this.scheme);
    }
  }
}
