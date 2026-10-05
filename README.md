# Blood Bank Management System
### CSE Minor Project — Java Swing + MySQL (JDBC)

A complete desktop application for managing blood bank operations: donors, blood
stock, donations, and hospital blood requests — with a login-protected admin
dashboard, full CRUD, live stock calculations, and validation throughout.

---

## 1. Project Abstract

The Blood Bank Management System is a Java desktop application that digitizes
the core operations of a blood bank: donor registration, donation recording,
blood stock tracking, and hospital blood-request processing. Built with Java
Swing for the interface and MySQL (via JDBC) for persistent storage, the system
replaces manual register-based tracking with a structured, validated, and
auditable digital workflow. An administrator logs in, views live statistics on
a dashboard, and manages every entity through dedicated CRUD screens. Business
rules — such as automatically adjusting blood stock when donations are recorded
or requests are approved, and preventing stock from going negative — are
enforced centrally in a service layer, keeping the system consistent and
reliable.

## 2. Problem Statement

Many small and mid-sized blood banks still rely on paper registers or
disconnected spreadsheets to track donors, stock, and requests. This leads to
duplicate donor entries, stale stock counts, delayed request fulfilment, and no
easy way to check availability of a given blood group before promising it to a
hospital. There is a need for a lightweight, self-contained desktop system that
a small team can install and run locally, without dependence on the internet or
complex enterprise infrastructure, while still enforcing correctness (e.g.,
never issuing more blood than is in stock).

## 3. Objectives

- Provide a secure, login-gated interface for blood bank administrators.
- Maintain an accurate, centralized donor database with full CRUD support.
- Track blood stock per blood group with automatic status classification.
- Automatically increase stock when donations are recorded.
- Automatically decrease stock when requests are approved/completed, while
  never allowing stock to go negative.
- Allow quick searching of blood availability and matching donors by blood
  group.
- Provide consolidated, filterable reports across all major entities.
- Demonstrate core Java/OOP concepts (encapsulation, layered architecture,
  interfaces via DAO pattern, exception handling) suitable for a CSE minor
  project and viva defense.

## 4. Scope

**In scope:** donor management, blood stock management, donation recording,
blood request lifecycle (Pending → Approved/Rejected → Completed), blood
search, reporting, single-admin authentication.

**Out of scope:** multi-branch blood bank networking, SMS/email
notifications, online/web access, role-based multi-user accounts, payment or
billing modules, government compliance reporting. These are noted under
Future Scope for potential extension.

## 5. Functional Requirements

| ID | Requirement |
|----|-------------|
| FR1 | The system shall authenticate an admin via username/password before granting access. |
| FR2 | The system shall allow adding, updating, deleting, searching, and listing donors. |
| FR3 | The system shall validate donor age, phone number, and blood group on entry. |
| FR4 | The system shall maintain a stock count per blood group with an auto-computed status (Available / Low Stock / Not Available). |
| FR5 | The system shall record donations and automatically increase the matching blood stock. |
| FR6 | The system shall record blood requests with a Pending status by default. |
| FR7 | The system shall check available stock before allowing a request to be approved/completed, and shall decrease stock automatically on approval/completion. |
| FR8 | The system shall never allow blood stock to become negative. |
| FR9 | The system shall allow searching blood availability by blood group and list matching donors. |
| FR10 | The system shall provide filterable reports for donors, stock, donations, and requests. |
| FR11 | The system shall display live dashboard statistics computed from the database. |
| FR12 | The system shall allow the admin to log out and return to the login screen. |

## 6. Non-Functional Requirements

- **Usability:** Simple, consistent Swing UI with clear labels, confirmation
  dialogs, and error messages suitable for non-technical demo/viva use.
- **Reliability:** All database writes use `PreparedStatement` with validation
  before execution; stock-affecting operations are guarded against negative
  values.
- **Performance:** Designed for a single-branch blood bank with hundreds to a
  few thousand records; queries are indexed via primary keys.
- **Maintainability:** Layered architecture (model / dao / service / ui) keeps
  concerns separated and each class independently explainable.
- **Portability:** Pure Java + MySQL; runs on any OS with a JDK and MySQL
  server, and imports cleanly into IntelliJ IDEA, Eclipse, or NetBeans.

## 7. Hardware/Software Requirements

**Hardware (minimum):**
- 4 GB RAM, dual-core processor, 500 MB free disk space

**Software:**
- JDK 17 or later (JDK 21 recommended)
- MySQL Server 8.x
- MySQL Connector/J (JDBC driver) — see Setup below
- IDE: IntelliJ IDEA / Eclipse / NetBeans (any one)
- OS: Windows, macOS, or Linux

