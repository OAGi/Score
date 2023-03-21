# Issue Labels
Primarily, three labels are used in Score development
1. Bug: An issue should be labeled Bug when there is an unexpected behavior or a behavior that is inconsistent to the design.
2. Enhancement: An issue should be labeled Enhancement when it is a about a new feature.
3. Design change: The Design change label is used when a behavior is as expected according to the current design but it needs to be worked on due to an intended change to the desirable behavior.

# Assignment of Issue to Milestone and Project
Issue can be assigned to a milestone and project by a person who works on the issue after observing the target release date of the milestone. The core team of Score may move the issue to a different milestone and project if there is a change in the target release date or it is clear that the issue cannot be closed by the release date.

# Avoid duplicate issues
It is incumbent to everyone to search for an existing issue that is same or similar before posting a new one. Unique comments could then be added to that issue and escalate the issue milestone if so desire.

# Create a new issue
Name of the issue shall communicate the problematic behavior or new functional requirement in a simple way. For instance, “A Context Category cannot be updated”. 

For a bug, how to reproduce the problematic behavior should be specified step by step. The expected behavior shall be described. Screen shots and stacktrace shall be provided in cases they are available. An example bug issue is provided below.

```
Name: The issue is that we cannot update a context category

Description:
To reproduce the issue:
1.	Login as an end user
2.	Create a context category
3.	Open the context category
4.	Change the description field
At this point the “Update” button is disabled.

The expected behavior is that a context category can be updated if we change the description field.
This issue applies to both developer and end user accounts.
```

# Close an issue
Issue should be closed by the person who run the acceptance test on the issue. If regression occurs afterward, the issue can be reopen or a new issue can be created.

# Moving issue in a project
See the [Contributing Guide](https://github.com/OAGi/Score/docs/CONTRIBUTING.md) under 'How to contribute'.
