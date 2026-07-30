                Client
                   │
                   ▼
      TransactionController
                   │
                   ▼
       TransactionService
                   │
                   ▼
   TransactionRepository
                   │
                   ▼
        PostgreSQL Database


Client
    │
    ▼
Spring Boot API
    │
    ▼
Validation
    │
    ▼
Fraud Engine
    │
    ├────────► Redis
    │
    ├────────► Kafka
    │
    ├────────► ML Service
    │
    ▼
PostgreSQL
    │
    ▼
React Dashboard