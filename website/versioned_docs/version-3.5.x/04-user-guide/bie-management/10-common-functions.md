---
title: "Common functions"
sidebar_position: 10
---

The controls described on this page — the search bar, the advanced search filters, filterable drop-down lists, commenting, and the message page — work the same way wherever they appear in connectCenter, on BIE pages as well as on Core Component, context, and account pages.

## How to use the Search field in general

Most list pages have a search bar at the top.
Its input field searches one specific field, which the placeholder names — for example, "Search by DEN" on the BIE and Core Component pages, "Search by Name" on the code list and context pages, or "Search by Subject" on the message page.
To run the search, type your term and press Enter, or click the magnifying-glass icon at the left of the input field; the list is not filtered while you are typing.

Searching is case-insensitive and matches substrings.
When you enter multiple words in the search field, they are treated as an AND search: the system returns entities that contain all of the words, in any order.
If you need to search for an exact phrase, enclose it in double quotes.
For example, entering `"Process Inventory"` in the "Search by DEN" field only returns entities whose DEN contains those exact words in that order.

On the "BIE" page, the DEN search also matches a BIE's display name, and while a search term is entered the results are ordered by their relevance to the term; entering a new term resets any column sort you had applied.

Other text fields — such as *Business Context*, *Version*, *Remark*, or *Definition* — are separate filters in the advanced search area (see [How to use Search Filters](#how-to-use-search-filters)) and follow the same matching rules.

## How to use Search Filters

A search bar includes the magnifying-glass search button, an input field, and a chevron down button with the tooltip "Show Advanced Search".
Click the chevron down button to reveal additional search filters; while they are shown, the chevron points up and its tooltip reads "Hide Advanced Search".
On pages that list release-specific entities, the search bar also includes a "Branch" drop-down at its left for selecting the release(s) to search in.

The following search filters are typically available within the advanced search area on most pages:

- *State*: Filter based on the entity state (e.g., WIP).
  Click on this filter and select one or more states from the list.

- *Owner*: Filter based on the owner.
  Click on this filter and select one or more users from the drop-down list.

- *Updater*: Filter based on the user who last modified the entity.
  Click on this filter and choose one or more users from the list.

- *Updated start date* / *Updated end date*: Filter based on the timeframe of the last update.
  Click on these filters and select a date from the calendar.
  You can use them together to set a time range or individually for just a start or end date.
  A small X icon next to a filled date filter clears it.

Many pages add filters of their own; the "BIE" page, for example, also offers *Business Context*, *Version*, *Remark*, and *Deprecated*.

Note: If a search filter is left blank, it will not be applied.

## Drop-down List

Drop-down lists that can grow long — such as *Owner*, *Updater*, and *Branch* — have a built-in filter.
The filter, an input field with the placeholder "Search...", is displayed at the top of the list after the drop-down is clicked.
The user can narrow down the values in the list by typing in a few characters; the match is a case-insensitive substring match.
For example, in the *Owner* drop-down, typing in "oa" will narrow down the list to usernames containing "oa".
If no value matches, the list says so (e.g., "No matching owner found.").
Short fixed lists such as *State* and *Deprecated* do not have a filter.

## Commenting

connectCenter allows users to post comments on entities so that they can communicate their reviews and suggestions.
Users can also edit or delete their own comments and reply to the comments made by other users.
Comments are supported on Core Components — the ACC, ASCCP, BCCP, and DT editors, including their associations and extensions — and on Code List and Agency ID List detail pages.
The BIE editor does not support comments.

To add a new comment to an entity,

1. Open the comment pane:

    - On a Core Component detail page, right-click a node in the tree in the left pane and select the "Comments" menu item.

    - On a Code List or Agency ID List detail page, click the comments (speech-bubble) icon button next to the page title in the top toolbar.

2. A pane titled "Comment" slides in at the right of the page. Write the comment you want to add in the text box.

3. Click the "Comment" button.

connectCenter does not generate a message or notification when a comment is posted; to read the comments on an entity, each user has to open that entity and its comment pane.
The pane shows all the comments made so far, per thread in chronological order: top-level comments appear in the order they were created, with replies indented beneath them.
Each comment shows the author's login ID and a relative timestamp (e.g., "3 days ago").
If another user posts a comment while you have the same entity open, it appears in the pane immediately.
Click the X icon to close the pane.

To reply to a comment, click the "Reply" link under a top-level comment, write your reply, and click the "Reply" button (or "Cancel").
Replies are single-level: a reply cannot itself be replied to.

A user can edit the comments made by him/her; a pencil (edit) icon is shown next to your own comments only.
To edit a comment,

1. Open the comment pane as described above.

2. Find the comment you want to edit and click the pencil (edit) icon next to it.

3. Edit the comment.

4. Click the "Edit" button to save changes or the "Cancel" button to discard the changes on this comment.

You can also delete your own comments by clicking the X icon next to them; the comment is removed immediately.
If the deleted comment has replies, it is kept in the thread as the placeholder "(This message has been deleted.)"; otherwise it disappears.
Only the author of a comment can edit or delete it.

## Message page

connectCenter delivers in-app messages through the "Message" page.
The **bell icon** at the right side of the top menu (between the Help menu and your username) carries a badge with the number of your unread messages, and its tooltip reports the count (e.g., "You have 3 unread notifications", or "You have no unread notifications").
The bell is filled while there are unread messages and outlined when there are none, and the badge updates in real time without a page refresh.

Click the bell icon to open the "Message" page.
It lists your messages with the columns *Subject* and *Created on*; unread messages show their subject in bold.
You can search the list by subject ("Search by Subject") and filter it by *Sender*, *Created start date*, and *Created end date*.

Click a subject to read a message.
Opening a message marks it as read and shows its formatted body, with a "Back to messages" button to return to the list and a "Discard" button to remove the message.
To remove several messages at once, check the checkboxes in front of them in the list and click the "Discard" button that appears above the table.

Currently, the only messages connectCenter generates are the approval requests sent to administrators when a new single sign-on user signs in and awaits account approval.
