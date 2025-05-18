package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.exception.SinistreException;
import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.repository.GuaranteeRepository;
import com.vermeg.sinistpro.repository.PolicyRepository;
import com.vermeg.sinistpro.repository.SinistreRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class SinistreService {
    private static final Map<String, Double> TYPE_SEVERITY = new HashMap<>();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String UPLOAD_DIR = "/uploads/";

    static {
        TYPE_SEVERITY.put("incendie", 0.9);
        TYPE_SEVERITY.put("accident", 0.7);
        TYPE_SEVERITY.put("theft", 0.5);
        TYPE_SEVERITY.put("flood", 0.6);
        TYPE_SEVERITY.put("vandalism", 0.4);
        TYPE_SEVERITY.put("breakdown", 0.3);
        TYPE_SEVERITY.put("other", 0.3);
        TYPE_SEVERITY.put("vehicle", 0.7);
        TYPE_SEVERITY.put("home", 0.6);
        TYPE_SEVERITY.put("health", 0.5);
        TYPE_SEVERITY.put("property", 0.8);
    }

    private final SinistreRepository repo;
    private final PolicyRepository policyRepository;
    private final GuaranteeRepository guaranteeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final GammaDistribution gammaDist = new GammaDistribution(2.0, 1.0);
    @Value("${sinistre.secret-key}")
    private String secretKey;

    public SinistreService(SinistreRepository repo, PolicyRepository policyRepository, GuaranteeRepository guaranteeRepository, JdbcTemplate jdbcTemplate) {
        this.repo = repo;
        this.policyRepository = policyRepository;
        this.guaranteeRepository = guaranteeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS sinistre_number_seq START 1 INCREMENT 1");
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sinistre_number_seq sequence or upload directory: " + e.getMessage());
        }
    }

    public Sinistre créerSinistre(SinistreRequest request) {
        validateSinistreRequest(request);

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
        sinistre.setAccidentType(request.getAccidentType());
        sinistre.setThirdPartyInvolved(request.getThirdPartyInvolved());
        sinistre.setPoliceReportNumber(request.getPoliceReportNumber());

        List<MediaReference> mediaReferences = new ArrayList<>();
        if (request.getMediaFiles() != null && !request.getMediaFiles().isEmpty()) {
            for (MultipartFile file : request.getMediaFiles()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String contentType = file.getContentType();
                        if (contentType == null || !contentType.matches("^(image/.*|video/.*)$")) {
                            throw new SinistreException("Invalid file type. Only images and videos are allowed.");
                        }

                        String fileName = "sinistre_" + sinistre.getNumeroSinistre() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        String filePath = UPLOAD_DIR + fileName;

                        File dest = new File(filePath);
                        file.transferTo(dest);

                        MediaReference mediaReference = new MediaReference();
                        mediaReference.setFilePath(filePath);
                        mediaReference.setFileType(contentType);
                        mediaReference.setCreatedAt(LocalDateTime.now());
                        mediaReference.setSinistre(sinistre);
                        mediaReferences.add(mediaReference);
                    } catch (IOException e) {
                        throw new SinistreException("Failed to upload file: " + e.getMessage());
                    }
                }
            }
        }
        sinistre.setMediaReferences(mediaReferences);

        sinistre.setMontantIndemnisation(sinistre.calculerMontantIndemnisation());
        return repo.save(sinistre);
    }

    private void validateSinistreRequest(SinistreRequest request) {
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new SinistreException("Claim type is required.");
        }
        if (!List.of("vehicle", "home", "health", "property").contains(request.getType().toLowerCase())) {
            throw new SinistreException("Invalid claim type. Must be 'vehicle', 'home', 'health', or 'property'.");
        }

        if (request.getDate() == null) {
            throw new SinistreException("Claim date is required.");
        }
        if (request.getDate().isAfter(LocalDateTime.now())) {
            throw new SinistreException("Claim date cannot be in the future.");
        }

        if (request.getLieu() == null || request.getLieu().trim().isEmpty()) {
            throw new SinistreException("Location is required.");
        }
        if (!request.getLieu().matches("[a-zA-Z0-9\\s,-]+")) {
            throw new SinistreException("Location must contain only letters, numbers, spaces, commas, or hyphens.");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new SinistreException("Description is required.");
        }
        if (request.getDescription().length() > 1000) {
            throw new SinistreException("Description cannot exceed 1000 characters.");
        }

        if (request.getPolicyId() == null) {
            throw new SinistreException("Policy ID is required.");
        }

        if (request.getAccidentType() == null || request.getThirdPartyInvolved() == null) {
            throw new SinistreException("Accident type and third-party involvement are required.");
        }
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

        List<Guarantee> guarantees = guaranteeRepository.findByPoliciesId(policy.getId());
        double coverageBonus = guarantees.stream().anyMatch(g -> g.getName().toLowerCase().contains(typeLower)) ? 0.5 : 0.0;

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



