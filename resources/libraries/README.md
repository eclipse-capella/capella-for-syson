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
# Arcadia SysML v2 Library

This library provides a reusable representation of the **Arcadia methodology** using **SysML v2**. It captures core Arcadia concepts such as components, functions, exchanges, and requirements in a way that aligns with the SysML v2 metamodel and reuse mechanisms.

## Objectives

- Provide a clean, reusable foundation for Arcadia-based modeling within SysML v2 tools
- Ensure consistency across engineering perspectives (Operational, System, Logical, Physical, EPBS)
- Facilitate allocation, traceability, and refinement using standard SysML v2 mechanisms
- Avoid redundancy by leveraging SysML v2 built-in capabilities such as actions, ports, and metadata

## Key Concepts & Mappings

### `ArcadiaElement`

All domain elements (functions, components, exchanges, requirements) extend this base structure to share common metadata:

- `description` : Free-text documentation
- `status` : Tracked via `@StatusInfo` annotation
- Title is implicitly handled via object `name` (no explicit `title` attribute)
- We do **not** include a `summary` attribute : it's rarely used in practice

### `ArcadiaPackage` metadata

Used to annotate any `SysML::Package` with the relevant Arcadia **engineering perspective**:

```sysml
metadata def ArcadiaPackage {
    :> annotatedElement : SysML::Package;
    enum engineeringPerspective : ArcadiaEngineeringPerspective;
}
```

### `ArcadiaEngineeringPerspective`

A semantic classification of Arcadia views, used to annotate packages:

- `Operational Analysis`
- `System Analysis`
- `Logical Architecture`
- `Physical Architecture`
- `EPBS Physical Architecture`
- `Generic`
- `System Engineering`

Engineering perspectives are modeled as metadata instead of separate package structures : this enables tools to flexibly present viewpoints without rigid structure imposed by the library.

## Core Elements

### `Component`

A reusable component structure across perspectives:

- Attributes:
  - `isActor` (Boolean)
  - `isHuman` (Boolean)
- Derived relationships:
  - `allocatedFunctions` → derived from `performedActions`
  - `subComponents` → derived from `subparts`

Component is not duplicated per perspective. Instead, all components use the same definition, and their context is inferred from packaging or metadata.
Allocation to Function is reused via standard performedActions rather than creating a custom mapping.

### `Function`

Defined as an `action` representing logical/physical/system functions:

- `subFunctions` → derived from `subactions`
- Port-based interactions modeled via ExchangeItems, not explicit parameters

Functions rely on the native Action type in SysML v2, including decomposition via subactions and allocations via performedActions.

No explicit in/out parameters : modeling is based on flows (FunctionalExchange) and ports.

### `FunctionalExchange`

Defined as a `flow`, it models behavior between two `Function` elements:

- Endpoints:
  - `source` → `Function`
  - `target` → `Function`
- Carries one or more `ExchangeItem`s via `payload`

Exchanges use the native flow mechanism in SysML v2 and can carry multiple ExchangeItems as payloads.

### `FunctionalChain`

- Reference to involved `FunctionalExchange`s
- Derived `involvedFunctions` from source and target of exchanges

Functional chains abstract higher-level scenarios or interactions, but rely on reusable exchanges and functions.

## Component Interfaces

### `ComponentPort`

- Specialization of a `port`, with:
  - `direction` (based on `FeatureDirectionKind`)
  - `allocatedFunctionPorts` → `Feature[*]`

Ports follow SysML v2 semantics : we do not define new directions or port types.
FunctionPort-to-ComponentPort allocation is modeled via reference (allocatedFunctionPorts), avoiding new allocation constructs.

### `ComponentExchange`

Connects two `ComponentPort`s:

- `cp1`, `cp2` as endpoints
- `allocatedExchangeItems` and `allocatedFunctionalExchanges` as traceability links

ComponentExchange is modeled as a standard SysML v2 interface with traceability links, not as a flow.

## Allocation Mechanisms

- `ExchangeAllocation` : maps a `FunctionalExchange` (as `MessageAction`) to a `ComponentExchange`
- Function-to-component allocation is **implicit** via SysML v2’s `performedActions`
- FunctionPort-to-ComponentPort links are maintained through `allocatedFunctionPorts`

Allocation links reuse existing SysML v2 relationships wherever possible. New allocation types are defined only where no native link exists (e.g., ExchangeAllocation).

## Requirements

### Native SysML v2 RequirementUsage (Recommended)

We recommend using **native SysML v2 `RequirementUsage`** directly instead of `ArcadiaRequirement`.

**Rationale:**
- `ArcadiaRequirement` adds no attributes or constraints beyond what native `RequirementUsage` provides
- Native `RequirementUsage` already includes all necessary capabilities:
  - `reqId` : Requirement identifier (e.g., "R1.1")
  - `text` : The "shall" statement
  - `documentation.body` : Additional notes/comments
- Better interoperability with standard SysML v2 tooling
- Simplifies the model by removing unnecessary typing indirection

### `ArcadiaRequirement` (Deprecated)

Retained for backwards compatibility only. Extends `ArcadiaElement` but adds no additional attributes:

```sysml
requirement def ArcadiaRequirement :> ArcadiaElement {
    private doc /* A generic Requirement... */
}
```

- No separate ID field : use native `reqId` instead
- Supports all standard SysML v2 requirement relationships
