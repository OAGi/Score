# Test Suite 33

> Code List Uplifting


## Test Case 33.1

> Developer Code List Uplifting

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #33.1.1
The developer cannot use this functionality.

### Test Step Pre-condition:



### Test Step:

## Test Case 33.2

> End User Code List Uplifting

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #33.2.1
The end user can visit the Uplift Code List page where he can choose a code list to uplift from a source release to a target release. The target release must be greater than the source release. The end user cannot select the Working branch.

#### Test Assertion #33.2.2
The end user can view in the Uplift Code List page any end user code list (i.e., New ones with or without base) in any state (i.e., WIP, QA, Production, Amended, Deleted, Deprecated).

#### Test Assertion #33.2.3
The end user can uplift an end user code list in WIP state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.4
The end user can uplift an end user code list in QA state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.5
The end user can uplift an end user code list in Production state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.6
The end user can uplift an end user code list in Deleted state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.7
The end user can uplift an end user deprecated code list from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.8
The end user can uplift an end user amended code list from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.9
The end user can uplift an end user derived code list from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.10
The end user can uplift an end user derived code list from a source release to a target release. If the developer code list of the target release and the uplifted end user code list contain some same values, the developer code list values are used. The user should be also notified about that.

### Test Step Pre-condition:



### Test Step: