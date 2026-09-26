package com.toolshare.init;

import com.toolshare.dao.ToolDao;
import com.toolshare.dao.UserDao;
import com.toolshare.model.Tool;
import com.toolshare.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;
    private final ToolDao toolDao;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(JdbcTemplate jdbcTemplate, UserDao userDao, ToolDao toolDao, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        this.toolDao = toolDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking database tables and initialization...");

        // 1. Ensure all tables exist (TiDB and MySQL Cloud compatible DDL)
        createTablesIfNotExist();

        // 2. Seed demo users if empty
        User lender = seedUsersIfEmpty();

        // 3. Seed demo tools if empty
        seedToolsIfEmpty(lender);
    }

    private void createTablesIfNotExist() {
        try {
            log.info("Executing table creation checks...");

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    full_name VARCHAR(100) NOT NULL,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    mobile VARCHAR(20) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    company_name VARCHAR(100) DEFAULT NULL,
                    address TEXT DEFAULT NULL,
                    active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tools (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    lender_id BIGINT NOT NULL,
                    tool_name VARCHAR(150) NOT NULL,
                    category VARCHAR(50) NOT NULL,
                    description TEXT NOT NULL,
                    tool_condition VARCHAR(30) DEFAULT 'Good',
                    location VARCHAR(100) NOT NULL,
                    hourly_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                    daily_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                    image_url VARCHAR(500) DEFAULT NULL,
                    availability_status VARCHAR(30) DEFAULT 'AVAILABLE',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS borrow_requests (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    tool_id BIGINT NOT NULL,
                    borrower_id BIGINT NOT NULL,
                    rental_type VARCHAR(20) NOT NULL DEFAULT 'DAILY',
                    duration INT NOT NULL DEFAULT 1,
                    start_time DATETIME NOT NULL,
                    expected_return_time DATETIME NOT NULL,
                    actual_return_time DATETIME DEFAULT NULL,
                    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                    fine_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                    status VARCHAR(30) DEFAULT 'PENDING',
                    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    approved_at DATETIME DEFAULT NULL,
                    returned_at DATETIME DEFAULT NULL,
                    notes TEXT DEFAULT NULL
                )
            """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS reviews (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    tool_id BIGINT NOT NULL,
                    borrower_id BIGINT NOT NULL,
                    borrow_request_id BIGINT NOT NULL,
                    rating INT NOT NULL,
                    comment TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS suggestions (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    subject VARCHAR(150) NOT NULL,
                    message TEXT NOT NULL,
                    status VARCHAR(30) DEFAULT 'OPEN',
                    admin_response TEXT DEFAULT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);

            log.info("All 5 database tables verified/created successfully.");
        } catch (Exception e) {
            log.error("Notice during table creation: {}", e.getMessage());
        }
    }

    private User seedUsersIfEmpty() {
        User lender = null;
        try {
            if (!userDao.existsByUsername("admin")) {
                User admin = new User();
                admin.setFullName("Ranjith J M");
                admin.setUsername("admin");
                admin.setEmail("admin@toolshare.com");
                admin.setMobile("9876543210");
                admin.setPassword(passwordEncoder.encode("ranjith567"));
                admin.setRole("ADMIN");
                admin.setActive(true);
                userDao.createUser(admin);
                log.info("Created default ADMIN user: admin / ranjith567");
            }

            if (!userDao.existsByUsername("lender1")) {
                lender = new User();
                lender.setFullName("Ranjith Tools & Equipment");
                lender.setUsername("lender1");
                lender.setEmail("lender@toolshare.com");
                lender.setMobile("9876543211");
                lender.setPassword(passwordEncoder.encode("lender123"));
                lender.setRole("LENDER");
                lender.setCompanyName("Chennai ToolWorks Ltd.");
                lender.setAddress("12 Anna Salai, Chennai, Tamil Nadu");
                lender.setActive(true);
                lender = userDao.createUser(lender);
                log.info("Created default LENDER user: lender1 / lender123");
            } else {
                lender = userDao.findByUsername("lender1").orElse(null);
            }

            if (!userDao.existsByUsername("borrower1")) {
                User borrower = new User();
                borrower.setFullName("Karthik DIY Builder");
                borrower.setUsername("borrower1");
                borrower.setEmail("borrower@toolshare.com");
                borrower.setMobile("9876543212");
                borrower.setPassword(passwordEncoder.encode("borrower123"));
                borrower.setRole("BORROWER");
                borrower.setAddress("45 North Usman Road, T.Nagar, Chennai");
                borrower.setActive(true);
                userDao.createUser(borrower);
                log.info("Created default BORROWER user: borrower1 / borrower123");
            }
        } catch (Exception e) {
            log.warn("Notice during user seeding: {}", e.getMessage());
        }
        return lender;
    }

    private void seedToolsIfEmpty(User lender) {
        try {
            if (toolDao.countAll() == 0) {
                log.info("Tools table is empty, seeding catalog...");
                boolean seededFromFile = false;

                try {
                    ClassPathResource dataRes = new ClassPathResource("data.sql");
                    if (dataRes.exists()) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataRes.getInputStream(), StandardCharsets.UTF_8))) {
                            String line;
                            StringBuilder sql = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                String trimmed = line.trim();
                                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("USE ")) continue;
                                sql.append(line).append(" ");
                                if (trimmed.endsWith(";")) {
                                    String statement = sql.toString().trim();
                                    if (statement.endsWith(";")) {
                                        statement = statement.substring(0, statement.length() - 1);
                                    }
                                    jdbcTemplate.execute(statement);
                                    sql.setLength(0);
                                    seededFromFile = true;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("data.sql seed note: {}, falling back to programmatic seed", ex.getMessage());
                }

                if (!seededFromFile || toolDao.countAll() == 0) {
                    seedFallbackTools(lender);
                }
                log.info("Seeding complete. Total tools in database: {}", toolDao.countAll());
            }
        } catch (Exception e) {
            log.warn("Notice during tools seeding: {}", e.getMessage());
        }
    }

    private void seedFallbackTools(User lender) {
        if (lender == null) {
            lender = userDao.findByUsername("lender1").orElse(null);
            if (lender == null) return;
        }
        try {
            toolDao.createTool(new Tool(null, lender.getId(),
                    "Bosch Professional 18V Cordless Hammer Drill",
                    "Power Tools",
                    "Heavy-duty brushless motor with 2x 4.0Ah lithium batteries, quick charger, and 30-piece drill & driver bit set. Maximum torque 63 Nm. Ideal for drilling into concrete, brick, wood, and metal.",
                    "Like New",
                    "Bengaluru - Koramangala",
                    new BigDecimal("30.00"),
                    new BigDecimal("180.00"),
                    "https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            toolDao.createTool(new Tool(null, lender.getId(),
                    "DeWalt 12-Inch Sliding Compound Miter Saw",
                    "Power Tools",
                    "Integrated CUTLINE blade positioning system for adjustment-free cutline accuracy. Dual horizontal steel rails with innovative clamping mechanism.",
                    "Good",
                    "Mumbai - Andheri West",
                    new BigDecimal("50.00"),
                    new BigDecimal("320.00"),
                    "https://images.unsplash.com/photo-1572981779307-38b8cabb2407?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            toolDao.createTool(new Tool(null, lender.getId(),
                    "Kärcher K5 Premium Smart High Pressure Washer",
                    "Cleaning",
                    "2100 PSI water pressure with Smart Control Bluetooth gun. Perfect for rapid patio deep cleaning, siding wash, and car detailing.",
                    "Brand New",
                    "Delhi NCR - Connaught Place",
                    new BigDecimal("40.00"),
                    new BigDecimal("240.00"),
                    "https://images.unsplash.com/photo-1581578731548-c64695cc6952?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            toolDao.createTool(new Tool(null, lender.getId(),
                    "Werner 20-Foot Aluminum Telescoping Ladder",
                    "Construction",
                    "Commercial grade 300-lb duty rating with dual-action feet that pivot for use on hard or penetrable surfaces. Easily extends and locks securely.",
                    "Good",
                    "Hyderabad - Hitec City",
                    new BigDecimal("25.00"),
                    new BigDecimal("150.00"),
                    "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            toolDao.createTool(new Tool(null, lender.getId(),
                    "Stihl Gas-Powered Trimmer & Brushcutter",
                    "Gardening",
                    "High-efficiency 2-stroke engine with anti-vibration technology and bike handle for ergonomic lawn edging and brush clearing.",
                    "Good",
                    "Chennai - Central",
                    new BigDecimal("35.00"),
                    new BigDecimal("210.00"),
                    "https://images.unsplash.com/photo-1617576683096-00fc8eecb3af?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            toolDao.createTool(new Tool(null, lender.getId(),
                    "Makita Variable Speed Angle Grinder 4-1/2 Inch",
                    "Workshop",
                    "Compact 11-Amp motor with tool-free wheel guard and vibration-absorbing side handle. Includes 5 cutting and flap discs.",
                    "Like New",
                    "Pune - Kothrud",
                    new BigDecimal("20.00"),
                    new BigDecimal("120.00"),
                    "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            toolDao.createTool(new Tool(null, lender.getId(),
                    "Craftsman 450-Piece Mechanics Tool Chest Set",
                    "Automotive",
                    "Complete set of 1/4, 3/8, and 1/2-inch drive ratchets, deep and shallow sockets, combination wrenches, and magnetic nut setters in a 3-drawer lockable chest.",
                    "Brand New",
                    "Kolkata - Salt Lake",
                    new BigDecimal("45.00"),
                    new BigDecimal("260.00"),
                    "https://images.unsplash.com/photo-1530124566582-a618bc2615dc?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            toolDao.createTool(new Tool(null, lender.getId(),
                    "Stanley FatMax 1000A Jump Starter & Compressor",
                    "Automotive",
                    "Heavy-duty brass clamps for instant vehicle starting without requiring another car. Built-in 120 PSI air compressor with digital gauge.",
                    "Good",
                    "Ahmedabad - SG Highway",
                    new BigDecimal("25.00"),
                    new BigDecimal("140.00"),
                    "https://images.unsplash.com/photo-1486006920555-c77dce18193b?auto=format&fit=crop&w=800&q=80",
                    "AVAILABLE", null, null));

            log.info("Fallback sample tools seeded successfully!");
        } catch (Exception e) {
            log.warn("Notice during fallback tool seed: {}", e.getMessage());
        }
    }
}
