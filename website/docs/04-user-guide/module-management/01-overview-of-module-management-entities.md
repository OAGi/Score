---
title: "Overview of Module Management Entities"
sidebar_position: 1
---

## Module

Module is generally a path to a directory or a file.
Files are shown without a file extension such as ‘xsd’ or ‘json’, because they are meant to be syntax independent.

A module can belong to one and only one module set.
Therefore, even though a module may have the same path, they are different module entities.

## Module Set

Module Set is a collection modules.
Typically, at least one module set is created per a release of CCs although this is not necessary as a new release may reuse prior module set particularly when there is no new file.
Multiple module sets may be created for a release for different directory structures or CC assignments are needed, for example, for different expressions.
An expression means a serialization of the CCs into files.
One syntax may have more than one expression.
For example, the connectSpec standard has multiple expressions in XML schema, one using global-type-global-element pattern, another using global-type-local-element pattern.

## Module Set Release

Module Set Release associates a module set with a release.
It allows a module set to be reused across releases.
This can save time when there is no new file or changes in the directory structure in a newer release.
The entity also facilitates the CC assignments to (file) modules in the module set as CC assignments have to be done in a context of a release.
