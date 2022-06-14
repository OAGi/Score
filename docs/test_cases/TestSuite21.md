# Test Suite 21

Module Management has three major functions Manage Module-Release Assignment, Manage CC-Module Assignment, and Manage Module Dependency. The data associated with these two functions are used for serializing the model into files. Data can be managed only by developers. End users cannot access to Module Management.

Add Module Management menu item under the Core Component menu. It has three submenus including Module-Release Assignment, CC-Module Assignment, and Module Dependency.

Note: Because entities related module management have no ownership. Multiple developers may edit them at the same time. Two-phase commit needs to be implemented to manage the concurrent edits of these entities.

## Test Case 21.1

> Manage Module Set

Pre-condition: N/A
This functionality allows the developer to create and assign modules to a module set.


### Test Assertion:

#### Test Assertion #21.1.1
From the Module Set list page, the developer can invoke “Create a Module Set”.

#### Test Assertion #21.1.2
At least Name is required (i.e., the Definition field is optional).

##### Test Assertion #21.1.2.a
The developer can also create a corresponding Module Set Release based on the Module set. In this case, the developer should define the “Release” as well as the assignments to copy from (i.e., Copy CC assignments from existing ones)

#### Test Assertion #21.1.3
The developer can edit the details of an existing Module Set. In particular, the developer can edit the Name and  Description fields.

#### Test Assertion #21.1.4
The developer has a few ways to add modules to the selected module set.

