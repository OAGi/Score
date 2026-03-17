Common functions
----------------

How to use the Search field in general
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

On many pages where entities need to be selected, you can find the entity you need by typing a search term into the Search field.
Common fields include *Name*, *Description*, *Property Term*, *DEN*, and *Business Context*.
Once you enter your term, click the "Search" button to list entities where those fields contain the entered terms.

When you enter multiple words in the search field, they are treated as an AND search.
The system will return entities containing both words in any order.
If you need to search for exact phrases, enclose them in double quotes.
For example, entering ["Process Inventory"] in the DEN search field will only return entities whose DEN contains those exact words in that order.

In future connectCenter release an autocomplete drop-down list with suggested terms will appear when you are typing a search term into the Filter field.
In case you want to narrow down the suggested terms, you have to continue typing.

How to use Search Filters
~~~~~~~~~~~~~~~~~~~~~~~~~

A search bar includes a Search button, an input field, and a chevron down button.
Click the chevron down button to reveal additional search filters.

The following search filters are typically available within the advanced search area on most pages:

- *Owner*: Filter based on the owner.
  Click on this filter and select one or more users from the drop-down list.

- *State*: Filter based on the entity state (e.g., WIP).
  Click on this filter and select one or more states from the list.

- *Updater*: Filter based on the user who last modified the entity.
  Click on this filter and choose one or more users from the list.

- *Updated Start Date* / *Updated End Date*: Filter based on the timeframe of the last update.
  Click on these filters and select a date from the calendar.
  You can use them together to set a time range or individually for just a start or end date.

Note: If a search filter is left blank, it will not be applied.

.. _drop-down-list-1:

Drop-down List
~~~~~~~~~~~~~~

Most drop-down lists have a built-in filter.
This is particularly useful when a list is big.
The filter is displayed after a drop-down list is clicked.
The user can narrow down the values in the list by typing in a few characters.
For example, in the *Owner* drop-down, typing in "oa" will narrow down the list to usernames containing "oa".

Commenting
~~~~~~~~~~

connectCenter allows users to post their comments in many entities (e.g., Core Components) so that they can communicate their reviews/suggestions.
Users can also edit their comments or reply to the comments made by other users.
So far, connectCenter support comments to Core Components only.

To add a new comment in an entity,

1. Click the |image4| icon. This icon is usually located in the details
   pane of the page (the right table of the page where the details of an
   entity are displayed).

2. In the window that is returned at the right of the page, write the
   comment you want to add.

3. Click the "Comment" button.

Note that connectCenter does not yet have a built-in notification mechanism.
Each user has to visit the entity in case he/she wants to view all its comments.
After opening the entity,

1. Click the |image5| icon.

2. In the window returned at the right of the page, you can see all the
   comments made so far. Comments are displayed per user in
   chronological order.

3. Click the X icon to close the window with the comments.

A user can edit comments made by him.
To do so,

1. Click the |image6| icon.

2. In the window returned at the right of the page, you can see all the
   comments made so far. Find the comment you want to edit and click the
   pencil button |image7| next to the comment.

3. Edit the comment.

4. Click the "Edit" button to save changes or the "Cancel" button to
   discard the changes on this comment.

Notification Page
~~~~~~~~~~~~~~~~~

connectCenter logs and stores notifications related to user actions in detail in a separate page.
To visit the notification page, click the **bell icon** located at the right side of the top menu.

Currently, connectCenter logs only the actions that are related to BIE reuse.
One example of such an action is when the user tries to Discard a BIE and this BIE is reused by another BIE.
In this case, connectCenter displays a notification at the bottom of the page and keeps a more detail log which is accessible via the notification page.
