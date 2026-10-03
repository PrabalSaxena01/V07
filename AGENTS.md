# AGENTS.md

# Project Identity

This repository contains an existing application that has evolved organically over multiple iterations.

The existing implementation contains useful functionality and accumulated knowledge, but its architecture, UX, and implementation patterns should NOT be treated as the final design.

We are now developing **V3**.

V3 is a deliberate reconstruction of the product.

The objective is to create a coherent, maintainable, extensible application with:

* a clean architecture
* a first-class plugin platform
* excellent UI/UX
* clear separation of concerns
* documented architectural decisions
* predictable APIs
* testable components
* a future-ready plugin distribution model

---

# 1. V3 IS A SEPARATE IMPLEMENTATION

V3 must be developed in a clearly separated location.

Recommended structure:

```text
project/
│
├── v1/ or legacy/
├── v2/
│
├── v3/
│   ├── ...
│   └── ...
│
├── docs/
│   ├── v3/
│   └── ...
│
└── AGENTS.md
```

If the repository already has a suitable structure, adapt to it without moving or deleting the existing implementations.

## Critical rule

**Do not modify V1/V2 as part of V3 development unless explicitly instructed.**

V1/V2 are reference implementations.

They may be inspected to understand:

* existing functionality
* business rules
* data models
* APIs
* user workflows
* edge cases
* integrations
* historical decisions

But they must not dictate V3 architecture or UX.

---

# 2. V3 IS NOT A REFACTOR

Do not think of V3 as:

```text
V2
 ↓
clean up
 ↓
V3
```

Think of it as:

```text
Existing Product
      │
      ├── useful functionality
      ├── business knowledge
      ├── data
      ├── APIs
      └── lessons from mistakes
              ↓
        V3 Requirements
              ↓
        New Architecture
              ↓
        New UX/UI
              ↓
        V3 Implementation
```

The existing application is a source of knowledge, not an architectural specification.

---

# 3. CORE V3 PRINCIPLES

V3 must prioritize:

1. Correctness
2. User experience
3. Maintainability
4. Extensibility
5. Security
6. Performance
7. Simplicity

Do not optimize for preserving existing implementation patterns.

Do not introduce complexity merely because it is considered "best practice".

Use the simplest architecture that satisfies the actual requirements.

---

# 4. PRODUCT-FIRST THINKING

Before implementing a feature, understand:

* what user problem it solves
* who uses it
* what the user's goal is
* what the primary workflow is
* what the failure cases are
* what information the user needs
* what actions the user should be able to take

Do not reproduce poor UX simply because V1/V2 already implemented it.

If an existing workflow is unnecessarily complicated, redesign it.

---

# 5. V3 UI/UX IS A CLEAN SLATE

The existing UI is NOT the visual specification for V3.

You are explicitly allowed and encouraged to redesign:

* navigation
* information architecture
* layouts
* interaction patterns
* dashboards
* forms
* settings
* onboarding
* empty states
* loading states
* error states
* animations
* responsive behavior
* component hierarchy
* typography
* spacing
* color system
* iconography
* accessibility

The objective is to produce the **best practical UI/UX for the product**, not the most faithful reproduction of the existing interface.

---

# 6. UX DESIGN PROCESS

Before implementing a significant user-facing feature, consider:

### User Goal

What is the user trying to accomplish?

### Primary Action

What is the most important action?

### Information Hierarchy

What should the user notice first, second, and third?

### Interaction Cost

How many unnecessary taps/clicks/steps exist?

### Feedback

How does the user know that an action succeeded?

### Failure

What happens when something goes wrong?

### Empty State

What does a new user see?

### Loading State

What does the user see while waiting?

### Error State

What can the user do after an error?

### Accessibility

Can the feature be used by users with different abilities and input methods?

---

# 7. DO NOT COPY EXISTING UX BLINDLY

If V2 contains:

```text
Feature A → screen → submenu → dialog → action
```

and V3 can accomplish the same goal more naturally with:

```text
Feature A → action
```

prefer the V3 approach.

Existing workflows should be treated as evidence about functionality, not proof that the workflow is good.

---

# 8. DESIGN SYSTEM

V3 should establish a coherent design system.

Define and consistently use:

* typography
* spacing
* colors
* surfaces
* borders
* shadows
* radii
* icons
* buttons
* inputs
* cards
* dialogs
* navigation
* feedback states
* responsive breakpoints
* motion/animation rules

Do not create one-off UI patterns when an existing design-system component can be used.

If the application already has a design system, evaluate it rather than automatically preserving it.

---

