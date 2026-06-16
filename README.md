# 🚀 Nivora Wallet – Fault-Tolerant Transaction System

A backend system designed to handle **safe and reliable wallet transfers** with strong guarantees against duplicate transactions, failures, and concurrency issues.

---

## 🧠 Problem Statement

In real-world payment systems, retries and network failures can lead to:

- ❌ Duplicate transactions
- ❌ Double debit issues
- ❌ Inconsistent state (money debited but not credited)

This project solves these problems by ensuring:

👉 **Exactly-once transaction execution**

---

## 🏗️ System Flow

```
┌─────────┐        ┌──────────────────┐        ┌─────────────────────┐
│  Client │──────▶ │   REST API Layer  │──────▶ │ Idempotency Check   │
└─────────┘        └──────────────────┘        └─────────────────────┘
                                                         │
                                          ┌──────────────┴──────────────┐
                                          │                             │
                                    (Duplicate?)                  (New Request)
                                          │                             │
                                   Return Cached               ┌────────▼────────┐
                                    Response                   │ Saga Orchestrator│
                                                               └────────┬────────┘
                                                                        │
                                                      ┌─────────────────┼─────────────────┐
                                                      │                 │                 │
                                               ┌──────▼─────┐  ┌───────▼──────┐  ┌───────▼──────┐
                                               │   DEBIT    │  │    CREDIT    │  │    STATUS    │
                                               │  (SELECT   │  │   receiver   │  │   UPDATE     │
                                               │ FOR UPDATE)│  │   wallet     │  │  → SUCCESS   │
                                               └──────┬─────┘  └───────┬──────┘  └───────┬──────┘
                                                      │                 │                 │
                                                   (fail?)           (fail?)           (fail?)
                                                      │                 │                 │
                                                      └─────────────────▼─────────────────┘
                                                               Compensating Transaction
                                                               → Mark as FAILED
                                                               → Rollback previous steps
```

---

## 🧩 Saga Workflow

| Step | Description | On Failure |
|------|-------------|------------|
| Debit | Deduct amount from sender wallet using `SELECT FOR UPDATE` | Abort, mark FAILED |
| Credit | Add amount to receiver wallet | Compensate: re-credit sender |
| Update | Mark transaction as SUCCESS | Compensate: reverse debit + credit |

---

## 🔄 Idempotency Handling

- Each request includes an **idempotency key** (client-generated UUID)
- On first request: execute transaction, store result against key
- On duplicate request: return cached response immediately — **no re-execution**
- Deduplication enforced at DB level with a unique constraint on idempotency key

---

## ⚙️ Key Features

- ✅ **Idempotent Transactions** — Prevents duplicate processing using idempotency keys
- 🔁 **Saga Pattern** — Consistency across multi-step transactions with compensating rollbacks
- 🔒 **Concurrency Control** — Race conditions eliminated via `SELECT FOR UPDATE` locking
- 💥 **Failure Handling** — System stays consistent even when intermediate steps fail
- 📦 **Database Sharding** — Wallet data partitioned by `user_id` hash using Apache ShardingSphere

---

## 🧠 Design Decisions

| Decision | Why |
|----------|-----|
| Saga over 2PC | Avoids distributed lock overhead; tolerates partial failures gracefully |
| Pessimistic locking (`SELECT FOR UPDATE`) | Guarantees zero race conditions on concurrent balance updates |
| ShardingSphere horizontal sharding | Distributes wallet data by `user_id` hash across N shards |
| Idempotency keys with DB deduplication | Exactly-once guarantee even on client retries |

---

## 🗄️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot, Spring Data JPA (Hibernate) |
| Database | MySQL |
| Sharding | Apache ShardingSphere |
| Utilities | Lombok |
| Containerization | Docker |

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- MySQL 8+
- Docker (optional)

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/Harshjha002/nivora-wallet.git
cd nivora-wallet

# 2. Configure your database
# Edit src/main/resources/application.yml
# Set your MySQL URL, username, and password

# 3. Build and run
./gradlew bootRun
```

### API Usage (Example)

```bash
# Transfer funds (idempotent)
curl -X POST http://localhost:8080/api/transfer \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: <unique-uuid>" \
  -d '{
    "senderId": 1,
    "receiverId": 2,
    "amount": 500.00
  }'
```

---

## 📊 Why This Project Matters

This project goes beyond basic CRUD and demonstrates:

- Exactly-once transaction execution using idempotency
- Distributed transaction management using the Saga pattern
- Concurrency control and race condition prevention at DB level
- Fault-tolerant system design that stays consistent under partial failures

---

## 🔮 Future Improvements

- Retry mechanism with exponential backoff for failed transactions
- JWT-based authentication and authorization
- Distributed tracing and metrics (Micrometer + Prometheus)
- Kafka integration for async event-driven saga steps

---

## 👨‍💻 Author

**Harsh Jha** — [LinkedIn](https://www.linkedin.com/in/harsh-jha-85722b254/) · [GitHub](https://github.com/Harshjha002)
