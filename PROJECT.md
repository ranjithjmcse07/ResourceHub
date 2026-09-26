# Resource Circulation System --- Complete Project Specification

## 1. Project Overview

Build a complete full-stack web application called **Resource
Circulation System (ResourceHub)**.

The platform allows people and organizations to share, lend, borrow, and
circulate reusable tools and equipment instead of purchasing items that
are only occasionally needed.

Examples: drills, ladders, welding machines, pressure washers, gardening
tools, cleaning equipment, construction tools, automotive tools, and
other reusable resources.

The system has three roles:

-   **LENDER** --- owns tools and makes them available.
-   **BORROWER** --- searches tools and requests to borrow them.
-   **ADMIN** --- manages users, tools, transactions, reviews, and
    suggestions.

The application must be a real working system connected to a MySQL
database, not only a UI prototype.

------------------------------------------------------------------------

## 2. Core Technology

### Backend

-   Java
-   Spring Boot 3.x
-   Spring Web
-   Spring Security
-   BCrypt password hashing
-   JWT authentication
-   Spring Data JPA / Hibernate
-   Maven

### Frontend

-   HTML5
-   CSS3
-   JavaScript
-   Responsive/mobile-friendly design

### Database

-   MySQL
-   Database name: `toolshare`

### Development

-   VS Code
-   Windows / PowerShell
-   Java 17+ compatible code

------------------------------------------------------------------------

## 3. Main Modules

1.  Authentication and Registration
2.  User/Profile Management
3.  Lender Dashboard
4.  Borrower Dashboard
5.  Tool Management
6.  Borrowing Request Management
7.  Lending/Return Tracking
8.  Fine Calculation
9.  Reviews and Ratings
10. Suggestions/Feedback
11. Admin Dashboard
12. Search and Filtering
13. Security and Authorization

------------------------------------------------------------------------

# 4. Authentication

## Registration

Registration fields:

-   Full name
-   Username
-   Mobile number
-   Email
-   Password
-   Confirm password
-   Role

For LENDER: - Company/organization name - Tool/lending address

For BORROWER: - Address

Requirements:

-   Username unique
-   Email unique
-   Required-field validation
-   Valid email
-   Valid mobile
-   Password confirmation
-   Valid role
-   Password must NEVER be stored as plaintext

Use BCrypt.

## Login

Fields:

-   Username or email
-   Password

After successful login:

1.  Validate credentials.
2.  Generate JWT.
3.  Return JWT and user role.
4.  Frontend stores the authentication token for the session.
5.  Redirect by role:

``` text
LENDER   -> Lender Dashboard
BORROWER -> Borrower Dashboard
ADMIN    -> Admin Dashboard
```

Protect all private APIs with Spring Security.

------------------------------------------------------------------------

# 5. Roles and Permissions

## LENDER

Can:

-   Register/login
-   View/update profile
-   Add tools
-   Add tool image
-   Edit own tools
-   Deactivate/delete own tools
-   Change availability
-   View borrower requests
-   Approve/reject requests
-   View active loans
-   Track expected return dates
-   Confirm returned tools
-   View lending history
-   View reviews
-   Submit suggestions
-   Logout

## BORROWER

Can:

-   Register/login
-   View/update profile
-   Browse tools
-   Search/filter tools
-   View tool details
-   Send borrowing requests
-   Select borrowing period
-   View request status
-   Cancel pending requests
-   View current borrowings
-   View return dates
-   Complete return workflow
-   View borrowing history
-   Give ratings/reviews after completed borrowing
-   Submit suggestions
-   Logout

## ADMIN

Can:

-   Login
-   View dashboard
-   View all users
-   Activate/deactivate users
-   View all tools
-   Remove inappropriate tools
-   View transactions
-   View reviews
-   View suggestions
-   Update suggestion status
-   Monitor platform activity

------------------------------------------------------------------------

# 6. Database

Create:

``` sql
CREATE DATABASE toolshare;
```

Use foreign keys and appropriate indexes.

## users

Suggested columns:

