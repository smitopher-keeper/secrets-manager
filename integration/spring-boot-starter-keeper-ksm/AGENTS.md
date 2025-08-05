# Agent Instructions

- Use JDK 21 for all Gradle commands.
- After modifying any files in this directory, run `./gradlew test` from this directory and make sure it succeeds before committing.

### ✅ Prefer `switch` Over `else-if` Chains

When branching on known constant values (like `enum`s or `String`s), **prefer a `switch` expression or statement** over chained `else-if` blocks.

#### ✳️ Why:
- Easier to read and maintain
- Better compiler checks (exhaustiveness with enums)
- Cleaner logic flow

#### 🔁 Instead of this:
```java
if (agentType.equals("X")) {
    ...
} else if (agentType.equals("Y")) {
    ...
} else {
    ...
}
```

#### ✅ Do this:
```java
switch (agentType) {
    case "X" -> { ... }
    case "Y" -> { ... }
    default  -> { ... }
}
```

> For Java 17+, **enhanced switch expressions** are preferred.