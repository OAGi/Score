Manage Business Terms
---------------------
Business term management functionality allows end users to create or import business terms
from another data dictionary applications such as an enterprise data dictionary management
and assign those business terms to data elements in the data exchange standard. It is not
intended to be a data dictionary management itself. This is the reason each business term
entity in connectCenter has only a few fields mainly for representing the identity of the business term.

Currently, connectCenter provides the business term functionality to end users only.
Developers won't see this functionality once login as developer.
The end users can view, create, edit or discard business terms through the View/Edit Business Term menu under the BIE menu.
The only way to assign business terms to BIEs is through the BIE detail page.
Note that in the current version, business term assignment to the root BIE node is not supported.
In addition, business terms are assigned/associated to two BIE types, ASBIE and BBIE, from the data standpoint, to allow for the most precise contextual assignment.
In other words, it means that the business terms are applicable to the ASBIEP and ABIE underneath the ASBIE within the context of the ABIE owner for the ASBIE and applicable to the BBIEP underneath the BBIE within the context of the ABIE owner of the BBIE.
Consequently, all business terms assigned to the ASBIEs or BBIEs that reference the same ASCC and BCC can be inferred as business terms of the ASCCP and ACC and the BCCP under the ASCC and BCC as well.

Create a Business Term
~~~~~~~~~~~~~~~~~~~~~~~
To create a business term:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. On the returned "Business Term" page, click "New Business Term" located at the top-right
   of the page.

4. On the returned "Create Business Term" page, fill out the following fields:

   1. Business Term (Mandatory) the main name of the business term
   2. External Reference URI (Mandatory) This uri should uniquely identify each business term. Note, up to 65535 characters allowed for this URI.
   3. External Reference Id(Optional)
   4. Comment(Optional) This is free-form text field for adding information about the business term in the context of the connectCenter tool. An example comment may be
      "This business term is not from the enterprise data dictionary."

5. Click the "Create" button.

Edit a Business Term
~~~~~~~~~~~~~~~~~~~~
To edit a business term:

1. On the top menu of the page, click "BIE".

2. Click "View/Edit Business Term" menu item.

3. Use the *Term* or *External Reference URI* to find the desired business term.
   Open its "Edit Business Term" page by clicking the business term name in Term column. See
   also `How to use the Search field in general <#how-to-use-the-search-field-in-general>`__.

4. You can change the *Business Term*, *External Reference URI*, *External Reference ID* and *Comments*
   fields.

5. You cannot change the *Definition* field, which is only updated through upload from external file.

6. Click the "Update" button.


Discard a Business Term
~~~~~~~~~~~~~~~~~~~~~~~~

Note that a business term can only be discarded if it is not assigned to any BIE.
Otherwise, you have to discard the assignment first (see `Discard the assignment of a business term from a BIE <#discard-the-assignment-of-a-business-term-from-a-bie>`__ ).

There are two methods for discarding a Business Term. The first one is:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. Locate the Business Term you want to discard. Use the *Term*, *External Reference URI*, *External Reference ID*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired Business Term.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the checkbox right before the desired
   business term name.

4. Click the "Discard" button at the top-right of the page.

5. A dialog is open where you can confirm or cancel the request. If the Business Term is assigned to a BIE, the system
   will not remove it. All the assignments for it must be removed first.

The second method is:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. Locate the Business Term you want to discard. Use the *Term*, *External Reference URI*, *External Reference ID*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired Business Term.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the Business Term Name to open its
   "Edit Business Term" page.

4. Click the "Discard" button.

5. A dialog is open where you can confirm or cancel the request. If the Business Term is assigned to a BIE, the system
   will not remove it. All the assignments for it must be removed first.

Assign business terms to BIEs
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To assign a business term to a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. Locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*, *Owner*, *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired BIE. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the BIE DEN to open its "Edit BIE" page.

4. Expand the tree structure until reaching the desired BIE node in the left navigation panel.

