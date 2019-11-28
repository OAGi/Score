# Score

The dotcom boom drove tremendous progress in system-interoperability standards. Brilliant and passionate collaborators from across the world and across industries produced standards for modeling processes, specifying partner information, developing registries, modeling data at various abstraction levels and so on. In many cases great work was ignored,  underutilized, or painfully implemented as a result of inadequate tooling. One of those standards was the Core Component Technical Specification (CCTS) from the United Nations Centre for Trade Facilitation and Electronic Business (UN/CEFACT), which has corresponding ISO specification: ISO 15000-5, Core Component Specification (CCS). 

The Open Applications Group (OAGi) and its members endured painful experience of implementing CCTS/CCS (hereafter just CCS). Something had to be done. OAGi has enjoyed a long and productive collaborative relationship with the National Institute of Standards and Technology (NIST, under the United States Department of Commerce). NIST researchers took initiative to develop a proof-of-concept application that would enable CCS-based library development and maintenance, and contextual profile development and maintenance. OAGi provided a rich CCS-compliant library, design input, and real-world testing.

Fast forward a few years and we have a tool: Score. Score has become an indispensable tool for multi-national companies in agriculture, aerospace, payroll processing, enterprise resource planning, and more. Score is now available as an open source project.

## Specify Context

Clearly specified context is a prerequisite to effective data model implementations. Key context categories include geopolitical, process, and industry. Score supports unlimited context categories, unlimited possible values for each categories, and unlimited business context definitions (set of context category/value pairs).

## Select a Core Component

Once you have selected a specified context, select a core component (message). For example, perhaps you specify a context of order-to-cash process for crop nutrition products in New Zealand. Then you choose purchase order as the first message.

## Profile the Core Component

Many information model libraries are large. Some so large that they may intimidate potential or actual implementers. Score makes it easy to select only the components necessary to meet the data-exchange of the applicable message interaction in the specified context.

## Produce XML Schema

When it comes to moving large amounts of data around reliably, XML is a proven workhorse. XML Schemas specify the structures and data types associated with each type of XML message. Score makes it easy to produce XML schemas from profiled core components giving developers exactly what they need to complete their integration projects on time and on budget.

## Produce JSON Schema

While XML is a proven workhorse, JSON has emerged as the preferred syntax lighter-weight “chatty” system interactions. Score makes it easy to produce JSON schemas from profiled core components giving developers exactly what they need to complete their integration projects on time and on budget.

## Produce API Specification

Message formats are only part of interoperability story. Delivering and retrieving messages among systems is another part. API specifications give developers the details they need to invoke or make available the services behind the APIs. Score makes it easy to generate OAS 3.0-compliant API specifications that complement the schemas.
