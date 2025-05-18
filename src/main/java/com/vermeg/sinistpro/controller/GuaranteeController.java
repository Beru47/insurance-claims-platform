package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.Guarantee;
import com.vermeg.sinistpro.service.GuaranteeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/guarantees")
public class GuaranteeController {

    private final GuaranteeService guaranteeService;

    public GuaranteeController(GuaranteeService guaranteeService) {
        this.guaranteeService = guaranteeService;
    }

    @PostMapping
    public ResponseEntity<GuaranteeDTO> createGuarantee(@RequestBody GuaranteeDTO request) {
        Guarantee guarantee = new Guarantee();
        guarantee.setName(request.getName());
        guarantee.setDescription(request.getDescription());
        guarantee.setType(request.getType());
        guarantee.setCoverageAmount(request.getCoverageAmount());
        Guarantee createdGuarantee = guaranteeService.createGuarantee(guarantee);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdGuarantee));
    }

    @PostMapping("/{guaranteeId}/policies/{policyId}")
    public ResponseEntity<GuaranteeDTO> associateGuaranteeWithPolicy(
            @PathVariable Long guaranteeId, @PathVariable Long policyId) {
        Guarantee guarantee = guaranteeService.associateGuaranteeWithPolicy(guaranteeId, policyId);
        return ResponseEntity.ok(toDTO(guarantee));
    }

    @GetMapping("/policies/{policyId}")
    public ResponseEntity<List<GuaranteeDTO>> getGuaranteesByPolicyId(@PathVariable Long policyId) {
        List<Guarantee> guarantees = guaranteeService.getGuaranteesByPolicyId(policyId);
        List<GuaranteeDTO> guaranteeDTOs = guarantees.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(guaranteeDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuaranteeDTO> getGuarantee(@PathVariable Long id) {
        Guarantee guarantee = guaranteeService.getGuarantee(id);
        return ResponseEntity.ok(toDTO(guarantee));
    }

    @GetMapping
    public ResponseEntity<List<GuaranteeDTO>> getAllGuarantees() {
        List<Guarantee> guarantees = guaranteeService.getAllGuarantees();
        List<GuaranteeDTO> guaranteeDTOs = guarantees.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(guaranteeDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GuaranteeDTO> updateGuarantee(@PathVariable Long id, @RequestBody GuaranteeDTO request) {
        Guarantee guarantee = new Guarantee();
        guarantee.setName(request.getName());
        guarantee.setDescription(request.getDescription());
        guarantee.setType(request.getType());
        guarantee.setCoverageAmount(request.getCoverageAmount());
        Guarantee updatedGuarantee = guaranteeService.updateGuarantee(id, guarantee);
        return ResponseEntity.ok(toDTO(updatedGuarantee));
    }

    @DeleteMapping("/{guaranteeId}/policies/{policyId}")
    public ResponseEntity<Void> removeGuaranteeFromPolicy(
            @PathVariable Long guaranteeId, @PathVariable Long policyId) {
        guaranteeService.removeGuaranteeFromPolicy(guaranteeId, policyId);
        return ResponseEntity.noContent().build();
    }

    private GuaranteeDTO toDTO(Guarantee guarantee) {
        GuaranteeDTO dto = new GuaranteeDTO();
        dto.setId(guarantee.getId());
        dto.setName(guarantee.getName());
        dto.setDescription(guarantee.getDescription());
        dto.setType(guarantee.getType());
        dto.setCoverageAmount(guarantee.getCoverageAmount());
        return dto;
    }

    static class GuaranteeDTO {
        private Long id;
        private String name;
        private String description;
        private String type;
        private BigDecimal coverageAmount;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public BigDecimal getCoverageAmount() {
            return coverageAmount;
        }

        public void setCoverageAmount(BigDecimal coverageAmount) {
            this.coverageAmount = coverageAmount;
        }
    }
}