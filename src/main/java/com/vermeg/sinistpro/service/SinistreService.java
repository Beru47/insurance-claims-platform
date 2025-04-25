package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.exception.SinistreException;
import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreRequest;
import com.vermeg.sinistpro.repository.PolicyRepository;
import com.vermeg.sinistpro.repository.SinistreRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SinistreService {
    private static final Map<String, Double> TYPE_SEVERITY = new HashMap<>();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    static {
        TYPE_SEVERITY.put("incendie", 0.9);
        TYPE_SEVERITY.put("accident", 0.7);
        TYPE_SEVERITY.put("theft", 0.5);
        TYPE_SEVERITY.put("flood", 0.6);
        TYPE_SEVERITY.put("vandalism", 0.4);
        TYPE_SEVERITY.put("breakdown", 0.3);
        TYPE_SEVERITY.put("other", 0.3);
    }

    private final SinistreRepository repo;
    private final PolicyRepository policyRepository;
    private final JdbcTemplate jdbcTemplate;
    private final GammaDistribution gammaDist = new GammaDistribution(2.0, 1.0);
    @Value("${sinistre.secret-key}")
    private String secretKey;

    public SinistreService(SinistreRepository repo, PolicyRepository policyRepository, JdbcTemplate jdbcTemplate) {
        this.repo = repo;
        this.policyRepository = policyRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        // Create the sequence if it doesn't exist
        try {
            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS sinistre_number_seq START 1 INCREMENT 1");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sinistre_number_seq sequence: " + e.getMessage());
        }
    }

    public Sinistre créerSinistre(SinistreRequest request) {
        Optional<Policy> policyOpt = policyRepository.findById(request.getPolicyId());
        if (policyOpt.isEmpty()) {
            throw new SinistreException("Policy not found");
        }
        Policy policy = policyOpt.get();
        if (!policy.getTypeAssurance().toLowerCase().contains(request.getType().toLowerCase())) {
            throw new SinistreException("Claim type does not match policy type");
        }
        int priority = calculerPriorityScore(request, policy);
        Sinistre sinistre = new Sinistre();
        sinistre.setType(request.getType());
        sinistre.setDate(request.getDate());
        sinistre.setLieu(request.getLieu());
        sinistre.setDescription(request.getDescription());
        sinistre.setStatus(priority >= 80 ? ClaimStatus.URGENT : ClaimStatus.PENDING);
        sinistre.setPriorityScore(priority);
        sinistre.setPolicy(policy);
        sinistre.setNumeroSinistre(generateNumeroSinistre());
        sinistre.setExpert(null);
        sinistre.setAdmin(null);
        sinistre.setMontantIndemnisation(sinistre.calculerMontantIndemnisation());
        return repo.save(sinistre);
    }

    public List<Sinistre> getAllSinistres() {
        return repo.findAll();
    }

    public Sinistre updateStatus(Long id, ClaimStatus newStatus) {
        Sinistre sinistre = repo.findById(id)
                .orElseThrow(() -> new SinistreException("Claim not found"));
        sinistre.setStatus(newStatus);
        return repo.save(sinistre);
    }

    public Sinistre consulterStatut(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new SinistreException("Sinistre introuvable"));
    }

    private int calculerPriorityScore(SinistreRequest request, Policy policy) {
        String typeLower = request.getType() != null ? request.getType().toLowerCase() : "other";
        double typeSeverity = TYPE_SEVERITY.getOrDefault(typeLower, 0.3);
        double highRiskLocation = request.getLieu() != null &&
                (request.getLieu().toLowerCase().contains("urban") ||
                        request.getLieu().toLowerCase().contains("dangereuse") ||
                        request.getLieu().toLowerCase().contains("industrial")) ? 1.0 : 0.0;
        double highSeverity = request.getDescription() != null &&
                (request.getDescription().toLowerCase().contains("severe") ||
                        request.getDescription().toLowerCase().contains("urgent") ||
                        request.getDescription().toLowerCase().contains("major")) ? 1.0 : 0.0;

        double typeLocationRisk = typeSeverity * highRiskLocation;
        double severityLocation = highSeverity * highRiskLocation;

        List<Sinistre> pastClaims = repo.findByPolicyClientId(policy.getClient().getId());
        double historicalRisk = calculateHistoricalRisk(pastClaims, request.getDate());

        double coverageBonus = policy.getGuarantees() != null &&
                policy.getGuarantees().stream().anyMatch(g -> g.contains(typeLower)) ? 0.5 : 0.0;

        double logit = -3.0 + 2.0 * typeSeverity + 0.8 * highRiskLocation +
                0.7 * highSeverity + 1.2 * typeLocationRisk +
                0.6 * severityLocation + 0.4 * historicalRisk + coverageBonus;
        double logisticProb = 1.0 / (1.0 + Math.exp(-logit));

        double prior = typeSeverity + 0.1 * highSeverity;
        prior = Math.min(prior, 1.0);

        double posterior = (prior * logisticProb) /
                (prior * logisticProb + (1 - prior) * (1 - logisticProb));

        double rawScore = posterior * 10;
        double normalizedScore = 100.0 / (1.0 + Math.exp(-rawScore + 5.0));

        return (int) normalizedScore;
    }

    private double calculateHistoricalRisk(List<Sinistre> pastClaims, LocalDateTime currentDate) {
        if (pastClaims.isEmpty()) return 0.0;

        double weightedCount = 0.0;
        double decayRate = 0.01;
        for (Sinistre claim : pastClaims) {
            long daysDiff = ChronoUnit.DAYS.between(claim.getDate(), currentDate);
            double weight = Math.exp(-decayRate * daysDiff);
            weightedCount += weight;
        }

        return gammaDist.cumulativeProbability(weightedCount) * 10.0;
    }

    private String generateNumeroSinistre() {
        try {
            String year = String.valueOf(LocalDateTime.now().getYear());
            Long sequence = jdbcTemplate.queryForObject("SELECT nextval('sinistre_number_seq')", Long.class);
            String input = sequence + ":" + year + ":" + secretKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String hex = String.format("%02x", new BigInteger(1, hashBytes)).substring(0, 10);
            BigInteger num = new BigInteger(hex, 16);
            BigInteger max = new BigInteger("3656158440062976"); // 36^10
            num = num.mod(max);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                int index = num.mod(BigInteger.valueOf(36)).intValue();
                result.append(CHARACTERS.charAt(index));
                num = num.divide(BigInteger.valueOf(36));
            }
            String numeroSinistre = result.reverse().toString();
            if (repo.findByNumeroSinistre(numeroSinistre).isPresent()) {
                return generateNumeroSinistre();
            }
            return numeroSinistre;
        } catch (NoSuchAlgorithmException e) {
            throw new SinistreException("Failed to generate numeroSinistre: " + e.getMessage());
        }
    }
}