## 8. Project Folder Structure

```text
BloodBankManagementSystem/
├── database/
│   └── schema.sql               -- full DB creation + sample data script
├── lib/                         -- place mysql-connector-j-*.jar here
├── src/
│   ├── Main.java                -- application entry point
│   ├── database/
│   │   └── DBConnection.java    -- JDBC connection utility
│   ├── model/                   -- POJOs / entities
│   │   ├── Admin.java
│   │   ├── Donor.java
│   │   ├── BloodStock.java
│   │   ├── Donation.java
│   │   └── BloodRequest.java
│   ├── dao/                     -- database access objects (PreparedStatement-based)
│   │   ├── AdminDAO.java
│   │   ├── DonorDAO.java
│   │   ├── BloodStockDAO.java
│   │   ├── DonationDAO.java
│   │   └── BloodRequestDAO.java
│   ├── service/                 -- business logic + validation
│   │   ├── ValidationUtil.java
│   │   ├── DonorService.java
│   │   ├── BloodStockService.java
│   │   ├── DonationService.java
│   │   ├── BloodRequestService.java
│   │   └── DashboardService.java
│   ├── exception/
│   │   └── BloodBankException.java   -- custom checked exception
│   └── ui/                      -- Swing screens
│       ├── UIConstants.java
│       ├── LoginFrame.java
│       ├── DashboardFrame.java
│       ├── DonorManagementUI.java
│       ├── BloodStockUI.java
│       ├── DonationUI.java
│       ├── BloodRequestUI.java
│       ├── SearchBloodUI.java
│       └── ReportsUI.java
└── README.md                    -- this file
```

## 9. Explanation of Every Java Class

### database
- **DBConnection** — Singleton-style utility that opens and returns one shared
  JDBC `Connection` to `blood_bank_db`, using the MySQL Connector/J driver.
  Configure `DB_USER`/`DB_PASSWORD` here.

### model (entities — pure data holders using encapsulation)
- **Admin** — represents an administrator login record (id, username, password).
- **Donor** — represents a registered donor with personal details, blood group,
  and last donation date.
- **BloodStock** — represents the unit count for one blood group; computes its
  own `getStatus()` (Available / Low Stock / Not Available) from the count.
- **Donation** — represents one donation event, linking a donor to units added
  to stock on a given date.
- **BloodRequest** — represents a hospital's request for blood, including its
  lifecycle `status`.

### exception
- **BloodBankException** — a custom checked exception used throughout the
  service layer to signal business-rule violations (e.g., insufficient stock,
  invalid input) with a clear message for the UI to display.

### dao (data access — one class per table, all queries use `PreparedStatement`)
- **AdminDAO** — verifies login credentials against the `admin` table.
- **DonorDAO** — CRUD + search (by name / blood group) + count for `donor`.
- **BloodStockDAO** — read stock, atomically increase/decrease units (the
  decrease query itself is guarded with `AND units_available >= ?` so it can
  never go negative even under concurrent use).
- **DonationDAO** — insert donation, list/search donation history (joined with
  donor name), count for dashboard.
- **BloodRequestDAO** — insert request, update status, list/search by status,
  count for dashboard.

### service (business logic — the "brain" of the app; UI never talks to DAO directly)
- **ValidationUtil** — static helpers for every validation rule (empty fields,
  age range, phone format, blood group, positive units, non-future dates).
- **DonorService** — validates and delegates to `DonorDAO`; used by the Donor
  Management screen.
- **BloodStockService** — wraps `BloodStockDAO`; `decreaseStock()` re-checks
  availability and throws `BloodBankException` if insufficient.
- **DonationService** — validates a donation, saves it, **and automatically
  calls `BloodStockService.increaseStock()`** — this is where Rule 1 (donation
  increases stock) lives.
- **BloodRequestService** — validates a request; `updateStatus()` implements
  Rule 2–4: checks stock before allowing Approved/Completed, calls
  `decreaseStock()`, and never touches stock on Rejected.
- **DashboardService** — aggregates live counts (`totalDonors`,
  `totalBloodUnits`, `totalRequests`, `totalDonations`) for the dashboard.

### ui (Swing screens — presentation only, always goes through the service layer)
- **UIConstants** — shared colors/fonts so every screen looks consistent.
- **LoginFrame** — username/password login, Login/Clear/Exit buttons.
- **DashboardFrame** — shows live stats and navigation buttons to every module.
- **DonorManagementUI** — full CRUD form + searchable `JTable` for donors.
- **BloodStockUI** — read-only table of all 8 blood groups with status.
- **DonationUI** — record a donation (auto-fills blood group from the selected
  donor) + searchable donation history table.
