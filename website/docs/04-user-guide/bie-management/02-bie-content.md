---
title: "BIE Content"
sidebar_position: 2
---

While BIEs are mainly created by connectCenter end users, they may also be created by connectCenter developers.
connectCenter considers the following entities as BIE-related content (or BIE content for short): the BIE itself, Context Category, Context Scheme, Business Context, End User Code List, End User Agency ID List, and End User Core Component.
The "BIE" menu in the application also groups artifacts built on top of BIEs — BIE Packages, OpenAPI Documents, Business Terms, and the Reuse Report — which are covered later in this section.

As the name implies, End User Code Lists, End User Agency ID Lists, and End User Core Components are those created and owned by end users.
There are also Developer Code Lists and Developer Core Components – see [Developer vs. End User Core Components](../core-component-management/02-key-concepts.md#developer-vs-end-user-core-components).
Developer Core Components (which include Developer Code Lists) have a different set of states than End User Core Components; parallel to the Developer Core Components (DCC), this guide also includes End User Code Lists and End User Agency ID Lists when referring to the End User Core Components (EUCC).
Please review the [End user CC states](../core-component-management/02-key-concepts.md#end-user-cc-states) section.
Since BIEs are similar to EUCCs in that they are already tied to a specific DCC release, and BIEs and EUCCs are used together in [BIE Extension](./06-manage-bie.md#extend-a-bie), BIEs have a similar set of states as EUCCs.
One difference is that EUCCs have a Deleted state while BIEs do not — discarding a BIE removes it permanently.
Details of BIE states and user access rights are described in [BIE States](./06-manage-bie.md#bie-states).

Since DCCs are standard CCs, EUCCs can use, i.e., can be made up of, not only EUCCs but also DCCs.
On the contrary, DCCs cannot use any EUCC.
Similarly, a BIE may belong to an end user or to a developer, and a parallel boundary applies to [BIE reuse](./06-manage-bie.md#bie-reuse): an end user can reuse BIEs owned by developers as well as by other end users, while a developer can reuse only developer-owned BIEs.
The "Select Profile BIE to reuse" dialog automatically restricts a developer's candidate list to developer-owned BIEs.
In either case, the BIE receiving the reuse must be in the WIP state and owned by the current user.

On the other hand, Context Category, Context Scheme, and Business Context do not have a boundary between end user ones and developer ones — both roles can create and edit them.
(In multi-tenant deployments, the "Context" menu is available only to administrators.)
A few other BIE-content functions are role-gated, however: the "OpenAPI Document" and "View/Edit Business Term" menu items appear only for end users, "Uplift Code List" is disabled for developers, and developers cannot create BIE user extensions.

![The BIE menu of a developer, without the OpenAPI Document and View/Edit Business Term items and with the Uplift Code List item greyed out](/img/user-guide/bie_menu_developer.png)