``` text
id
full_name
username
mobile
email
password
role
company_name
address
active
created_at
updated_at
```

Role values:

``` text
LENDER
BORROWER
ADMIN
```

## tools

``` text
id
lender_id
tool_name
category
description
condition
location
daily_rate
image_url
availability_status
created_at
updated_at
```

Availability:

``` text
AVAILABLE
REQUESTED
BORROWED
MAINTENANCE
INACTIVE
```

## borrow_requests

``` text
id
tool_id
borrower_id
start_date
expected_return_date
actual_return_date
status
requested_at
approved_at
returned_at
fine_amount
```

Status:

``` text
PENDING
APPROVED
REJECTED
CANCELLED
ACTIVE
RETURN_REQUESTED
COMPLETED
```

## reviews

``` text
id
tool_id
borrower_id
rating
comment
created_at
```

Rating must be 1--5.

## suggestions

``` text
id
user_id
subject
message
status
admin_response
created_at
```

Suggestion status:

``` text
OPEN
REVIEWED
RESOLVED
```

------------------------------------------------------------------------

# 7. Relationships

``` text
LENDER User 1 ---- N Tools

Tool 1 ---- N Borrow Requests

BORROWER User 1 ---- N Borrow Requests

Tool 1 ---- N Reviews

BORROWER User 1 ---- N Reviews

User 1 ---- N Suggestions
```

Use DTOs where necessary to avoid recursive JSON serialization.

------------------------------------------------------------------------

# 8. Main Borrowing Workflow

Implement this complete flow:

``` text
Borrower Login
      ↓
Browse Tools
      ↓
Select Tool
      ↓
View Tool Details
      ↓
Choose Start Date
      ↓
Choose Return Date
      ↓
Send Borrow Request
      ↓
Lender Receives Request
      ↓
Accept / Reject
      ↓
If Accepted
      ↓
Tool becomes BORROWED
      ↓
Borrower uses tool
      ↓
Return
      ↓
Transaction becomes COMPLETED
      ↓
Tool becomes AVAILABLE
      ↓
Borrower can submit Review
```

Business rules:

1.  Cannot request unavailable tool.
2.  Borrower cannot borrow their own tool.
3.  Start date cannot be in the past.
4.  Return date must be after start date.
5.  Prevent duplicate active requests for the same tool.
6.  Prevent overlapping approved borrowing periods.
7.  Approval makes the tool unavailable.
8.  Completion makes the tool available.
9.  Rejected requests cannot become active.
10. Completed transactions cannot be edited as active transactions.

------------------------------------------------------------------------

# 9. Lending Period and Fine

Calculate:

``` text
Number of Days = Return Date - Start Date
```

If a daily rate exists:

``` text
Total Cost = Number of Days × Daily Rate
```

Fine:

``` text
Late Days = Actual Return Date - Expected Return Date

If Late Days > 0:
Fine = Late Days × Fine Per Day
```

Use a single configurable value such as:

``` text
finePerDay = 50
```

Do not hard-code the fine throughout the application.

Example:

``` text
Expected Return: 10 Sep
Actual Return:   13 Sep
Late Days:       3
Fine Per Day:    ₹50
Fine:            ₹150
```

Do not add real online payment processing unless requested later.

------------------------------------------------------------------------

# 10. Lender Dashboard

Show:

-   Welcome message
-   Total tools
-   Available tools
-   Borrowed tools
-   Pending requests
-   Active loans
-   Completed loans

### My Tools

Display:

-   Image
-   Tool name
-   Category
-   Condition
-   Location
-   Status
-   Daily rate
-   Edit
-   Deactivate/Delete

### Borrow Requests

Display:

-   Borrower name
-   Tool
-   Start date
-   Return date
-   Request date
-   Status
-   Approve
-   Reject

### Active Loans

Display:

-   Tool
-   Borrower
-   Start date
-   Expected return
-   Days remaining
-   Return status

### History

Show completed/rejected transactions.

All dashboard statistics must come from the database.

------------------------------------------------------------------------