/*package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.exception.SinistreException;
import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.repository.PolicyRepository;
import com.vermeg.sinistpro.repository.SinistreRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class SinistreService {
    private static final Map<String, Double> TYPE_SEVERITY = new HashMap<>();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String UPLOAD_DIR = "/uploads/"; // Configure in application.yml for production

    // New AI/ML components
    //private final FraudDetectionService fraudDetectionService;
    //private final CostPredictionModel costPredictionModel;
    // private final ClaimPriorityModel priorityModel;

    static {
        TYPE_SEVERITY.put("incendie", 0.9);
        TYPE_SEVERITY.put("accident", 0.7);
        TYPE_SEVERITY.put("theft", 0.5);
        TYPE_SEVERITY.put("flood", 0.6);
        TYPE_SEVERITY.put("vandalism", 0.4);
        TYPE_SEVERITY.put("breakdown", 0.3);
        TYPE_SEVERITY.put("other", 0.3);
        TYPE_SEVERITY.put("vehicle", 0.7);
        TYPE_SEVERITY.put("home", 0.6);
        TYPE_SEVERITY.put("health", 0.5);
        TYPE_SEVERITY.put("property", 0.8);
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
        try {
            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS sinistre_number_seq START 1 INCREMENT 1");
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sinistre_number_seq sequence or upload directory: " + e.getMessage());
        }
    }

    public Sinistre créerSinistre(SinistreRequest request) {
        validateSinistreRequest(request);

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

        // Set type-specific fields
        switch (request.getType().toLowerCase()) {
            case "vehicle":
                sinistre.setVehicleType(request.getVehicleType());
                sinistre.setVehicleMake(request.getVehicleMake());
                sinistre.setVehicleModel(request.getVehicleModel());
                sinistre.setVehicleYear(request.getVehicleYear());
                sinistre.setVin(request.getVin());
                sinistre.setAccidentType(request.getAccidentType());
                sinistre.setThirdPartyInvolved(request.getThirdPartyInvolved());
                sinistre.setPoliceReportNumber(request.getPoliceReportNumber());
                break;
            case "home":
                sinistre.setPropertyAddress(request.getPropertyAddress());
                sinistre.setDamageType(request.getDamageType());
                sinistre.setDamageExtent(request.getDamageExtent());
                sinistre.setAffectedAreas(request.getAffectedAreas() != null ? request.getAffectedAreas() : new ArrayList<>());
                sinistre.setEmergencyServicesCalled(request.getEmergencyServicesCalled());
                break;
            case "health":
                sinistre.setMedicalCondition(request.getMedicalCondition());
                sinistre.setTreatmentLocation(request.getTreatmentLocation());
                sinistre.setTreatmentDate(request.getTreatmentDate());
                sinistre.setDoctorName(request.getDoctorName());
                sinistre.setMedicalBillAmount(request.getMedicalBillAmount());
                sinistre.setHospitalizationRequired(request.getHospitalizationRequired());
                break;
            case "property":
                sinistre.setPropertyType(request.getPropertyType());
                sinistre.setIncidentCause(request.getIncidentCause());
                sinistre.setPropertyDamageDescription(request.getPropertyDamageDescription());
                sinistre.setEstimatedLossValue(request.getEstimatedLossValue());
                sinistre.setBusinessInterruption(request.getBusinessInterruption());
                break;
            default:
                throw new SinistreException("Unsupported claim type: " + request.getType());
        }

        // Handle media files
        List<MediaReference> mediaReferences = new ArrayList<>();
        if (request.getMediaFiles() != null && !request.getMediaFiles().isEmpty()) {
            for (MultipartFile file : request.getMediaFiles()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String contentType = file.getContentType();
                        if (contentType == null || !contentType.matches("^(image/.*|video/.*)$")) {
                            throw new SinistreException("Invalid file type. Only images and videos are allowed.");
                        }

                        String fileName = "sinistre_" + sinistre.getNumeroSinistre() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                        String filePath = UPLOAD_DIR + fileName;

                        File dest = new File(filePath);
                        file.transferTo(dest);

                        MediaReference mediaReference = new MediaReference();
                        mediaReference.setFilePath(filePath);
                        mediaReference.setFileType(contentType);
                        mediaReference.setSinistre(sinistre);
                        mediaReferences.add(mediaReference);
                    } catch (IOException e) {
                        throw new SinistreException("Failed to upload file: " + e.getMessage());
                    }
                }
            }
        }
        sinistre.setMediaReferences(mediaReferences);

        sinistre.setMontantIndemnisation(sinistre.calculerMontantIndemnisation());
        return repo.save(sinistre);
    }

    private void validateSinistreRequest(SinistreRequest request) {
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new SinistreException("Claim type is required.");
        }
        if (!List.of("vehicle", "home", "health", "property").contains(request.getType().toLowerCase())) {
            throw new SinistreException("Invalid claim type. Must be 'vehicle', 'home', 'health', or 'property'.");
        }

        if (request.getDate() == null) {
            throw new SinistreException("Claim date is required.");
        }
        if (request.getDate().isAfter(LocalDateTime.now())) {
            throw new SinistreException("Claim date cannot be in the future.");
        }

        if (request.getLieu() == null || request.getLieu().trim().isEmpty()) {
            throw new SinistreException("Location is required.");
        }
        if (!request.getLieu().matches("[a-zA-Z0-9\\s,-]+")) {
            throw new SinistreException("Location must contain only letters, numbers, spaces, commas, or hyphens.");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new SinistreException("Description is required.");
        }
        if (request.getDescription().length() > 1000) {
            throw new SinistreException("Description cannot exceed 1000 characters.");
        }

        if (request.getPolicyId() == null) {
            throw new SinistreException("Policy ID is required.");
        }

        // Type-specific validation
        switch (request.getType().toLowerCase()) {
            case "vehicle":
                if (request.getVehicleType() == null || request.getVehicleMake() == null ||
                        request.getVehicleModel() == null || request.getVehicleYear() == null ||
                        request.getVin() == null || request.getAccidentType() == null ||
                        request.getThirdPartyInvolved() == null) {
                    throw new SinistreException("All vehicle-specific fields are required.");
                }
                if (!request.getVin().matches("[A-HJ-NPR-Z0-9]{17}")) {
                    throw new SinistreException("Invalid VIN format. Must be 17 alphanumeric characters (excluding I, O, Q).");
                }
                break;
            case "home":
                if (request.getPropertyAddress() == null || request.getDamageType() == null ||
                        request.getDamageExtent() == null || request.getAffectedAreas() == null ||
                        request.getEmergencyServicesCalled() == null) {
                    throw new SinistreException("All home-specific fields are required.");
                }
                break;
            case "health":
                if (request.getMedicalCondition() == null || request.getTreatmentLocation() == null ||
                        request.getTreatmentDate() == null || request.getDoctorName() == null ||
                        request.getMedicalBillAmount() == null || request.getHospitalizationRequired() == null) {
                    throw new SinistreException("All health-specific fields are required.");
                }
                if (request.getMedicalBillAmount() <= 0) {
                    throw new SinistreException("Medical bill amount must be positive.");
                }
                break;
            case "property":
                if (request.getPropertyType() == null || request.getIncidentCause() == null ||
                        request.getPropertyDamageDescription() == null || request.getEstimatedLossValue() == null ||
                        request.getBusinessInterruption() == null) {
                    throw new SinistreException("All property-specific fields are required.");
                }
                if (request.getEstimatedLossValue() <= 0) {
                    throw new SinistreException("Estimated loss value must be positive.");
                }
                break;
        }
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
}*/





