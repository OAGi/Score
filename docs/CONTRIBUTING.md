# Contributing Guideline

## Learn about Score
  - [Read installation guide and component architecture](https://github.com/OAGi/Score/wiki/Basic-Installation-Guide-using-Docker-for-Score-Application-Release-1.1.2-and-up)
  - [Read user guide or at least read its content.](./user_guide/index.rst) and/or [Test Case Document](./test_cases/) particularly if you are modifying an existing functionality. Even if you are modifying a certain functionality or adding a new, it might impact other functionalities as well, so it is good to know overall Score behavior. Get advise from the community how changes or additions may impact other functionalities will help speed up the development.
  - [Overview of database structure](https://oagiscore.atlassian.net/wiki/spaces/SCORE/pages/4403363841/Overview+of+Score+Database+Structure)
  - How to set up Score development environment (I think this should be included in the Development Environment Requirement or the other way around)
    - How to initialize database with OAGIS data (for development testing and acceptance testing).
    - [How to set up Score locally](https://github.com/OAGi/Score/wiki/Getting-Score-develop-environments-with-Docker,-Node.js,-and-JDK.)
## How to contribute  
  - Fork the current Score repo to developer's private repo.
  - Create an issue if one does not exist (see also [General Guideline to Issue Management](./GeneralGuidelineToIssueManagement.md))
  - Discuss issue in the Score meeting
    - If issue is accepted,
      - assign label bug, enhancement, or design change
    - If issue is not accepted, then close issue.
  - When start working on the issue.
    - Assign milestone and project
    - Make sure that a specific branch for a specific milestone and issue exists (e.g., 'develop/2.4/1291' branch for the issue #1291 in the 2.4 milestone. 'develop/2.4/1291' branch will be merged into 'develop/2.4', and 'develop/2.4' will be merged into 'develop'.)
    - Once entered a project, the issue is placed on "To DO" card.
  - After that, the issue may traverse through the following cards a few rounds in various order. These cards only serve as indications that the issue has been through these stages. In some cases, with multiple developers working on the same issue, some of these activities can occur in parallel, e.g., writing test assertions (in detail) may happen at the same time as coding. Comments may be made in the issue when some of these tasks are not needed for the issue. 
    - "Writing Test Assertion" Card - See [Overview of test case document](./OverviewOfTestCaseDocument) about how to write test assertions.
    - "Coding and Unit Testing" Card - See - [Development environment requirement](https://github.com/OAGi/Score/wiki/Getting-Score-develop-environments-with-Docker,-Node.js,-and-JDK.) and [Overview of code structure](https://oagiscore.atlassian.net/wiki/spaces/SCORE/pages/4417093633/Overview+of+Code+Structure) to learn about what you may need to start writing code and the coding convention.
    - "Implementing test script" Card - See - Overview of test script development and execution
    - "Debugging" - This means executing the test and may also be changing content associated with other tasks.
    - "Updating user guide"
  - Once, all of the above tasks are done, create a pull request. Alternatively, a pull request can be created after every task. Comments indicating the specific task and the issue the pull request is about should be provided.
  - If all tasks are completed for the issue, the person who merges the content closes the issue. The issue automatically goes to the Close card.
  - If the pull request is not accepted, the developer may cycle through the above tasks again or simply put it back in the "Debugging" card.
  - If the merge causes a failure in a routine test, a new issue should be created that optionally cites the original issue.
