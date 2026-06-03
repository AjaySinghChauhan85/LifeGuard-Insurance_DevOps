package com.lifeguard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Virtual Thread configuration — Java 21 stable feature.
 *
 * Virtual threads (Project Loom) replace platform threads for I/O-bound work
 * such as database queries and HTTP calls, delivering high throughput without
 * tuning thread-pool sizes.
 *
 * Spring Boot 3.2+ auto-configures Tomcat with virtual threads when
 * {@code spring.threads.virtual.enabled=true} is set in application.yml.
 * This bean adds an explicit executor for @Async tasks.
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * Executor backed by Java 21 virtual threads.
     * Used by Spring's @Async and @Scheduled methods.
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        // Executors.newVirtualThreadPerTaskExecutor() is a stable Java 21 API
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Named task executor using virtual threads — wired into Spring's
     * async infrastructure.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
