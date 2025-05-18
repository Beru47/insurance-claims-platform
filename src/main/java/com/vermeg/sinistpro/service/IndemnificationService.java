/*package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.repository.SinistreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class IndemnificationService {
    private final SinistreRepository sinistreRepository;

    public IndemnificationService(SinistreRepository sinistreRepository) {
        this.sinistreRepository = sinistreRepository;
    }

    public Sinistre calculateIndemnity(Long sinistreId) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found"));

        BigDecimal indemnity = calculateBaseIndemnity(sinistre);
        indemnity = applyActuarialAdjustment(indemnity, sinistre);
        indemnity = optimizePayment(indemnity, sinistre);

        sinistre.setMontantIndemnisation(indemnity);
        return sinistreRepository.save(sinistre);
    }

    private BigDecimal calculateBaseIndemnity(Sinistre sinistre) {
        Policy policy = sinistre.getPolicy();
        BigDecimal baseAmount = BigDecimal.ZERO;

        if (policy.isValid() && policy.getGuarantees().stream()
                .anyMatch(g -> g.contains(sinistre.getType().toLowerCase()))) {
            switch (sinistre.getType().toLowerCase()) {
                case "incendie":
                    baseAmount = BigDecimal.valueOf(5000);
                    break;
                case "accident":
                    baseAmount = BigDecimal.valueOf(1000);
                    break;
                default:
                    baseAmount = BigDecimal.valueOf(500);
            }
            // Damage estimate from description
            if (sinistre.getDescription() != null && sinistre.getDescription().toLowerCase().contains("severe")) {
                baseAmount = baseAmount.multiply(BigDecimal.valueOf(1.5));
            }
        }
        return baseAmount;
    }

    private BigDecimal applyActuarialAdjustment(BigDecimal baseAmount, Sinistre sinistre) {
        // Adjust based on claim frequency for the client
        long pastClaims = sinistreRepository.findByPolicyClientId(sinistre.getPolicy().getClient().getId()).size();
        if (pastClaims > 3) {
            return baseAmount.multiply(BigDecimal.valueOf(0.9)); // 10% reduction
        }
        return baseAmount.multiply(BigDecimal.valueOf(1.1)); // 10% increase for future costs
    }

    private BigDecimal optimizePayment(BigDecimal amount, Sinistre sinistre) {
        // Optimization: cap based on priority
        BigDecimal maxPayout = sinistre.getPriorityScore() >= 80 ? BigDecimal.valueOf(15000) : BigDecimal.valueOf(10000);
        return amount.compareTo(maxPayout) > 0 ? maxPayout : amount;
    }
}*/



/*package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Assure;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.repository.SinistreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class IndemnificationService {
    private final SinistreRepository sinistreRepository;

    public IndemnificationService(SinistreRepository sinistreRepository) {
        this.sinistreRepository = sinistreRepository;
    }

    public Sinistre calculateIndemnity(Long sinistreId) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found"));

        BigDecimal indemnity = calculateBaseIndemnity(sinistre);
        indemnity = applyActuarialAdjustment(indemnity, sinistre);
        indemnity = optimizePayment(indemnity, sinistre);

        sinistre.setMontantIndemnisation(indemnity);
        return sinistreRepository.save(sinistre);
    }

    private BigDecimal calculateBaseIndemnity(Sinistre sinistre) {
        Assure assure = sinistre.getAssure();
        BigDecimal baseAmount = BigDecimal.ZERO;

        if (assure.isPolicyValid() && assure.getGuarantees().contains(sinistre.getType().toLowerCase() + "_coverage")) {
            switch (sinistre.getType().toLowerCase()) {
                case "incendie":
                    baseAmount = BigDecimal.valueOf(5000);
                    break;
                case "accident":
                    baseAmount = BigDecimal.valueOf(1000);
                    break;
                default:
                    baseAmount = BigDecimal.valueOf(500);
            }
            // Damage estimate from description
            if (sinistre.getDescription() != null && sinistre.getDescription().toLowerCase().contains("severe")) {
                baseAmount = baseAmount.multiply(BigDecimal.valueOf(1.5));
            }
        }
        return baseAmount;
    }

    private BigDecimal applyActuarialAdjustment(BigDecimal baseAmount, Sinistre sinistre) {
        // Simple actuarial model: adjust based on claim frequency
        long pastClaims = sinistre.getAssure().getSinistres().size();
        if (pastClaims > 3) {
            return baseAmount.multiply(BigDecimal.valueOf(0.9)); // 10% reduction
        }
        return baseAmount.multiply(BigDecimal.valueOf(1.1)); // 10% increase for future costs
    }

    private BigDecimal optimizePayment(BigDecimal amount, Sinistre sinistre) {
        // Optimization: cap based on priority
        BigDecimal maxPayout = sinistre.getPriorityScore() >= 80 ? BigDecimal.valueOf(15000) : BigDecimal.valueOf(10000);
        return amount.compareTo(maxPayout) > 0 ? maxPayout : amount;
    }
}*/