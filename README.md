# 💳 Nivora Pay — Distributed Wallet System

A **scalable backend wallet service** built with Spring Boot that supports **wallet-based transactions**, powered by **Saga orchestration** and **database sharding (ShardingSphere)**.

---

## 🚀 What This Project Does

Nivora Pay enables:

* Wallet creation and management
* Credit and debit operations
* Wallet-to-wallet money transfer
* Distributed transaction handling using Saga pattern
* Scalable data handling using database sharding

---

## 🧠 Core Architecture

### 🔹 Saga-Based Transaction Flow

Each transaction is executed as a **Saga**, ensuring consistency across multiple steps:

1. **Start transaction**
2. **Debit source wallet**
3. **Credit destination wallet**
4. **Update transaction status**
5. **On failure → Compensation (rollback executed)**

---

### 🔹 Key Components

* **WalletService** → balance operations (credit, debit, validation)
* **TransactionService** → transaction creation and management
* **TransferSagaService** → initiates saga flow
* **SagaOrchestrator** → manages execution & compensation
* **Saga Steps**

  * `DebitSourceWalletStep`
  * `CreditDestinationWalletStep`
  * `UpdateTransactionStatus`

---

## 🏗️ Tech Stack

* **Java 17**
* **Spring Boot**
* **Spring Data JPA**
* **MySQL**
* **Apache ShardingSphere**
* **Gradle**

---

## 📂 Project Structure

```
controllers/        → REST APIs
services/           → business logic
services/saga/      → saga orchestration
repositories/       → data access layer
entities/           → database models
dtos/               → request/response objects
config/             → application + saga config
```

---

## 📡 API Endpoints

### 🔹 Wallet APIs

* `POST /api/v1/wallet/{userId}` → Create wallet
* `POST /api/v1/wallet/{userId}/credit` → Credit wallet
* `POST /api/v1/wallet/{userId}/debit` → Debit wallet

---

### 🔹 Transaction API

**Base URL**

```
http://localhost:8081/api/v1/transactions
```

---

### ➤ Send Money

```
POST /api/v1/transactions
```

**Description**
Transfers money between wallets using Saga orchestration.

---

### 🧾 Request

```json
{
  "fromWalletId": 1256613059640688640,
  "toWalletId": 1256612945660477440,
  "amount": 50,
  "description": "Payment for lunch"
}
```

---

### ✅ Response

```json
{
  "sagaInstanceId": 101,
}
```

---

## ⚙️ Configuration

### application.properties

* Server configuration
* Database connection
* JPA settings

### sharding.yml

* Sharding rules
* Data source configuration
* Wallet/user-based routing

---

## ▶️ Run Locally

```bash
git clone https://github.com/your-username/nivora-pay.git
cd nivora-pay
./gradlew bootRun
```

---

## 🔥 Key Highlights

* Implements **Saga pattern for distributed transactions**
* Handles **failure using compensation logic**
* Uses **database sharding for scalability**
* Designed with **modular and extensible architecture**

---

## ⚠️ Failure Handling

* Insufficient balance → transaction fails
* Saga rollback triggers compensation steps
* Ensures **data consistency across operations**

---

## 🔮 Future Improvements

* Idempotency for safe retries
* Retry mechanism for failed saga steps
* Event-driven architecture (Kafka/RabbitMQ)
* Authentication & authorization
* Monitoring and logging

---

## 👤 Author

**Harsh Jha**
Backend Developer — focused on scalable systems

---

## ⭐ Why This Project Matters

This project goes beyond CRUD and demonstrates:

* Distributed transaction design
* Fault-tolerant systems
* Scalable backend architecture

---
