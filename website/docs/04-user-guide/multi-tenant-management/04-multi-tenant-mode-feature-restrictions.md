---
title: "Multi-tenant mode feature restrictions"
sidebar_position: 4
---

## Features not available

The following features are not available to users when connectCenter is in multi-tenant mode:

* Manage modules
* Manage core components
* Make BIE reusable
* Create ABIE extension locally
* Create ABIE extension globally
* Business Term Management

## Features with restricted behavior

* Create BIE: A user must be associated with a tenant to be able to create a BIE.
* Manage associations between business contexts (BCs) and BIEs:

    * The BCs available to the user are limited by tenant association.
    * Users who are not associated with a tenant cannot create a BIE because they cannot assign a business context during BIE creation.
    * Admin users do not have special authorization in this case.

* Manage context: Restricted to users with the Admin right in multi-tenant mode.
* Transfer BIE ownership: Users to whom BIE ownership may be transferred are limited to users associated with tenants associated with business contexts associated with the BIE.
