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

## Design Decisions

| Decision | Why |
|----------|-----|
| Saga over 2PC | Avoids distributed lock overhead; tolerates partial failures |
| Pessimistic locking (SELECT FOR UPDATE) | Guarantees zero race conditions on balance updates |
| ShardingSphere horizontal sharding | Distributes wallet data by user_id hash across N shards |
| Idempotency keys | Exactly-once guarantee even on client retries |

## ⚙️ Key Features

- ✅ **Idempotent Transactions**  
  Prevents duplicate processing using idempotency keys.

- 🔁 **Saga Pattern Implementation**  
  Ensures consistency across multi-step transactions (debit → credit → status update).

- ⚡ **Asynchronous Processing**  
  Non-blocking transaction execution using async workflows.

- 🔒 **Concurrency Control**  
  Prevents race conditions using database-level locking.

- 💥 **Failure Handling**  
  Maintains system consistency even when steps fail.

- 📦 **Database Sharding (Basic)**  
  Distributes wallet data across partitions for scalability.

---

## 🏗️ System Flow

---

## 🔄 Idempotency Handling

- Each request includes an **idempotency key**
- Duplicate requests return the same transaction
- Prevents multiple executions of the same transfer

---

## 🧩 Saga Workflow

| Step | Description |
|------|------------|
| Debit | Deduct amount from sender wallet |
| Credit | Add amount to receiver wallet |
| Update | Mark transaction as SUCCESS |

If any step fails:
- System marks transaction as **FAILED**
- Prevents inconsistent state

---

## 🗄️ Tech Stack

- Java  
- Spring Boot  
- Spring Data JPA (Hibernate)  
- MySQL  
- Lombok  
- ShardingSphere (for database sharding)

---

## 📊 Why This Project Matters

This project goes beyond basic CRUD and demonstrates:

- Exactly-once transaction execution using idempotency  
- Distributed transaction management using Saga pattern  
- Concurrency control and race condition handling  
- Fault-tolerant backend system design  

---

## 🚀 Future Improvements

- Retry mechanism for failed transactions  
- JWT-based authentication  
- Monitoring and metrics (observability)  
- Message queue integration (Kafka/RabbitMQ)  

---

## 👨‍💻 Author

Harsh Jha  
