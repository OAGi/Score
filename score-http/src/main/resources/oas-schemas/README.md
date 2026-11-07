The JSON Schemas of the OpenAPI Specification, as published by the OpenAPI Initiative (Apache License 2.0):

- `oas-3.0.4-schema.json`: https://spec.openapis.org/oas/3.0/schema/2024-10-18
- `oas-3.1.2-schema.json`: https://spec.openapis.org/oas/3.1/schema/2025-09-15 (without Schema Object validation)
- `oas-3.1.2-schema-base.json`: https://spec.openapis.org/oas/3.1/schema-base/2025-09-15 (with Schema Object validation)
- `oas-3.1-dialect.json`: https://spec.openapis.org/oas/3.1/dialect/2024-11-10
- `oas-3.1-meta.json`: https://spec.openapis.org/oas/3.1/meta/2024-11-10

They are loaded from here, never from the network, to check the HTTP headers of an OpenAPI Document (Issue #1791).