/* package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.exception.SinistreException;
import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreRequest;
import com.vermeg.sinistpro.repository.PolicyRepository;
import com.vermeg.sinistpro.repository.SinistreRepository;
import lombok.Value;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class SinistreService {
    private static final Map<String, Double> TYPE_SEVERITY = new HashMap<>();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SINISTRE_NUMBER_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    static {
        TYPE_SEVERITY.put("incendie", 0.9);
        TYPE_SEVERITY.put("accident", 0.7);
        TYPE_SEVERITY.put("theft", 0.5);
        TYPE_SEVERITY.put("flood", 0.6);
        TYPE_SEVERITY.put("vandalism", 0.4);
        TYPE_SEVERITY.put("breakdown", 0.3);
        TYPE_SEVERITY.put("other", 0.3);
    }

    private final JdbcTemplate jdbcTemplate;
    private final SinistreRepository repo;
    private final PolicyRepository policyRepository;
    private final GammaDistribution gammaDist = new GammaDistribution(2.0, 1.0);
    @Value("${sinistre.secret-key}")
    private String secretKey;

    public SinistreService(SinistreRepository repo, PolicyRepository policyRepository, JdbcTemplate jdbcTemplate) {
        this.repo = repo;
        this.policyRepository = policyRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Sinistre créerSinistre(SinistreRequest request) {
        Optional<Policy> policyOpt = policyRepository.findById(request.getPolicyId());
        if (policyOpt.isEmpty()) {
            throw new SinistreException("Policy not found");
        }
        Policy policy = policyOpt.get();
        if (!policy.getTypeAssurance().toLowerCase().contains(request.getType().toLowerCase())) {
            throw new SinistreException("Claim type does not match policy type");
        }
        int priority = calculerPriorityScore(request, policy);
        Sinistre sinistre = new Sinistre();
        sinistre.setType(request.getType());
        sinistre.setDate(request.getDate());
        sinistre.setLieu(request.getLieu());
        sinistre.setDescription(request.getDescription());
        sinistre.setStatus(priority >= 80 ? ClaimStatus.URGENT : ClaimStatus.PENDING);
        sinistre.setPriorityScore(priority);
        sinistre.setPolicy(policy);
        sinistre.setNumeroSinistre(generateNumeroSinistre());
        sinistre.setExpert(null);
        sinistre.setAdmin(null);
        sinistre.setMontantIndemnisation(sinistre.calculerMontantIndemnisation());
        return repo.save(sinistre);
    }

    public List<Sinistre> getAllSinistres() {
        return repo.findAll();
    }

    public Sinistre updateStatus(Long id, ClaimStatus newStatus) {
        Sinistre sinistre = repo.findById(id)
                .orElseThrow(() -> new SinistreException("Claim not found"));
        sinistre.setStatus(newStatus);
        return repo.save(sinistre);
    }

    public Sinistre consulterStatut(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new SinistreException("Sinistre introuvable"));
    }

    private int calculerPriorityScore(SinistreRequest request, Policy policy) {
        String typeLower = request.getType() != null ? request.getType().toLowerCase() : "other";
        double typeSeverity = TYPE_SEVERITY.getOrDefault(typeLower, 0.3);
        double highRiskLocation = request.getLieu() != null &&
                (request.getLieu().toLowerCase().contains("urban") ||
                        request.getLieu().toLowerCase().contains("dangereuse") ||
                        request.getLieu().toLowerCase().contains("industrial")) ? 1.0 : 0.0;
        double highSeverity = request.getDescription() != null &&
                (request.getDescription().toLowerCase().contains("severe") ||
                        request.getDescription().toLowerCase().contains("urgent") ||
                        request.getDescription().toLowerCase().contains("major")) ? 1.0 : 0.0;

        double typeLocationRisk = typeSeverity * highRiskLocation;
        double severityLocation = highSeverity * highRiskLocation;

        List<Sinistre> pastClaims = repo.findByPolicyClientId(policy.getClient().getId());
        double historicalRisk = calculateHistoricalRisk(pastClaims, request.getDate());

        double coverageBonus = policy.getGuarantees() != null &&
                policy.getGuarantees().stream().anyMatch(g -> g.contains(typeLower)) ? 0.5 : 0.0;

        double logit = -3.0 + 2.0 * typeSeverity + 0.8 * highRiskLocation +
                0.7 * highSeverity + 1.2 * typeLocationRisk +
                0.6 * severityLocation + 0.4 * historicalRisk + coverageBonus;
        double logisticProb = 1.0 / (1.0 + Math.exp(-logit));

        double prior = typeSeverity + 0.1 * highSeverity;
        prior = Math.min(prior, 1.0);

        double posterior = (prior * logisticProb) /
                (prior * logisticProb + (1 - prior) * (1 - logisticProb));

        double rawScore = posterior * 10;
        double normalizedScore = 100.0 / (1.0 + Math.exp(-rawScore + 5.0));

        return (int) normalizedScore;
    }

    private double calculateHistoricalRisk(List<Sinistre> pastClaims, LocalDateTime currentDate) {
        if (pastClaims.isEmpty()) return 0.0;

        double weightedCount = 0.0;
        double decayRate = 0.01;
        for (Sinistre claim : pastClaims) {
            long daysDiff = ChronoUnit.DAYS.between(claim.getDate(), currentDate);
            double weight = Math.exp(-decayRate * daysDiff);
            weightedCount += weight;
        }

        return gammaDist.cumulativeProbability(weightedCount) * 10.0;
    }

    private String generateNumeroSinistre() {
        try {
            String year = String.valueOf(LocalDateTime.now().getYear());
            Long sequence = jdbcTemplate.queryForObject("SELECT nextval('sinistre_number_seq')", Long.class);
            String input = sequence + ":" + year + ":" + secretKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String hex = String.format("%02x", new BigInteger(1, hashBytes)).substring(0, 10);
            BigInteger num = new BigInteger(hex, 16);
            BigInteger max = new BigInteger("3656158440062976"); // 36^10
            num = num.mod(max);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                int index = num.mod(BigInteger.valueOf(36)).intValue();
                result.append(CHARACTERS.charAt(index));
                num = num.divide(BigInteger.valueOf(36));
            }
            String numeroSinistre = result.reverse().toString();
            if (repo.findByNumeroSinistre(numeroSinistre).isPresent()) {
                return generateNumeroSinistre();
            }
            return numeroSinistre;
        } catch (NoSuchAlgorithmException e) {
            throw new SinistreException("Failed to generate numeroSinistre: " + e.getMessage());
        }
    }

    /*private String generateNumeroSinistre() {
        StringBuilder number = new StringBuilder(SINISTRE_NUMBER_LENGTH);
        for (int i = 0; i < SINISTRE_NUMBER_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            number.append(CHARACTERS.charAt(index));
        }
        String result = number.toString();
        int maxRetries = 3;
        int attempts = 0;
        while (repo.findByNumeroSinistre(result).isPresent() && attempts < maxRetries) {
            number.setLength(0);
            for (int i = 0; i < SINISTRE_NUMBER_LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                number.append(CHARACTERS.charAt(index));
            }
            result = number.toString();
            attempts++;
        }
        if (attempts >= maxRetries) {
            throw new SinistreException("Unable to generate unique numeroSinistre after " + maxRetries + " attempts");
        }
        return result;
    }



} */












