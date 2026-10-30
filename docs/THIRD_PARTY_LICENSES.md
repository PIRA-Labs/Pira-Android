# Third-party license audit

This is a release-audit record, not a relicensing notice.

| Component | Location | Status |
|---|---|---|
| AndroidX AppCompat/Core KTX and transitive libraries | Gradle dependencies | Upstream licenses apply (primarily Apache-2.0); retain notices when redistributing. |
| Xiaomi/XMS wearable library | `app/libs/xms-wearable-lib_1.4_release.aar` | License and redistribution terms are not present in this repository. Confirm them with the source/vendor before a public release. |
| Pirophone/isotipo brand assets | `app/src/main/res/drawable*` | PIRA-provided brand assets; confirm ownership/permission for public redistribution. |
| Gradle, Kotlin, Android SDK | Build tooling only | Tool licenses are not redistributed as application source. |

No third-party source has been relicensed under the repository MIT license. The XMS AAR and brand-asset provenance are release blockers until documented.
