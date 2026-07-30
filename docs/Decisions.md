# Design Decisions

UUID

Reason

Distributed systems generate IDs independently.

------------------------------------------------

BigDecimal

Reason

Avoid floating point precision errors.

------------------------------------------------

Layered Architecture

Reason

Separation of concerns.

------------------------------------------------

DTO

Reason

Prevent exposing database entities.

------------------------------------------------

Constructor Injection

Reason

Immutable dependencies.

------------------------------------------------

ResponseEntity

Reason

Control HTTP status and headers.

------------------------------------------------

Validation

Reason

Reject invalid requests before business logic.