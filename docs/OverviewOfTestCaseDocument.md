# Purpose of this document
The purpose of this document is to describe what test case document is, its structure, and convention to help Score contributors authoring its content.

# Purpose of the Test Case Document
The purpose of the test case document is to document expected behavior (normal and exception ones) of the Score’s functionalities. This includes all rules and constraints on certain functionalities. This information is then used for developing acceptance test scripts and oftentimes also for developing the user guide.

Test Case Document can be access at this [link](./docs/test_cases).

# Document Structure
Test case document provides three levels of functional decomposition (or grouping). The first level is Test Suites (TS). Each Test Suite has one or more Test Cases (TC).  Each Test Case typically has many Test Assertions (TA) and Test Steps. Test Steps are optional. It is a human readable version of test scripts. Having them as a prelude to test script writing will make the task easier. However, it could be cumbersome to keep them in sync as some ideas may change quite a bit when writing the test script.

Pre-conditions may be needed in each of the level. They indicate the situation in which the Test Suite, Test Case, or Test Assertion are applicable and that the System Under Test (SUT) must be injected with data or actions such that its state satisfies those conditions before applying the test. 

Although there is no one correct way to structure and decompose a functionality into Test Suites, Test Cases, and Test Assertions, typically there is one Test Suite for a functionality. A functionality typically maps to a menu item of the application, for example, a BIE Management Test Suite for the whole BIE Management menu. But the most important point about the decomposition is to make the authoring different testing situations simpler and more manageable. Therefore, the test case author may feel like it helps to decompose the functionality of the View/Edit BIE menu into two test suites rather than one, that are Standard Developer BIE Management Test Suite and End User BIE Management Test Suite, because there are potentially differences in business rules when the Standard Developer uses the functionality vs. when the End user uses it. This is absolutely fine. On the other hands, the test case developer and the test script developer may together decide to combine them later to make the test script and its execution more efficient. 

After Test Suite, Test Case provides another level of grouping and decomposition of the system behavior. Test Assertion then indicates a specific compliance. We may say that Test Case is a grouping of Test Assertions that can share a common pre-condition, i.e., system and data set up, but there may be other grouping criteria such as a certain sub-functionality. TAs are typically written as a flow of related actions such add, edit, then delete certain fields or an entity. This may help the author ensure that he/she covers all aspects of the functionality. Test Assertion (TA) is a single condition/behavior of the TC. 

As mentioned earlier, Test Steps are optional. They describe a sequence of actions that lead to the verification of each test assertion. Some Test Steps are just preparation steps. It is a good practice to provide links to Test Assertions on the step, which verifies one or more Test Assertions.

Example: “Test Suite 10: Working branch Core Component Management Behaviors for Developer” consists of TCs that test the functions related to Core Components in the Working branch when the current user is logged in using a developer user account. “Test Case 10.3: Editing a brand-new developer ACC” test the editing functionality of a brand-new ACC when it is being edited by a developer. “TA 1.5 A warning should be given when the Definition is empty” checks the behavior related to the Definition field of a ACC.

# Test document format
Each Test Suite is written in Markdown, and consists of the description and multiple test cases. Each Test Case has the description, pre-conditions, assertions, and test steps. Here’s a form of the test suite:

```
# Test Suite #{$test_suite_number} 
**{$test_suite_name}**

> {$test_suite_description}

## Test Case #{$test_suite_number.$test_case_number} 
**{$test_case_name}**

> {$test_case_description}

{$pre_conditions_of_test_case if available}

### Test Assertion:

#### Test Assertion #{$test_suite_number.$test_case_number.$test_assertion_number}
{$test_assertion_description}

### Test Step Pre-condition:

{$pre_conditions_of_test_step if available}

### Test Step:

{$test_steps}
```

- ```test_suite_number```: An assigned test suite number for indexing. Each test suite has a unique number, and it is a sequential starting with 1.

- ```test_suite_description```: A description of the test suite.

- ```number_of_test_case```: An assigned test case number for indexing. Each test case has a unique number within the test suite, and it is a sequential starting with 1.

- ```test_case_description```: A description of the test case.

- ```test_assertion_number```: An assigned test assertion number for indexing. Each test assertion has a unique number within the test case, and it is a sequential starting with 1.

- ```test_assertion_description```: A description of the test assertion.

- ```pre_conditions_of_test_step```: A set of pre-conditions of test steps. Normally, it contains prerequisites.

- ```test_steps```: A sequence of test steps. Each step could reference targeted test assertion(s).

The following is an example document of the test suite for OAGIS developer authentication in Mark Down format.

```
# Test Suite 1

> OAGIS developer Authentication and Authorized Functions


## Test Case 1.1

> Built-in OAGIS developer account exists

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #1.1.1
Built-in OAGIS developer account exists.

...

#### Test Assertion #1.1.4
Built-in OAGIS developer account cannot login with an invalid password.

### Test Step Pre-condition:

1. There is no existing user session in the browser.

### Test Step:

1. A user opens the Score homepage.
2. The user logs in with the username "oagis" and valid password, namely "oagis".
3. Verify that the user successfully logged in and that it has the OAGIS developer role. (Assertion [#1](#test-assertion-111), [#2](#test-assertion-112))
4. The user logs out. (Assertion [#3](#test-assertion-113))
5. The user logs in with the username "oagis" and a random invalid password.
6. Verify that the user got notified with an invalid log in. (Assertion [#4](#test-assertion-114))

## Test Case 1.2

> OAGIS developer's authorized functionalities

...
```
