.. _manage-context:

Manage Context
--------------

A business context is required to create a BIE.
Therefore, the user should understand the context management before the BIE management.

.. _understanding-context:

Understanding Context
~~~~~~~~~~~~~~~~~~~~~

There are three main concepts related to context management, namely, Context Category, Context Scheme and Business Context.
The dependency between these three concepts and the BIE is that in order for a BIE to be created, a Business Context is needed, but a Business Context requires a Context Scheme, and a Context Scheme requires a Context Category.
Hence, from the user’s perspective it is easier for him to understand these concepts if they are described in sequence starting with Business Context first, then Context Scheme, and finally Context Category.

The intent of a Business Context is to define the situation in which a BIE should be used.
Therefore, a Business Context must be specified in order to create a BIE (as a BIE is a usage of a CC in a particular Business Context.
A Business Context is specified by a combination of Context Scheme values.

As the name suggests, Context Scheme values within a Business Context are provided from one or more Context Schemes, each typically from different Context Categories.
For example, a Context Scheme may be a standard industry classification scheme; and hence, its Context Category is Industry Context Category.

A Context Category indicates what a Context Scheme is about.
Another example of Context Category can be Application Context Category or Business Process Context Category.
Similar Context Schemes maintained by different standard agencies or different versions of the same standard schemes, may be used in specifying a Business Context.
Context Category provides a way to indicate that these Context Schemes are intending to specify the same dimension about the Business Context.

It should be noted that for connectCenter, all the values within a Business Context are always interpreted conjunctively.
For example, if the Business Context consists of [Automotive, Electronics] context values for the **Industry** context category, [Electronic Component Purchasing] for the **Business Process**, [Outbound B2B] for the **Integration Type**, it means that the BIE, to which the Business Context is assigned, is applicable when the transaction is an Outbound B2B transaction, within the Electronic Component Purchasing business process, **AND** Automotive and Electronics industries.
The business context suggests that the BIE should be used in a business process involving Automotive Manufacturer/Vendor buying parts from an Electronics Manufacturer/Vendor.
If the industry context were to have only Automotive context value, it would mean the BIE is for transacting within the Automotive industry.

Create a Context Category
~~~~~~~~~~~~~~~~~~~~~~~~~

To create a Context Category:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Category" from the drop-down list.

3. The "Context Category" page listing existing Context Categories is
   returned. The user is encouraged to search using the *Name* or the
   *Description* fields and reuse existing Context Category first before
   creating a new one (see `How to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__)

4. If the desired Context Category does not exist, click the "New Context
   Category" button located at the top-right of the page.

5. Fill out the *Name* (Mandatory) and *Description* (Optional) fields.
   Note that there is no standard format for the Context Category name.
   However, it is recommended that a space-separated with uppercase
   initials is used, e.g., "Business Process Context Category".

6. Click the "Create" button.

Create a Context Scheme
~~~~~~~~~~~~~~~~~~~~~~~

A Context Category must exist in order to create a Context Scheme (see `Create a Context Category <#create-a-context-category>`__).

To create a Context Scheme:

1. On the top menu of the page, click "Context".

   1. Choose "View/Edit Context Scheme" from the drop-down list. The
      "Context Scheme" page is returned. It is recommended that the user
      search and try to reuse existing Context Schemes first. He can do
      that using the *Name* or *Description* (see `How to use the Search
      field in general <#how-to-use-the-search-field-in-general>`__),
      *Updater*, *Updated start date*, and *"Updated end date"* search
      filters (see `How to use Search
      Filters <#how-to-use-search-filters>`__)

2. If the desired context scheme does not exist, click "New Context
   Scheme" located at the top-right of the page.

3. Fill out the following fields:

   1. *Context Category* (Mandatory), choose from existing ones in the
      drop-down list. The user can type in the field to narrow down the
      list.

   2. *Name* (Mandatory). It is recommended that the format of the name
      be space-separated uppercase initials, such as "NAIC Industry
      Classification Scheme". This will enhance the human readability.

   3. *Load from Code List* (Optional). This button allows for importing
      code list values to the Context Scheme being created and also
      filling out the *Scheme ID*, *Agency* *ID* and *Version* fields
      with the same information from the code list. They can be still
      changed though. For more details please see the `Load from a Code
      List <#load-from-a-code-list>`__ subsection.

   4. *Scheme ID* (Mandatory). This is a free-form text field for
      specifying the unique identifier of the context scheme – may be
      assigned by the organization maintaining the context scheme.

   5. *Agency ID* (Mandatory). This is a free-form text field for
      specifying the unique identifier of the organization maintaining
      the context scheme.

   6. *Version* (Mandatory). This is a free-form text field for
      specifying the version of the context scheme.

   7. *Description* (Optional). This is a free-form text field for
      documenting what the context scheme is, its purposes, etc.

..

   It should be noted that the combination of the *Scheme ID*, *Agency
   ID*, and *Version* has to be unique in the database.

   The application will also give a warning if the user tries to create
   a Context Scheme which has the *Scheme ID* and *Agency ID* the same
   as those of an existing one but with a different *Name*.

4. Add a Context Scheme value (Optional). See details about adding a
   value and other Context Scheme value manipulations in the `Update a
   Context Scheme <#update-a-context-scheme>`__ section.

5. Click the "Create" button.

Load from a Code List
^^^^^^^^^^^^^^^^^^^^^

The button "Load from a Code List" is used to automatically load Context Scheme information with information from a Code List including *Scheme ID*, *Agency ID*, *Version*, and Code List values.
When the button is clicked, a dialog for selecting a Code List appears.
Note that only a code list in Published or Production state is allowed.

Once a Code List is selected, the values from the code list are copied into the Context Scheme.
They can be still changed though.
The Context Scheme values loaded from the code list can be changed or removed.
New values can be also added (see `Update a Context Scheme <#update-a-context-scheme>`__).

Create a Business Context
~~~~~~~~~~~~~~~~~~~~~~~~~

A Context Scheme must be available to create a Business Context.
See `Create a Context Scheme <#create-a-context-scheme>`__.

To create a Business Context:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Business Context" from the drop-down list. The
   "Business Contexts" page is returned showing the available Business
   Contexts. The user is encouraged to reuse a Business Context. On this
   page, the user can browse and search for an existing Business Context
   that can meet his/her need or he can use the *Updater*, *Updated
   start date* and *Updated end date* search filters (see `How to use
   Search Filters <#how-to-use-search-filters>`__)

3. If existing Business Contexts do not meet your needs, click the "New
   Business Context" button at the top-right of the page to create a new
   one.

4. Fill out the *Name* (Mandatory) of the Business Context in the
   *Create Business Context* page. It is recommended that the format of
   the name be space-separated uppercase initials, such as "B2B Lab
   Management Inspection Order". This will enhance the human
   readability.

5. Click the "Add" button near the bottom of the page to add one or more
   Business Context Values, although this is optional.

6. Once finish with the business context values, click the "Create"
   button at the bottom of the page.

Update a Context Category
~~~~~~~~~~~~~~~~~~~~~~~~~

To update a Context Category:

1. On the top menu of the page, click "Context".

2. Click "View/Edit Context Category" menu item.

3. Use the *Name* and Description fields to find the desired Context
   Category. Open its "Edit Context Category" page by clicking the Name
   or GUID. See also `How to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__.

4. You can change the *Name* and the *Description* fields of the Context
   Category.

5. Click the "Update" button.

Update a Context Scheme
~~~~~~~~~~~~~~~~~~~~~~~

To update a Context Scheme:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Scheme" from the drop-down list.

3. Locate the Context Scheme you want to update. You may search by using
   the *Name* (see `How to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__) *Updater*,
   *Updated start date*, or *Updated end date* search filters (see `How
   to use Search Filters <#how-to-use-search-filters>`__). Click on the
   Name or GUID of the Context Scheme to open its editing page.

4. On the returned "Edit Context Scheme" page, the following actions may
   be performed:

   1. Change *Context Category*, *Name*, *Scheme ID*, *Agency ID*,
      *Version*, *Description*. See `Create a Context
      Scheme <#create-a-context-scheme>`__ for required fields and the
      uniqueness constraint. The "Load from Code List" button may be
      used again to initialize some of these fields and the Context
      Scheme Values. Note, however, that all existing values will be
      removed.

   2. Add a Context Scheme Value.

      1. Click the "Add" button located in the Context Scheme Values
         table.

      2. In the pop-up dialog, fill in the following fields:

         1. *Value* (Mandatory)

         2. *Meaning* (Optional)

      3. Click the "Add" button

      4. To cancel the addition, use the Esc key or click outside the
         dialog.

   3. Remove a Context Scheme Value by clicking the checkbox in front of
      the row of the Context Scheme Value you want to remove and then
      click the "Remove" button.

   4. Update a Context Scheme Value by clicking on its Value. The dialog
      pop-up where you can edit the *Value* (mandatory) and *Meaning*
      (optional). Click "Save" to save changes. Alternatively, hit Esc
      key or click outside of the dialog to discard changes.

5. Click the "Update" button. No change is recorded to the database
   unless this update request is submitted.

Update a Business Context
~~~~~~~~~~~~~~~~~~~~~~~~~

To update a Business Context:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Business Context" from the drop-down list.

3. Locate the Business Context you want to update. Use the *Name* (`How
   to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__), *Updater*,
   *Updated start date*, or *Updated end date* search filters to help
   locate the desired business context (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on its Name or GUID
   to open its "Edit Business Context" page.

4. The "Edit Business Context" page is returned where the followings
   actions can be performed:

   1. Change the *Name* of the Business Context.

   2. Add a *Business Context Value*.

      1. Click the "Add" button in the Business Context Values table.

      2. In pop-up dialog, select the *Context Category*, then *Context
         Scheme*, and select a value from within the Context Scheme
         (this has to be done in such order). Finally, Click the "Add"
         button. To add another Business Context Value following the
         same steps again. All the values are always interpreted
         conjunctively as described earlier (see `Understanding
         Context <#understanding-context>`__). To cancel this addition, use the
         Esc key or click outside the dialog.

   3. Remove a Business Context Value by clicking on the checkbox
      located in front of the row of the Business Context Value you want
      to remove and then click the "Remove" button.

   4. Update a Business Context Value by clicking on the row you want to
      make change. The dialog pop-up where you change the value as
      described in #2 – Add a Business Context Value.

5. Click the "Update" button to save changes. No change is recorded to
   the database unless this update request is submitted.

Discard a Business Context
~~~~~~~~~~~~~~~~~~~~~~~~~~

Note that a business context can only be discarded if there is no BIE using it.

There are two methods for discarding a Business Context.
The first one is discarding a Business Context individually:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Business Context" from the drop-down list.

3. Locate the Business Context you want to discard. Use the *Name* (`How
   to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__), *Updater*,
   *Updated start date,* or *Updated end date* search filters to help
   locate the desired Business Context (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the Name or GUID
   to open its "Edit Business Context" page.

4. Click the "Discard" button.

5. A dialog is open where you can confirm or cancel the request. If the
   Business Context is used by a BIE, the system will not remove it. All
   BIEs using it has to be removed first. However, if BIEs are already
   published, they cannot be removed either.

The second method can discard multiple Business Contexts simultaneously:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Business Context" from the drop-down list.

3. Click the checkbox in front of the Business Context you want to
   discard.

4. Click the "Discard" button at the right-top of the page.

5. Confirm your intention as described in #5 of the first method.

Discard a Context Scheme
~~~~~~~~~~~~~~~~~~~~~~~~

Note that a Context Scheme can only be discarded if there is no Business Context using it.

There are two methods for discarding a Context Scheme.
The first discards a Context Scheme individually:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Scheme" from the drop-down list.

3. Locate the Context Scheme you want to discard. Use the Name (`How to
   use the Search field in
   general <#how-to-use-the-search-field-in-general>`__), *Updater*\ ",
   *Updated start date*, and *Updated end date* search filters (see `How
   to use Search Filters <#how-to-use-search-filters>`__) to help locate
   the desired Context Scheme. Click on its Name or GUID to open its
   "Edit Context Scheme" page.

4. Click the "Discard" button.

5. A dialog is open where you can confirm or cancel the request. If
   there is a Business Context using the Context Scheme, the application
   will not remove it.

The second method can discard multiple Context Schemes simultaneously:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Scheme" from the drop-down list.

3. Click the checkbox in front of the Context Scheme you want to
   discard.

4. Click the "Discard" button at the top-right of the page.

5. Confirm your intention as described in #5 of the first method.

Discard a Context Category
~~~~~~~~~~~~~~~~~~~~~~~~~~

Note that a Context Category can only be discarded if there is no Context Scheme using it.

There are two methods for discarding a Context Category.
The first one discards a Context Category individually:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Category" from the drop-down list.

3. Locate the Context Category you want to discard. Use the Name (`How
   to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__), *Updater*\ ",
   *Updated start date*, and *Updated end date* search filters (see `How
   to use Search Filters <#how-to-use-search-filters>`__) to help locate
   the desired Context Category. Click on its Name or GUID to open its
   "Edit Context Category" page.

4. Click the "Discard" button.

5. A dialog is open where you can confirm or cancel the request. If
   there are Context Schemes using the Context Category, the application
   will not remove the Context Category. It will display GUIDs of
   Context Schemes using the Context Category. They have to be discarded
   first.

The second method can discard multiple Context Categories simultaneously:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Category" from the drop-down list.

3. Click the checkbox in front of the Context Category you want to
   discard.

4. Click the "Discard" button at the top-right of the page.

5. Confirm your intention as described in #5 of the first method.

Search and view Context Category detail
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view detail of a Context Category:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Category" from the drop-down list. The
   "Context Category" page is returned.

3. Scroll down to find the desired Context Category or use the Search
   feature. When the desired Context Category is found, click the Name
   or the GUID of the Context Category to open the "Edit Context
   Category" page where its detail can be viewed. See `How to use the
   Search field in general <#how-to-use-the-search-field-in-general>`__
   for help with the search.

Search and view Context Scheme detail
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To retrieve a Context Scheme:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Context Scheme" from the drop-down list.

3. Locate the desired Context Scheme using the pagination at the bottom
   or use the *Name* (see also `How to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__), *Updater*,
   *Updated start date*, and *Updated end date* search filters (see `How
   to use Search Filters <#how-to-use-search-filters>`__). Click on the
   Name or the GUID of the Context Scheme to open the "Edit Context
   Scheme" page where its detail can be viewed.

Search and view Business Context detail
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To retrieve a Business Context:

1. On the top menu of the page, click "Context".

2. Choose "View/Edit Business Context" from the drop-down list.

3. Locate the desired Context Scheme using the pagination at the bottom
   or use the *Name* (`How to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__), *Updater*,
   *Updated start date*, and *Updated end date* search filters (see `How
   to use Search Filters <#how-to-use-search-filters>`__). Click on the
   Name or the GUID of the Context Scheme to open the "Edit Context
   Scheme" page where its detail can be viewed.