- **BloodRequestUI** — submit a request + Approve/Complete/Reject buttons with
  confirmation dialogs + status filter.
- **SearchBloodUI** — pick a blood group, see unit count/status, and see
  matching donors.
- **ReportsUI** — tabbed reports (Donors, Stock, Donations, Requests) each with
  its own filter.

### Main
- **Main** — sets the system look-and-feel and launches `LoginFrame` on the
  Swing Event Dispatch Thread.

## 10. Explanation of Every Database Table

- **admin** `(admin_id PK, username, password)` — stores login credentials.
  Seeded with `admin` / `admin123`.
- **donor** `(donor_id PK, full_name, age, gender, blood_group, phone_number,
  email, address, last_donation_date)` — one row per registered donor.
- **blood_stock** `(blood_group PK, units_available)` — exactly 8 rows, one per
  blood group (A+, A-, B+, B-, AB+, AB-, O+, O-), seeded with starting units.
- **donation** `(donation_id PK, donor_id FK → donor, blood_group FK →
  blood_stock, donation_date, units_donated)` — one row per donation event;
  `donor_id` cascades on delete.
- **blood_request** `(request_id PK, patient_name, hospital_name,
  contact_number, blood_group FK → blood_stock, units_required, request_date,
  status)` — one row per hospital request; `status` moves through
  Pending → Approved/Rejected → Completed.

## 11. Setup and Run Instructions

### Step 1 — Create the database
1. Open MySQL Workbench, phpMyAdmin, or the `mysql` CLI.
2. Run the full script in `database/schema.sql`. This creates the
   `blood_bank_db` database, all five tables, and inserts sample data
   (including the default admin login and starting stock).

```bash
mysql -u root -p < database/schema.sql
```

### Step 2 — Get the JDBC driver
1. Download **MySQL Connector/J** (a `.jar` file, e.g.
   `mysql-connector-j-8.4.0.jar`) from `https://dev.mysql.com/downloads/connector/j/`.
2. Place it in the `lib/` folder of this project.

### Step 3 — Configure the connection
Open `src/database/DBConnection.java` and update these three lines to match
your local MySQL setup:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/blood_bank_db?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "root";
```

### Step 4a — Run in IntelliJ IDEA
1. `File → Open` and select the `BloodBankManagementSystem` folder.
2. `File → Project Structure → Libraries → +` and add the connector JAR from `lib/`.
3. Right-click `src/Main.java → Run 'Main.main()'`.

### Step 4b — Run in Eclipse
1. `File → Import → Existing Projects into Workspace` (or create a new Java
   Project and copy the `src` folder in).
2. Right-click the project → `Build Path → Add External Archives` → select the
   connector JAR from `lib/`.
3. Right-click `Main.java → Run As → Java Application`.

### Step 4c — Run in NetBeans
1. `File → New Project → Java with Existing Sources`, point it at this folder.
2. Right-click project → `Properties → Libraries → Add JAR/Folder` → select the
   connector JAR from `lib/`.
3. Right-click `Main.java → Run File`.

### Step 4d — Run from the command line
```bash
# Compile (Windows uses ; instead of : as the classpath separator)
javac -d out -cp lib/mysql-connector-j-8.4.0.jar $(find src -name "*.java")

