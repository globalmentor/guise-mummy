# Plan: Implement `--describe` Plan Description

**Ticket:** [GUISE-229]  
**Design:** [describe-flag.md](../designs/describe-flag.md)

## Context

The `MummyPlan` artifact tree, built during the PLAN lifecycle phase, contains all the data needed to report site composition — artifact counts, redirect inventory — but this data is only visible at TRACE log level via `GuiseMummy.printArtifactDescription()`. This plan implements the CLI surface and formatting logic to expose the plan as human-readable output.

## Scope

This plan covers the initial implementation: the `guise plan` subcommand with `--describe`/`--verbose`, and the `--describe-plan` flag on existing post-PLAN subcommands. The output format is the summary view (Option A from the design document). Tree view and sectioned report are deferred.

## Steps

### 1. Add `MummyExecution` enum to `GuiseMummy`

**File:** `mummy/src/main/java/dev/guise/mummy/GuiseMummy.java` (modify)

Add an enum representing optional executions that can be activated during mummification, analogous to Maven plugin executions bound to a phase:

```java
/// Optional executions that can be activated during mummification, analogous to Maven plugin executions bound to a phase.
public enum MummyExecution {
    /// Describe the site plan after the PLAN phase.
    DESCRIBE_PLAN
}
```

Change the `mummify()` signature to accept an execution set:

```java
public void mummify(@NonNull final GuiseProject project,
        @NonNull final LifeCyclePhase phase,
        @NonNull final Set<MummyExecution> executions) throws IOException
```

After `context.setPlan(plan)` in the PLAN phase, gate the describe behavior:

```java
if (executions.contains(MummyExecution.DESCRIBE_PLAN)) {
    new PlanDescriber(plan, context.getSiteSourceDirectory()).describeTo(System.out, isVerbose());
}
```

Output goes to `System.out` (not the logger) — this is user-requested information.

```java
```

The `isVerbose()` reference requires `GuiseMummy` to know the verbose flag. Add a `verbose` field parallel to `full`/`setFull()`:

```java
private boolean verbose = false;

public boolean isVerbose() { return verbose; }

public void setVerbose(final boolean verbose) { this.verbose = verbose; }
```

The CLI sets `mummifier.setVerbose(isVerbose())` before calling `mummify()`, the same way it already sets `mummifier.setFull(full)`. The `--verbose` / `-v` flag is provided by `BaseCliApplication` with `ScopeType.INHERIT` and is currently unused anywhere in Guise Mummy — this is a clean first use with no conflict.

### 2. Add `PlanDescriber` utility class

**File:** `mummy/src/main/java/dev/guise/mummy/PlanDescriber.java` (new)

Create a utility class in the `mummy` module (not `cli`) that takes a `MummyPlan` and a site source directory, and writes a human-readable description to an `Appendable`. This keeps the logic testable independently of the CLI and picocli.

The class walks the artifact tree in a single pass using local counters and a redirect list — no intermediate data record is needed. The `MummyPlan` *is* the data; an additional record would duplicate it without adding value.

Responsibilities:
- Walk the artifact tree from `plan.getRootArtifact()` recursively via `CollectionArtifact.getChildArtifacts()`.
- Classify each artifact: `CollectionArtifact` → collection, `PageMummifier` → page, `ImageMummifier` → image, else → other.
- Collect artifacts with `PROPERTY_TAG_MUMMY_ALT_LOCATION` into a redirect list, distinguishing collection vs. page redirects by whether the artifact's relative target path ends in `/`.
- Provide a `describeTo(Appendable, boolean verbose)` method that formats the output as specified in the design document.

**References:**
- [Artifact.java](../../mummy/src/main/java/dev/guise/mummy/Artifact.java) — `PROPERTY_TAG_MUMMY_ALT_LOCATION` constant
- [MummyPlan.java](../../mummy/src/main/java/dev/guise/mummy/MummyPlan.java) — `getRootArtifact()`

### 3. Add `plan` subcommand to `GuiseCli`

**File:** `cli/src/main/java/dev/guise/cli/GuiseCli.java` (modify)

Add a `plan` method following the existing subcommand pattern:

```java
@Command(description = "Plans a site by discovering and classifying artifacts.", mixinStandardHelpOptions = true)
public void plan(
    @Parameters(...) @Nullable Path argProjectDirectory,
    @Option(names = "--site-source-dir", ...) @Nullable Path argSiteSourceDirectory,
    @Option(names = "--site-target-dir", ...) @Nullable Path argSiteTargetDirectory,
    @Option(names = "--site-description-target-dir", ...) @Nullable Path argSiteDescriptionTargetDirectory,
    @Option(names = "--describe", description = "Prints a human-readable description of the site plan.", defaultValue = "true") final boolean describe)
    throws IOException { ... }
```

