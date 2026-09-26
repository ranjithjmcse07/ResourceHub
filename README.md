# ResourceHub — Equipment & Tool Circulation System

An Amazon/E-Commerce inspired full-stack web application designed for sharing, lending, borrowing, and circulating tools and equipment with **dual hourly and daily pricing**.

Built with **Spring Boot 3**, **Spring JDBC (`JdbcTemplate`)**, **MySQL 9.2**, **Spring Security (JWT + BCrypt)**, and a modern responsive **HTML5/CSS3/JavaScript** frontend.

---

## 🌟 Key Features

1. **Amazon / E-Commerce Inspired UI**:
   - Top navigation bar with category selector dropdown, live search bar, location badge, and user session menu.
   - Category sub-navbar (Power Tools, Cleaning, Construction, Gardening, Workshop, Automotive).
   - Product catalog with high-resolution tool imagery, condition badges, star ratings, and **Dual Pricing display (₹/hour & ₹/day)**.
   - Amazon-style product details page with image preview, full specifications, lender verification badges, and a live-calculating "Buy Box / Rent Box".

2. **Dual Rental Duration & Pricing Model**:
   - Choose to rent by the **Hour** (e.g. ₹30/hr) or by the **Day** (e.g. ₹180/day).
   - Interactive live price breakdown calculator.
   - Configurable automatic late fine calculation:
     $$\text{Late Fine} = \text{Late Duration} \times \text{Fine Rate}$$

3. **Pure JDBC with Direct SQL**:
   - Uses `JdbcTemplate` with explicit SQL queries in dedicated DAO classes (`UserDao`, `ToolDao`, `BorrowRequestDao`, `ReviewDao`, `SuggestionDao`).
   - Direct control over relational queries, joins, and aggregates.
   - Ideal for academic and college project submissions.

4. **Role-Based Workflows**:
   - **Lender**: Add/edit tools with dual rates, approve/reject borrow requests, manage active loans, track earnings.
   - **Borrower**: Browse/filter tools, request rentals, view active rentals, return equipment, write verified 1–5 star reviews.
   - **Admin**: Monitor all users, moderate tool listings, audit full transaction ledger, and resolve user feedback/suggestions.

---

## 🚀 Quick Start Guide

### 1. Database Setup
The application connects to MySQL on port 3306 using database `toolshare`.
The schema and seed scripts are located in `database/`:
- `database/schema.sql` (Creates tables: `users`, `tools`, `borrow_requests`, `reviews`, `suggestions`)
- `database/sample_data.sql` (Pre-seeded tools with dual pricing)

### 2. Run the Backend
From the `backend/` directory:
```powershell
mvn clean compile
mvn spring-boot:run
```
The server will start at: `http://localhost:8080`

### 3. Open the Frontend
Open your browser and navigate to:
```
http://localhost:8080/index.html
```
*(All static assets are bundled directly into Spring Boot).*

---

## 👤 Pre-configured Demo Accounts

For instant testing, use the 1-Click Fast Login buttons on `login.html`:

| Role | Username | Password | Access / Capabilities |
| :--- | :--- | :--- | :--- |
| **Borrower** | `borrower1` | `borrower123` | Rent tools, return equipment, write reviews |
| **Lender** | `lender1` | `lender123` | List tools, approve/reject requests, track loans |
| **Admin** | `admin` | `ranjith567` | Full platform control (Administrator: **Ranjith J M**) |

---

## 📁 Project Structure

```
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/toolshare/
│       │   ├── ToolShareApplication.java
│       │   ├── config/ (SecurityConfig, CORS)
│       │   ├── security/ (JwtService, JwtAuthenticationFilter, CustomUserDetailsService)
│       │   ├── model/ (User, Tool, BorrowRequest, Review, Suggestion)
│       │   ├── dto/ (Auth, Tool, BorrowRequest, Review, Stats)
│       │   ├── dao/ (UserDao, ToolDao, BorrowRequestDao, ReviewDao, SuggestionDao with JDBC)
│       │   ├── service/ (Business logic & workflows)
│       │   ├── controller/ (REST API endpoints)
│       │   ├── exception/ (GlobalExceptionHandler)
│       │   └── init/ (DataInitializer auto-seeding)
│       └── resources/
│           ├── application.properties
│           └── static/ (Frontend files)
├── frontend/
│   ├── index.html (Amazon Marketplace Home)
│   ├── tool-details.html (Amazon 3-Column Product Page)
│   ├── login.html (Sign In with 1-Click demo accounts)
│   ├── register.html (Borrower & Lender registration)
│   ├── lender-dashboard.html (Lender Workspace)
│   ├── borrower-dashboard.html (Borrower Workspace)
│   ├── admin-dashboard.html (Admin Center)
│   ├── css/style.css (Amazon-style UI Design System)
│   └── js/api.js (Central JWT API Client)
├── database/
│   ├── schema.sql
│   └── sample_data.sql
└── PROJECT.md
```
