package org.oagi.score.gateway.http.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Score HTTP Gateway",
                version = "3.4",
                description = """
                        The **Score HTTP Gateway** is the backend API layer of the [Score](https://github.com/OAGi/Score) platform, designed to facilitate seamless interaction between the UI and database through a robust set of RESTful endpoints.
                        
                        It provides comprehensive support for modeling, managing, and validating enterprise data structures in conformance with the [UN/CEFACT Core Component Technical Specification (CCTS) Version 3.0](https://unece.org/trade/documents/2009/09/uncefact-core-components-technical-specification-version-30).
                        
                        ### Key Responsibilities
                        - Serve as the primary interface between UI clients and backend data models
                        - Enable structured access to modeling artifacts and business entities
                        - Enforce business rules, semantic constraints, and data integrity
                        - Support interoperable, standards-based data modeling
                        
                        ### Architectural Pattern
                        The API design adheres to **Command Query Responsibility Segregation (CQRS)**:
                        - **Queries**: Endpoints for retrieving data with no side effects
                        - **Commands**: Endpoints for creating, updating, or deleting data
                        
                        This separation of concerns improves clarity, scalability, and maintainability.
                        
                        ### Core Features
                        The gateway focuses on managing the following key modeling constructs:
                        
                        - **Core Components**
                          Based on the CCTS metamodel, Score supports the full range of core component types:
                          - `ACC` (Aggregate Core Component): Container for properties (ASCCs and BCCs)
                          - `ASCC` (Association Core Component): Represents an association from an ACC to an ASCCP
                          - `BCC` (Basic Core Component): Represents a use of a BCCP within an ACC
                          - `ASCCP` (Association Core Component Property): References another ACC as a reusable role
                          - `BCCP` (Basic Core Component Property): Represents a reusable property with a specific data type
                          - `DT` (Data Type): Defines primitive and derived types, optionally with:
                            - `SC` (Supplementary Components): Additional attributes attached to data types
                          - `Code List` and `Agency ID List`: Value domains used by data types for standardized enumerations
                        
                          Relationships are strictly modeled per CCTS:
                          - A `BCCP` must reference a `DT` as its underlying type.
                          - An `ASCCP` must reference an `ACC` as its role class.
                          - `ACC` structures are composed using `ASCC` and `BCC`, each referencing `ASCCP` or `BCCP` respectively.
                        
                        - **Business Information Entities (BIEs)**
                          BIEs extend and contextualize Core Components for specific business applications. Their structure mirrors the relationships in Core Components:
                          - `ABIE` (Aggregate BIE) is based on `ACC`, representing a high-level container for other BIEs.
                          - `ASBIE` (Association BIE) and `BBIE` (Basic BIE) are based on `ASCC` and `BCC`, respectively, representing associations and properties that extend the business context.
                          - `ASBIEP` and `BBIEP` are based on `ASCCP` and `BCCP`, respectively, representing the relationship between business components and reusable properties or roles.
                        
                        - **Contexts**
                          Contexts define the specific conditions or environments under which BIEs are interpreted or applied. They ensure that data models are accurate and conform to the requirements of the specific domain or business context in which they are being used. Contexts may vary based on:
                          - Industry standards, such as retail, finance, or healthcare
                          - Regulatory or geographic factors (e.g., regional compliance requirements)
                          - Business processes or workflows that influence how data should be structured and interpreted
                          - Application-specific conditions that modify or extend the use of BIEs
        
                          The gateway supports several context management features, which help ensure data consistency and traceability in various environments:
                          - `Context Category`: Define and categorize different context types to organize the way contexts are applied to business entities.
                          - `Context Scheme`: Define context schemes with associated values, ensuring standardized use of contexts across various systems and business processes.
                          - `Business Context`: Define business-specific contexts along with their associated values, allowing precise control over how BIEs are applied in specific business environments or processes.
                        
                        These context management capabilities are critical for ensuring that the modeling constructs are used appropriately in different business settings, fostering interoperability and semantic clarity.
                        
                        ### Intended Use
                        The Score HTTP Gateway is intended for:
                        - UI applications built atop the Score platform
                        - System integrators and service developers
                        - Tools and services requiring structured, standards-based data modeling APIs
                        
                        It delivers a consistent, modular, and extensible interface to power collaborative data governance and enterprise modeling.
                        """,
                contact = @Contact(name = "Support", email = "member.services@oagi.org"),
                license = @License(name = "MIT License", url = "https://github.com/OAGi/Score/blob/master/LICENSE.txt")
        )
)
public class SwaggerConfig {

}
