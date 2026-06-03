package com.lifeguard.controller;

import com.lifeguard.model.PolicyRequest;
import com.lifeguard.model.PolicyResponse;
import com.lifeguard.model.Policy;
import com.lifeguard.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PolicyController — Java 21 migration:
 *  - No Lombok; constructor injection manually.
 *  - Uses record-based request/response types.
 *  - Clean pattern-matching optional handling.
 */
@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.ok(policyService.createPolicy(request));
    }

    @GetMapping
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PolicyResponse>> getActivePolicies() {
        return ResponseEntity.ok(policyService.getActivePolicies());
    }

    @GetMapping("/{policyNumber}")
    public ResponseEntity<PolicyResponse> getPolicyByNumber(@PathVariable String policyNumber) {
        // Java 16+ pattern-matching map → clean Optional handling
        return policyService.getPolicyByNumber(policyNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/holder/{email}")
    public ResponseEntity<List<PolicyResponse>> getPoliciesByEmail(@PathVariable String email) {
        return ResponseEntity.ok(policyService.getPoliciesByEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyRequest request) {
        return ResponseEntity.ok(policyService.updatePolicy(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PolicyResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam Policy.PolicyStatus status) {
        return ResponseEntity.ok(policyService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
