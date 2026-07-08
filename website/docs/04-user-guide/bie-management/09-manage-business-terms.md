---
title: "Manage Business Terms"
sidebar_position: 9
---

Business term management functionality allows end users to create or import business terms from another data dictionary applications such as an enterprise data dictionary management and assign those business terms to data elements in the data exchange standard. It is not intended to be a data dictionary management itself. This is the reason each business term entity in connectCenter has only a few fields mainly for representing the identity of the business term.

Currently, connectCenter provides the business term functionality to end users only. Developers won't see this functionality once login as developer. The end users can view, create, edit or discard business terms through the View/Edit Business Term menu under the BIE menu. Business terms are also imported in bulk from an external file through the "Upload Business Terms" import dialog on the same page (see [Load Business Terms from external source](#load-business-terms-from-external-source)).

Business terms are assigned to BIEs directly in the BIE editor. On the *Details* pane of an ASBIE or BBIE node, the assigned business terms appear as chips in the *Business Terms* field, next to the *Remark* field. A chip is added through a multi-select "Assign Business Term" dialog opened from that field, and each chip offers in-place actions (preview, set/unset preferred, edit its Type Code, and unassign). Note that in the current version, business term assignment to the root BIE node is not supported, and it is not supported on BBIE_SC (supplementary component) nodes either. Business terms are assigned/associated to two BIE types, ASBIE and BBIE, from the data standpoint, to allow for the most precise contextual assignment. In other words, it means that the business terms are applicable to the ASBIEP and ABIE underneath the ASBIE within the context of the ABIE owner for the ASBIE and applicable to the BBIEP underneath the BBIE within the context of the ABIE owner of the BBIE. Consequently, all business terms assigned to the ASBIEs or BBIEs that reference the same ASCC and BCC can be inferred as business terms of the ASCCP and ACC and the BCCP under the ASCC and BCC as well.

## Create a Business Term

To create a business term:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. On the returned "Business Term" page, click "New Business Term" located at the top-right of the page.

4. On the returned "Create Business Term" page, fill out the following fields:

    1. Business Term (Mandatory) the main name of the business term
    2. External Reference URI (Mandatory) This uri should uniquely identify each business term. Note, up to 65535 characters allowed for this URI.
    3. External Reference Id(Optional)
    4. Comment(Optional) This is free-form text field for adding information about the business term in the context of the connectCenter tool. An example comment may be "This business term is not from the enterprise data dictionary."

5. Click the "Create" button.

:::note
A business term is identified by the combination of its *Business Term* name and *External Reference URI*. connectCenter does not let you create or save a business term when another business term already has the same *Business Term* + *External Reference URI* pair. Two business terms may share the same name as long as their *External Reference URI* values differ.
:::

## Edit a Business Term

To edit a business term:

1. On the top menu of the page, click "BIE".

2. Click "View/Edit Business Term" menu item.