# 11. Borrower Dashboard

Show:

-   Available tools count
-   Pending requests
-   Active borrowings
-   Completed borrowings
-   Total fines

### Browse Tools

Tool cards:

-   Image
-   Tool name
-   Category
-   Location
-   Condition
-   Availability
-   Daily rate
-   View Details

### My Requests

Show:

-   Tool
-   Start date
-   Return date
-   Status
-   Request date

### Current Borrowings

Show:

-   Tool
-   Lender
-   Start date
-   Expected return
-   Days remaining
-   Return action

### History

Show:

-   Tool
-   Dates
-   Completion status
-   Fine
-   Review action

------------------------------------------------------------------------

# 12. Tool Details

Show:

-   Large image
-   Tool name
-   Category
-   Description
-   Condition
-   Lender
-   Location
-   Availability
-   Daily rate
-   Average rating
-   Reviews

If available:

``` text
Request to Borrow
```

If unavailable:

``` text
Currently Unavailable
```

------------------------------------------------------------------------

# 13. Search and Filters

Search by:

-   Tool name
-   Category
-   Location

Filters:

-   Availability
-   Category
-   Condition
-   Price/rate range

Sort by:

-   Newest
-   Name
-   Rating
-   Price

Use backend filtering where practical.

Categories:

``` text
Construction
Electrical
Gardening
Cleaning
Automotive
Home Repair
Agriculture
Workshop
Safety Equipment
Other
```

------------------------------------------------------------------------

# 14. Tool Images

Allow lenders to add a tool image.

For version 1, either:

-   image URL, or
-   local file upload

If using local upload:

-   Validate image type.
-   Limit file size.
-   Use safe file names.
-   Store path/URL in database.

Avoid complicated cloud storage unless required.

------------------------------------------------------------------------

# 15. Reviews and Ratings

Only a borrower with a completed borrowing transaction can review that
tool.

Fields:

-   Rating 1--5
-   Comment

Prevent duplicate review for the same completed transaction.

Display average rating on tool details.

------------------------------------------------------------------------

# 16. Suggestions

Logged-in users can submit:

-   Subject
-   Message

Admin can:

-   View
-   Mark reviewed
-   Resolve
-   Add response

------------------------------------------------------------------------

# 17. Profile

Show:

-   Full name
-   Username
-   Email
-   Mobile
-   Address
-   Role

Allow safe profile updates.

Users must NOT be allowed to change their own role.

------------------------------------------------------------------------

# 18. Navigation

## Public

``` text
Home | About | Login | Register
```

## Lender

``` text
Dashboard
My Tools
Add Tool
Borrow Requests
Active Loans
History
Profile
Suggestions
Logout
```

## Borrower

``` text
Dashboard
Browse Tools
My Requests
Current Borrowings
History
Profile
Suggestions
Logout
```

## Admin

``` text
Dashboard
Users
Tools
Transactions
Reviews
Suggestions
Logout
```

------------------------------------------------------------------------

# 19. UI/UX

Make the application simple for users with different levels of education
and technical knowledge.

Requirements:

-   Responsive design
-   Mobile-friendly
-   Clear buttons
-   Large readable text
-   Simple labels
-   Consistent navigation
-   Form validation
-   Success/error messages
-   Loading indicators
-   Confirmation dialogs
-   Empty-state messages

Examples:

``` text
No tools available.
No pending requests.
Your request was submitted successfully.
```

Do not make fake/non-functional buttons.

------------------------------------------------------------------------

# 20. Suggested Pages

``` text
/
├── Home
├── About
├── Login
├── Register
│
├── lender/
│   ├── dashboard
│   ├── tools
│   ├── add-tool
│   ├── edit-tool
│   ├── requests
│   ├── active-loans
│   ├── history
│   └── profile
│
├── borrower/
│   ├── dashboard
│   ├── tools
│   ├── tool-details
│   ├── requests
│   ├── current-borrowings
│   ├── history
│   └── profile
│
└── admin/
    ├── dashboard
    ├── users
    ├── tools
    ├── transactions
    ├── reviews
    └── suggestions
```

