# AKIDS-A3-SPEC-VALIDATION-001 v0.1.0 — Official-photo provenance validation

- Stage: A3 implementation
- Status: IMPLEMENTED IN SOURCE; tests authored but NOT RUN
- Runtime: local deterministic validation; no network access

## Change

The official-photo specification validator now fails closed unless an authority-verified record has:
- a nonblank issuing authority;
- a syntactically valid HTTPS URL with a host and no embedded user information;
- a valid ISO-8601 calendar effective date;
- a valid ISO-8601 calendar verification date that is not in the future;
- an effective date that is not in the future;
- valid bounded photo dimensions, head-height and eye-line values.

The validator accepts an explicit `asOf` date for deterministic testing. User-defined templates remain valid when their geometry is valid, but they can never enable an official-compliance claim.

The 300-DPI millimetre-to-pixel helper retains bounded input validation.

## Tests authored

`OfficialPhotoSpecEngineTest.kt` covers:
- current authority-backed specification;
- user-defined template separation;
- rejection of HTTP sources;
- malformed dates;
- future effective dates;
- future verification dates;
- 35 × 45 mm conversion at 300 DPI;
- invalid dimension rejection.

## Verification boundary

The JUnit tests have been added but have not been executed. Gradle dependency resolution, Gradle build, unit test execution, Android runtime tests and A4 verification remain NOT RUN. This validator does not independently verify whether a cited authority page is genuine, current, or legally authoritative; source review and evidence capture remain required before adding any real jurisdiction profile.
