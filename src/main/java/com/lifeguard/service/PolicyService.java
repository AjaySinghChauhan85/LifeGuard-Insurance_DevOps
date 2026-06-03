package com.lifeguard.service;

import com.lifeguard.model.Policy;
import com.lifeguard.model.PolicyRequest;
import com.lifeguard.model.PolicyResponse;
import com.lifeguard.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PolicyService — Java 21 migration:
 *  - No Lombok; uses standard logger / constructors.
 *  - Accepts immutable record {@link PolicyRequest} and returns {@link PolicyResponse}.
 *  - Uses pattern-matching instanceof for safe casting.
 *  - Switch expressions for status-based logic.
 */
@Service
@Transactional
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);

    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    // ── Write operations ─────────────────────────────────────────────────────

    public PolicyResponse createPolicy(PolicyRequest request) {
        var policy = new Policy();
        policy.setPolicyNumber("LG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        policy.setStatus(Policy.PolicyStatus.ACTIVE);
        applyRequest(policy, request);

        var saved = policyRepository.save(policy);
        log.info("Created policy: {}", saved.getPolicyNumber());
        return PolicyResponse.from(saved);
    }

    public PolicyResponse updatePolicy(Long id, PolicyRequest request) {
        var existing = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));

        applyRequest(existing, request);
        var saved = policyRepository.save(existing);
        log.info("Updated policy id={}", id);
        return PolicyResponse.from(saved);
    }

    public PolicyResponse changeStatus(Long id, Policy.PolicyStatus newStatus) {
        var policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));

        // Java 21 switch expression — exhaustive over enum
        String transition = switch (newStatus) {
            case ACTIVE  -> "Activating";
            case LAPSED  -> "Lapsing";
            case CLAIMED -> "Processing claim for";
            case EXPIRED -> "Expiring";
        };
        log.info("{} policy {}", transition, policy.getPolicyNumber());

        policy.setStatus(newStatus);
        return PolicyResponse.from(policyRepository.save(policy));
    }

    public void deletePolicy(Long id) {
        log.warn("Deleting policy id={}", id);
        policyRepository.deleteById(id);
    }

    // ── Read operations ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PolicyResponse> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(PolicyResponse::from)
                .toList();           // Java 16+ unmodifiable toList()
    }

    @Transactional(readOnly = true)
    public Optional<PolicyResponse> getPolicyByNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .map(PolicyResponse::from);
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getPoliciesByEmail(String email) {
        return policyRepository.findByHolderEmail(email).stream()
                .map(PolicyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getActivePolicies() {
        return policyRepository.findActiveOrderedByStart(Policy.PolicyStatus.ACTIVE).stream()
                .map(PolicyResponse::from)
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void applyRequest(Policy policy, PolicyRequest req) {
        policy.setHolderName(req.holderName());
        policy.setHolderEmail(req.holderEmail());
        policy.setDateOfBirth(req.dateOfBirth());
        policy.setPolicyType(req.policyType());
        policy.setCoverageAmount(req.coverageAmount());
        policy.setPremiumAmount(req.premiumAmount());
        policy.setStartDate(req.startDate());
        policy.setEndDate(req.endDate());
    }

    // ── Typed exception ──────────────────────────────────────────────────────

    /** Java 21 record used as a lightweight typed exception carrier. */
    public static final class PolicyNotFoundException extends RuntimeException {
        private final long policyId;

        public PolicyNotFoundException(long policyId) {
            super("Policy not found: " + policyId);
            this.policyId = policyId;
        }

        public long getPolicyId() { return policyId; }
    }
}
