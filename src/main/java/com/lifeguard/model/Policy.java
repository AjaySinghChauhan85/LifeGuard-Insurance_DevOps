package com.lifeguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Policy entity — Java 21: no Lombok needed; uses standard accessors.
 * Enums replaced by sealed interface hierarchy for exhaustive pattern matching.
 */
@Entity
@Table(name = "policies")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String policyNumber;

    @Column(nullable = false)
    @NotBlank
    private String holderName;

    @Column(nullable = false)
    @Email
    private String holderEmail;

    @Column(nullable = false)
    @Past
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType;

    @Column(nullable = false)
    @DecimalMin("1000.00")
    private BigDecimal coverageAmount;

    @Column(nullable = false)
    @DecimalMin("1.00")
    private BigDecimal premiumAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    // ── Sealed types for exhaustive switch expressions (Java 21) ────────────

    /**
     * Sealed interface for policy types — enables exhaustive pattern matching.
     */
    public sealed interface PolicyKind permits PolicyKind.Term, PolicyKind.WholeLife, PolicyKind.Endowment {
        record Term(int termYears) implements PolicyKind {}
        record WholeLife() implements PolicyKind {}
        record Endowment(LocalDate maturityDate) implements PolicyKind {}
    }

    /** Compute a human-readable summary using Java 21 pattern matching switch. */
    public static String describeKind(PolicyKind kind) {
        return switch (kind) {
            case PolicyKind.Term(int years)            -> "Term policy covering %d years".formatted(years);
            case PolicyKind.WholeLife()                -> "Whole-life policy with lifelong coverage";
            case PolicyKind.Endowment(var maturity)    -> "Endowment policy maturing on %s".formatted(maturity);
        };
    }

    // ── Enums (kept for JPA persistence) ────────────────────────────────────

    public enum PolicyType { TERM, WHOLE_LIFE, ENDOWMENT }
    public enum PolicyStatus { ACTIVE, LAPSED, CLAIMED, EXPIRED }

    // ── Standard accessors (no Lombok) ──────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public String getHolderEmail() { return holderEmail; }
    public void setHolderEmail(String holderEmail) { this.holderEmail = holderEmail; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public PolicyType getPolicyType() { return policyType; }
    public void setPolicyType(PolicyType policyType) { this.policyType = policyType; }

    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public void setCoverageAmount(BigDecimal coverageAmount) { this.coverageAmount = coverageAmount; }

    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(BigDecimal premiumAmount) { this.premiumAmount = premiumAmount; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public PolicyStatus getStatus() { return status; }
    public void setStatus(PolicyStatus status) { this.status = status; }

    public Policy() {}

    @Override
    public String toString() {
        // Java 21 text block for readable multi-line string
        return """
                Policy {
                  id           = %d
                  policyNumber = %s
                  holderName   = %s
                  holderEmail  = %s
                  type         = %s
                  status       = %s
                  coverage     = %s
                  premium      = %s
                }""".formatted(id, policyNumber, holderName, holderEmail,
                               policyType, status, coverageAmount, premiumAmount);
    }
}
