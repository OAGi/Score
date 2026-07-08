---
title: "BIE Content"
sidebar_position: 2
---

While BIEs are mainly created by connectCenter end users, they may also be created by connectCenter developers. connectCenter considers these entities as BIE related content (or BIE content for short) – the BIE itself, Context Category, Context Scheme, Business Context, End User Code List, and End User Core Component.

As the name implied, End User Code List and End User Core Component are those created and owned by the end user.
There are also Developer Code List and Developer Core Component – see [Developer vs. End User Core Components](../core-component-management/02-key-concepts.md#developer-vs-end-user-core-components).
Developer Core Components (that includes Developer Code Lists) have a different set of states than End User Core Components and End User Code List (parallel to the Developer Core Components (DCC), let us also include End User Code Lists when referring to the End User Core Components (EUCC)).
Please review the [End user CC states](../core-component-management/02-key-concepts.md#end-user-cc-states) section.
Since BIEs are similar to EUCC in that they are already tied to a specific DCC release and BIEs and EUCCs are used together in [BIE Extension](./06-manage-bie.md#extend-a-bie), BIEs have a similar set of states as EUCCs.
Details of BIE states and user access right are described in [BIE States](./06-manage-bie.md#bie-states).

Since DCCs are standard CCs, EUCCs can use, i.e., can make up of not only EUCCs but also DCCs.
On the contrary, DCCs cannot use any EUCC.
Similarly, as a BIE may belong to an end user or belong to a developer.
An end user BIE may reuse a developer BIE but not vice versa.

On the other hand, Context Category, Context Scheme, and Business Context do not have boundary between end user ones and developer ones (although this may change in the future).
