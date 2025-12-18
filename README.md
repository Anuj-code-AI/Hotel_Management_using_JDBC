# 🏨 Hotel Management System (JDBC + MySQL)

A simple, menu-driven **Hotel Reservation System** built using **Core Java, JDBC, and MySQL**.  
This project is created to practice real-world JDBC concepts such as **CRUD**, **PreparedStatement**,  
**transactions**, **commit/rollback**, **SQL injection protection**, **constraints**, and  
**modular DB design**.

---

## 🚀 Features

### ✔ Room Management
- Static room list (001–020, 101–120, 201–220)
- CHECK constraint to ensure only valid rooms exist
- Room status tracking (`EMPTY` / `FULL`)
- Automatically updates on booking or checkout

### ✔ Reservation Management
- Make new reservations
- Auto timestamp for `check_in`
- Live update of `check_out` timestamp
- Auto billing based on stay duration
- Protects queries using **PreparedStatement**
- Validates room availability before booking

### ✔ Transaction Management
- Uses `setAutoCommit(false)`
- Applies **COMMIT** after success
- Uses **ROLLBACK** on failure (safe DB operations)

### ✔ Search Reservations
Search using:
- Guest Name
- Room Number
- Contact Number
- Reservation ID

### ✔ Security
- No SQL Injection (uses `?` placeholders)🏨 Hotel Management System (JDBC + MySQL)
  A simple, menu-driven Hotel Reservation System built using Core Java, JDBC, and MySQL.
  This project is created to practice real-world JDBC concepts such as CRUD, PreparedStatement,
  transactions, commit/rollback, SQL injection protection, constraints, and
  modular DB design.

🚀 Features
✔ Room Management
Static room list (001–020, 101–120, 201–220)
CHECK constraint to ensure only valid rooms exist
Room status tracking (EMPTY / FULL)
Automatically updates on booking or checkout
✔ Reservation Management
Make new reservations
Auto timestamp for check_in
Live update of check_out timestamp
Auto billing based on stay duration
Protects queries using PreparedStatement
Validates room availability before booking
✔ Transaction Management
Uses setAutoCommit(false)
Applies COMMIT after success
Uses ROLLBACK on failure (safe DB operations)
✔ Search Reservations
Search using:

Guest Name
Room Number
Contact Number
Reservation ID
✔ Security
No SQL Injection (uses ? placeholders)
MySQL constraints enforced:
PRIMARY KEY
FOREIGN KEY
CHECK
DEFAULT
ENUM
🗂 Project Structure
🛠 Tech Stack
Java 17+
MySQL 8+
JDBC
IntelliJ IDEA
Git + GitHub
⚙ Database Setup
Run the following MySQL commands before starting:

CREATE DATABASE hotel_db;
USE hotel_db;


##😂 How to Run

- MySQL constraints enforced:
    - PRIMARY KEY
    - FOREIGN KEY
    - CHECK
    - DEFAULT
    - ENUM

---

## 🗂 Project Structure
## 🛠 Tech Stack

- **Java 17+**
- **MySQL 8+**
- **JDBC**
- **IntelliJ IDEA**
- **Git + GitHub**

---

## ⚙ Database Setup

Run the following MySQL commands before starting:

```sql
CREATE DATABASE hotel_db;
USE hotel_db;
```

---

## ▶️ How to Run

Open the project in IntelliJ

Make sure mysql-connector-j.jar is added to classpath

### Update your DB credentials in:
```
src/resources/config.properties
```

### Example:
```
db.url=jdbc:mysql://localhost:3306/hotel_db
db.username=root
db.password=your_password
```

### Run:
```
HotelReservationSystem.java
```
---

## 🎉 Thank You for Checking Out the Project!

### If you like it, ⭐ star the repository on GitHub!
 
---

