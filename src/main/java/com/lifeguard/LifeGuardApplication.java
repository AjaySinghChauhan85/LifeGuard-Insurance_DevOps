package com.lifeguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LifeGuard Insurance Platform — Java 21 + Spring Boot 3.3
 *
 * Key Java 21 features used across the codebase:
 *  - Records           : PolicyRequest, PolicyResponse, ApiError
 *  - Sealed interfaces : Policy.PolicyKind hierarchy
 *  - Pattern matching  : switch expressions, instanceof patterns
 *  - Text blocks       : multi-line JPQL queries, toString()
 *  - Virtual threads   : VirtualThreadConfig (Project Loom — stable in 21)
 *  - Stream.toList()   : unmodifiable list shorthand (Java 16+)
 */
@SpringBootApplication
public class LifeGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(LifeGuardApplication.class, args);
    }
}
