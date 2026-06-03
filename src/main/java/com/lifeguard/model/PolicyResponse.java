package com.lifeguard.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable response DTO — Java 21 record.
 * Static factory maps from entity to avoid exposing the entity directly.
 */
public record PolicyResponse(
        Long id,
        String policyNumber,
        String holderName,
        String holderEmail,
        LocalDate dateOfBirth,
        Policy.PolicyType policyType,
        BigDecimal coverageAmount,
        BigDecimal premiumAmount,
        LocalDate startDate,
        LocalDate endDate,
        Policy.PolicyStatus status
) {
    /** Static factory — named constructor pattern. */
    public static PolicyResponse from(Policy p) {
        return new PolicyResponse(
                p.getId(),
                p.getPolicyNumber(),
                p.getHolderName(),
                p.getHolderEmail(),
                p.getDateOfBirth(),
                p.getPolicyType(),
                p.getCoverageAmount(),
                p.getPremiumAmount(),
                p.getStartDate(),
                p.getEndDate(),
                p.getStatus()
        );
    }
}
