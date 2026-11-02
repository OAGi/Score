---
title: "Connecting your GitHub account"
sidebar_position: 1
---

You connect and disconnect GitHub from the *GitHub* box that appears on each component detail page.
There is no separate settings page, account page, or top-menu item for GitHub — the connection is managed entirely from this box.
Your connection is per user and global to your session, so once you connect, the connected state is shown on every component's GitHub box.

To connect your GitHub account:

1. Open the detail page of any core component (for example an ACC, ASCCP, BCCP, DT, code list, or agency ID list).

2. Find the *GitHub* box below the *Definition* field.

3. Click the "Connect GitHub" button.
   connectCenter redirects you to GitHub to authorize the connection, then returns you to the same page.

4. On the GitHub consent screen, review and grant the requested access.

Once connected, the box header shows the GitHub mark, the title "GitHub", and your account as `@<login>` in bold, followed by a "Disconnect" button.

To disconnect, click the "Disconnect" button in the box header.
Disconnecting removes your stored GitHub token from connectCenter.
Any issues already linked to your components remain visible in read-only form after you disconnect.

> Note that if your administrator turns on Projects board sync after you have already connected, you must disconnect and connect again so that connectCenter can request the additional GitHub permissions board sync needs.
