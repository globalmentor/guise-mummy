# Flange Domain Verification

Replace the blanket "not supported" warning in `FlangeWebSite.prepare()` with domain verification that compares the Guise project's configured domains against the resolved Flange environment's domain exports, warning on mismatches.

## Overview

Single-step change — inline domain verification in `FlangeWebSite.prepare()`, no new methods or tests.

- **Step 1:** Replace current alt-domain warning in `prepare()` with inline domain verification

No unit tests: the logic is advisory (warnings only), linear, and not independently testable without log-capture infrastructure — which is not warranted for advisory checks.

## Background

### Guise project domain configuration (all optional)

| Guise config key    | Accessor                                        | Return type                       | Semantics                                                          |
| ------------------- | ----------------------------------------------- | --------------------------------- | ------------------------------------------------------------------ |
| `domain`            | `GuiseMummy.findConfiguredDomain()`              | `Optional<DomainName>`            | Project base domain (e.g. `example.com.`)                          |
| `site.domain`       | `GuiseMummy.findConfiguredSiteDomain()`           | `Optional<DomainName>`            | Canonical web domain; defaults to `domain` if not explicitly set   |
| `site.altDomains`   | `GuiseMummy.findConfiguredSiteAltDomains()`       | `Optional<Collection<DomainName>>`| Alt domains that redirect to the canonical web domain              |

All return absolute `DomainName` values (trailing dot) when present.

In Guise, the dependencies are soft: `site.domain` and `site.altDomains` use `domain` as a resolution base for relative values, but absolute values can exist independently.

### Flange environment domain exports

| Export constant      | Example raw value     | Semantics                                           |
| -------------------- | --------------------- | --------------------------------------------------- |
| `DOMAIN_NAME`        | `example.com`         | Base domain (no trailing dot)                       |
| `WEB_DOMAIN_NAME`    | `www.example.com`     | Canonical web domain (no trailing dot)              |
| `ALT_WEB_DOMAIN_NAME`| `example.com`         | Alt web domain that redirects (no trailing dot)     |

Retrieved via `flangeEnv.findOutput(…)` → `Optional<String>`. Each is optional — absent if the environment was provisioned without domain configuration. In Flange, the dependencies are strict: `altWebDomain` requires `webDomain` requires `domain`.

### Domain comparison

Flange exports domain names without a trailing dot (relative form). Guise stores them in absolute form (FQDN with trailing dot). To compare, resolve the Flange export value to absolute form via `ROOT.resolve(DomainName.of(rawExport))`, following the same pattern used in `EnvSpec` (see `flange/cli/src/main/java/dev/flange/cli/EnvSpec.java` ~line 350).

Both Guise `find…()` accessors and `flangeEnv.findOutput(…).map(…).map(ROOT::resolve)` produce `Optional<DomainName>`. Comparing two optionals via `Optional.equals()` handles all cases naturally: both empty (no-op), one empty (mismatch), both present with same/different value.

`DomainName.equals()` is a case-sensitive string comparison. DNS is case-insensitive per RFC 1035, but in practice both Guise config values and Flange CloudFormation exports are consistently lowercase, so this is acceptable.

## Step 1: Inline domain verification in `prepare()`

### File target

- `mummy/src/main/java/dev/guise/mummy/deploy/flange/FlangeWebSite.java` ~line 309–311

### Change

Replace the current block:

```java
if(context.getConfiguration().findCollection(CONFIG_KEY_SITE_ALT_DOMAINS, String.class).isPresent()) {
    getLogger().atWarn().log("Alternative domain redirects (`{}`) are not supported with Flange deployment; ignoring.", CONFIG_KEY_SITE_ALT_DOMAINS);
}
```

with inline verification of all three domain pairs. For `domain` and `site.domain`, the pattern is identical — retrieve the Guise optional, and if present, compare against the corresponding Flange optional:

```java
final var configuration = context.getConfiguration();
final var foundGuiseDomain = findConfiguredDomain(configuration);
if(foundGuiseDomain.isPresent()) {
    final var foundFlangeDomain = flangeEnv.findOutput(DOMAIN_NAME).map(DomainName::of).map(ROOT::resolve);
    if(!foundFlangeDomain.equals(foundGuiseDomain)) {
        getLogger().atWarn().log("Project `{}` `{}` does not match Flange environment domain `{}`.",
                CONFIG_KEY_DOMAIN, foundGuiseDomain.orElseThrow(), foundFlangeDomain.orElse(null));
    }
}
final var foundGuiseSiteDomain = findConfiguredSiteDomain(configuration);
if(foundGuiseSiteDomain.isPresent()) {
    final var foundFlangeWebDomain = flangeEnv.findOutput(WEB_DOMAIN_NAME).map(DomainName::of).map(ROOT::resolve);
    if(!foundFlangeWebDomain.equals(foundGuiseSiteDomain)) {
        getLogger().atWarn().log("Project `{}` `{}` does not match Flange environment web domain `{}`.",
                CONFIG_KEY_SITE_DOMAIN, foundGuiseSiteDomain.orElseThrow(), foundFlangeWebDomain.orElse(null));
    }
}
findConfiguredSiteAltDomains(configuration).ifPresent(guiseAltDomains -> {
    final var foundFlangeAltWebDomain = flangeEnv.findOutput(ALT_WEB_DOMAIN_NAME).map(DomainName::of).map(ROOT::resolve);
    if(!foundFlangeAltWebDomain.filter(guiseAltDomains::contains).isPresent()) {
        getLogger().atWarn().log("Flange environment alt web domain `{}` is not among project `{}` {}.",
                foundFlangeAltWebDomain.orElse(null), CONFIG_KEY_SITE_ALT_DOMAINS, guiseAltDomains);
    }
    if(guiseAltDomains.size() > 1) {
        getLogger().atWarn().log("Project configures {} `{}` entries, but Flange supports only one alt web domain.",
                guiseAltDomains.size(), CONFIG_KEY_SITE_ALT_DOMAINS);
    }
});
```

The Flange optional is computed inside the `if`/`ifPresent` block — no work is done unless the Guise project specifies a value.

### Import additions

- `import static com.globalmentor.net.DomainName.*;` — for `ROOT`

Note: `DomainName` itself is already imported via `com.globalmentor.net.*`. All `GuiseMummy` accessors and config keys are available via the existing `import static dev.guise.mummy.GuiseMummy.*`. Flange export constants are available via the existing `import static dev.flange.platform.aws.FlangePlatformAws.Templates.Exports.*`.

## Rejected alternatives

### Extracted static helper method

The original plan extracted a `validateDomains(Configuration, AwsFlangeEnvironment)` static method to enable isolated unit testing. Rejected because:

- The method is advisory-only (logging, no return value, no exceptions). Unit tests would need log-capture infrastructure to verify behavior — effort disproportionate to the risk.
- A static method can't use `Clogged.getLogger()`, requiring either `Clogr.getLogger(FlangeWebSite.class)` (viable but a stylistic departure from the rest of the class) or passing a `Logger` parameter (unnecessary coupling).
- The total logic is ~20 lines — small enough to inline without harming readability.

### Hard failure on mismatch

Making mismatches a `ConfiguredStateException`. Rejected because the Guise project configuration is informational — it doesn't control the Flange infrastructure. A mismatch indicates the user's expectations may not be met, but deployment can still proceed.
