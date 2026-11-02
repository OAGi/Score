---
title: "GitHub Integration"
sidebar_position: 0
---

connectCenter can link your core components to issues on GitHub and keep those issues informed as your work progresses.
The integration lets you do three things.
First, you can link one or more GitHub issues to a core component (ACC, ASCCP, BCCP, DT, code list, or agency ID list) so the issue and the component are connected.
Second, when you change a component's state, connectCenter can post a status comment — typically a change summary of what happened — onto each linked issue.
Third, if your administrator has turned on Projects board sync, changing a component's state can move the linked issue's card to a mapped column on a GitHub Projects v2 board.

Two prerequisites apply before you can use any of this.
The feature must be enabled by an administrator (see [Administrator setup](./05-administrator-setup.md)).
Each user connects their own personal GitHub account, and every comment and board move is made with that user's own GitHub token.
This means GitHub actions reflect *your* GitHub identity and *your* GitHub permissions.

All GitHub actions are best-effort.
A GitHub outage or a missing permission never blocks or rolls back a component state change — connectCenter simply skips the GitHub part and continues.
