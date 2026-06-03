# Java 21 Migration Notes — LifeGuard Insurance Platform

## Overview

Migrated from **Java 17 / Spring Boot 3.2.0** to **Java 21 / Spring Boot 3.3.0**.

---

## Java 21 Features Adopted

### 1. Records (JEP 395 — stable since Java 16)
`PolicyRequest`, `PolicyResponse`, and `ApiError` are now immutable records.

```java
// Before (Lombok @Data + setter-heavy entity)
Policy policy = new Policy();
policy.setHolderName("Jane");
// ...

// After — immutable DTO
public record PolicyRequest(
    @NotBlank String holderName,
    @Email    String holderEmail,
    ...
) {}
```

Records automatically provide:
- Canonical constructor, `equals()`, `hashCode()`, `toString()`
- Compact constructors for validation logic (see `ApiError`)

---

### 2. Sealed Interfaces + Pattern Matching Switch (JEP 441 — stable in Java 21)

`Policy.PolicyKind` is a sealed interface with `record` permits:

```java
public sealed interface PolicyKind
    permits PolicyKind.Term, PolicyKind.WholeLife, PolicyKind.Endowment {
    record Term(int termYears)              implements PolicyKind {}
    record WholeLife()                      implements PolicyKind {}
    record Endowment(LocalDate maturityDate) implements PolicyKind {}
}

// Exhaustive switch — compiler enforces all cases are handled
return switch (kind) {
    case PolicyKind.Term(int years)         -> "Term policy covering %d years".formatted(years);
    case PolicyKind.WholeLife()             -> "Whole-life policy with lifelong coverage";
    case PolicyKind.Endowment(var maturity) -> "Endowment policy maturing on %s".formatted(maturity);
};
```

---

### 3. Virtual Threads / Project Loom (JEP 444 — stable in Java 21)

`VirtualThreadConfig` registers `Executors.newVirtualThreadPerTaskExecutor()`.

`application.yml` sets:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```
This makes Spring Boot 3.2+ run **all Tomcat request threads as virtual threads** automatically — no pool sizing needed.

**Impact**: removes the `server.tomcat.threads.max` bottleneck for I/O-bound workloads (DB queries, external HTTP calls).

---

### 4. Text Blocks (JEP 378 — stable since Java 15)

JPQL queries and `toString()` methods use text blocks for readability:

```java
@Query("""
        SELECT p FROM Policy p
        WHERE p.status = :status
        ORDER BY p.startDate DESC
        """)
```

---

### 5. Switch Expressions (JEP 361 — stable since Java 14)

Used in `PolicyService.changeStatus()` for exhaustive enum handling:

```java
String transition = switch (newStatus) {
    case ACTIVE  -> "Activating";
    case LAPSED  -> "Lapsing";
    case CLAIMED -> "Processing claim for";
    case EXPIRED -> "Expiring";
};
```

---

### 6. Stream.toList() (Java 16+)

Replaced `Collectors.toUnmodifiableList()` with the cleaner `.toList()`:

```java
return policyRepository.findAll().stream()
    .map(PolicyResponse::from)
    .toList();
```

---

### 7. Generational ZGC (Java 21)

Dockerfile updated to use `-XX:+UseZGC -XX:+ZGenerational` — the new generational mode of ZGC (stable in Java 21) for lower GC pause times.

---

## Breaking Changes / Migration Actions

| Area | Before | After |
|------|--------|-------|
| Lombok | `@Data`, `@RequiredArgsConstructor`, `@Slf4j` on all classes | Removed — records + standard constructors + `LoggerFactory` |
| API input type | `Policy` entity directly | `PolicyRequest` record (immutable DTO) |
| API output type | `Policy` entity directly | `PolicyResponse` record (no JPA leakage) |
| Error responses | None / raw exceptions | `ApiError` record via `GlobalExceptionHandler` |
| Spring Boot | 3.2.0 | 3.3.0 |
| Docker base image | `eclipse-temurin:17-jre-alpine` | `eclipse-temurin:21-jre-alpine` |
| Maven build image | `maven:3.9.4-eclipse-temurin-17` | `maven:3.9.6-eclipse-temurin-21` |
| Kubernetes memory limit | 1Gi | 768Mi (virtual threads use far less stack memory) |

---

## Running Locally

```bash
# Requires Java 21+
java -version   # openjdk 21...

mvn clean package --enable-preview
java --enable-preview -jar target/lifeguard-insurance-1.0.0.jar
```

Or via Docker Compose (unchanged):
```bash
docker compose up --build
```