/*package com.vermeg.sinistpro.service;

//import com.vermeg.sinistpro.event.ClaimEventPublisher;

import com.vermeg.sinistpro.exception.SinistreException;
import com.vermeg.sinistpro.model.Assure;
import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreRequest;
import com.vermeg.sinistpro.repository.AssureRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import com.vermeg.sinistpro.repository.SinistreRepository;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SinistreService {
    private static final Map<String, Double> TYPE_SEVERITY = new HashMap<>();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SINISTRE_NUMBER_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    static {
        TYPE_SEVERITY.put("incendie", 0.9);
        TYPE_SEVERITY.put("accident", 0.7);
        TYPE_SEVERITY.put("theft", 0.5);
        TYPE_SEVERITY.put("flood", 0.6);
        TYPE_SEVERITY.put("vandalism", 0.4);
        TYPE_SEVERITY.put("breakdown", 0.3);
        TYPE_SEVERITY.put("other", 0.3);
    }

    private final PolicyRepository policyRepository;
    private final SinistreRepository repo;
    private final AssureRepository assureRepository;
    private final GammaDistribution gammaDist = new GammaDistribution(2.0, 1.0);

    public SinistreService(SinistreRepository repo, AssureRepository assureRepository) {
        this.repo = repo;
        this.assureRepository = assureRepository;
    }

    public Sinistre créerSinistre(SinistreRequest request) {
        Assure assure = assureRepository.findById(request.getAssureId())
                .orElseThrow(() -> new SinistreException("Assure not found"));
        int priority = calculerPriorityScore(request, assure);
        Sinistre sinistre = new Sinistre();
        sinistre.setType(request.getType());
        sinistre.setDate(request.getDate());
        sinistre.setLieu(request.getLieu());
        sinistre.setDescription(request.getDescription());
        sinistre.setStatus(priority >= 80 ? ClaimStatus.URGENT : ClaimStatus.PENDING);
        sinistre.setPriorityScore(priority);
        sinistre.setpolicy(policy);
        return repo.save(sinistre);
    }

    public List<Sinistre> getAllSinistres() {
        return repo.findAll();
    }

    public Sinistre updateStatus(Long id, ClaimStatus newStatus) {
        Sinistre sinistre = repo.findById(id)
                .orElseThrow(() -> new SinistreException("Claim not found"));
        sinistre.setStatus(newStatus);
        return repo.save(sinistre);
    }

    public Sinistre consulterStatut(Long id) {
        return repo.findById(id).orElseThrow(() -> new SinistreException("Sinistre introuvable"));
    }

    private int calculerPriorityScore(SinistreRequest request, Assure assure) {
        String typeLower = request.getType() != null ? request.getType().toLowerCase() : "other";
        double typeSeverity = TYPE_SEVERITY.getOrDefault(typeLower, 0.3);
        double highRiskLocation = request.getLieu() != null &&
                (request.getLieu().toLowerCase().contains("urban") ||
                        request.getLieu().toLowerCase().contains("dangereuse") ||
                        request.getLieu().toLowerCase().contains("industrial")) ? 1.0 : 0.0;
        double highSeverity = request.getDescription() != null &&
                (request.getDescription().toLowerCase().contains("severe") ||
                        request.getDescription().toLowerCase().contains("urgent") ||
                        request.getDescription().toLowerCase().contains("major")) ? 1.0 : 0.0;

        double typeLocationRisk = typeSeverity * highRiskLocation;
        double severityLocation = highSeverity * highRiskLocation;

        List<Sinistre> pastClaims = repo.findByAssureId(request.getAssureId());
        double historicalRisk = calculateHistoricalRisk(pastClaims, request.getDate());

        double coverageBonus = assure.getGuarantees() != null &&
                assure.getGuarantees().stream().anyMatch(g -> g.contains(typeLower)) ? 0.5 : 0.0;

        double logit = -3.0 + 2.0 * typeSeverity + 0.8 * highRiskLocation +
                0.7 * highSeverity + 1.2 * typeLocationRisk +
                0.6 * severityLocation + 0.4 * historicalRisk + coverageBonus;
        double logisticProb = 1.0 / (1.0 + Math.exp(-logit));

        double prior = typeSeverity + 0.1 * highSeverity;
        prior = Math.min(prior, 1.0);

        double posterior = (prior * logisticProb) /
                (prior * logisticProb + (1 - prior) * (1 - logisticProb));

        double rawScore = posterior * 10;
        double normalizedScore = 100.0 / (1.0 + Math.exp(-rawScore + 5.0));

        return (int) normalizedScore;
    }

    private double calculateHistoricalRisk(List<Sinistre> pastClaims, LocalDateTime currentDate) {
        if (pastClaims.isEmpty()) return 0.0;

        double weightedCount = 0.0;
        double decayRate = 0.01;
        for (Sinistre claim : pastClaims) {
            long daysDiff = ChronoUnit.DAYS.between(claim.getDate(), currentDate);
            double weight = Math.exp(-decayRate * daysDiff);
            weightedCount += weight;
        }

        return gammaDist.cumulativeProbability(weightedCount) * 10.0;
    }

    private String generateNumeroSinistre() {
        StringBuilder number = new StringBuilder(SINISTRE_NUMBER_LENGTH);
        for (int i = 0; i < SINISTRE_NUMBER_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            number.append(CHARACTERS.charAt(index));
        }
        String result = number.toString();
        // Retry if duplicate
        while (repo.findByNumeroSinistre(result).isPresent()) {
            number.setLength(0);
            for (int i = 0; i < SINISTRE_NUMBER_LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                number.append(CHARACTERS.charAt(index));
            }
            result = number.toString();
        }
        return result;
    }
}


    /* hedhaaa ema lezem nzid zoneservice to detecct high risk zones
    private int calculerPriorityScore(ClaimRequest request) {
        int score = 0;

        // 1. Claim Type
        switch (request.getType().toLowerCase()) {
            case "incendie" -> score += 70;
            case "accident" -> score += 50;
            default -> score += 30;
        }

        // 2. High-Risk Zones (e.g., use an external API)
        if (zoneService.isHighRiskZone(request.getLieu())) score += 30;

        // 3. Historical Claims (e.g., frequent claims increase priority)
        int historicalClaims = repo.countByAssureId(request.getAssureId());
        score += Math.min(historicalClaims * 10, 20);

        return Math.min(score, 100);
    }*/


    /*private int calculerPriorityScore(SinistreRequest request) {
        int score = 0;
        switch (request.getType().toLowerCase()) {
            case "incendie":
                score += 70;
                break;
            case "accident":
                score += 50;
                break;
            default:
                score += 30;
        }
        if (request.getLieu() != null && (request.getLieu().toLowerCase().contains("urban") ||
                request.getLieu().toLowerCase().contains("dangereuse"))) {
            score += 30;
        }
        if (request.getDescription() != null && (request.getDescription().toLowerCase().contains("severe") ||
                request.getDescription().toLowerCase().contains("urgent"))) {
            score += 20;
        }
        return Math.min(score, 100);
    }


    public Sinistre consulterStatut(Long id) {
        return repo.findById(id).orElseThrow(() -> new SinistreException("Sinistre introuvable"));
    }*/

    /*
    public Sinistre updateStatus(Long id, ClaimStatus newStatus) {
    Sinistre sinistre = repo.findById(id)
        .orElseThrow(() -> new SinistreException("Claim not found"));
    sinistre.setStatus(newStatus);
    repo.save(sinistre);

    // Notify via WebSocket
    simpMessagingTemplate.convertAndSend("/topic/sinistre/" + id, newStatus);
    return sinistre;
}

    */



