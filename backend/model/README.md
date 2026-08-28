<!--
  Copyright (c) 2026 Obeo.
  This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v2.0
  which accompanies this distribution, and is available at
  https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors:
      Obeo - initial API and implementation
-->

# Capella Model

This root module contains various model services used in the project.

- `capella-model-edit`: contains the icons used in the application to display Arcadia elements.
- `capella-model-services`: contains Arcadia perspective-level services
- `capella-model-transverse-services`: contains transverse services

## Guide to using and structuring services

Before adding a new service or method, use the rules below to decide where it belongs.

Each Arcadia perspective exposes four services: Mutation, Query, RepresentationMutation, RepresentationQuery.
In addition, the `capella-model-transverse-services` module defines _transverse_ services which are used by several pieces of the application (e.g. several perspective, the explorer, etc).

### Perspective and transverse services

- Logical Architecture
  `LAMutationService`, `LAQueryService`, `LARepresentationMutationService`, `LARepresentationQueryService`
- Operational Analysis
  `OAMutationService`, `OAQueryService`, `OARepresentationMutationService`, `OARepresentationQueryService`
- Physical Architecture
  `PAMutationService`, `PAQueryService`, `PARepresentationMutationService`, `PARepresentationQueryService`
- System Analysis
  `SAMutationService`, `SAQueryService`, `SARepresentationMutationService`, `SARepresentationQueryService`
- Transverse
  `TransverseMutationService`, `TransverseQueryService`, `TransverseRepresentationMutationService`, `TransverseRepresentationQueryService`

### Key considerations for adding methods

- Perspective specificity: does the method belong to a specific perspective or is it transverse
  - A service is transverse as soon as it is needed by more than one representation, for example, a creation service is typically used in a diagram but also in the explorer, or in the details view.
- Service requirements: do you need injected beans, the editing context, or view-level objects (like diagram nodes)
  - If yes, use a representation service, but make sure you separate the part of the code that needs to access graphical-level data from the part that operates on pure semantic elements (e.g. an edge creation service may require source/target nodes to compute its container, but the creation itself is a regular service).
  - Non-representation services must keep an empty constructor and should not declare constructors with parameters.
  - Note that representation services are harder to test, so they should only be used when strictly necessary.
- Model modification: if the service changes the model, use a Mutation service, otherwise a Query service

As a rule of thumb, new services should be considered transverse, non-representation services by default, and moved to a specific perspective or a representation service when necessary.

---

## Type-safe AQL service references with `ServiceMethod`

### Why we recommend it

Calling AQL services from Java with raw strings is fragile. `ServiceMethod` lets you write:

```java
ServiceMethod.of2(DetailsViewService::setNewValue)
    .aqlSelf("'declaredName'", ViewFormDescriptionConverter.NEW_VALUE);
```

instead of:

```java
AQLUtils.getSelfServiceCallExpression("setNewValue",
    List.of("'declaredName'", ViewFormDescriptionConverter.NEW_VALUE));
```

Benefits:

- Refactoring friendly: IDE rename updates the method reference
- Find usages and navigation work out of the box
- Fewer string literals, fewer typos
- Automated arity check
- Same AQL output as before
- Compile errors when the Service class changes!

### How it works

- You pass a **serializable method reference** to `ServiceMethod.ofN(...)`
  The helper reads the method name from the lambda metadata and stores it.
- You then build the AQL expression with `.aqlSelf(...)` or `.aql(var, ...)`
- Parameters to `.aql*` are AQL snippets as strings, exactly like `AQLUtils` expects

The helper is very small. It does not instantiate your service or invoke it. It only extracts the Java method name and delegates to `AQLUtils`.

### Usage patterns

**Unbound instance method with self + 2 params**

```java
setNewValueOperation.setExpression(
  ServiceMethod.<DetailsViewService, Element, String, Object>of2(DetailsViewService::setNewValue)
      .aqlSelf("'declaredName'", ViewFormDescriptionConverter.NEW_VALUE)
);
```

Signature in `DetailsViewService`:

```java
boolean setNewValue(Element self, String featureName, Object newValue)
```

**Unbound instance method with self + 1 param**

```java
setNewDescriptionOperation.setExpression(
  ServiceMethod.<LAMutationService, Usage, String>of1(LAMutationService::setElementDescription)
      .aqlSelf(ViewFormDescriptionConverter.NEW_VALUE)
);
// LAMutationService#setElementDescription(Usage self, String newDescription)
```

**Predicate with no extra params**

```java
componentWidgetIf.setPredicateExpression(
  ServiceMethod.of0(LAQueryService::isComponent).aqlSelf());
```

**Targeting a variable instead of self**

```java
String exprOnVar = ServiceMethod.of1(MySvc::formatName)
    .aql("elt", "'; '");
```

**Static service method**

```java
String expr = ServiceMethod.ofStatic1(MyStaticSvc::upper)
    .aqlSelf("name");
```

### When the compiler complains: add a type witness

If you see an error like:

> The type X does not define methodName(Object, Object, ...)

the compiler could not infer the exact generic types for your unbound reference.
Add a **type witness** to `ofN(...)` so it matches your real signature.

Examples:

```java
// setNewValue(Element, String, Object)
ServiceMethod.<DetailsViewService, Element, String, Object>of2(DetailsViewService::setNewValue)

// setElementDescription(Usage, String)
ServiceMethod.<LAMutationService, Usage, String>of1(LAMutationService::setElementDescription)

// predicate isComponent(Element)
ServiceMethod.<LAQueryService, Element>of0(LAQueryService::isComponent)
```

Alternative if you prefer casts:

```java
ServiceMethod.of2(
  (ServiceMethod.Inst2<DetailsViewService, Element, String, Object>)
  DetailsViewService::setNewValue
)
```

### Mapping to `AQLUtils` APIs

`ServiceMethod` mirrors the common `AQLUtils` shapes:

- `.aqlSelf()` → `getSelfServiceCallExpression(serviceName)`
- `.aqlSelf(p1, ..., pn)` → `getSelfServiceCallExpression(serviceName, List.of(...))`
- `.aql(var)` → `getServiceCallExpression(var, serviceName)`
- `.aql(var, p1, ..., pn)` → `getServiceCallExpression(var, serviceName, List.of(...))`

### Performance and constraints

- Overhead is negligible at startup. The helper only reflects the lambda once to read the method name.
- Works for unbound instance and static methods.
- If you later persist the produced AQL strings into models, note that persisted strings will not be automatically refactored. You still gain safety in all Java call sites that build those strings.
