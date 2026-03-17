Multi-tenant management
=======================

Multi-tenant mode should be enabled at the first deployment of connectCenter.
While it is possible to switch multi-tenant mode on or off, it is not recommended to go back and forth between the two modes.

Multi-tenant mode
-----------------

Multi-tenant management functions are available only to users who have the Admin right when multi-tenant mode is enabled.

connectCenter multi-tenant feature enables organizations to offer connectCenter-based BIE development services to multiple members on a single instance with visibility limited by tenant.
The visibility is restricted by the business contexts associated with the BIE and the tenant.
In other words, if a BIE has a business context that is associated with a tenant, then end users in that tenant can see the BIE.
Users can also see BIEs whose business contexts do not belong to any tenant.

Enable multi-tenant mode
~~~~~~~~~~~~~~~~~~~~~~~~

By default, connectCenter does not use multi-tenant mode.
To enable multi-tenant mode:

1. Click your account name at the top-right of the page.

2. Choose "Settings" from the drop-down list.

3. Click "Application settings" on the left side of the page.

4. Click "Enable" for "Multi-tenant mode".

5. In the "Enable multi-tenant mode?" dialog, review the warning and click "Enable".
   Click "Cancel" if you do not want to change the mode.

6. Wait for the "Updated" message.

Add a tenant
~~~~~~~~~~~~

To add a tenant:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click the "New Tenant" button.

3. On the "Create Tenant" page, enter a value in the *Name* field.

4. Click the "Create" button.

Manage tenant-user associations
-------------------------------

Associate a tenant and user
~~~~~~~~~~~~~~~~~~~~~~~~~~~

To associate a tenant and user:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Users" for the desired tenant.

3. On the "Users Management" page, click the "Add User" button.

4. Search for the desired user by Login ID, or use Advanced Search to filter by Name, Organization, or Status.

5. Click the "Add" button for the desired user.

Dissociate a tenant and user
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To dissociate a tenant and user:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Users" for the desired tenant.

3. On the "Users Management" page, find the desired user.

4. Click the "Remove" button for that user.

Manage tenant-business context associations
-------------------------------------------

Associate a tenant and business context
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To associate a tenant and business context:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Business Context" for the desired tenant.

3. On the "Business Context Management" page, click the "Add Business Context" button.

4. Search for the desired business context by Name, or use Advanced Search to filter by Updater or Updated date.

5. Click the "Add" button for the desired business context.

Dissociate a tenant and business context
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To dissociate a tenant and business context:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Business Context" for the desired tenant.

3. On the "Business Context Management" page, find the desired business context.

4. Click the "Remove" button for that business context.

Multi-tenant mode feature restrictions
--------------------------------------

Features not available
~~~~~~~~~~~~~~~~~~~~~~

The following features are not available to users when connectCenter is in multi-tenant mode:

* Manage modules
* Manage core components
* Make BIE reusable
* Create ABIE extension locally
* Create ABIE extension globally
* Business Term Management

Features with restricted behavior
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Create BIE: A user must be associated with a tenant to be able to create a BIE.
* Manage associations between business contexts (BCs) and BIEs:

   * The BCs available to the user are limited by tenant association.
   * Users who are not associated with a tenant cannot create a BIE because they cannot assign a business context during BIE creation.
   * Admin users do not have special authorization in this case.

* Manage context: Restricted to users with the Admin right in multi-tenant mode.
* Transfer BIE ownership: Users to whom BIE ownership may be transferred are limited to users associated with tenants associated with business contexts associated with the BIE.