------------------------------------------------------------------------

# 21. Backend Structure

Use a layered Spring Boot architecture:

``` text
backend/
└── src/main/java/com/toolshare/toolshare/

    ToolShareApplication.java

    config/
        SecurityConfig.java
        JwtConfig.java

    controller/
        AuthController.java
        UserController.java
        ToolController.java
        BorrowRequestController.java
        ReviewController.java
        SuggestionController.java
        AdminController.java

    dto/
        LoginRequest.java
        RegisterRequest.java
        AuthResponse.java
        ToolRequest.java
        BorrowRequestDto.java
        ReviewRequest.java

    entity/
        User.java
        Tool.java
        BorrowRequest.java
        Review.java
        Suggestion.java

    repository/
        UserRepository.java
        ToolRepository.java
        BorrowRequestRepository.java
        ReviewRepository.java
        SuggestionRepository.java

    service/
        AuthService.java
        UserService.java
        ToolService.java
        BorrowRequestService.java
        ReviewService.java
        SuggestionService.java

    security/
        JwtService.java
        JwtAuthenticationFilter.java
        CustomUserDetailsService.java

    exception/
        GlobalExceptionHandler.java
        ResourceNotFoundException.java
        BadRequestException.java
```

Adapt this to the existing project rather than destroying working code.

------------------------------------------------------------------------

# 22. REST API

## Auth

``` text
POST /api/auth/register
POST /api/auth/login
```

## User

``` text
GET /api/users/me
PUT /api/users/me
```

## Tools

``` text
GET    /api/tools
GET    /api/tools/{id}
POST   /api/tools
PUT    /api/tools/{id}
DELETE /api/tools/{id}
PATCH  /api/tools/{id}/availability
```

## Borrow Requests

``` text
POST   /api/borrow-requests
GET    /api/borrow-requests/my
GET    /api/borrow-requests/lender
PATCH  /api/borrow-requests/{id}/approve
PATCH  /api/borrow-requests/{id}/reject
PATCH  /api/borrow-requests/{id}/return
PATCH  /api/borrow-requests/{id}/cancel
```

## Reviews

``` text
POST /api/reviews
GET  /api/tools/{toolId}/reviews
```

## Suggestions

``` text
POST /api/suggestions
GET  /api/suggestions/my
```

## Admin

``` text
GET    /api/admin/users
PATCH  /api/admin/users/{id}/status
GET    /api/admin/tools
DELETE /api/admin/tools/{id}
GET    /api/admin/transactions
GET    /api/admin/suggestions
```

Use correct HTTP status codes:

``` text
200 OK
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

------------------------------------------------------------------------

# 23. Security

Mandatory:

-   Spring Security
-   BCrypt
-   JWT
-   Role-based authorization
-   Backend validation
-   DTOs
-   No plaintext passwords
-   Never return passwords
-   CORS configuration
-   Proper error handling
-   Ownership checks

For example:

-   Lender can edit only their own tools.
-   Borrower can view/manage only their own requests.
-   Admin-only APIs require ADMIN role.

Do not trust arbitrary user IDs sent by the frontend when authenticated
identity is available from JWT.

------------------------------------------------------------------------

# 24. application.properties

Use environment variables where practical:

``` properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/toolshare?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