# 9. UI CONSISTENCY

A user should feel that the entire application was designed by one system.

Avoid:

* different button styles for different features
* inconsistent spacing
* different form behavior
* arbitrary colors
* inconsistent terminology
* inconsistent navigation
* different loading patterns
* different error presentation
* duplicated UI components with slightly different behavior

When a new UI pattern is introduced, determine whether it should become part of the shared design system.

---

# 10. UI/UX DOCUMENTATION

Create:

```text
docs/v3/
├── UX_PRINCIPLES.md
├── INFORMATION_ARCHITECTURE.md
├── DESIGN_SYSTEM.md
└── USER_FLOWS.md
```

Document important UX decisions.

For significant UX decisions, record:

```text
Problem
Users affected
Options considered
Chosen approach
Why
Trade-offs
```

---

# 11. ARCHITECTURE AUDIT

Before writing V3 implementation code, inspect the existing project.

Do not modify V1/V2 during the audit.

Create:

`docs/v3/ARCHITECTURE_AUDIT.md`

Analyze:

* repository structure
* application architecture
* modules
* dependencies
* data flow
* APIs
* database
* authentication
* authorization
* state management
* external services
* configuration
* logging
* error handling
* testing
* background jobs
* caching
* integrations
* plugins
* UI architecture
* technical debt

Also identify:

* duplicated logic
* dead code
* circular dependencies
* tightly coupled components
* accidental abstractions
* hidden dependencies
* undocumented assumptions
* architectural shortcuts

Do not guess when something is unclear.

Record it under:

`Unknowns`

---

# 12. FUNCTIONAL INVENTORY

Create:

`docs/v3/FUNCTIONAL_INVENTORY.md`

List existing functionality.

For every feature record:

```text
Feature
Purpose
Current implementation
Dependencies
User workflow
Data involved
API involved
Known problems
V3 recommendation
```

Classify each feature as:

```text
KEEP
REDESIGN
REIMPLEMENT
DEPRECATE
UNCERTAIN
```

These classifications are recommendations for V3 architecture, not automatic instructions to delete functionality.

---

# 13. TARGET ARCHITECTURE

Create:

`docs/v3/TARGET_ARCHITECTURE.md`

Define:

* architectural style
* module boundaries
* dependency direction
* domain boundaries
* API boundaries
* data layer
* state management
* plugin system
* configuration
* authentication
* authorization
* error handling
* logging
* testing
* external integrations
* UI architecture

Every significant architectural decision must include reasoning.

---

# 14. V3 DIRECTORY STRUCTURE

Create:

`docs/v3/PROJECT_STRUCTURE.md`

Define the proposed V3 structure before implementation.

Example:

```text
v3/
├── app/
├── core/
├── plugins/
├── features/
├── infrastructure/
├── shared/
├── tests/
└── ...
```

Do NOT blindly copy this example.

Design the structure based on the actual technology stack and requirements.

For every major directory explain:

* responsibility
* allowed contents
* forbidden contents
* dependencies

---

# 15. PLUGIN PLATFORM

V3 is fundamentally a plugin platform.

The application must support:

1. Built-in plugins
2. Individually installed plugins
3. Plugin packs
4. Future store-distributed plugins

The built-in plugins are the **starter pack**.

They must use the same plugin architecture and contract as external plugins wherever technically possible.

---

# 16. CORE VS PLUGIN

The core platform owns:

* runtime
* plugin lifecycle
* plugin registry
* plugin contracts
* permissions
* dependency resolution
* configuration framework
* plugin APIs
* installation
* updates
* removal
* compatibility
* security boundaries

Plugins own:

* feature-specific behavior
* feature-specific UI
* feature-specific data
* plugin-specific configuration
* plugin-specific endpoints
* plugin-specific workflows

The core should NOT contain feature-specific implementation merely because that feature currently ships by default.

---

# 17. PLUGIN CONTRACT

Create:

`docs/v3/PLUGIN_SPECIFICATION.md`

Every plugin must have a machine-readable manifest containing appropriate information such as:

```text
plugin ID
name
version
API version
description
publisher
permissions
dependencies
capabilities
configuration
endpoints
UI extensions
compatibility
```

The exact manifest schema must be defined in the specification.

Do not allow individual plugins to invent their own incompatible conventions.

---

# 18. PLUGIN IDENTITY

Every plugin must have a stable unique ID.

The ID must not depend on:

* display name
* folder name
* version

Changing:

```text
"My Calendar"
```

to:

```text
"Calendar"
```

must not create a new plugin identity.

---

