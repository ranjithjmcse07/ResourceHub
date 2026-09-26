-- ==========================================================
-- ToolShare Database Schema (Resource Circulation System)
-- Uses Spring JDBC / Direct SQL with MySQL
-- ==========================================================

CREATE DATABASE IF NOT EXISTS toolshare;
USE toolshare;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    mobile VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('LENDER', 'BORROWER', 'ADMIN') NOT NULL,
    company_name VARCHAR(100) DEFAULT NULL,
    address TEXT DEFAULT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Tools Table (with dual hourly and daily pricing)
CREATE TABLE IF NOT EXISTS tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lender_id BIGINT NOT NULL,
    tool_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    tool_condition ENUM('Brand New', 'Like New', 'Good', 'Fair') DEFAULT 'Good',
    location VARCHAR(100) NOT NULL,
    hourly_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    daily_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    image_url VARCHAR(500) DEFAULT NULL,
    availability_status ENUM('AVAILABLE', 'REQUESTED', 'BORROWED', 'MAINTENANCE', 'INACTIVE') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tools_lender FOREIGN KEY (lender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Borrow Requests / Transactions Table
CREATE TABLE IF NOT EXISTS borrow_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    rental_type ENUM('HOURLY', 'DAILY') NOT NULL DEFAULT 'DAILY',
    duration INT NOT NULL DEFAULT 1,
    start_time DATETIME NOT NULL,
    expected_return_time DATETIME NOT NULL,
    actual_return_time DATETIME DEFAULT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    fine_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'ACTIVE', 'RETURNED', 'COMPLETED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at DATETIME DEFAULT NULL,
    returned_at DATETIME DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    CONSTRAINT fk_requests_tool FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    CONSTRAINT fk_requests_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Reviews and Ratings Table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tool_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    borrow_request_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_tool FOREIGN KEY (tool_id) REFERENCES tools(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_request FOREIGN KEY (borrow_request_id) REFERENCES borrow_requests(id) ON DELETE CASCADE,
    CONSTRAINT uq_borrower_request_review UNIQUE (borrow_request_id)
);

-- 5. Suggestions / Feedback Table
CREATE TABLE IF NOT EXISTS suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subject VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    status ENUM('OPEN', 'REVIEWED', 'RESOLVED') DEFAULT 'OPEN',
    admin_response TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_suggestions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for fast searching and filtering
CREATE INDEX idx_tools_category ON tools(category);
CREATE INDEX idx_tools_location ON tools(location);
CREATE INDEX idx_tools_status ON tools(availability_status);
CREATE INDEX idx_requests_status ON borrow_requests(status);
