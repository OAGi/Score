import {ChangeDetectionStrategy, Component, Inject, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {OasDocHttpHeader, OasParameterLocation} from '../domain/openapi-doc';
import {
  adaptToLocation,
  defaultExplode,
  defaultStyle,
  DEFAULT_CONTENT_MEDIA_TYPE,
  directionLabel,
  exampleToText,
  hasErrors,
  locationOf,
  OasDocHttpHeaderErrors,
  PARAMETER_LOCATIONS,
  parseExampleText,
  parseSchemaText,
  serializationWarnings,
  schemaToText,
  Serialization,
  serializationOf,
  STYLES_BY_LOCATION,
  OasDocHttpHeaderContext,
  validateHeaderDraft,
  valueWarnings
} from '../domain/oas-doc-http-header-form';

/** What the dialog returns: the header to keep (its working copy), or that it is to be deleted. */
export type OasDocHttpHeaderDialogResult = { action: 'save'; header: OasDocHttpHeader } | { action: 'delete' };

/**
 * Issue #1791: dialog to add or edit one HTTP header of an operation (or of the whole document) of an OpenAPI
 * Document, or to delete it. It edits a working copy and returns it: the caller changes the document in the page, and
 * Update saves it.
 */
@Component({
  standalone: false,
  selector: 'score-oas-doc-http-header-dialog',
  templateUrl: './oas-doc-http-header-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./oas-doc-http-header-dialog.component.css']
})
export class OasDocHttpHeaderDialogComponent {
  private dialogRef = inject(MatDialogRef<OasDocHttpHeaderDialogComponent>);

  header: OasDocHttpHeader;
  isNew: boolean;
  locations = PARAMETER_LOCATIONS;
  /** What the dialog is about: a request parameter or a response header, as the column tells. */
  directionLabel: string;
  /** Where the header applies: one operation, or the whole document. */
  scopeLabel: string;
  /** The other headers of the document, to tell a header that is there already. */
  others: OasDocHttpHeader[];
  /** The path of the operation and the OpenAPI version of the document, which some of the rules are about. */
  context: OasDocHttpHeaderContext;

  schemaText: string;
  exampleText: string;
  serialization: Serialization;
  /** The media type that was given, kept while the value is serialized with a schema, in case the user comes back. */
  private rememberedMediaType?: string;

  constructor(@Inject(MAT_DIALOG_DATA) data: any) {
    this.header = data.header;
    this.isNew = !!data.isNew;
    this.directionLabel = directionLabel(this.header.direction);
    this.scopeLabel = data.scopeLabel || '';
    this.others = data.others || [];
    this.context = {resourcePath: data.resourcePath, openApiVersion: data.openApiVersion};
    this.schemaText = schemaToText(this.header.schema);
    this.exampleText = exampleToText(this.header.example);
    this.serialization = serializationOf(this.header);
  }

  get isRequest(): boolean {
    return this.header.direction === 'REQUEST';
  }

  /** Where the value is: what a request parameter says, always a header for a response header. */
  get location(): OasParameterLocation {
    return locationOf(this.header);
  }

  set location(location: OasParameterLocation) {
    if (!this.isRequest) {
      return; // a response header is always in a header
    }
    this.header.in = location;
    adaptToLocation(this.header);
    // OpenAPI recommends content for a cookie: it is what a new cookie starts with
    if (this.isNew && location === 'cookie' && this.serialization === 'schema') {
      this.setSerialization('content');
    }
  }

  get isPathParameter(): boolean {
    return this.isRequest && this.header.in === 'path';
  }

  get isQueryParameter(): boolean {
    return this.isRequest && this.location === 'query';
  }

  get styles(): string[] {
    return STYLES_BY_LOCATION[this.location];
  }

  get defaultStyleName(): string {
    return defaultStyle(this.location);
  }

  /** The style the value has: the one that was picked, or else the default of its location. */
  get styleChoice(): string {
    return this.effectiveStyle;
  }