# 19. PLUGIN LIFECYCLE

Define a predictable lifecycle.

At minimum support the concepts of:

```text
DISCOVERED
VALIDATED
INSTALLED
ENABLED
RUNNING
DISABLED
UNINSTALLED
```

Installation and activation should be separate concepts.

---

# 20. PLUGIN DEPENDENCIES

Plugins must explicitly declare dependencies.

Examples:

```text
Plugin A
    ↓
requires Plugin B >= 2.0
```

Do not allow hidden dependencies.

The platform should detect:

* missing dependencies
* incompatible dependencies
* circular dependencies

before activation.

---

# 21. PLUGIN PERMISSIONS

Plugins must request explicit permissions.

Possible permissions include:

* storage
* network
* notifications
* user data
* filesystem
* external services
* background execution
* device capabilities

Do not automatically grant unrestricted access.

A plugin must function according to its granted permissions.

---

# 22. PLUGIN API

Create:

`docs/v3/PLUGIN_API.md`

Plugin-facing APIs must be explicit and documented.

Documentation should include:

* interface/endpoint
* purpose
* request
* response
* errors
* authentication
* authorization
* permissions
* version
* compatibility
* rate limits where applicable

Plugins must not rely on private core implementation details.

---

# 23. PLUGIN ENDPOINTS

Plugin endpoints must be namespaced.

A convention such as:

```text
/api/plugins/{plugin-id}/...
```

may be used, but the exact convention must be decided and documented.

Plugin endpoints must not collide with:

* core endpoints
* other plugin endpoints
* reserved system endpoints

---

# 24. PLUGIN EVENTS

If plugins need to communicate asynchronously, use a documented event mechanism.

Plugins must not directly manipulate another plugin's internal state.

Prefer:

```text
Plugin A
   ↓
Event / Public API
   ↓
Plugin B
```

over:

```text
Plugin A
   ↓
Plugin B internal implementation
```

---

# 25. PLUGIN STORAGE

Plugin data must have clear ownership.

Plugin A must not directly manipulate Plugin B's storage.

Prefer:

```text
Plugin A → Plugin A storage
Plugin B → Plugin B storage
```

Shared data must use an explicit platform contract.

Define what happens to plugin data when a plugin is:

* disabled
* updated
* uninstalled

---

# 26. PLUGIN CONFIGURATION

Plugin configuration must be isolated.

Plugins should declare:

* configuration keys
* types
* defaults
* validation
* required/optional values
* sensitive values

The core should not need to understand the internal configuration logic of every plugin.

---

# 27. PLUGIN UI EXTENSIONS

Plugins may contribute UI only through defined extension points.

Potential extension points include:

* navigation
* commands
* dashboard widgets
* settings
* contextual actions
* pages
* cards
* panels

Plugins should not arbitrarily manipulate unrelated core UI internals.

The extension system must be documented.

---

# 28. BUILT-IN PLUGINS

Built-in plugins are the starter pack.

They should not receive special architectural treatment simply because they ship with V3.

If a built-in plugin requires privileged access, those privileges must be:

1. explicit
2. documented
3. justified

The eventual goal is:

```text
             Plugin Contract
              /           \
             /             \
Built-in Plugins      Store Plugins
```

not:

```text
Built-in Plugins → special internal APIs

External Plugins → public APIs
```

unless a documented technical constraint requires this.

---

# 29. PLUGIN PACKS

Support the concept of plugin packs.

A pack contains multiple plugins.

Example:

```text
Productivity Pack
├── Tasks
├── Calendar
└── Notes
```

A pack should be a distribution/install concept, not a fundamentally different plugin architecture.

Installing a pack should resolve into its constituent plugins and dependencies.

---

# 30. FUTURE WEB STORE

Do not implement the web store unless explicitly requested.

However, design V3 so that a future store can distribute plugins without changing the plugin runtime.

The architecture should separate:

```text
Plugin Runtime
        ↓
Plugin Package
        ↓
Distribution Mechanism
```

A plugin may eventually come from:

* built-in application resources
* local installation
* plugin pack
* official store
* another supported registry

The runtime contract should remain the same.

---

# 31. STORE VS RUNTIME

Do not tightly couple the runtime to store-specific concepts.

Runtime manifest:

```text
What does this plugin need to run?
```

Store metadata:

```text
How should this plugin be distributed/presented?
```

Keep these concerns separate.

---

# 32. PLUGIN SECURITY

Treat plugins as potentially untrusted.

Consider:

* permission enforcement
* API authorization
* input validation
* secret isolation
* filesystem access
* network access
* resource usage
* package integrity
* publisher identity
* signatures
* update security

