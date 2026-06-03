package com.lifeguard.repository;

import com.lifeguard.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByHolderEmail(String email);

    List<Policy> findByStatus(Policy.PolicyStatus status);

    /** Java 21 text-block-friendly JPQL — cleaner multi-line query. */
    @Query("""
            SELECT p FROM Policy p
            WHERE p.status = :status
            ORDER BY p.startDate DESC
            """)
    List<Policy> findActiveOrderedByStart(Policy.PolicyStatus status);
}
