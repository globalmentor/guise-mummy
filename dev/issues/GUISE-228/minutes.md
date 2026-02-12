# Minutes

<!-- Categories: Pivot | Insight | Decision | Finding | Lesson | Milestone | Open | Resolved -->

- 2026-02-11 **Finding**: `guise-skeleton.min.css` is a vendored Guise Skeleton dependency artifact, not a Guise Mummy source file. Editing dependency artifacts is inappropriate for this ticket — the CSS banner URL should remain at `guise.io` until Guise Skeleton migrates and Guise Mummy consumes updated artifacts. Of the six domain URL changes, five were legitimate (Java constants, templates, test resources); only the CSS file was a dependency that should not have been modified.