Do not give plugins unrestricted access simply because they are installed.

---

# 33. PLUGIN VERSIONING

Plugins must declare:

```text
plugin version
plugin API version
minimum platform version
maximum supported platform version where appropriate
dependencies
```

Breaking plugin API changes must be explicitly versioned and documented.

Do not silently break installed plugins.

---

# 34. PLUGIN TESTING

The plugin system should eventually allow an external developer to validate a plugin without understanding the entire V3 codebase.

Provide mechanisms for testing:

* manifest
* lifecycle
* compatibility
* dependencies
* permissions
* endpoints
* configuration
* storage
* migrations
* uninstall behavior

---

# 35. PLUGIN DEVELOPER EXPERIENCE

Eventually an external developer should be able to read:

```text
PLUGIN_SPECIFICATION.md
PLUGIN_API.md
PLUGIN_DEVELOPMENT.md
```

and understand how to create a plugin without reading V3's internal implementation.

Create:

```text
docs/v3/PLUGIN_DEVELOPMENT.md
```

when sufficient information exists.

---

# 36. ARCHITECTURAL DECISION RECORDS

Create:

`docs/v3/DECISIONS.md`

Every significant decision must be documented.

Use:

```markdown
# ADR-001: <Decision>

## Status
Accepted

## Context

## Decision

## Alternatives Considered

## Why

## Consequences

### Positive

### Negative

### Risks

## Date
YYYY-MM-DD
```

Important decisions include:

* architecture
* database
* API
* plugin contract
* plugin lifecycle
* permissions
* plugin storage
* plugin API versioning
* state management
* UI architecture
* design system
* major dependencies
* authentication
* authorization
* external services
* migration strategy

Do not create ADRs for trivial implementation details.

---

# 37. REQUIREMENT FOR DECISION TRANSPARENCY

Never silently make a major architectural decision.

If a decision materially affects:

* architecture
* plugin contract
* database
* API
* security
* authentication
* major dependencies
* UI architecture
* public interfaces

stop and present:

```text
DECISION REQUIRED

Problem:
...

Context:
...

Option A:
...

Option B:
...

Option C:
...

Trade-offs:
...

Recommended approach:
...

Decision:
WAITING FOR USER
```

For small implementation decisions, proceed autonomously.

---

# 38. MIGRATION PLAN

Create:

`docs/v3/MIGRATION_PLAN.md`

V3 should be implemented incrementally.

Preferred sequence:

```text
1. Audit existing application
2. Define V3 requirements
3. Define information architecture
4. Define UX principles
5. Define design system
6. Define target architecture
7. Define plugin contract
8. Define plugin API
9. Define core platform
10. Implement plugin runtime
11. Implement starter plugins
12. Implement primary product workflows
13. Implement secondary features
14. Test
15. Remove V3 technical debt
16. Final architecture review
```

Adjust this sequence based on actual dependencies.

---

# 39. NO BIG-BANG REWRITE

Do not attempt to reproduce the entire application in one giant implementation step.

Build V3 in coherent increments.

Each increment should ideally:

* compile
* run
* be testable
* have documented behavior
* have updated architecture documentation

---

# 40. TESTING

V3 should have tests around:

* core business logic
* plugin contract
* plugin lifecycle
* plugin permissions
* plugin dependencies
* plugin APIs
* critical user workflows
* persistence
* authentication
* authorization
* important edge cases

Do not optimize purely for coverage percentage.

Tests should protect important behavior.

---

# 41. PERFORMANCE

Do not prematurely optimize.

When performance matters:

1. identify the actual problem
2. understand the bottleneck
3. measure where possible
4. implement the simplest appropriate solution
5. document meaningful trade-offs

Do not introduce caching, queues, workers, or complex infrastructure without justification.

---

# 42. SECURITY

Explicitly consider:

* authentication
* authorization
* secrets
* input validation
* injection
* sensitive data
* plugin permissions
* package integrity
* API access
* logging
* external integrations

Never put secrets in:

* source code
* documentation
* test fixtures
* logs
* client-side code

---

# 43. DATABASE

Treat persistent data as a contract.

Before changing schemas:

1. understand current data
2. identify consumers
3. identify migration requirements
4. determine compatibility
5. define rollback strategy where practical

Never casually destroy data.

---

# 44. API CONTRACTS

Treat APIs as contracts.

Document:

* request
* response
* validation
* errors
* authentication
* authorization
* versioning

Avoid silent breaking changes.

---

