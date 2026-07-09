---
title: "Overview of Module Management Entities"
sidebar_position: 1
---

## Module

A Module is a path to a directory (a Module Directory) or to a file (a Module File).
Module Files are shown without a file extension such as ".xsd" or ".json", because they are meant to be syntax independent.
The extension is appended only when the module set release is [exported](./03-manage-module-set-release.md#export-a-module-set-release), depending on the chosen expression.

A module can belong to one and only one module set.
Therefore, even though modules in different module sets may have the same path, they are different module entities.

Besides its name, a Module File can carry an optional namespace and an optional version.
The namespace may be used in certain expressions (some overriding can occur, e.g., a component's own namespace takes precedence), and the version is serialized as the version attribute of the file in the XML Schema expression.

## Module Set

A Module Set is a collection of modules organized in a directory hierarchy.
It belongs to a [library](../../../home-page/index.md).
Typically, at least one module set is created per release of the CCs, although this is not necessary as a new release may reuse a prior module set, particularly when there is no new file.
Multiple module sets may be created for a release when different directory structures or CC assignments are needed, for example, for different expressions.

An expression means a serialization of the CCs into files; connectCenter supports the XML Schema and JSON Schema expressions.
One syntax may have more than one expression.
For example, the connectSpec standard has multiple expressions in XML schema, one using the global-type-global-element pattern, another using the global-type-local-element pattern.

## Module Set Release

A Module Set Release associates a module set with a release.
It allows a module set to be reused across releases.
This can save time when there are no new files or changes in the directory structure in a newer release.

The entity also provides the context for assigning CCs to the module files in the module set, as CC assignments have to be done in the context of a release.
When a new module set release is created, the CC assignments of an existing module set release can be copied into it; connectCenter matches the CCs across the two releases by their GUID and the module files by their path.

One module set release per release can be marked as the "Default" one.
The CC assignments of the default module set release are used in related CC management functions, e.g., in the "Module" filter and column on the [View/Edit Core Component page](../../03-search-and-browse-cc-library.md).