# Run
java -cp "out:lib/mysql-connector-j-8.4.0.jar" Main
```

### Default Admin Login
```
Username: admin
Password: admin123
```

## 12. Sample Data

`database/schema.sql` seeds:
- 4 sample donors across different blood groups
- Starting stock for all 8 blood groups (2–15 units each)
- 3 sample donation records
- 2 sample blood requests (one Pending, one Approved)

## 13. Future Scope

- Multi-user roles (receptionist, technician, admin) with permission levels.
- SMS/email alerts to donors when their blood group runs low.
- Expiry-date tracking per blood unit (not just aggregate counts).
- Multi-branch support with inter-branch stock transfer.
- Exportable PDF/Excel reports.
- Web or mobile companion app for donor self-registration.

## 14. Conclusion

This project demonstrates a complete, working Java desktop application that
applies core software engineering principles — layered architecture, input
validation, transactional business rules, and a clean Swing UI — to a
practical healthcare-adjacent problem. It is scoped and structured to be
fully explainable class-by-class in a college viva, while still functioning
as a real, usable tool for a small blood bank.

## 15. Testing / Test Cases

| # | Test Case | Steps | Expected Result |
|---|-----------|-------|------------------|
| 1 | Valid login | Enter `admin` / `admin123`, click Login | Dashboard opens |
| 2 | Invalid login | Enter wrong password, click Login | Error dialog: "Invalid username or password." |
| 3 | Empty login fields | Leave both fields blank, click Login | Warning dialog before hitting the database |
| 4 | Add donor — valid data | Fill all fields correctly, click Add Donor | Success dialog, new row appears in table, donor count on dashboard increases |
| 5 | Add donor — invalid age | Enter age `10`, click Add Donor | Validation error: age must be 18–65 |
| 6 | Add donor — invalid phone | Enter `12345`, click Add Donor | Validation error: invalid phone number |
| 7 | Update donor | Select a row, change name, click Update Donor | Table reflects new name |
| 8 | Delete donor | Select a row, click Delete Donor, confirm | Row removed, donor count decreases |
| 9 | Search donor by blood group | Go to Search Blood, pick `O+` | Matching donors listed, correct stock count shown |
| 10 | Record donation | Select donor, enter units `1`, click Record Donation | Donation appears in history; matching blood_stock unit count increases by 1 |
| 11 | Submit blood request | Fill request form, submit | Request appears with status `Pending` |
| 12 | Approve request — sufficient stock | Select a Pending request within stock limits, click Approve Selected | Status becomes `Approved`; stock decreases by the requested units |
| 13 | Approve request — insufficient stock | Request more units than available, try to approve | Warning dialog: "Cannot approve request: insufficient stock" — stock and status unchanged |
| 14 | Reject request | Select Pending request, click Reject Selected | Status becomes `Rejected`; stock unchanged |
| 15 | Stock never negative | Attempt to approve two large requests that together exceed stock | Second approval is blocked by validation |
| 16 | Reports filter | Open Reports → Blood Requests tab, filter by `Completed` | Only completed requests shown |
| 17 | Dashboard stats accuracy | Add a donor and a donation, reopen Dashboard | Total Donors and Total Donations counts reflect the additions |
| 18 | Logout | Click Logout on dashboard, confirm | Returns to Login screen |

## 16. Viva Questions and Answers

**Q1. Why did you use a layered architecture (model/dao/service/ui)?**
A: It separates concerns — models hold data, DAOs handle raw SQL, services
hold business rules and validation, and UI only handles presentation. This
makes each class independently testable and easy to explain, and means a
business rule (like "stock can't go negative") lives in exactly one place.

**Q2. Why `PreparedStatement` instead of `Statement`?**
A: `PreparedStatement` prevents SQL injection by parameterizing input values,
and it's more efficient for repeated queries since the SQL is precompiled.

**Q3. How does blood stock stay accurate?**
A: It's never edited directly by the user. It only changes through two
service-layer paths: `DonationService.recordDonation()` increases it, and
`BloodRequestService.updateStatus()` decreases it when a request is
approved/completed — and both go through `BloodStockService`, which re-checks
availability before any decrease.

**Q4. How do you guarantee stock never goes negative?**
A: Two layers of defence: (1) `BloodStockService.decreaseStock()` checks
current units against the requested amount and throws a
`BloodBankException` if insufficient; (2) the SQL `UPDATE` itself includes
`AND units_available >= ?` as a final guard, so even a race condition cannot
push it below zero.

**Q5. What design pattern does the DAO layer follow?**
A: The DAO (Data Access Object) pattern — one class per table that isolates
all SQL for that table behind plain Java method calls, so the rest of the
app never writes SQL directly.

**Q6. Why is `BloodBankException` a checked exception?**
A: Business-rule violations (bad input, insufficient stock) are conditions
the calling code is expected to catch and handle (show a message to the
user), not programming errors — a checked exception forces callers to
handle them explicitly.

**Q7. How is the dashboard kept up to date?**
A: `DashboardService.getStats()` runs live `COUNT`/`SUM` queries against the
database every time the dashboard is shown or refreshed — it never caches
stale numbers.

**Q8. What happens if two requests are approved at nearly the same time?**
A: The SQL update for decreasing stock is atomic and conditional
(`WHERE units_available >= ?`), so even without explicit locking, a
concurrent update that would push stock negative simply fails to match any
row, and the service layer reports it as insufficient stock.

**Q9. Why Java Swing instead of a web framework?**
A: The project scope calls for a standalone desktop application that runs
without a server or browser, is simple to demo offline, and directly
showcases core Java GUI and JDBC skills appropriate for a CSE minor project.

**Q10. How would you extend this system for multiple blood bank branches?**
A: Add a `branch` table, add a `branch_id` foreign key to `donor`,
`blood_stock`, `donation`, and `blood_request`, and scope all DAO queries by
the logged-in admin's branch — the layered architecture means only the DAO
and service layers would need to change, not the UI logic.