app.jwt.secret=${JWT_SECRET:CHANGE_THIS_TO_A_LONG_RANDOM_SECRET}
app.jwt.expiration-ms=86400000
```

Never commit a real personal database password or production JWT secret.

------------------------------------------------------------------------

# 25. Database Files

Create:

``` text
database/
├── schema.sql
└── sample_data.sql
```

`schema.sql` must create all required tables.

`sample_data.sql` may contain safe demo data.

Never store plaintext passwords in SQL. Use BCrypt hashes.

------------------------------------------------------------------------

# 26. Error Response

Use a consistent API format.

Example error:

``` json
{
  "success": false,
  "message": "Tool not found"
}
```

Success:

``` json
{
  "success": true,
  "message": "Tool added successfully",
  "data": {}
}
```

------------------------------------------------------------------------

# 27. Frontend API Utility

Create a central API utility that:

-   Adds JWT Authorization header.
-   Handles JSON.
-   Handles API errors.
-   Handles expired authentication.
-   Redirects to login when appropriate.
-   Avoids duplicated fetch code.

Authorization:

``` text
Authorization: Bearer <JWT>
```

------------------------------------------------------------------------

# 28. Architecture

``` text
                 ┌─────────────────────┐
                 │        USER         │
                 │ Lender/Borrower/Admin│
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │      FRONTEND       │
                 │ HTML/CSS/JavaScript │
                 └──────────┬──────────┘
                            │ REST + JWT
                            ▼
                 ┌─────────────────────┐
                 │    SPRING BOOT      │
                 ├─────────────────────┤
                 │ Controllers         │
                 │ Services            │
                 │ Security/JWT        │
                 │ Validation          │
                 └──────────┬──────────┘
                            │ JPA/Hibernate
                            ▼
                 ┌─────────────────────┐
                 │       MySQL         │
                 │      toolshare      │
                 └─────────────────────┘
