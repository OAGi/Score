# Test Suite 45

**BIE State Transition and Validation Rules for Base, Inherited, Reused, and Code List Dependencies**

> Common pre-condition: an end user account can sign in and access `View/Edit BIE` and `View/Edit Code List`. In the same release and business context, the following records exist or can be created by the same end user: Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2.

## Test Case 45.1

**Move Derived BOM from WIP to QA**

Pre-condition: In the same release and business context, Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 exist and are owned by the same end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. All BIEs and both code lists are in `WIP`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | reused and base dependency | reused by Base BOM; base of Derived BOM Header | `WIP` | the row should show `This BIE must be in 'QA'.` while unselected; that row-level message should disappear after the row is checked; after submission, this record reaches `QA`. (Assertion [#4](#test-assertion-4514), [#5](#test-assertion-4515), [#2](#test-assertion-4512)) |
| Derived BOM Header | reused dependency | reused by Derived BOM; inherits from Base BOM Header | `WIP` | the row should show `This BIE must be in 'QA'.` while unselected; that row-level message should disappear after the row is checked; after submission, this record reaches `QA`. (Assertion [#4](#test-assertion-4514), [#5](#test-assertion-4515), [#2](#test-assertion-4512)) |
| Base BOM | inherited dependency | reused by Derived BOM | `WIP` | the row should show `This BIE must be in 'QA'.` while unselected; that row-level message should disappear after the row is checked; after submission, this record reaches `QA`. (Assertion [#4](#test-assertion-4514), [#5](#test-assertion-4515), [#2](#test-assertion-4512)) |
| **Derived BOM** | transition root | inherits from Base BOM; reuses Derived BOM Header | `WIP` | the dependency dialog should block the move and show the summary `This BIE cannot move to 'QA'.` until every required row is selected; after submission, this record reaches `QA`. (Assertion [#1](#test-assertion-4511), [#2](#test-assertion-4512), [#3](#test-assertion-4513)) |
| Security Classification | reused dependency | reused by Base BOM and Derived BOM | `WIP` | the row should show `This BIE must be in 'QA'.` while unselected; that row-level message should disappear after the row is checked; after submission, this record reaches `QA`. (Assertion [#4](#test-assertion-4514), [#5](#test-assertion-4515), [#2](#test-assertion-4512)) |
| Code List 1 | assigned code list | assigned on Base BOM | `WIP` | the row should show `This code list must be in 'QA'.` while unselected; that row-level message should disappear after the row is checked; after submission, this code list reaches `QA`. (Assertion [#4](#test-assertion-4514), [#5](#test-assertion-4515), [#2](#test-assertion-4512)) |
| Code List 2 | assigned code list | assigned on Base BOM Header | `WIP` | the row should show `This code list must be in 'QA'.` while unselected; that row-level message should disappear after the row is checked; after submission, this code list reaches `QA`. (Assertion [#4](#test-assertion-4514), [#5](#test-assertion-4515), [#2](#test-assertion-4512)) |

### Test Assertion:

#### Test Assertion #45.1.1
Derived BOM cannot move from `WIP` to `QA` while any required same-owner reused or inherited BIE dependency remains in `WIP` and is not selected in the dependency dialog.

#### Test Assertion #45.1.2
Derived BOM can move from `WIP` to `QA` when the required same-owner dependency chain is selected in the dependency dialog.

#### Test Assertion #45.1.3
For this root BIE, the blocking dependency dialog shows the validation summary `This BIE cannot move to 'QA'.` and includes blocking rows for Base BOM Header, Derived BOM Header, Base BOM, Security Classification, Code List 1, and Code List 2 while they remain in `WIP`.

#### Test Assertion #45.1.4
The blocking BIE rows use the message `This BIE must be in 'QA'.`, and the blocking code list rows use the message `This code list must be in 'QA'.`

#### Test Assertion #45.1.5
As each dependency row is selected one by one, the dialog is revalidated and the remaining unselected blocking rows continue to show their expected validation messages until they are selected.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification are in `WIP`.
3. Code List 1 and Code List 2 are in `WIP` and are owned by the same end user.

### Test Step:
1. Sign in to connectCenter as the end user who owns the test records.
2. Open Derived BOM in the BIE editor.
3. Invoke `Move to QA`.
4. Verify that the dependency dialog is displayed instead of only a simple confirmation dialog. (Assertion [#1](#test-assertion-4511), [#3](#test-assertion-4513))
5. Verify that the dialog shows the validation summary `This BIE cannot move to 'QA'.` (Assertion [#3](#test-assertion-4513))
6. Verify that the dialog shows blocking rows for Base BOM Header, Derived BOM Header, Base BOM, Security Classification, Code List 1, and Code List 2 while they remain in `WIP`. (Assertion [#3](#test-assertion-4513))
7. Verify that the blocking BIE rows use the message `This BIE must be in 'QA'.` and that the blocking code list rows use the message `This code list must be in 'QA'.` (Assertion [#4](#test-assertion-4514))
8. Select one required blocking row and verify that the dialog is revalidated and that every remaining unselected blocking row still shows its expected validation message. Repeat this one-by-one selection and revalidation check until only one required blocking row remains unselected. (Assertion [#5](#test-assertion-4515))
9. Verify that `Update` remains unavailable while the last required blocking row is still unselected. (Assertion [#1](#test-assertion-4511))
10. Select the final required blocking row.
11. Verify that `Update` becomes available. (Assertion [#2](#test-assertion-4512))
12. Submit the transition.
13. Verify that Derived BOM moves to `QA` and that the selected same-owner dependency rows also move to `QA`. (Assertion [#2](#test-assertion-4512))

## Test Case 45.2

**Move Base BOM from WIP to QA**

Pre-condition: In the same release and business context, Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 exist and are owned by the same end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. All BIEs and both code lists are in `WIP`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | reused dependency | reused by Base BOM; base of Derived BOM Header | `WIP` | this should appear as a required blocking BIE row in the forward chain; after it is checked and the transition is submitted, this record reaches `QA`. (Assertion [#1](#test-assertion-4521), [#2](#test-assertion-4522), [#3](#test-assertion-4523)) |
| Derived BOM Header | associated derived BIE | inherits from Base BOM Header | `WIP` | this row may appear as associated context through `Base of`, but it should not be the required forward blocking row that controls the root transition. (Assertion [#4](#test-assertion-4524)) |
| **Base BOM** | transition root | reuses Base BOM Header and Security Classification | `WIP` | the dependency dialog should block the move and show the summary `This BIE cannot move to 'QA'.` until the required blocking rows are selected; after submission, this record reaches `QA`. (Assertion [#1](#test-assertion-4521), [#2](#test-assertion-4522), [#3](#test-assertion-4523)) |
| Derived BOM | associated derived BIE | inherits from Base BOM | `WIP` | this row may appear as associated context, but it should not be the required forward blocking row that controls the root transition. (Assertion [#4](#test-assertion-4524)) |
| Security Classification | reused dependency | reused by Base BOM and Derived BOM | `WIP` | this should appear as a required blocking BIE row in the forward chain; after it is checked and the transition is submitted, this record reaches `QA`. (Assertion [#1](#test-assertion-4521), [#2](#test-assertion-4522), [#3](#test-assertion-4523)) |
| Code List 1 | assigned code list | assigned on Base BOM | `WIP` | this should appear as a required blocking code-list row in the forward chain; after it is checked and the transition is submitted, this code list reaches `QA`. (Assertion [#1](#test-assertion-4521), [#2](#test-assertion-4522), [#3](#test-assertion-4523)) |
| Code List 2 | assigned code list | assigned on Base BOM Header | `WIP` | this should appear as a required blocking code-list row in the forward chain; after it is checked and the transition is submitted, this code list reaches `QA`. (Assertion [#1](#test-assertion-4521), [#2](#test-assertion-4522), [#3](#test-assertion-4523)) |

### Test Assertion:

#### Test Assertion #45.2.1
Base BOM cannot move from `WIP` to `QA` while a required same-owner reused BIE dependency remains in `WIP` and is not selected in the dependency dialog.

#### Test Assertion #45.2.2
Base BOM can move from `WIP` to `QA` when the required same-owner dependency chain is selected in the dependency dialog.

#### Test Assertion #45.2.3
For this root BIE, the blocking dependency dialog shows the validation summary `This BIE cannot move to 'QA'.` and includes blocking rows for Base BOM Header, Security Classification, Code List 1, and Code List 2 while they remain in `WIP`.

#### Test Assertion #45.2.4
For this root BIE, additional associated BIE rows can appear through `Base of` relations, but the required forward blocking chain is the reused and assigned-code-list path rather than the derived path.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification are in `WIP`.
3. Code List 1 and Code List 2 are in `WIP` and are owned by the same end user.

### Test Step:
1. Sign in to connectCenter as the end user who owns the test records.
2. Open Base BOM in the BIE editor.
3. Invoke `Move to QA`.
4. Verify that the dependency dialog is displayed instead of only a simple confirmation dialog. (Assertion [#1](#test-assertion-4521), [#3](#test-assertion-4523))
5. Verify that the dialog shows the validation summary `This BIE cannot move to 'QA'.` (Assertion [#3](#test-assertion-4523))
6. Verify that the dialog shows blocking rows for Base BOM Header, Security Classification, Code List 1, and Code List 2 while they remain in `WIP`. (Assertion [#3](#test-assertion-4523))
7. Verify that any additional associated derived rows are not the required forward blocking chain for this transition. (Assertion [#4](#test-assertion-4524))
8. Leave one required blocking row unselected and verify that `Update` remains unavailable. (Assertion [#1](#test-assertion-4521))
9. Select every required same-owner blocking row in the chain.
10. Verify that `Update` becomes available. (Assertion [#2](#test-assertion-4522))
11. Submit the transition.
12. Verify that Base BOM moves to `QA` and that the selected same-owner dependency rows also move to `QA`. (Assertion [#2](#test-assertion-4522))

## Test Case 45.3

**Move Derived BOM Header from WIP to QA**

Pre-condition: In the same release and business context, Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 exist and are owned by the same end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. All BIEs and both code lists are in `WIP`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | base dependency | base of Derived BOM Header | `WIP` | this should appear as a required blocking BIE row for this transition; after it is checked and the transition is submitted, this record reaches `QA`. (Assertion [#1](#test-assertion-4531), [#2](#test-assertion-4532), [#3](#test-assertion-4533)) |
| **Derived BOM Header** | transition root | inherits from Base BOM Header | `WIP` | the dependency dialog should block the move and show the summary `This BIE cannot move to 'QA'.` until the required blocking rows are selected; after submission, this record reaches `QA`. (Assertion [#1](#test-assertion-4531), [#2](#test-assertion-4532), [#3](#test-assertion-4533)) |
| Base BOM | unrelated associated root | reuses Base BOM Header | `WIP` | this should not control the dialog outcome for this case because it is outside the required forward chain of the root transition. |
| Derived BOM | unrelated associated root | reuses Derived BOM Header | `WIP` | this should not control the dialog outcome for this case because it is outside the required forward chain of the root transition. |
| Security Classification | separate reused node | reused under primary branch | `WIP` | this should not control the dialog outcome for this case because it is outside the required forward chain of the root transition. |
| Code List 1 | assigned code list | assigned on Base BOM | `WIP` | this should not control the dialog outcome for this case because it is outside the required forward chain of the root transition. |
| Code List 2 | assigned code list | assigned on Base BOM Header | `WIP` | this should appear as a required blocking code-list row for this transition; after it is checked and the transition is submitted, this code list reaches `QA`. (Assertion [#1](#test-assertion-4531), [#2](#test-assertion-4532), [#3](#test-assertion-4533)) |

### Test Assertion:

#### Test Assertion #45.3.1
Derived BOM Header cannot move from `WIP` to `QA` while its required same-owner Base BIE dependency remains in `WIP` and is not selected in the dependency dialog.

#### Test Assertion #45.3.2
Derived BOM Header can move from `WIP` to `QA` when the required same-owner dependency chain is selected in the dependency dialog.

#### Test Assertion #45.3.3
For this root BIE, the blocking dependency dialog shows the validation summary `This BIE cannot move to 'QA'.` and includes blocking rows for Base BOM Header and Code List 2 while they remain in `WIP`.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header and Derived BOM Header are in `WIP`.
3. Code List 2 is in `WIP` and is owned by the same end user.

### Test Step:
1. Sign in to connectCenter as the end user who owns the test records.
2. Open Derived BOM Header in the BIE editor.
3. Invoke `Move to QA`.
4. Verify that the dependency dialog is displayed instead of only a simple confirmation dialog. (Assertion [#1](#test-assertion-4531), [#3](#test-assertion-4533))
5. Verify that the dialog shows the validation summary `This BIE cannot move to 'QA'.` (Assertion [#3](#test-assertion-4533))
6. Verify that the dialog shows blocking rows for Base BOM Header and Code List 2 while they remain in `WIP`. (Assertion [#3](#test-assertion-4533))
7. Leave one required blocking row unselected and verify that `Update` remains unavailable. (Assertion [#1](#test-assertion-4531))
8. Select every required same-owner blocking row in the chain.
9. Verify that `Update` becomes available. (Assertion [#2](#test-assertion-4532))
10. Submit the transition.
11. Verify that Derived BOM Header moves to `QA` and that the selected same-owner dependency rows also move to `QA`. (Assertion [#2](#test-assertion-4532))

## Test Case 45.4

**Move Base BOM Header from WIP to QA**

Pre-condition: In the same release and business context, Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 exist and are owned by the same end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. Base BOM Header, Derived BOM Header, and Code List 2 are in `WIP`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| **Base BOM Header** | transition root | base of Derived BOM Header; reused by Base BOM | `WIP` | the dependency dialog should block the move and show the summary `This BIE cannot move to 'QA'.` until the required code-list row is selected; after submission, this record reaches `QA`. (Assertion [#1](#test-assertion-4541), [#2](#test-assertion-4542), [#3](#test-assertion-4543)) |
| Derived BOM Header | associated derived BIE | inherits from Base BOM Header | `WIP` | this may appear as an associated row through `Base of`, but it should not be the required forward blocking row for this case. (Assertion [#4](#test-assertion-4544)) |
| Base BOM | associated reusing BIE | reuses Base BOM Header | `WIP` | this may appear as associated context, but it should not be the required forward blocking row for this case. (Assertion [#4](#test-assertion-4544)) |
| Derived BOM | associated downstream BIE | reuses Derived BOM Header | `WIP` | this may appear as associated context, but it should not be the required forward blocking row for this case. (Assertion [#4](#test-assertion-4544)) |
| Security Classification | separate reused node | reused under primary branch | `WIP` | this should not control the dialog outcome for this case because it is outside the required forward chain of the root transition. |
| Code List 1 | assigned code list | assigned on Base BOM | `WIP` | this should not control the dialog outcome for this case because it is outside the required forward chain of the root transition. |
| Code List 2 | required assigned code list | assigned on Base BOM Header | `WIP` | this should appear as the required blocking row with the message `This code list must be in 'QA'.`; that row-level message should disappear after the row is checked; after submission, this code list reaches `QA`. (Assertion [#1](#test-assertion-4541), [#2](#test-assertion-4542), [#3](#test-assertion-4543)) |

### Test Assertion:

#### Test Assertion #45.4.1
Base BOM Header cannot move from `WIP` to `QA` while its assigned same-owner code list remains in `WIP` and is not selected in the dependency dialog.

#### Test Assertion #45.4.2
Base BOM Header can move from `WIP` to `QA` when Code List 2 is selected in the dependency dialog.

#### Test Assertion #45.4.3
For this root BIE, the blocking dependency dialog shows the validation summary `This BIE cannot move to 'QA'.` and includes Code List 2 as the required blocking row with the message `This code list must be in 'QA'.`

#### Test Assertion #45.4.4
For this root BIE, an associated derived BIE row can also appear through a `Base of` relation, but that row is not the required forward blocking row for the root transition.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header and Derived BOM Header are in `WIP`.
3. Code List 2 is in `WIP` and is owned by the same end user.

### Test Step:
1. Sign in to connectCenter as the end user who owns the test records.
2. Open Base BOM Header in the BIE editor.
3. Invoke `Move to QA`.
4. Verify that the dependency dialog is displayed instead of only a simple confirmation dialog. (Assertion [#1](#test-assertion-4541), [#3](#test-assertion-4543))
5. Verify that the dialog shows the validation summary `This BIE cannot move to 'QA'.` (Assertion [#3](#test-assertion-4543))
6. Verify that the dialog shows Code List 2 as the required blocking row with the message `This code list must be in 'QA'.` (Assertion [#3](#test-assertion-4543))
7. Verify that any associated derived BIE row is not the required forward blocking row for the root transition. (Assertion [#4](#test-assertion-4544))
8. Leave Code List 2 unselected and verify that `Update` remains unavailable. (Assertion [#1](#test-assertion-4541))
9. Select Code List 2.
10. Verify that `Update` becomes available. (Assertion [#2](#test-assertion-4542))
11. Submit the transition.
12. Verify that Base BOM Header moves to `QA` and that the selected Code List 2 also moves to `QA`. (Assertion [#2](#test-assertion-4542))

## Test Case 45.5

**Move Security Classification from WIP to QA**

Pre-condition: In the same release and business context, Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 exist and are owned by the same end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. Security Classification is in `WIP`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | unrelated header node | separate branch | `WIP` | this should remain unchanged because it is outside the transition path. |
| Derived BOM Header | unrelated header node | separate branch | `WIP` | this should remain unchanged because it is outside the transition path. |
| Base BOM | dependent consumer | reuses Security Classification | `WIP` | this should remain unchanged because this case moves only the reused classification BIE itself. |
| Derived BOM | dependent consumer | reuses Security Classification | `WIP` | this should remain unchanged because this case moves only the reused classification BIE itself. |
| **Security Classification** | transition root | reused by Base BOM and Derived BOM | `WIP` | the system should not show the dependency dialog for this record; it should use only the simple confirmation path and then move this record to `QA`. (Assertion [#1](#test-assertion-4551), [#2](#test-assertion-4552)) |
| Code List 1 | assigned code list | assigned on Base BOM | `WIP` | this should remain unchanged because it is outside the transition path. |
| Code List 2 | assigned code list | assigned on Base BOM Header | `WIP` | this should remain unchanged because it is outside the transition path. |

### Test Assertion:

#### Test Assertion #45.5.1
The Security Classification can move from `WIP` to `QA` without a dependency dialog when it has no reused or inherited BIE prerequisites and no assigned code lists.

#### Test Assertion #45.5.2
For this root BIE, the system shows only a simple confirmation path for the `WIP` to `QA` transition rather than the dependency-resolution dialog.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Security Classification is in `WIP`.
3. No assigned code list is attached directly to Security Classification.

### Test Step:
1. Sign in to connectCenter as the end user who owns the test records.
2. Open Security Classification in the BIE editor.
3. Invoke `Move to QA`.
4. Verify that the dependency dialog is not displayed and that the system instead follows the simple confirmation path for the transition. (Assertion [#1](#test-assertion-4551), [#2](#test-assertion-4552))
5. Confirm the transition.
6. Verify that Security Classification moves to `QA`. (Assertion [#1](#test-assertion-4551))

## Test Case 45.6

**Move Derived BOM from QA to Production with Compatible Code Lists**

Pre-condition: In the same release and business context, Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 exist and are owned by the same end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. All BIEs are in `QA`. Both code lists are in `QA`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | reused and base dependency | reused by Base BOM; base of Derived BOM Header | `QA` | this should appear as a promotable dependency row when it still needs promotion; after selection and submission, it reaches `Production`. (Assertion [#1](#test-assertion-4561), [#3](#test-assertion-4563)) |
| Derived BOM Header | reused dependency | reused by Derived BOM; inherits from Base BOM Header | `QA` | this should appear as a promotable dependency row when it still needs promotion; after selection and submission, it reaches `Production`. (Assertion [#1](#test-assertion-4561), [#3](#test-assertion-4563)) |
| Base BOM | inherited dependency | reused by Derived BOM | `QA` | this should appear as a promotable dependency row when it still needs promotion; after selection and submission, it reaches `Production`. (Assertion [#1](#test-assertion-4561), [#3](#test-assertion-4563)) |
| **Derived BOM** | transition root | inherits from Base BOM; reuses Derived BOM Header | `QA` | the dependency dialog should show the lower-state rows that still need promotion; after every required row is selected and the transition is submitted, this record reaches `Production`. (Assertion [#1](#test-assertion-4561), [#3](#test-assertion-4563)) |
| Security Classification | reused dependency | reused by Base BOM and Derived BOM | `QA` | this may appear as a promotable dependency row when it still needs promotion; after selection and submission, it reaches `Production` if included in the transition. (Assertion [#1](#test-assertion-4561)) |
| Code List 1 | assigned code list | assigned on Base BOM | `QA` | this should appear as a promotable code-list row when it still needs promotion; after selection and submission, it reaches `Production`. (Assertion [#2](#test-assertion-4562), [#3](#test-assertion-4563)) |
| Code List 2 | assigned code list | assigned on Base BOM Header | `QA` | this should appear as a promotable code-list row when it still needs promotion; after selection and submission, it reaches `Production`. (Assertion [#2](#test-assertion-4562), [#3](#test-assertion-4563)) |

### Test Assertion:

#### Test Assertion #45.6.1
Derived BOM can move from `QA` to `Production` when all required reused and Base BIE dependencies are already in `Production` or are selected for the same transition.

#### Test Assertion #45.6.2
Assigned code lists on the dependency path can be cascaded from `QA` to `Production` together with the BIE transition when they are same-owner rows.

#### Test Assertion #45.6.3
For this graph, a successful `QA` to `Production` transition of Derived BOM may require Base BOM Header, Derived BOM Header, Base BOM, Code List 1, and Code List 2 to reach `Production`.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification are in `QA`.
3. Code List 1 and Code List 2 are in `QA` and are owned by the same end user.

### Test Step:
1. Sign in to connectCenter as the end user who owns the test records.
2. Open Derived BOM in the BIE editor.
3. Invoke `Move to Production`.
4. Verify that the dependency dialog shows the lower-state BIE and code-list rows that still need promotion. (Assertion [#1](#test-assertion-4561), [#3](#test-assertion-4563))
5. Select the required same-owner BIE rows in the dependency chain.
6. Select Code List 1 and Code List 2 when they are still in `QA`.
7. Verify that `Update` becomes available after all required rows are selected. (Assertion [#1](#test-assertion-4561), [#2](#test-assertion-4562))
8. Submit the transition.
9. Verify that Derived BOM reaches `Production`. (Assertion [#1](#test-assertion-4561))
10. Verify that the selected same-owner code lists also reach `Production`. (Assertion [#2](#test-assertion-4562))

## Test Case 45.7

**Move Base BOM Header from QA to WIP with Same-Owner Dependent BIEs**

Pre-condition: In the same release and business context, Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 exist and are owned by the same end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. Base BOM Header, Derived BOM Header, Base BOM, and Derived BOM are in `QA`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| **Base BOM Header** | transition root | base of Derived BOM Header; reused by Base BOM | `QA` | the dependency dialog should block the backward move until the required dependent BIE rows are selected; after submission, this record returns to `WIP`. (Assertion [#1](#test-assertion-4571), [#2](#test-assertion-4572), [#3](#test-assertion-4573)) |
| Derived BOM Header | backward dependent BIE | inherits from Base BOM Header | `QA` | this should appear as a required backward dependency row; after it is checked and the transition is submitted, this record returns to `WIP`. (Assertion [#1](#test-assertion-4571), [#2](#test-assertion-4572), [#3](#test-assertion-4573)) |
| Base BOM | backward dependent BIE | reuses Base BOM Header | `QA` | this should appear as a required backward dependency row; after it is checked and the transition is submitted, this record returns to `WIP`. (Assertion [#1](#test-assertion-4571), [#2](#test-assertion-4572), [#3](#test-assertion-4573)) |
| Derived BOM | backward dependent BIE | depends on Base BOM and Derived BOM Header | `QA` | this should appear as a required backward dependency row; after it is checked and the transition is submitted, this record returns to `WIP`. (Assertion [#1](#test-assertion-4571), [#2](#test-assertion-4572), [#3](#test-assertion-4573)) |
| Security Classification | separate node | not the target of this backward chain | `WIP` | this should stay unchanged because it is outside the backward dependency chain for this case. |
| Code List 1 | assigned code list | assigned on Base BOM | `QA` | this should stay unchanged because it is outside the backward dependency chain for this case. |
| Code List 2 | assigned code list | assigned on Base BOM Header | `QA` | this should stay unchanged because it is outside the backward dependency chain for this case. |

### Test Assertion:

#### Test Assertion #45.7.1
Base BOM Header cannot move from `QA` to `WIP` while dependent same-owner BIEs remain ahead in `QA` and are not selected in the dependency dialog.

#### Test Assertion #45.7.2
Base BOM Header can move from `QA` to `WIP` when the dependent same-owner BIE chain is selected and no dependent BIE on that chain is in `Production`.

#### Test Assertion #45.7.3
For this graph, the backward dependency chain can include Derived BOM Header, Base BOM, and Derived BOM.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Derived BOM Header, Base BOM, and Derived BOM are all in `QA`.
3. The dependent BIE rows are owned by the same end user.

### Test Step:
1. Sign in to connectCenter as the end user who owns the test records.
2. Open Base BOM Header in the BIE editor.
3. Invoke `Back to WIP`.
4. Verify that the dependency dialog is displayed because dependent BIEs are still ahead in `QA`. (Assertion [#1](#test-assertion-4571))
5. Verify that the dialog shows Derived BOM Header, Base BOM, and Derived BOM as backward dependency rows. (Assertion [#3](#test-assertion-4573))
6. Leave one required dependent BIE row unselected and verify that `Update` remains unavailable. (Assertion [#1](#test-assertion-4571))
7. Select the required same-owner dependent BIE rows.
8. Verify that `Update` becomes available. (Assertion [#2](#test-assertion-4572))
9. Submit the transition.
10. Verify that Base BOM Header moves to `WIP` and that the selected dependent same-owner BIE rows also move to `WIP`. (Assertion [#2](#test-assertion-4572))

## Test Case 45.8

**Block Base BOM from WIP to QA by a Different-Owner Header Dependency**

Pre-condition: In the same release and business context, two end user accounts exist: one transition owner and one other end user. Base BOM, Derived BOM Header, Derived BOM, Security Classification, and Code List 1 are owned by the transition owner. Base BOM Header and Code List 2 are owned by the other end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. Base BOM Header, Base BOM, Derived BOM Header, Derived BOM, Security Classification, and Code List 1 are in `WIP`. Code List 2 is already in `QA`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | different-owner reused dependency | reused by Base BOM; base of Derived BOM Header | `WIP` | this should appear as the blocking different-owner BIE row with the ownership message format `This BIE is owned by '<other-owner-login-id>' and cannot be updated.`; its checkbox should be disabled; the row should continue blocking the transition after other selectable rows are checked. (Assertion [#1](#test-assertion-4581), [#2](#test-assertion-4582), [#3](#test-assertion-4583)) |
| Derived BOM Header | associated derived BIE | inherits from Base BOM Header | `WIP` | this should remain unchanged because this case does not submit any transition. |
| **Base BOM** | transition root | reuses Base BOM Header and Security Classification | `WIP` | the dependency dialog should remain blocked because a required reused BIE is owned by a different end user, so this record stays in `WIP`. (Assertion [#1](#test-assertion-4581), [#3](#test-assertion-4583)) |
| Derived BOM | associated derived BIE | inherits from Base BOM | `WIP` | this should remain unchanged because this case does not submit any transition. |
| Security Classification | same-owner reused dependency | reused by Base BOM and Derived BOM | `WIP` | this should appear as a selectable same-owner blocking row with `This BIE must be in 'QA'.`; after it is checked, the transition should still remain blocked by the different-owner row. (Assertion [#1](#test-assertion-4581), [#3](#test-assertion-4583)) |
| Code List 1 | same-owner assigned code list | assigned on Base BOM | `WIP` | this should appear as a selectable same-owner blocking row with `This code list must be in 'QA'.`; after it is checked, the transition should still remain blocked by the different-owner row. (Assertion [#1](#test-assertion-4581), [#3](#test-assertion-4583)) |
| Code List 2 | different-owner assigned code list on the header dependency | assigned on Base BOM Header | `QA` | this should not block this case because it is already in the target-compatible state. |

### Test Assertion:

#### Test Assertion #45.8.1
Base BOM cannot move from `WIP` to `QA` when a required reused BIE dependency is owned by a different end user and therefore cannot be selected for the same transition.

#### Test Assertion #45.8.2
The different-owner reused BIE row uses the ownership message format `This BIE is owned by '<other-owner-login-id>' and cannot be updated.` and remains non-selectable in the dependency dialog.

#### Test Assertion #45.8.3
Even after all selectable same-owner blocking rows are checked, `Update` remains unavailable while the different-owner reused BIE row remains unresolved.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header is owned by the other end user and is in `WIP`.
3. Base BOM, Security Classification, and Code List 1 are owned by the transition owner and are in `WIP`.
4. Code List 2 is owned by the other end user and is already in `QA`.

### Test Step:
1. Sign in to connectCenter as the transition owner.
2. Open Base BOM in the BIE editor.
3. Invoke `Move to QA`.
4. Verify that the dependency dialog is displayed and that the validation summary `This BIE cannot move to 'QA'.` is shown. (Assertion [#1](#test-assertion-4581))
5. Verify that Base BOM Header appears as a blocking different-owner row with the ownership message format `This BIE is owned by '<other-owner-login-id>' and cannot be updated.` and that its checkbox is disabled. (Assertion [#1](#test-assertion-4581), [#2](#test-assertion-4582))
6. Verify that Security Classification and Code List 1 appear as selectable same-owner blocking rows with the messages `This BIE must be in 'QA'.` and `This code list must be in 'QA'.` (Assertion [#3](#test-assertion-4583))
7. Select the same-owner selectable blocking rows.
8. Verify that `Update` remains unavailable because the different-owner Base BOM Header row still blocks the transition. (Assertion [#1](#test-assertion-4581), [#3](#test-assertion-4583))
9. Cancel the dependency dialog.
10. Verify that Base BOM remains in `WIP`. (Assertion [#1](#test-assertion-4581))

## Test Case 45.9

**Block Base BOM from WIP to QA by a Different-Owner Assigned Code List**

Pre-condition: In the same release and business context, two end user accounts exist: one transition owner and one other end user. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, and Code List 2 are owned by the transition owner. Code List 1 is owned by the other end user. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. Base BOM assigns Code List 1. Base BOM Header assigns Code List 2. All BIEs and both code lists are in `WIP`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | same-owner reused dependency | reused by Base BOM; base of Derived BOM Header | `WIP` | this should appear as a selectable same-owner blocking row with `This BIE must be in 'QA'.`; after it is checked, the transition should still remain blocked by the different-owner code list row. (Assertion [#1](#test-assertion-4591), [#3](#test-assertion-4593)) |
| Derived BOM Header | associated derived BIE | inherits from Base BOM Header | `WIP` | this should remain unchanged because this case does not submit any transition. |
| **Base BOM** | transition root | reuses Base BOM Header and Security Classification; assigns Code List 1 | `WIP` | the dependency dialog should remain blocked because an assigned code list is owned by a different end user, so this record stays in `WIP`. (Assertion [#1](#test-assertion-4591), [#3](#test-assertion-4593)) |
| Derived BOM | associated derived BIE | inherits from Base BOM | `WIP` | this should remain unchanged because this case does not submit any transition. |
| Security Classification | same-owner reused dependency | reused by Base BOM and Derived BOM | `WIP` | this should appear as a selectable same-owner blocking row with `This BIE must be in 'QA'.`; after it is checked, the transition should still remain blocked by the different-owner code list row. (Assertion [#1](#test-assertion-4591), [#3](#test-assertion-4593)) |
| Code List 1 | different-owner assigned code list | assigned on Base BOM | `WIP` | this should appear as the blocking different-owner code-list row with the ownership message format `This code list is owned by '<other-owner-login-id>' and cannot be updated.`; its checkbox should be disabled; the row should continue blocking the transition after other selectable rows are checked. (Assertion [#1](#test-assertion-4591), [#2](#test-assertion-4592), [#3](#test-assertion-4593)) |
| Code List 2 | same-owner assigned code list | assigned on Base BOM Header | `WIP` | this should appear as a selectable same-owner blocking row with `This code list must be in 'QA'.`; after it is checked, the transition should still remain blocked by the different-owner code list row. (Assertion [#1](#test-assertion-4591), [#3](#test-assertion-4593)) |

### Test Assertion:

#### Test Assertion #45.9.1
Base BOM cannot move from `WIP` to `QA` when an assigned code list on the dependency path is owned by a different end user and therefore cannot be selected for the same transition.

#### Test Assertion #45.9.2
The different-owner code-list row uses the ownership message format `This code list is owned by '<other-owner-login-id>' and cannot be updated.` and remains non-selectable in the dependency dialog.

#### Test Assertion #45.9.3
Even after all selectable same-owner blocking rows are checked, `Update` remains unavailable while the different-owner code-list row remains unresolved.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Base BOM, Security Classification, and Code List 2 are owned by the transition owner and are in `WIP`.
3. Code List 1 is owned by the other end user and is in `WIP`.

### Test Step:
1. Sign in to connectCenter as the transition owner.
2. Open Base BOM in the BIE editor.
3. Invoke `Move to QA`.
4. Verify that the dependency dialog is displayed and that the validation summary `This BIE cannot move to 'QA'.` is shown. (Assertion [#1](#test-assertion-4591))
5. Verify that Code List 1 appears as a blocking different-owner row with the ownership message format `This code list is owned by '<other-owner-login-id>' and cannot be updated.` and that its checkbox is disabled. (Assertion [#1](#test-assertion-4591), [#2](#test-assertion-4592))
6. Verify that Base BOM Header, Security Classification, and Code List 2 appear as selectable same-owner blocking rows with the messages `This BIE must be in 'QA'.` and `This code list must be in 'QA'.` (Assertion [#3](#test-assertion-4593))
7. Select the same-owner selectable blocking rows.
8. Verify that `Update` remains unavailable because the different-owner Code List 1 row still blocks the transition. (Assertion [#1](#test-assertion-4591), [#3](#test-assertion-4593))
9. Cancel the dependency dialog.
10. Verify that Base BOM remains in `WIP`. (Assertion [#1](#test-assertion-4591))

## Test Case 45.10

**Administrator Discards Base BOM from QA**

Pre-condition: In the same release and business context, an administrator account exists. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification exist and are owned by an end user rather than the administrator. Base BOM reuses Base BOM Header and Security Classification. Derived BOM inherits from Base BOM and reuses Derived BOM Header. Both Base BOM and Derived BOM are in `QA`. Base BOM Header, Derived BOM Header, and Security Classification are in `QA`. Code List 1 and Code List 2 are assigned in the graph and are in `QA`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | reused prerequisite | reused by Base BOM and base of Derived BOM Header | `QA` | this should remain in `QA` and should not appear as a discard dependency row because it can continue to exist without Base BOM. (Assertion [#4](#test-assertion-45104)) |
| Derived BOM Header | reused prerequisite | reused by Derived BOM | `QA` | this should remain in `QA` and should not appear as a discard dependency row because it can continue to exist without Base BOM. (Assertion [#4](#test-assertion-45104)) |
| **Base BOM** | discard root | reuses Base BOM Header and Security Classification; base of Derived BOM | `QA` | the dependency dialog should block discard and show the summary `This BIE cannot be discarded.` until every required dependent BIE row is selected; after submission, this record is removed. (Assertion [#1](#test-assertion-45101), [#2](#test-assertion-45102), [#3](#test-assertion-45103)) |
| Derived BOM | dependent BIE | inherits from Base BOM | `QA` | this should appear as a blocking BIE row with the discard-specific message `This BIE must be discarded.` while it is not selected; after it is checked and the discard is submitted, the record is removed together with Base BOM. (Assertion [#1](#test-assertion-45101), [#2](#test-assertion-45102), [#3](#test-assertion-45103)) |
| Security Classification | reused prerequisite | reused by Base BOM and Derived BOM | `QA` | this should remain in `QA` and should not appear as a discard dependency row because it can continue to exist without Base BOM. (Assertion [#4](#test-assertion-45104)) |
| Code List 1 | assigned code list | assigned on Base BOM | `QA` | this should remain unchanged and should not appear as a discard dependency row because discard does not cascade to code lists in this flow. (Assertion [#4](#test-assertion-45104)) |
| Code List 2 | assigned code list | assigned on Base BOM Header | `QA` | this should remain unchanged and should not appear as a discard dependency row because discard does not cascade to code lists in this flow. (Assertion [#4](#test-assertion-45104)) |

### Test Assertion:

#### Test Assertion #45.10.1
An administrator can initiate discard for a BIE in `QA`, even when the BIE is not in `WIP`.

#### Test Assertion #45.10.2
For administrator discard, the dependency dialog still enforces required dependent BIE selection before the root BIE can be discarded.

#### Test Assertion #45.10.3
The discard dependency dialog uses the discard-specific validation summary `This BIE cannot be discarded.`, uses the row-level message `This BIE must be discarded.`, and uses `Discard` as the confirmation action rather than `Update`.

#### Test Assertion #45.10.4
Assigned code lists and non-dependent prerequisite BIEs do not appear as discard dependency rows and are not discarded together with the selected BIEs.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, Security Classification, Code List 1, and Code List 2 are in `QA`.
3. The administrator does not own the test records.

### Test Step:
1. Sign in to connectCenter as the administrator.
2. Open Base BOM in the BIE editor.
3. Invoke `Discard`.
4. Verify that the dependency dialog is displayed and that the validation summary `This BIE cannot be discarded.` is shown. (Assertion [#1](#test-assertion-45101), [#2](#test-assertion-45102), [#3](#test-assertion-45103))
5. Verify that Derived BOM appears as the blocking BIE row with the message `This BIE must be discarded.` while it is not selected. (Assertion [#2](#test-assertion-45102), [#3](#test-assertion-45103))
6. Verify that Base BOM Header, Derived BOM Header, Security Classification, Code List 1, and Code List 2 do not appear as discard dependency rows. (Assertion [#4](#test-assertion-45104))
7. Verify that the dialog action button label is `Discard` and that it remains unavailable while Derived BOM is unselected. (Assertion [#2](#test-assertion-45102), [#3](#test-assertion-45103))
8. Select Derived BOM.
9. Verify that `Discard` becomes available. (Assertion [#2](#test-assertion-45102), [#3](#test-assertion-45103))
10. Submit the discard.
11. Verify that Base BOM and Derived BOM are removed from the BIE list or can no longer be opened. (Assertion [#1](#test-assertion-45101), [#2](#test-assertion-45102))
12. Verify that Base BOM Header, Derived BOM Header, Security Classification, Code List 1, and Code List 2 still exist in `QA`. (Assertion [#4](#test-assertion-45104))

## Test Case 45.11

**Administrator Discards Derived BOM from Production**

Pre-condition: In the same release and business context, an administrator account exists. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification exist and are owned by an end user rather than the administrator. Derived BOM Header inherits from Base BOM Header. Base BOM reuses Base BOM Header. Derived BOM inherits from Base BOM. Derived BOM reuses Derived BOM Header. Both Base BOM and Derived BOM reuse Security Classification. All five BIEs are in `Production`. Code List 1 and Code List 2 are assigned on the graph and are in `Production`.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | retained prerequisite | reused by Base BOM; base of Derived BOM Header | `Production` | this should remain in `Production` and should not appear as a discard dependency row because it does not depend on Derived BOM. (Assertion [#1](#test-assertion-45111), [#4](#test-assertion-45114)) |
| Derived BOM Header | retained prerequisite | reused by Derived BOM; inherits from Base BOM Header | `Production` | this should remain in `Production` and should not appear as a discard dependency row because it can continue to exist after Derived BOM is discarded. (Assertion [#1](#test-assertion-45111), [#4](#test-assertion-45114)) |
| Base BOM | retained base BIE | base of Derived BOM | `Production` | this should remain in `Production` and should not appear as a discard dependency row because it does not depend on Derived BOM. (Assertion [#1](#test-assertion-45111), [#4](#test-assertion-45114)) |
| **Derived BOM** | discard root | inherits from Base BOM; reuses Derived BOM Header | `Production` | because no other BIEs in the graph depend on it, discard should use the simple confirmation dialog rather than the dependency table; after confirmation, this record is removed. (Assertion [#1](#test-assertion-45111), [#2](#test-assertion-45112), [#3](#test-assertion-45113)) |
| Security Classification | retained prerequisite | reused by Base BOM and Derived BOM | `Production` | this should remain in `Production` and should not appear as a discard dependency row because it can continue to exist after Derived BOM is discarded. (Assertion [#1](#test-assertion-45111), [#4](#test-assertion-45114)) |
| Code List 1 | assigned code list | assigned on Base BOM | `Production` | this should remain unchanged and should not appear in a dependency dialog for this discard. (Assertion [#4](#test-assertion-45114)) |
| Code List 2 | assigned code list | assigned on Base BOM Header | `Production` | this should remain unchanged and should not appear in a dependency dialog for this discard. (Assertion [#4](#test-assertion-45114)) |

### Test Assertion:

#### Test Assertion #45.11.1
An administrator can initiate discard for a BIE in `Production`, even though a non-administrator cannot discard from that state.

#### Test Assertion #45.11.2
For administrator discard from `Production`, when the selected BIE has no dependent BIE rows, the flow falls back to the simple discard confirmation rather than showing the dependency dialog.

#### Test Assertion #45.11.3
The production-state discard confirmation still uses the discard-specific confirmation copy and the `Discard` action label.

#### Test Assertion #45.11.4
Assigned code lists and retained prerequisite BIEs do not participate in the discard dependency selection and remain in `Production`.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification are in `Production`.
3. Code List 1 and Code List 2 are in `Production`.
4. The administrator does not own the test records.

### Test Step:
1. Sign in to connectCenter as the administrator.
2. Open Derived BOM in the BIE editor.
3. Invoke `Discard`.
4. Verify that the simple discard confirmation is displayed with the `Discard` action and without the dependency validation summary or dependency table. (Assertion [#1](#test-assertion-45111), [#2](#test-assertion-45112), [#3](#test-assertion-45113))
5. Verify that Base BOM Header, Derived BOM Header, Base BOM, Security Classification, Code List 1, and Code List 2 do not appear as discard dependency rows because they do not depend on Derived BOM. (Assertion [#2](#test-assertion-45112), [#4](#test-assertion-45114))
6. Submit the discard.
7. Verify that Derived BOM is removed from the BIE list or can no longer be opened. (Assertion [#1](#test-assertion-45111))
8. Verify that Base BOM Header, Derived BOM Header, Base BOM, Security Classification, Code List 1, and Code List 2 still exist in `Production`. (Assertion [#4](#test-assertion-45114))

## Test Case 45.12

**Administrator Bulk-Discards QA BIEs from the BIE List**

Pre-condition: In the same release and business context, an administrator account exists. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification are owned by end users rather than the administrator. Base BOM reuses Base BOM Header and Security Classification. Derived BOM inherits from Base BOM and reuses Derived BOM Header. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification are in `QA`. The administrator can access the BIE list page.

| Record | Role in graph | Relationship focus | Initial state | Expected UI/result in case |
| --- | --- | --- | --- | --- |
| Base BOM Header | retained prerequisite | reused by Base BOM and base of Derived BOM Header | `QA` | this should not appear as a discard dependency row and should remain in `QA` after the selected discard. (Assertion [#1](#test-assertion-45121), [#3](#test-assertion-45123)) |
| **Base BOM** | selected root row from list | reuses Base BOM Header and Security Classification; base of Derived BOM | `QA` | the list-page discard flow should open the same discard dependency dialog used by the editor and should remain blocked until the required dependent BIE row is selected; after submission, this record is removed. (Assertion [#1](#test-assertion-45121), [#2](#test-assertion-45122), [#3](#test-assertion-45123)) |
| Derived BOM | dependent BIE | inherits from Base BOM and reuses Security Classification | `QA` | this should appear as the blocking BIE row in the discard dependency dialog with `This BIE must be discarded.` until selected; after submission, this record is removed. (Assertion [#1](#test-assertion-45121), [#2](#test-assertion-45122), [#3](#test-assertion-45123)) |
| Security Classification | selected root row from list | reused by Base BOM and Derived BOM | `QA` | this root row should participate in the bulk discard, but it should not appear as an additional blocking dependency row. After submission, this record is removed. (Assertion [#1](#test-assertion-45121), [#2](#test-assertion-45122), [#3](#test-assertion-45123)) |

### Test Assertion:

#### Test Assertion #45.12.1
An administrator can invoke discard from the BIE list for selected `QA` rows owned by other users.

#### Test Assertion #45.12.2
The list-page discard flow uses the same discard dependency dialog rules as the BIE editor, including the summary `Selected BIEs cannot be discarded.` for multi-select validation failures.

#### Test Assertion #45.12.3
The list-page discard dialog uses `Discard` as the confirmation action and requires all blocking dependent BIE rows to be selected before submission.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied.
2. Base BOM Header, Derived BOM Header, Base BOM, Derived BOM, and Security Classification are in `QA`.
3. The administrator does not own the test records.

### Test Step:
1. Sign in to connectCenter as the administrator.
2. Open the BIE list page.
3. Select Base BOM and Security Classification in the list.
4. Invoke the toolbar `Discard` action.
5. Verify that the discard dependency dialog is displayed and that it uses the multi-select validation summary `Selected BIEs cannot be discarded.` or, when only one root row is selected by the current UI implementation, the matching single-root discard summary. (Assertion [#1](#test-assertion-45121), [#2](#test-assertion-45122))
6. Verify that Derived BOM appears as the blocking BIE row with `This BIE must be discarded.` while it is not selected, and that Base BOM Header and Derived BOM Header do not appear as discard dependency rows. (Assertion [#2](#test-assertion-45122), [#3](#test-assertion-45123))
7. Verify that the dialog action button label is `Discard`. (Assertion [#3](#test-assertion-45123))
8. Leave one blocking row unselected and verify that `Discard` remains unavailable. (Assertion [#3](#test-assertion-45123))
9. Select the required blocking BIE row.
10. Verify that `Discard` becomes available. (Assertion [#3](#test-assertion-45123))
11. Submit the discard.
12. Verify that Base BOM, Derived BOM, and Security Classification are removed from the BIE list, while Base BOM Header and Derived BOM Header remain in `QA`. (Assertion [#1](#test-assertion-45121), [#2](#test-assertion-45122))
