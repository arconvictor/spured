# SPURED — Audit-Compliant Historical Data Service

A [Quarkus](https://quarkus.io/) REST service for managing corporate-client ("Grosskunde") records that can **never be deleted or silently overwritten**, only versioned — built to satisfy a legal requirement (common for insurers and other regulated businesses) that customer data must remain fully auditable: every change has to be traceable to who made it, when, and what the record looked like before and after.

> 🧪 This is a portfolio version of a service I originally built at an insurance company, reworked here to stand on its own. It depends on an internal, closed-source history-tracking library (see [Dependencies](#-dependencies) below) that isn't publicly available, so **this repository is for code review, not for running out of the box** — cloning it and running `quarkus:dev` will fail to resolve that dependency unless you have access to it.

---

## 🧠 The core idea: current state + full history, never a destructive write

Every Grosskunde has two representations in the database:

- **`S_GKD`** — the *current* row, mapped by the `SGrosskunde` entity. This is what a normal query returns.
- **`H_GKD`** — an *append-only* audit table, mapped by `HGrosskunde`. Every time a Grosskunde is created or updated, a new row is added here representing that time slice — nothing is ever updated or deleted from it.

So an "update" in this system is never a `... SET` on the current row: it's the creation of a new time slice with its own validity period, while the record's full history stays queryable and intact. That's what makes the data audit-compliant: a regulator (or an internal auditor) can always reconstruct exactly what a customer's record looked like at any point in the past, and who changed it.

The `@AuditInfo`, `@HistoryType` and `@TupleConverter` annotations on `SGrosskunde` are what wire an entity into this behavior — they come from the internal history-tracking library, not from Quarkus/Hibernate itself.

---

## 🏗️ Architecture

```
GrosskundeResource  (REST, /v2/clients)
        │  implements GrosskundeApi
        ▼
GrosskundeService    ← business logic: create / update / terminate / query
        │
        ├── SGrosskundeMapper       (Input → new entity, or merge into a new time slice)
        ├── SGrosskundeConverter    (raw DB tuple → HGrosskunde historical record)
        └── GrosskundeRepository    ← current-state queries (JPQL) + historical queries (HistoryRepository)
```

- **`resource`** — the REST layer: request/response handling only, no business logic.
- **`dto`** — `GrosskundeDto` (what the API accepts/returns) and `GrosskundeInput` (validated internal model); `GrosskundeService` orchestrates the actual use cases.
- **`entity`** — `SGrosskunde` (current state) and `HGrosskunde` (a historical time slice), plus the mapper/converter between them.
- **`control`** — `GrosskundeRepository`, the only class that talks to the database directly.

---

## 🚀 API

All endpoints are under `/v2/clients`.

| Method | Path | Description |
|---|---|---|
| `GET` | `/v2/clients` | List all currently active Grosskunden |
| `GET` | `/v2/clients/{id}` | Get a Grosskunde's current data |
| `GET` | `/v2/clients/{id}?history=true` | Get the full historical timeline for a Grosskunde |
| `POST` | `/v2/clients` | Create a new Grosskunde |
| `PATCH` | `/v2/clients/{id}` | Update a Grosskunde (creates a new time slice; the old one is closed, never deleted) |

---

## 🛠️ Tech stack

- Java 21
- Quarkus 3.16
- Hibernate ORM with Panache
- Oracle JDBC + Liquibase (schema migrations)
- REST Assured + `@QuarkusTest` / `@QuarkusIntegrationTest` for testing
- JaCoCo for coverage

## 📦 Dependencies

Besides the usual Quarkus/Hibernate/Oracle stack, this project depends on a proprietary internal library, `history-port-quarkus`, that implements the actual versioning mechanics (`HistoryAlgorithm`, `HistoryRepository`, `@AuditInfo`, `@HistoryType`, etc. — everything under the `de.hansemerkur.port.history` package). It was written by a colleague at the company this project came from, isn't published anywhere public, and isn't included in this repo — which is why this project won't build standalone. It's shared here to show the design and the REST/service/entity layers built on top of it, not as a runnable artifact.

---

## ▶️ Running (with access to the internal dependency)

```bash
./mvnw compile quarkus:dev
```

## 🧪 Testing

```bash
./mvnw test
```

Integration tests (`@QuarkusIntegrationTest`) build and run the packaged application:

```bash
./mvnw verify
```

Both `application-dev.properties` and `application-test.properties` mock the user/transaction-id providers (`mock.user.id`, `mock.transaction.id`) that the history framework needs, so tests don't require a real authenticated caller.

---

## ⚠️ Notes on this portfolio version

- There's an open `//FIXME` in `GrosskundeResource.createGrosskunde` questioning whether `@Transactional` is actually needed there — left as-is rather than guessed at, since resolving it requires knowing the original transaction boundaries.
