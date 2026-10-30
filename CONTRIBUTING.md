# Contributing

Work only in `Pira-Android`; the TaakCompanion repository is research-only and must not be modified.

Use JDK 17 and run:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Never commit API keys, signing material, `local.properties`, build outputs, or device logs. Keep new physical capabilities typed and allowlisted. Model output is untrusted and cannot authorize itself.