3. Use the *Term* or *External Reference URI* to find the desired business term. Open its "Edit Business Term" page by clicking the business term name in Term column. See also [How to use the Search field in general](./10-common-functions.md#how-to-use-the-search-field-in-general).

4. You can change the *Business Term*, *External Reference URI*, *External Reference ID* and *Comments* fields.

5. You cannot change the *Definition* field, which is only updated through upload from external file.

6. Click the "Update" button.

:::note
The same uniqueness rule applies when saving edits: the *Business Term* + *External Reference URI* pair must not duplicate another business term. Reusing an existing name with a different *External Reference URI* is allowed.
:::

## Discard a Business Term

Note that a business term can only be discarded if it is not assigned to any BIE. Otherwise, you have to discard the assignment first (see [Discard the assignment of a business term from a BIE](#discard-a-business-term-from-a-bie) ).

There are two methods for discarding a Business Term. The first one is:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. Locate the Business Term you want to discard. Use the *Term*, *External Reference URI*, *External Reference ID*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired Business Term. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the checkbox right before the desired business term name.

4. Click the "Discard" button at the top-right of the page.

5. A dialog is open where you can confirm or cancel the request. If the Business Term is assigned to a BIE, the system will not remove it. All the assignments for it must be removed first.

The second method is:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. Locate the Business Term you want to discard. Use the *Term*, *External Reference URI*, *External Reference ID*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired Business Term. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the Business Term Name to open its "Edit Business Term" page.

4. Click the "Discard" button.

5. A dialog is open where you can confirm or cancel the request. If the Business Term is assigned to a BIE, the system will not remove it. All the assignments for it must be removed first.

## Assign business terms to BIEs

Business terms are assigned to a BIE node in place, on the *Details* pane of the BIE editor. The *Business Terms* field (showing the assigned terms as chips) is displayed only for ASBIE and BBIE nodes, only when the tenant has the business term feature enabled, and only for end users.

To assign a business term to a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. Locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*, *Owner*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the BIE DEN to open its "Edit BIE" page.

4. Expand the tree structure until reaching the desired BIE node in the left navigation panel.

5. Select the desired BIE node. The detail for that node is displayed in the right panel. Check the *Used* checkbox if it is unchecked and click the "Update" button at the top-right of the page. The "+" (add) button next to the *Business Terms* field is enabled only after the node is saved (until then its tooltip reads "Save the BIE to assign business terms").

6. Click the "+" button next to the *Business Terms* field to open the "Assign Business Term" dialog.

7. In the dialog, locate the business term(s) to be assigned. Use the "Search by Business Term" box or the advanced filters (*Updater*, *Updated start date*, *Updated end date*, *External Reference URI*, *External Reference ID*, and *Definition*) to narrow the list.

8. Select one or more business terms by clicking their checkboxes. The master checkbox in the header selects or clears all rows visible on the current page and shows a partial (indeterminate) state when only some of them are selected. A business term that is already used elsewhere can still be selected and reused.

9. Fill out the *Type Code* (optional, free text, up to 30 characters). The same business term can be assigned to the same BIE more than once only when the *Type Code* differs.

10. Click the "Assign" button. When more than one term is selected the button shows the count, e.g. "Assign (3)". All selected terms are assigned to the BIE node in one action, and only that node's *Business Terms* field is refreshed. If any selected term would duplicate an existing assignment for the same BIE and Type Code, the assignment is blocked and a dialog titled "Invalid parameters" reports that one or more of the selected assignments already exist.

**Set a preferred business term.** Each chip has a star. Click the empty star (tooltip "Set as preferred") to mark that term as the preferred (primary) business term for the BIE, or click the filled star (tooltip "Unset preferred") to clear it. Only one business term can be preferred per BIE; setting one as preferred clears any previously preferred term on the same BIE. Chips are ordered with the preferred term first, then by the order in which they were assigned.

**Edit a Type Code in place.** Click a chip to turn its Type Code into an inline text box (up to 30 characters), then press Enter or click the check icon to save (press Escape or click away to cancel). If the new Type Code would duplicate another assignment for the same BIE and Type Code, an inline error appears on the field reading "Another business term assignment for the same BIE and type code already exists!" and the input stays focused so you can correct it. The duplicate check ignores the assignment you are editing.

## View Business Term Assignments of a BIE

The business terms assigned to a BIE node are shown as chips in the *Business Terms* field on the *Details* pane, next to the *Remark* field.

To view the business terms currently assigned to a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. Locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*, *Owner*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the BIE DEN to open its "Edit BIE" page.

4. Expand the tree structure until reaching the desired BIE node in the left navigation panel.

5. Select the desired BIE node, and the detail for that BIE node is displayed in the right panel. The *Business Terms* field lists each assigned business term as a chip. A chip shows the business term name and, when set, its Type Code as a smaller sub-label. The preferred business term is marked with a filled star. Business terms are shown only for *Used* ASBIE and BBIE nodes.

6. Hover over a chip to open a preview card that shows, when present, the *External Reference URI*, *External Reference ID*, *Definition*, and *Comment* of the term. The card header is the business term name; clicking it opens the full business term detail page in a new browser tab.

7. For an inherited BIE node, open the *Inherits from ...* tab to see the business terms carried from the base BIE. On this tab the *Business Terms* field and its preview cards are read-only (no add, remove, star, or Type Code editing is offered there).

## Discard a business term from a BIE

Discarding the assignment of a business term from a BIE removes the association of the business term from the given BIE. The prerequisite for permanently removing a business term from connectCenter is to discard all the assignments for that business term first.

To discard the assignment of a business term from a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. Locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*, *Owner*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the BIE DEN to open its "Edit BIE" page.

4. Expand the tree to find the desired BIE node in the left navigation panel.

5. Select the desired BIE node, and the detail for that node is displayed in the right panel. The assigned business terms are listed as chips in the *Business Terms* field. Only a *Used* ASBIE or BBIE node has business term assignments for removal, and the assignment can be removed only from the current tab (the *Inherits from ...* tab is read-only).

6. On the chip of the business term to be unassigned, click the "x" (cancel) icon (tooltip "Unassign").

7. A confirmation dialog headed "Remove this business term assignment?" asks you to confirm that the business term will be unassigned from this BIE. Click "Remove" to confirm, or cancel to keep it. Only the assignment for this BIE node is removed; the business term itself remains in connectCenter and can still be assigned elsewhere.

## Load Business Terms from external source

This is the preferred way for the end users to create business terms in connectCenter. This method allows the end users to bulk import business terms from an external file through the "Upload Business Terms" import dialog. The dialog accepts CSV, TSV, and Excel (.xlsx) files up to 10 MB. When a file exported from another Business Glossary application (for example Collibra, Alation, Microsoft Purview, Informatica, IBM, erwin, Atlan, data.world, SAP, or Google Dataplex) is uploaded, the dialog recognizes the export format and pre-fills the column mapping accordingly, which you then verify or adjust. The *External Reference URI* remains the key for each business term: if the URI already exists in connectCenter, that business term is updated; otherwise a new one is created.

To import from an external file:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. On the returned "Business Term" page, click the "Upload Business Terms" button at the top-right of the page. The "Upload Business Terms" dialog opens as a four-step wizard: *Upload file*, *Map columns*, *Review & select*, and *Result*. You can cancel the wizard at any step before the import runs with the "x" (close) button at the top-right of the dialog.

4. On the *Upload file* step, drag and drop a file onto the drop zone, or click it (or the "Browse" button) to pick a file. To start from the native template, click "Download template" to save "businessTermTemplateWithExample.csv" and fill it in. A file whose type is not CSV, TSV, or .xlsx, or that exceeds 10 MB, is rejected; when a replacement pick is rejected, the previously accepted file stays selected. Use the "x" on the file tile to remove the file.

5. After the file is read, an information line reports how many rows and columns were found. For an Excel workbook with more than one worksheet, choose the sheet from the "Worksheet" drop-down (changing the worksheet or removing the file cancels any read still in progress). A file with a single worksheet advances to the *Map columns* step automatically; otherwise, pick the worksheet and click "Next".

6. On the *Map columns* step, verify the mapping. An amber notice reminds you to confirm that each source column maps to the correct business term field. Map the source columns to *Business Term* (required), *External Reference ID*, *Definition*, and *Comment*, and provide the *External Reference URI* (required) either by selecting "Map a column" and choosing the "URI column", or by selecting "Build from base URL + ID" and entering a base URL plus an ID column. Click "Next" when a column is mapped to *Business Term* and the *External Reference URI* is provided.

7. On the *Review & select* step, review the rows. Summary chips show how many rows are ready, how many need review, and how many are selected. Each cell is editable; valid rows show a green check and invalid rows show a warning with a tooltip listing the problems, and cannot be selected until fixed. Select the rows to import (a row's *Business Term* and *External Reference URI* are required), then click "Import N selected".

8. On the *Result* step, the outcome is shown per row: summary chips report how many terms were created, updated, and failed, and the table lists each row's *Business Term*, *External Reference URI*, *Outcome* (CREATED, UPDATED, or FAILED), and a *Detail* message for failed rows. Click "Close" to return to the "Business Term" page, which reloads when at least one term was created or updated.

You can then locate the imported business terms on the "Business Term" page using the search filters: *Term*, *External Reference URI*, *External Reference ID*, *Updater*, *Updated start date*, or *Updated end date*.
