package com.cms;

import com.cms.entity.*;
import com.cms.repository.ComplaintHistoryRepository;
import com.cms.repository.ComplaintRepository;
import com.cms.repository.NotificationRepository;
import com.cms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class ComplaintManagementApplication {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintManagementApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ComplaintManagementApplication.class, args);
        logger.info("=================================================================");
        logger.info(" Online Complaint Management System Backend is running!");
        logger.info(" REST API Base URL: http://localhost:8080/api");
        logger.info("=================================================================");
    }

    /**
     * Seeds initial Admin and Sample User along with test complaints if empty.
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                     ComplaintRepository complaintRepository,
                                     com.cms.service.ComplaintService complaintService,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed Admin if not exists
            if (!userRepository.existsByEmail("admin@cms.com")) {
                User admin = new User(
                        "System Administrator",
                        "admin@cms.com",
                        "9876543210",
                        passwordEncoder.encode("admin123"),
                        Role.ROLE_ADMIN
                );
                userRepository.save(admin);
                logger.info("Created default Administrator account: admin@cms.com / admin123");
            }

            // Seed Demo Citizen User if not exists
            if (!userRepository.existsByEmail("user@cms.com")) {
                User citizen = new User(
                        "John Doe",
                        "user@cms.com",
                        "9123456780",
                        passwordEncoder.encode("user123"),
                        Role.ROLE_USER
                );
                userRepository.save(citizen);
                logger.info("Created default Citizen account: user@cms.com / user123");
            }

            if (!userRepository.existsByEmail("jane@cms.com")) {
                User citizen2 = new User(
                        "Jane Smith",
                        "jane@cms.com",
                        "9988776655",
                        passwordEncoder.encode("user123"),
                        Role.ROLE_USER
                );
                userRepository.save(citizen2);
                logger.info("Created second Citizen account: jane@cms.com / user123");
            }

            // Seed rich randomized sample complaints if empty
            if (complaintRepository.count() == 0) {
                complaintService.generateRandomComplaints(12);
                logger.info("Seeded 12 randomized complaints across diverse categories and statuses.");
            }
        };
    }
}