```

------------------------------------------------------------------------

# 29. Project Structure

Preferred final structure:

``` text
Resource Circulation System V2/
│
├── backend/
│   ├── pom.xml
│   └── src/
│
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── css/
│   ├── js/
│   ├── images/
│   └── pages/
│
├── database/
│   ├── schema.sql
│   └── sample_data.sql
│
├── docs/
│   ├── API.md
│   ├── DATABASE.md
│   └── ARCHITECTURE.md
│
├── PROJECT.md
└── README.md
```

If an existing working structure differs, inspect it first and adapt
rather than deleting useful files.

------------------------------------------------------------------------

# 30. Development Order

Build in this order:

## Phase 1

-   Inspect existing project.
-   Verify Java/Maven.
-   Verify MySQL.
-   Configure backend.

## Phase 2

-   Database.
-   Entities.
-   Repositories.
-   JPA.

## Phase 3

-   Registration.
-   BCrypt.
-   Login.
-   JWT.
-   Spring Security.
-   Role protection.

Do not move on until authentication works.

## Phase 4

Lender: - Dashboard - Add/edit/deactivate tools - Requests -
Approve/reject - Active loans - History

## Phase 5

Borrower: - Dashboard - Browse - Search/filter - Tool details -
Request - Current borrowing - Return - History

## Phase 6

-   Reviews
-   Ratings

## Phase 7

-   Suggestions

## Phase 8

-   Admin

## Phase 9

-   Responsive UI
-   Validation
-   Loading/error/empty states

## Phase 10

-   Full end-to-end testing

------------------------------------------------------------------------

# 31. Testing

Test:

## Authentication

-   Valid registration
-   Duplicate username
-   Duplicate email
-   Wrong password
-   Valid login
-   Invalid JWT
-   Role restrictions

## Tools

-   Add
-   Edit
-   Delete/deactivate
-   Ownership protection
-   Browse
-   Search/filter

## Borrowing

-   Valid request
-   Invalid dates
-   Duplicate request
-   Approve
-   Reject
-   Return
-   Fine calculation

## Reviews

-   Completed borrowing can review
-   Duplicate review prevented
-   Invalid rating rejected

## Suggestions

-   Submit
-   Admin view
-   Resolve

------------------------------------------------------------------------

# 32. Demo Workflow

Use this exact end-to-end demonstration.

### Lender

Register as LENDER.

Login.

Add:

``` text
Tool: Electric Drill
Category: Electrical
Condition: Good
Location: Chennai
Daily Rate: 100
```

### Borrower

Register as BORROWER.

Login.

Browse and open Electric Drill.

Select dates.

Send request.

### Lender

Login.

Open Borrow Requests.

Approve request.

### Borrower

Login/refresh.

See Active Borrowing.

Return tool.

### System

Set transaction:

``` text
COMPLETED
```

Set tool:

``` text
AVAILABLE
```

Calculate any late fine.

### Borrower

Submit:

``` text
Rating: 5
Review: Good tool and easy borrowing process.
```

This complete workflow must use the actual backend and MySQL database.

------------------------------------------------------------------------

# 33. College Project Explanation

## Problem Statement

People and organizations often purchase tools that are used only
occasionally. A resource circulation platform allows owners to share
tools with others and improves utilization of existing resources.

## Proposed Solution

A web-based Resource Circulation System connects lenders and borrowers
and manages tool listings, borrowing requests, approvals, returns,
fines, reviews, suggestions, and administration.

## Main Modules

1.  Authentication
2.  User Management
3.  Lender
4.  Borrower
5.  Tool Management
6.  Borrowing/Transaction
7.  Fine Calculation
8.  Review/Rating
9.  Suggestions
10. Admin

------------------------------------------------------------------------

# 34. Non-Functional Requirements

### Security

-   BCrypt
-   JWT
-   Spring Security
-   Role authorization

### Performance

-   Efficient queries
-   Pagination for large lists
-   Avoid unnecessary API calls

### Usability

-   Simple UI
-   Responsive design
-   Clear messages

### Maintainability

-   Layered architecture
-   DTOs
-   Services
-   Repositories
-   Controllers

### Reliability

-   Database constraints
-   Transaction management
-   Exception handling

------------------------------------------------------------------------

# 35. Future Enhancements

Not required for version 1:

-   Online payment
-   Email/SMS notifications
-   Push notifications
-   Maps/GPS
-   Nearby tools
-   Chat
-   QR-code handover
-   Identity verification
-   Cloud image storage
-   Android/iOS app
-   Recommendation system
-   Advanced analytics
-   Maintenance reminders

------------------------------------------------------------------------

# 36. Final Acceptance Checklist

The AI developer must verify:

-   [ ] Project builds successfully.
-   [ ] MySQL connection works.
-   [ ] Registration works.
-   [ ] Login works.
-   [ ] BCrypt is used.
-   [ ] JWT works.
-   [ ] LENDER role works.
-   [ ] BORROWER role works.
-   [ ] ADMIN role works.
-   [ ] Lender dashboard works.
-   [ ] Borrower dashboard works.
-   [ ] Admin dashboard works.
-   [ ] Lender can add/edit/deactivate tools.
-   [ ] Borrower can browse/search/filter.
-   [ ] Borrow request works.
-   [ ] Approval/rejection works.
-   [ ] Active borrowing works.
-   [ ] Return works.
-   [ ] Fine calculation works.
-   [ ] History works.
-   [ ] Reviews work.
-   [ ] Suggestions work.
-   [ ] Unauthorized APIs are blocked.
-   [ ] Responsive UI works.
-   [ ] No important button is fake.
-   [ ] README is complete.
-   [ ] SQL scripts are included.
-   [ ] End-to-end demo works.

------------------------------------------------------------------------

# 37. Instructions to the AI Developer / Antigravity

You are responsible for turning this specification into a complete
working project.

Before modifying anything:

1.  Inspect the entire existing repository.
2.  Understand the current backend, frontend, database, and
    configuration.
3.  Preserve working code.
4.  Do not unnecessarily rebuild or delete the project.
5.  Fix existing compilation/runtime problems first.
6.  Implement incrementally.
7.  Keep frontend and backend API contracts synchronized.
8.  Test each module before moving to the next.
9.  Do not use fake data for core features.
10. Do not expose passwords or secrets.
11. Keep code readable and maintainable.
12. Use beginner-friendly naming.
13. Update documentation when architecture changes.
14. Verify the complete user journey before declaring completion.

When finished, report:

-   Implemented features
-   Files created/modified
-   Database setup
-   Backend run command
-   Frontend run command
-   Demo accounts if created
-   Test results
-   Known limitations
-   Optional future enhancements

The goal is a **fully working end-to-end Resource Circulation System**,
not a visual mockup.
