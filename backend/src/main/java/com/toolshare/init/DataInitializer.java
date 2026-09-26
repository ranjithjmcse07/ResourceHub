package com.toolshare.init;

import com.toolshare.dao.ToolDao;
import com.toolshare.dao.UserDao;
import com.toolshare.model.Tool;
import com.toolshare.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final ToolDao toolDao;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DataSource dataSource, UserDao userDao, ToolDao toolDao, PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.toolDao = toolDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking database schema and initialization...");

        // 1. Ensure database schema is created before running any queries
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.setContinueOnError(true);
            populator.setIgnoreFailedDrops(true);
            populator.execute(dataSource);
            log.info("Database schema checked/created successfully.");
        } catch (Exception e) {
            log.warn("Notice during schema setup: {}", e.getMessage());
        }

        // 2. Seed demo users if empty
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

        // 3. Seed demo tools if empty
        try {
            if (toolDao.countAll() == 0) {
                log.info("Seeding tools into database...");
                try {
                    ResourceDatabasePopulator dataPopulator = new ResourceDatabasePopulator();
                    dataPopulator.addScript(new ClassPathResource("data.sql"));
                    dataPopulator.setContinueOnError(true);
                    dataPopulator.execute(dataSource);
                    log.info("Tools seeded successfully from data.sql!");
                } catch (Exception dataEx) {
                    log.warn("data.sql execution note: {}, falling back to programmatic seed", dataEx.getMessage());
                    seedFallbackTools(lender);
                }
            }
        } catch (Exception e) {
            log.warn("Notice during tools seeding: {}", e.getMessage());
        }
    }

    private void seedFallbackTools(User lender) {
        if (lender == null) return;
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
