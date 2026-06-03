package com.lifeguard.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable DTO using a Java 21 record.
 * Records provide compact constructor, equals/hashCode/toString automatically.
 */
public record PolicyRequest(
        @NotBlank String holderName,
        @Email @NotBlank String holderEmail,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Policy.PolicyType policyType,
        @NotNull @DecimalMin("1000.00") BigDecimal coverageAmount,
        @NotNull @DecimalMin("1.00") BigDecimal premiumAmount,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {}