  set styleChoice(style: string) {
    if (style === this.effectiveStyle) {
      return;
    }
    // the default of the location is not kept: it is the specification that says it
    if (style === this.defaultStyleName) {
      delete this.header.style;
    } else {
      this.header.style = style;
    }
    // the explode follows the style: it is the default of the new style, which is true for deepObject, as OpenAPI wants it
    delete this.header.explode;
    if (style === 'deepObject') {
      this.header.explode = true;
    }
  }

  /** The style the value has now: the one that was picked, or the default of its location. */
  get effectiveStyle(): string {
    return this.header.style ?? defaultStyle(this.location);
  }

  /** The explode the style has by default, which the list of explodes marks. */
  get defaultExplodeOfStyle(): boolean {
    return defaultExplode(this.effectiveStyle);
  }

  /** 'true' or 'false': the explode that was picked, or else the default of the style. */
  get explodeChoice(): string {
    return String(this.header.explode ?? this.defaultExplodeOfStyle);
  }

  set explodeChoice(choice: string) {
    const explode = choice === 'true';
    // what is the default of the style is not kept: it is the specification that says it, and the expression has no need to
    if (explode === this.defaultExplodeOfStyle) {
      delete this.header.explode;
    } else {
      this.header.explode = explode;
    }
  }

  /** 'true' or 'false': what is false, its default, is not kept. */
  get allowReservedChoice(): string {
    return String(!!this.header.allowReserved);
  }

  set allowReservedChoice(choice: string) {
    if (choice === 'true') {
      this.header.allowReserved = true;
    } else {
      delete this.header.allowReserved;
    }
  }

  setSerialization(serialization: Serialization) {
    this.serialization = serialization;
    if (serialization === 'content') {
      this.header.contentMediaType = this.rememberedMediaType || DEFAULT_CONTENT_MEDIA_TYPE;
      // a style, explode and allowReserved are of the schema, not of content
      delete this.header.style;
      delete this.header.explode;
      delete this.header.allowReserved;
    } else {
      this.rememberedMediaType = this.header.contentMediaType;
      delete this.header.contentMediaType;
    }
  }

  /** What OpenAPI recommends against, in the value as it is given now: nothing when there is nothing wrong with it. */
  get warnings(): string[] {
    const schema = parseSchemaText(this.schemaText).schema;
    const example = parseExampleText(this.exampleText).example;
    return [...serializationWarnings(this.location, this.serialization, schema, example),
      ...valueWarnings(this.header, schema, example, this.serialization)];
  }

  get errors(): OasDocHttpHeaderErrors {
    return validateHeaderDraft({
      name: this.header.name,
      in: this.header.in,
      style: this.header.style,
      explode: this.header.explode,
      allowReserved: this.header.allowReserved,
      contentMediaType: this.header.contentMediaType,
      schemaText: this.schemaText,
      exampleText: this.exampleText,
      direction: this.header.direction
    }, this.others, this.header.oasOperationId, this.context);
  }

  get isValid(): boolean {
    return !hasErrors(this.errors);
  }

  save() {
    if (!this.isValid) {
      return;
    }
    const header: OasDocHttpHeader = {
      oasOperationId: this.header.oasOperationId,
      name: this.header.name.trim(),
      description: this.header.description?.trim() || undefined,
      schema: parseSchemaText(this.schemaText).schema as any,
      example: parseExampleText(this.exampleText).example,
      direction: this.header.direction,
      ...(this.isRequest ? {in: this.header.in ?? 'header'} : {}),
      // OpenAPI: a path parameter MUST be required
      required: this.isPathParameter ? true : this.header.required
    };
    if (this.serialization === 'content') {
      header.contentMediaType = (this.header.contentMediaType as string).trim();
    } else {
      if (this.header.style) {
        header.style = this.header.style;
      }
      if (this.header.explode !== undefined && this.header.explode !== null) {
        header.explode = this.header.explode;
      }
      if (this.isQueryParameter && this.header.allowReserved) {
        header.allowReserved = true;
      }
    }
    this.dialogRef.close({action: 'save', header} as OasDocHttpHeaderDialogResult);
  }

  delete() {
    this.dialogRef.close({action: 'delete'} as OasDocHttpHeaderDialogResult);
  }

  cancel() {
    this.dialogRef.close();
  }
}