5. Select the desired BIE node. The detail for that node is displayed in the right panel. Check the *Used* checkbox if
   it is unchecked and click the "Update" button at the top-right of the page. The "Assign Business Term" button will be enabled.

6. Click the "Assign Business Term" button.

7. On the newly opened "Assign Business Term" page, locate the business term to be assigned. Use the *Business Term*,
   *External Reference URI*, *External Reference ID*, *Updater*, *Updated start date* or *Updated end date* search filters
   to help locate the desired business term.

8. Select the desired business term. Fill out the Type Code (optional). Note that the same business term with different Type
   Code can be assigned to the same BIE.

9. Check or uncheck the Preferred Business Term checkbox. Note that only one business term can be preferred for each selected BIE.

10. Click the "Create" button.

View Business Term Assignments of a BIE
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view all the business terms currently assigned to a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. Locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*,
   *Owner*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the BIE DEN to open its "Edit BIE" page.

4. Expand the tree structure until reaching the desired BIE node in the left navigation panel.

5. Select the desired BIE node, and the detail for that BIE node is displayed in the right panel. Only the *Used* node has business term
   assignments.

6. Click the "Show Business Terms" button in the right panel.

7. On the newly opened "Business Term Assignement" page, the selected BIE is displayed right before the "Turn off" button.
   All the business terms assigned for the selected BIE are displayed in the table below the "Search" button.


Discard a business term from a BIE
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Discarding the assignment of a business term from a BIE removes the association of the business term from the given BIE.
The prerequisite for permanently removing a business term from connectCenter is to discard all the assignments for that business term first.

To discard the assignment of a business term from a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. Locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*,
   *Owner*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the BIE DEN to open its "Edit BIE" page.

4. Expand the tree to find the desired BIE node in the left navigation panel.

5. Select the desired BIE node, and the detail for that node is displayed in the right panel. Only the *Used* node has business term
   assignments for removal.

6. Click the "Show Business Terms" button in the right panel.

7. On the newly opened "Business Term Assignement" page, the selected BIE is displayed right before the "Turn off" button.
   All the business terms assigned for the selected BIE are displayed in the table below the "Search" button.

8. Located the business term to be discarded from the assignment. Use the *Business Term*, *External Reference URI*, *Type Code*
   , *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired business term.
   Note that *Preferred Only* checkbox can help filter efficiency too.

9. Click the "Search" button.

10. Select the desired business term. The "Discard" button at the top right of the page will be enabled. Click "Discard",  and a
    dialog is open where you can confirm or cancel the request. Only the assignment for this given BIE is permanently removed.
    The Business Term Assignment page of that BIE node is still displayed. More assignments can be discarded.

Load Business Terms from external source
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This is the preferred way for the end users to create business terms in connectCenter.
This method allows the end users to bulk upload business terms from an external csv file.

To upload from an external file:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit Business Term" from the drop-down list.

3. On the returned "Business Term" page, click the "Upload Business Terms" button at the top right corner of the page.

4. On the returned "Upload Business Terms" page, click the "Download template" button at the top right corner of the page.
   A csv template file named "businessTermTemplateWithExample" will be saved into your "Download" folder on your local
   computer.

5. Use the template as the format to upload business terms to connectCenter. Vocabulary exported from another application needs to be
   formatted into this template. Note that businessTerm and externalReferenceUri columns are required. The externalReferenceUri
   will be used as the key for the business term. If an externalReferenceUri entry already exists in connectCenter, the information for that
   business term will be updated. If not, a new business term will be created in connectCenter.

6. Go back to "Upload Business Term" page, click the attach button (paper clipper icon) and choose the modified csv file
   in the pop-up choose-file window. Finally click the "Open" button in the pop-up window.

7. An "Uploaded" message will be displayed for confirmation.

8. Go back to the top menu of the page, click "BIE".

9.  Choose "View/Edit Business Term" from the drop-down list.

10. On the returned "Business Term" page, you can locate the uploaded business terms using the search filters: *Term*,
    *External Reference URI*, *External Reference ID*, *Updater*, *Updated start date* or *Updated end date*.