# 45. ERROR HANDLING

Errors must be predictable.

Do not:

* silently swallow errors
* expose internal implementation details
* duplicate error handling unnecessarily
* return inconsistent error structures

Use consistent error boundaries.

---

# 46. DEPENDENCIES

Before adding a dependency:

1. check existing dependencies
2. determine whether one already solves the problem
3. evaluate complexity
4. evaluate maintenance
5. evaluate security
6. determine whether a local implementation is simpler

Significant dependency decisions must be documented.

---

# 47. CODE QUALITY

Prefer:

* cohesive modules
* clear responsibilities
* explicit dependencies
* predictable naming
* small focused components
* testable business logic
* minimal hidden state
* consistent patterns

Avoid:

* god classes
* god components
* utility dumping grounds
* circular dependencies
* duplicate business logic
* premature abstractions
* clever code
* unexplained magic values

---

# 48. COMMENTS

Comments should explain **why**, not obvious implementation details.

Prefer clear code over excessive comments.

Document:

* non-obvious constraints
* invariants
* external assumptions
* important trade-offs

---

# 49. UI IMPLEMENTATION QUALITY

Every important user-facing feature should account for:

* loading
* empty
* success
* error
* disabled
* offline/unavailable
* first-use
* destructive-action confirmation where appropriate
* responsive behavior
* keyboard/input accessibility where applicable

Do not consider a feature complete merely because its happy path works.

---

# 50. UX QUALITY BAR

Before considering a significant UI feature complete, evaluate:

### Clarity

Can the user understand what this screen does?

### Efficiency

Can the primary task be completed with minimal unnecessary interaction?

### Consistency

Does it follow the V3 design system?

### Feedback

Does the interface clearly communicate state changes?

### Recovery

Can the user recover from mistakes?

### Accessibility

Can the feature be used by a broad range of users?

### Visual hierarchy

Is the most important information visually dominant?

### Responsiveness

Does the interface work appropriately across supported screen sizes?

---

# 51. DESIGN EXPLORATION

For major screens, it is acceptable to explore multiple UI approaches before implementation.

Do not automatically reproduce the current UI.

Compare alternatives based on:

* user goals
* information hierarchy
* interaction cost
* consistency
* accessibility
* complexity
* responsiveness

Once a direction is chosen, document important decisions.

---

# 52. DO NOT OVER-ENGINEER

The fact that V3 is designed to support a plugin marketplace does NOT mean every part of the application must become abstract or configurable.

Use extensibility where extensibility is actually required.

Avoid:

```text
interface
    ↓
factory
    ↓
abstract factory
    ↓
registry
    ↓
provider
    ↓
adapter
```

when a simple implementation would work.

Architecture should serve the product.

---

# 53. LEGACY CODE

Legacy code may be:

* inspected
* analyzed
* tested
* reused conceptually
* migrated selectively

But it should not automatically be copied into V3.

When copying code from V1/V2, ask:

> "Would we design this this way if we were starting today?"

If not, redesign it.

---

# 54. FINAL REVIEW

Before declaring V3 mature, create:

`docs/v3/FINAL_ARCHITECTURE_REVIEW.md`

Compare:

```text
Original Product
       ↓
Problems Identified
       ↓
V3 Design
       ↓
Implementation
       ↓
Final Architecture
```

Verify:

* functionality
* UX
* architecture
* plugin contract
* plugin lifecycle
* API contracts
* security
* tests
* documentation
* performance
* accessibility
* maintainability

List remaining technical debt explicitly.

---

# 55. FIRST ACTION

When beginning V3 work:

**DO NOT MODIFY SOURCE CODE.**

First inspect the existing project.

Then produce:

1. Repository overview
2. Existing architecture
3. Functional inventory
4. Existing plugin analysis
5. Existing UI/UX analysis
6. Major technical debt
7. Important existing behaviors
8. Problems that should NOT be carried into V3
9. Proposed V3 architecture
10. Proposed V3 information architecture
11. Proposed V3 UX direction
12. Proposed plugin architecture
13. Proposed migration/build sequence
14. Unknowns requiring clarification

Create the relevant documents under:

```text
docs/v3/
```

Then STOP.

Wait for approval before beginning substantial V3 implementation.

---

# 56. GOLDEN RULE

The purpose of V3 is not:

> "Make the existing application cleaner."

The purpose of V3 is:

> **Build the product we should have built if we had understood the requirements, architecture, plugin ecosystem, and user experience from the beginning.**

Use the existing application as a source of knowledge.

Do not inherit its mistakes.

Build V3 deliberately.