##### Test Assertion #21.1.4.a
The developer can create a new module file and add it to the set. For the detail of the module file, only the Name is required (the UI should indicate to the user that backslash is used, maybe by giving an example). Namespace and Version Number are optional. The developer cannot create a new module file without the name.
##### Test Assertion #21.1.4.b
The developer can change the details of a module (e.g., using a dialog “View/Edit a Module”. The Name of the module is unique in a directory. Namespace and Version Number are optional.
##### Test Assertion #21.1.4.c
The developer can create a new module directory and add it to the set. For the detail of the module directory, only the Name is required.
##### Test Assertion #21.1.4.d
The developer can modify the name of a module directory, it cannot be left empty though. The name of the module directory must be unique inside its parent module directory (i.e., there should not be exist two directories with the same name under the same parent module directory; in other words the combination of the name and path of directory must be unique).
##### Test Assertion #21.1.4.e
The developer can copy a module directory from another Module Set. The developer should also have the option to copy all its module subdirectories and module files as well. The selected module directory, subdirectories and files are then copied (by reference) to the current module set (these modules are sorted alphabetically by the module path). Copy function must be idempotent, i.e., the developer can copy the same set module directory multiple times and only module directories and files not already exist in the current module set shall be added.
##### Test Assertion #21.1.4.f
The developer can select a module to copy from another Module Set rather than a directory. In that case, only the selected module is copied. This copy function must be also idempotent.

#### Test Assertion #21.1.5
The developer can discard a module directory or file currently included in the Module Set.

##### Test Assertion #21.1.5.a
In case of a module directory that is not empty, the system should ask the developer to confirm that he wants to discard all its module subdirectories and their module files.
##### Test Assertion #21.1.5.b
If a module file is used in a Module Release Set, the system should ask the developer to confirm that the corresponding assignments to CCs will be also discarded
##### Test Assertion #21.1.5.c
If a module directory contains a module file is used in a Module Release Set, the system should ask the developer to confirm that the corresponding assignments to CCs will be also discarded. Check also the case of discarding a module directory that contains a module directory which contains a module file.

#### Test Assertion #21.1.6
The developer can discard a module set when it has not been assigned to any release.

#### Test Assertion #21.1.7
The developer cannot discard a module set that has been assigned to a release (in the Release Module Set).

#### Test Assertion #21.1.8
The end user can view module sets but cannot make any change or add a new one.

#### Test Assertion #21.1.9
The developer can view any existing module set. He can also edit its details (i.e., there is no ownership).

### Test Step Pre-condition:



### Test Step:

## Test Case 21.2

> Manage Release Module Set

Pre-condition: N/A
Release module set is an entity that refers to module set the developer would like to use for a release.


### Test Assertion:

#### Test Assertion #21.2.1
The developer can create a Release Module Set. The fields “Module Set” and “Release” are mandatories. The former is a drop-down list containing the existing Module Sets and the latter contains all Releases including the Working Branch and a Draft Release Branch.

##### Test Assertion #21.2.1.a
The developer can select this Release Module set to be the default one for the specific selected Branch.

#### Test Assertion #21.2.2
After working on assignment to another release, the developer selects a release previously worked on. The system shall be able to display the module sets already assigned previously.

#### Test Assertion #21.2.3
A Release Module Set can be edited by any other developer (i.e., there is no ownership). The fields “Module Set” and “Release” are mandatories.

#### Test Assertion #21.2.4
The developer can Export a Module Set Release while viewing the details of a Module Set Release.

#### Test Assertion #21.2.5
The end user can view the release module set but cannot make any change.

#### Test Assertion #21.2.6
The developer can create a Release Module Set and assign a Draft release.

##### Test Assertion #21.2.6.a
The developer can move the Draft release back to Initialized state. In this case, the assignments to CCs are not kept (I.e., they are discarded)
##### Test Assertion #21.2.6.b
The developer cannot discard a Release if it is used in a Module Set Release.
##### Test Assertion #21.2.6.c
The developer can move the Release to Draft state again. In this case, the Release Module set is updated accordingly so that to contain the CCs of the Draft Release.

### Test Step Pre-condition:



### Test Step:

## Test Case 21.3

> Manage CC-Module Assignment

Pre-condition: The developer is on the CC-Module Assignment page. At least two releases are available in the database. Each release has at least two module sets assigned.
CC-Module assignment allows developers to assign CCs to the modules in a module set. CCs must be in the same release as the module set.


### Test Assertion:

#### Test Assertion #21.3.1
All branches are available to select from.

#### Test Assertion #21.3.2
After the developer select a release, only module sets assigned to that release can be selected.

#### Test Assertion #21.3.3
After both the release and module set have been selected, CCs and Modules are listed in the “Unassigned CCs” and “Module” pane. Neither ASCC nor BCC are listed.

##### Test Assertion #21.3.3.a
The developer can select one CC and a module and assign the CC to that module. The assigned CC shall then be moved from the “Unassigned CCs” pane to the “CCs in Module” pane (or to “Assigned in “module_name””); the selected module shall still be highlighted.
##### Test Assertion #21.3.3.b
The developer can select a few CCs and a module and assign those CCs to that module. The assigned CCs shall then be moved from the “Unassigned CCs” pane to the “CCs in Module” (or to “Assigned in “module_name””) pane; the selected module shall still be highlighted.
##### Test Assertion #21.3.3.c
The developer selects the module used in 2.1, and the system shall show the CCs assigned in 2.1 in the “CCs in Module” pane.
##### Test Assertion #21.3.3.d
Similar to 2.2 but the developer can use “CC Filter by DEN” and “Module Filter” to help with the assignment.
##### Test Assertion #21.3.3.e
The developer cannot select multiple modules at a time.
##### Test Assertion #21.3.3.f
With a module selected in the Module pane, the developer can select multiple CCs in the module and unassign them. The unassigned CCs shall be moved from the “CCs in Module” pane to the “Unassigned CCs” pane.

#### Test Assertion #21.3.4
The end user can view CC-Module assignment but cannot make any change.

### Test Step Pre-condition:



### Test Step:

## Test Case 21.4

> Manage module dependency

Pre-condition: N/A
This functionality allows the developer to add/discard dependencies between modules about, e.g., include or import.
The assumption is that the module dependency does not change for a module set used in different releases.


### Test Assertion:

#### Test Assertion #21.4.1
The developer can access module dependency management from the Module Set List page. Once the Module Dependency page is opened. The module set from which the function is invoked is selected.

#### Test Assertion #21.4.2
The developer can access module dependency management from the application menu. In this case, the newest module set is selected by default.

#### Test Assertion #21.4.3
On the Module Dependency page, the developer can select another module set to retrieve module dependencies for the module set.

#### Test Assertion #21.4.4
When there is no module dependency information for the module set, the Module Dependency Tree simply lists all the modules in the set.

#### Test Assertion #21.4.5
The developer can drag one or more modules over another module to create dependencies. If both modules have namespaces and the namespaces are the same then the dependency type shall be ‘include’; otherwise ‘import’. If one both modules don’t have namespaces, assign the ‘Default Dependency Type’ specified on the UI.

#### Test Assertion #21.4.6
The developer can discard one or more dependencies at a time.

#### Test Assertion #21.4.7
The developer cannot add a duplicate dependency to the same module.

#### Test Assertion #21.4.8
The developer can Derive Dependencies. This allows the dependencies to be derived based on CC-Module Assignment. A dialog shall pop up for the developer to a CC-Module assignment by selecting a release and one of the assigned module sets. The system has to cluster CCs into modules and create a module dependency graph based on CC dependencies. The derived dependencies shall be added to existing dependencies ignoring the duplicated ones that already exist. The same rules about dependency types specified in TA 5 are applied.

#### Test Assertion #21.4.9
The developer can Copy Dependencies. This allows the developer to select a module set. Then for modules common across the current and selected module set, the system copies the dependencies of each module into this module set, ignoring the dependencies pointing to a module absence from the current module set.

#### Test Assertion #21.4.10
The developer can check for cyclical dependencies in the module set. A cyclical dependency exist b/w two modules A & B if A depends on B and B also depends on A. The dialog box shall list the pairs of modules that have a cyclical dependency where the developer can discard some dependencies.

#### Test Assertion #21.4.11
The end user can view module dependency but cannot make any change.

### Test Step Pre-condition:



### Test Step: