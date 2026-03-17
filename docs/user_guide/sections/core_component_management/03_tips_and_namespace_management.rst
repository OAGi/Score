Standard/Developer Core Component Management
--------------------------------------------

This part of the user guide covers developer CC management.
`End user core component management <#end-user-core-component-management>`__ will be covered in the `BIE extension <#extend-a-bie>`__ section.
**To manage developer core components, make sure that the Working branch is selected**.

Core Component Management Tips and Tricks
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you have already started using connectCenter for CC management, this section may help increasing your productivity.

1. To navigate to a particular node of the CC tree quickly, use the
   search box on the top of CC tree. It should be noted that 1) Current
   selected node is context node the search from and 2) Search term
   should be space-separated words as appearing in the tree.

2. Use multiple browser tabs to view, create or edit CCs. You can even
   use another tab to create a new CC to be used in CC being edited in
   another tab.

3. Click on the Ellipsis (the three dots) next to the node to open the
   context menu. You can find more shortcuts and macros such as:

   1. Open the CC associated with node in a new browser tab. This allows
      you to make changes or investigate some more details about that CC
      without leaving the top-level CC you are interested in.

   2. A macro to create an ASCCP from an opened ACC.

   3. A macro to create OAGi Extension.

   4. Faster tree expansion with "Expansion 2" and "Expansion 3", which
      means expanding the tree 2 or 3 levels in one click.

   5. The "Where Used" menu allows you to analyze where a particular CC
      is referenced, including when an ACC is used as a based ACC.

Namespace Management
~~~~~~~~~~~~~~~~~~~~

All CCs require a namespace.
Namespaces are designated as either Standard (i.e., developer namespace) or Non-standard (i.e., end user namespace).

Namespace is used for two purposes in connectCenter - 1) It designates the universe to which the CC belongs and 2) It can be used in XML schema or other syntactical expression; however, some overriding can occur during the expression.

Find a namespace
^^^^^^^^^^^^^^^^

1. Click on the "View/Edit Namespace" under the "Core Component" menu at
   the top of the page.

2. Use any of the filter fields on the top or use pagination buttons at
   the bottom of the page to find the desired namespace.

3. Click on any column header to sort the namespaces. This can also help
   find the desired namespace.

View detail of a namespace
^^^^^^^^^^^^^^^^^^^^^^^^^^

1. `Find the desired namespace <#find-a-namespace>`__.

2. The whole detail of the namespace is displayed in the list of the
   namespaces table; or click on the URI of the desired namespace to
   open the "Namespace Detail" page. **Tips**: On some browsers, hold
   down Ctrl on the keyboard and click to open the detail page in a new
   tab.

Edit detail of a namespace
^^^^^^^^^^^^^^^^^^^^^^^^^^

1. The user has to be the owner of the namespace to make changes, but
   the ownership can be transferred. See also `Transfer ownership of a
   namespace <#transfer-ownership-of-a-namespace>`__.

2. `Find the desired namespace <#find-a-namespace>`__.

3. Click on the URI of the desired namespace to open the "Namespace
   Detail" page.

4. Change the properties of the namespace. The URI is required and must
   follow the URI syntax.

5. Click "Update" to save changes or click "Back" to cancel changes.

Create a namespace
^^^^^^^^^^^^^^^^^^

1. Click on the "View/Edit Namespace" under the "Core Component" menu at
   the top of the page.

2. Click the "New Namespace" button at the top right of the page.

3. Specify at least the URI according to the URI specification syntax.

4. **Notice** that a namespace created by a developer user is by default
   a Standard namespace (the *Standard* check box is checked and
   locked). When the end user creates a namespace, the *Standard* check
   box is unchecked and locked, i.e., the namespace is an end-user
   namespace.

5. Click "Create" to save the new namespace or click "Back" to cancel.

**Notice** that a namespace created by a developer user is by default a Standard namespace (the "Standard" check box is checked and locked).
When the end user creates a namespace, the "Standard" check box is unchecked and locked, i.e., the namespace is an end-user namespace.

Discard a namespace
^^^^^^^^^^^^^^^^^^^

Discard permanently delete the namespace.
Only the owner can discard, but the ownership can be transferred.
See also `Transfer ownership of a namespace <#transfer-ownership-of-a-namespace>`__.

1. `Find the desired namespace <#find-a-namespace>`__.

2. There are two ways to invoke the discard function.

   1. Click on the three-dot ellipsis on the right of namespace to
      discard and click on "Discard", or

   2. `Open the detail page of the
      namespace <#view-detail-of-a-namespace>`__ and click the "Discard"
      button.

3. Click "Discard" again on the pop-up dialog to confirm; or click
   "Cancel" to go back. If the namespace is not used by any other
   entity, it will be discarded. Otherwise, a message is returned
   indicating that the namespace cannot be deleted.

Transfer ownership of a namespace
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If another user needs to update or manage the namespace, its ownership must be transferred to that user.
Only the current owner of the namespace can transfer to another user.
The transfer can occur only between the same user types – developer or end user.

1. `Find the desired namespace <#find-a-namespace>`__.

2. There are two ways to invoke the ownership transfer function.

   1. Click on the two opposite arrows icon next to the username in the
      *Owner* column, or

   2. Click on the three-dot ellipsis on the right of the namespace
      entry, then click "Transfer Ownership".

3. The dialog listing transferable users pops up. Use the search fields
   or pagination to find the desired user.

4. Check the checkbox in front of the user entry and click "Transfer".
   Alternatively, click "Cancel" to go back.