The method:
1. Creates `GuiseMummy` and `GuiseProject` as other subcommands do.
2. Passes `describe ? EnumSet.of(MummyExecution.DESCRIBE_PLAN) : EnumSet.noneOf(MummyExecution.class)` as the execution set.
3. Calls `mummifier.mummify(project, LifeCyclePhase.PLAN, executions)`.

The describe behavior happens inside `mummify()` at the PLAN phase, not in the CLI layer. `--verbose` is inherited from `BaseCliApplication` (declared with `ScopeType.INHERIT`) and does not need per-subcommand declaration.

Note: `--describe` defaults to `true` for the `plan` command, since describing the plan is the point of the command. The flag exists so it can be explicitly set to `--describe=false` if needed in the future, and for symmetry with `--describe-plan`.

### 4. Add `--describe-plan` flag to existing subcommands

**File:** `cli/src/main/java/dev/guise/cli/GuiseCli.java` (modify)

Add `--describe-plan` parameter to the `mummify`, `prepareDeploy`, and `deploy` methods:

```java
@Option(names = "--describe-plan", description = "Prints a human-readable description of the site plan.", defaultValue = "false") final boolean describePlan
```

The method passes `describePlan ? EnumSet.of(MummyExecution.DESCRIBE_PLAN) : EnumSet.noneOf(MummyExecution.class)` to `mummify()`. All describe logic remains inside `GuiseMummy`, not in `GuiseCli`.

The `validate` call site passes `EnumSet.noneOf(MummyExecution.class)` since there is nothing to describe before PLAN.

### 5. Add unit tests for `PlanDescriber`

**File:** `mummy/src/test/java/dev/guise/mummy/PlanDescriberTest.java` (new)

Test the classification and formatting logic:

- Construct a mock/stub artifact tree with known artifact types (pages, images, collections, opaque files).
- Add `altLocation` properties to some artifacts.
- Verify counts match expectations.
- Verify redirect extraction produces correct source → target mappings.
- Verify collection vs. page redirect classification.
- Verify default output format (no verbose).
- Verify verbose output includes redirect detail lines.

This tests the `PlanDescriber` as an internal API directly, per the testing philosophy.

### 6. Integration smoke test

Manually verify against `demo-basic` or `demo-hello-world`:

```
guise plan --describe
guise plan --describe --verbose
guise mummify --describe-plan
```

Confirm output matches the format specified in the design document.

## Implementation Order

Step 1 modifies `GuiseMummy`. Step 2 is independent. Steps 3 and 4 depend on 1 and 2. Step 5 can proceed in parallel with step 3. Step 6 is final verification.

Suggested sequence: **1 → 2 → 5 → 3 → 4 → 6**

## Alternatives Considered

- **Post-hoc `findPlan()` accessor on `GuiseMummy`**: Originally proposed adding a `findPlan()` method to retrieve the plan after `mummify()` completes, with the CLI performing the describe. Rejected: the describe behavior is phase-bound (fires after PLAN, before MUMMIFY), which is the engine's concern, not the CLI's.
- **`MummificationResult` return type on `mummify()`**: Originally proposed changing `mummify()` from `void` to return a result record (holding `deployUrls` and optionally the plan), to eliminate the mutable `deployUrls` field. Rejected as out of scope: the `deployUrls` pattern predates this ticket, works, and refactoring it adds risk without serving the describe feature.
- **Intermediate `PlanDescription` record**: Originally proposed a record holding extracted counts and redirect entries, separate from `MummyPlan`. Rejected: the plan *is* the data. The describer walks it and writes output in a single pass with local counters. An intermediate record would duplicate the plan's content without adding value. If a second consumer of computed statistics appears later, it can be extracted then.

## Resolved Questions

- **`--describe` default on `plan`**: `--describe` defaults to `true` for `guise plan`. This effectively makes `guise plan` the "info" command — the plan phase runs and the description prints as default behavior. The flag exists so it can be explicitly set to `--describe=false` if a future use case for silent planning emerges.
- **Output to stdout vs. stderr**: stdout. This is user-requested information, not logging or error output.
- **How `GuiseMummy` accesses `verbose`**: `setVerbose(boolean)` on `GuiseMummy`, parallel to `setFull()`. CLI calls `mummifier.setVerbose(isVerbose())` before `mummify()`. `--verbose` is currently unused in Guise Mummy (defined only in `BaseCliApplication`), so this is a clean first use with no conflict.
- **ANSI styling**: Deferred. Output is plain text for now. No known need for terminal styling in the initial implementation.

[GUISE-229]: ../../GUISE-229/
