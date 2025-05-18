package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.model.Expert.Specialite;
import com.vermeg.sinistpro.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpertService {
    private static final Logger logger = LoggerFactory.getLogger(ExpertService.class);
    private static final List<String> ASSURANCE_AGENCIES = Arrays.asList("Tunis", "Sfax", "Sousse", "Bizerte");

    private static final Map<String, List<String>> REGION_MAPPINGS = new HashMap<>();

    static {
        REGION_MAPPINGS.put("Tunis", Arrays.asList("Tunis", "Ariana", "Ben Arous", "Manouba", "La Marsa", "Carthage", "Sidi Bou Said"));
        REGION_MAPPINGS.put("Sfax", Arrays.asList("Sfax", "Sakiet Ezzit", "Sakiet Eddaier", "Gremda", "El Ain", "Thyna"));
        REGION_MAPPINGS.put("Sousse", Arrays.asList("Sousse", "Hammam Sousse", "Msaken", "Kalaa Kebira", "Enfidha", "Monastir"));
        REGION_MAPPINGS.put("Bizerte", Arrays.asList("Bizerte", "Menzel Bourguiba", "Mateur", "Ras Jebel", "Sejnane"));
    }

    private final ExpertRepository expertRepository;
    private final SinistreRepository sinistreRepository;
    private final ReportRepository reportRepository;
    private final MeetingRepository meetingRepository;
    private final NotificationRepository notificationRepository;
    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public ExpertService(ExpertRepository expertRepository, SinistreRepository sinistreRepository,
                         ReportRepository reportRepository, MeetingRepository meetingRepository,
                         NotificationRepository notificationRepository, ClientRepository clientRepository,
                         AdminRepository adminRepository) {
        this.expertRepository = expertRepository;
        this.sinistreRepository = sinistreRepository;
        this.reportRepository = reportRepository;
        this.meetingRepository = meetingRepository;
        this.notificationRepository = notificationRepository;
        this.clientRepository = clientRepository;
        this.adminRepository = adminRepository;
    }

    public Expert findExpertById(Long expertId) {
        return expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("Expert not found with id: " + expertId));
    }

    public Optional<Expert> findExpertByUserId(Long userId) {
        return expertRepository.findByUserId(userId);
    }

    public Expert saveExpert(Expert expert) {
        if (expert.getUser() == null || !expert.getUser().getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_EXPERT"))) {
            throw new IllegalArgumentException("Expert must be associated with a user having ROLE_EXPERT");
        }
        return expertRepository.save(expert);
    }

    public List<Expert> findExpertsBySpecialite(Specialite specialite) {
        return expertRepository.findBySpecialite(specialite);
    }

    public void assignExpertToSinistre(Sinistre sinistre) {
        List<Expert> availableExperts = expertRepository.findAllWithExpertRole();
        logger.debug("Available experts from findAllWithExpertRole: {}", availableExperts.size());
        if (availableExperts.isEmpty()) {
            logger.warn("No experts found with ROLE_EXPERT, falling back to experts by specialty");
            try {
                Specialite specialite = Specialite.valueOf(sinistre.getType().toUpperCase());
                availableExperts = expertRepository.findBySpecialite(specialite);
                logger.debug("Available experts from findBySpecialite: {}", availableExperts.size());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid sinistre type for specialite: {}", sinistre.getType());
                return;
            }
        }
        assignExpert(sinistre, availableExperts)
                .ifPresent(expert -> {
                    sinistre.setExpert(expert);
                    sinistreRepository.save(sinistre);
                    logger.info("Assigned expert {} to sinistre {}", expert.getNom(), sinistre.getNumeroSinistre());
                });
    }

    public Map<String, Object> assignExpertToSinistreWithDetails(Sinistre sinistre) {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> expertScores = previewExpertScores(sinistre);
        result.put("allExpertScores", expertScores);

        if (expertScores.isEmpty()) {
            result.put("success", false);
            result.put("message", "No experts available for assignment");
            return result;
        }

        Map<String, Object> selectedExpertDetails = expertScores.get(0);
        Long selectedExpertId = (Long) selectedExpertDetails.get("expertId");
        Expert selectedExpert = findExpertById(selectedExpertId);

        sinistre.setExpert(selectedExpert);
        sinistreRepository.save(sinistre);
        logger.info("Assigned expert {} to sinistre {}", selectedExpert.getNom(), sinistre.getNumeroSinistre());

        result.put("success", true);
        result.put("selectedExpert", selectedExpertDetails);
        result.put("message", "Expert " + selectedExpert.getNom() + " assigned to sinistre " + sinistre.getNumeroSinistre());

        return result;
    }

    public int calculateExpertWorkload(Long expertId) {
        Expert expert = findExpertById(expertId);
        List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expertId);
        int claimCount = activeClaims.size();
        int totalPriorityScore = activeClaims.stream()
                .mapToInt(Sinistre::getPriorityScore)
                .sum();

        expert.setWorkload(claimCount, totalPriorityScore);
        return expert.getWorkload();
    }

    private Optional<Expert> assignExpert(Sinistre sinistre, List<Expert> availableExperts) {
        if (sinistre == null) {
            logger.warn("Cannot assign expert: sinistre is null");
            return Optional.empty();
        }
        if (availableExperts.isEmpty()) {
            logger.warn("Cannot assign expert: no experts available");
            return Optional.empty();
        }

        logger.debug("Sinistre type: {}", sinistre.getType());

        List<Expert> matchingExperts = availableExperts.stream()
                .filter(expert -> {
                    boolean matchesSpecialite = expert.getSpecialite().name().equalsIgnoreCase(sinistre.getType());
                    logger.debug("Expert {} specialite: {}, matches: {}", expert.getNom(), expert.getSpecialite(), matchesSpecialite);
                    return matchesSpecialite;
                })
                .filter(expert -> {
                    boolean hasRoleExpert = expert.getUser() != null &&
                            expert.getUser().getRoles().stream()
                                    .anyMatch(role -> role.getName().equals("ROLE_EXPERT"));
                    logger.debug("Expert {} has ROLE_EXPERT: {}", expert.getNom(), hasRoleExpert);
                    return hasRoleExpert;
                })
                .toList();

        if (matchingExperts.isEmpty()) {
            logger.warn("No experts found for sinistre type: {}", sinistre.getType());
            return Optional.empty();
        }

        matchingExperts.forEach(expert -> {
            List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expert.getId());
            int claimCount = activeClaims.size();
            int totalPriorityScore = activeClaims.stream()
                    .mapToInt(Sinistre::getPriorityScore)
                    .sum();
            expert.setWorkload(claimCount, totalPriorityScore);
            logger.debug("Expert {} workload: {}", expert.getNom(), expert.getWorkload());
        });

        return matchingExperts.stream()
                .map(expert -> new ExpertScore(expert, calculateScore(sinistre, expert)))
                .peek(score -> logger.debug("Expert {} score: {}", score.expert.getNom(), score.score))
                .max((s1, s2) -> Double.compare(s1.score, s2.score))
                .map(expertScore -> {
                    logger.debug("Selected expert: {}", expertScore.expert.getNom());
                    return Optional.of(expertScore.expert);
                })
                .orElse(Optional.empty());
    }

    private double calculateScore(Sinistre sinistre, Expert expert) {
        double proximityScore = calculateProximityScore(sinistre, expert);
        double workloadScore = calculateWorkloadScore(expert.getWorkload());
        double complexityAdjustment = adjustForComplexity(sinistre.getPriorityScore());

        logger.debug("Expert {}: Proximity score: {}, Workload score: {}, Complexity adjustment: {}",
                expert.getNom(), proximityScore, workloadScore, complexityAdjustment);

        return 0.5 * proximityScore + 0.4 * workloadScore + 0.1 * complexityAdjustment;
    }

    private double calculateProximityScore(Sinistre sinistre, Expert expert) {
        if (sinistre.getLieu() == null || expert.getLocation() == null) {
            return 50.0;
        }

        String clientLocation = (sinistre.getPolicy() != null && sinistre.getPolicy().getClient() != null)
                ? sinistre.getPolicy().getClient().getAdresse()
                : null;

        double sinistreProximity = calculateIndividualProximityScore(sinistre.getLieu(), expert.getLocation());
        double clientProximity = (clientLocation != null)
                ? calculateIndividualProximityScore(clientLocation, expert.getLocation())
                : sinistreProximity;

        return (sinistreProximity + clientProximity) / 2.0;
    }

    private double calculateIndividualProximityScore(String sinistreLieu, String expertLocation) {
        if (sinistreLieu.equalsIgnoreCase(expertLocation)) {
            return 100.0;
        }

        String sinistreRegion = findRegion(sinistreLieu);
        String expertRegion = findRegion(expertLocation);

        if (sinistreRegion != null && expertRegion != null && sinistreRegion.equals(expertRegion)) {
            return 85.0;
        }

        if (sinistreLieu.toLowerCase().contains(expertLocation.toLowerCase()) ||
                expertLocation.toLowerCase().contains(sinistreLieu.toLowerCase())) {
            return 75.0;
        }

        if (sinistreRegion != null && expertRegion != null) {
            int regionDistance = calculateRegionDistance(sinistreRegion, expertRegion);
            return Math.max(25.0, 60.0 - (regionDistance * 10.0));
        }

        return 25.0;
    }

    private String findRegion(String location) {
        if (location == null) {
            return null;
        }

        String normalizedLocation = location.toLowerCase().trim();

        for (Map.Entry<String, List<String>> entry : REGION_MAPPINGS.entrySet()) {
            if (entry.getKey().toLowerCase().equals(normalizedLocation)) {
                return entry.getKey();
            }
        }

        for (Map.Entry<String, List<String>> entry : REGION_MAPPINGS.entrySet()) {
            if (entry.getValue().stream()
                    .anyMatch(city -> city.toLowerCase().equals(normalizedLocation) ||
                            normalizedLocation.contains(city.toLowerCase()))) {
                return entry.getKey();
            }
        }

        return null;
    }

    private int calculateRegionDistance(String region1, String region2) {
        Map<String, Map<String, Integer>> distanceMatrix = new HashMap<>();

        distanceMatrix.put("Tunis", new HashMap<String, Integer>() {{
            put("Tunis", 0);
            put("Bizerte", 1);
            put("Sousse", 2);
            put("Sfax", 3);
        }});

        distanceMatrix.put("Bizerte", new HashMap<String, Integer>() {{
            put("Tunis", 1);
            put("Bizerte", 0);
            put("Sousse", 3);
            put("Sfax", 4);
        }});

        distanceMatrix.put("Sousse", new HashMap<String, Integer>() {{
            put("Tunis", 2);
            put("Bizerte", 3);
            put("Sousse", 0);
            put("Sfax", 1);
        }});

        distanceMatrix.put("Sfax", new HashMap<String, Integer>() {{
            put("Tunis", 3);
            put("Bizerte", 4);
            put("Sousse", 1);
            put("Sfax", 0);
        }});

        return distanceMatrix.getOrDefault(region1, new HashMap<>()).getOrDefault(region2, 5);
    }

    private double calculateWorkloadScore(int workload) {
        if (workload <= 30) {
            return 100.0;
        } else if (workload <= 50) {
            return 90.0 - ((workload - 30) * 0.5);
        } else if (workload <= 100) {
            return 80.0 - ((workload - 50) * 0.6);
        } else {
            return Math.max(0, 50.0 - ((workload - 100) * 0.5));
        }
    }

    private double adjustForComplexity(Integer priorityScore) {
        if (priorityScore == null) {
            return 50.0;
        }

        if (priorityScore >= 80) {
            return 70.0;
        } else if (priorityScore >= 50) {
            return 60.0;
        } else {
            return 100.0 - priorityScore;
        }
    }

    public List<Sinistre> getAssignedClaims(Long expertId) {
        Expert expert = findExpertById(expertId);
        return sinistreRepository.findByExpertId(expertId);
    }

    public Sinistre getClaimDetails(Long expertId, Long sinistreId) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (expertId != null) {
            Expert expert = findExpertById(expertId);
            if (sinistre.getExpert() == null || !expert.equals(sinistre.getExpert())) {
                throw new RuntimeException("Expert with id " + expertId + " is not assigned to sinistre " + sinistreId);
            }
        }
        return sinistre;
    }

    public Report writeReport(Long expertId, Long sinistreId, String content) {
        Expert expert = findExpertById(expertId);
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (sinistre.getExpert() == null || !sinistre.getExpert().getId().equals(expertId)) {
            throw new RuntimeException("Expert not assigned to this sinistre");
        }

        Report report = new Report();
        report.setSinistre(sinistre);
        report.setExpert(expert);
        report.setContent(content);
        report.setCreatedAt(LocalDateTime.now());
        Report savedReport = reportRepository.save(report);

        notifyAdminAndClient(sinistre, "New report submitted by expert " + expert.getNom() + " for sinistre " + sinistre.getNumeroSinistre());

        return savedReport;
    }

    public Meeting scheduleMeeting(Long expertId, Long sinistreId, Meeting.MeetingType meetingType, LocalDateTime meetingDate, String requestedLocation) {
        Expert expert = findExpertById(expertId);
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (sinistre.getExpert() == null || !sinistre.getExpert().getId().equals(expertId)) {
            throw new RuntimeException("Expert not assigned to this sinistre");
        }

        String meetingLocation = findNearestAgency(sinistre.getLieu(), expert.getLocation(), requestedLocation);
        if (meetingLocation == null) {
            throw new RuntimeException("No suitable assurance agency found for meeting");
        }

        Meeting meeting = new Meeting();
        meeting.setSinistre(sinistre);
        meeting.setExpert(expert);
        meeting.setMeetingType(meetingType);
        meeting.setMeetingDate(meetingDate);
        meeting.setLocation(meetingLocation);
        meeting.setCreatedAt(LocalDateTime.now());
        Meeting savedMeeting = meetingRepository.save(meeting);

        Client client = clientRepository.findById(sinistre.getPolicy().getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Notification notification = new Notification();
        notification.setUser(client.getUser());
        notification.setSinistre(sinistre);
        notification.setMessage("Meeting scheduled: " + meetingType + " on " + meetingDate + " at " + meetingLocation);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);

        return savedMeeting;
    }

    public void approveClaim(Long expertId, Long sinistreId, String approvalNotes) {
        Expert expert = findExpertById(expertId);
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (sinistre.getExpert() == null || !sinistre.getExpert().getId().equals(expertId)) {
            throw new RuntimeException("Expert not assigned to this sinistre");
        }

        sinistre.setStatus(ClaimStatus.APPROVED);
        sinistre.setMontantIndemnisation(sinistre.calculerMontantIndemnisation());
        sinistreRepository.save(sinistre);

        String message = "Claim " + sinistre.getNumeroSinistre() + " approved by expert " + expert.getNom() +
                ". Indemnification: " + sinistre.getMontantIndemnisation() + ". Notes: " + approvalNotes;
        notifyAdminAndClient(sinistre, message);
    }

    private void notifyAdminAndClient(Sinistre sinistre, String message) {
        Client client = clientRepository.findById(sinistre.getPolicy().getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Notification clientNotification = new Notification();
        clientNotification.setUser(client.getUser());
        clientNotification.setSinistre(sinistre);
        clientNotification.setMessage(message);
        clientNotification.setCreatedAt(LocalDateTime.now());
        clientNotification.setRead(false);
        notificationRepository.save(clientNotification);

        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            Notification adminNotification = new Notification();
            adminNotification.setUser(admin.getUser());
            adminNotification.setSinistre(sinistre);
            adminNotification.setMessage(message);
            adminNotification.setCreatedAt(LocalDateTime.now());
            adminNotification.setRead(false);
            notificationRepository.save(adminNotification);
        }
    }

    private String findNearestAgency(String sinistreLieu, String expertLocation, String requestedLocation) {
        if (ASSURANCE_AGENCIES.contains(requestedLocation)) {
            return requestedLocation;
        }

        if (sinistreLieu != null) {
            for (String agency : ASSURANCE_AGENCIES) {
                if (sinistreLieu.toLowerCase().contains(agency.toLowerCase())) {
                    return agency;
                }
            }

            String sinistreRegion = findRegion(sinistreLieu);
            if (sinistreRegion != null && ASSURANCE_AGENCIES.contains(sinistreRegion)) {
                return sinistreRegion;
            }
        }

        if (expertLocation != null) {
            for (String agency : ASSURANCE_AGENCIES) {
                if (expertLocation.toLowerCase().contains(agency.toLowerCase())) {
                    return agency;
                }
            }

            String expertRegion = findRegion(expertLocation);
            if (expertRegion != null && ASSURANCE_AGENCIES.contains(expertRegion)) {
                return expertRegion;
            }
        }

        return ASSURANCE_AGENCIES.get(0);
    }

    public Map<String, Object> calculateExpertWorkloadWithDetails(Long expertId) {
        Expert expert = findExpertById(expertId);
        Map<String, Object> workloadDetails = new HashMap<>();

        workloadDetails.put("expertId", expert.getId());
        workloadDetails.put("expertName", expert.getNom());
        workloadDetails.put("specialite", expert.getSpecialite().name());
        workloadDetails.put("location", expert.getLocation());

        List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expertId);
        int claimCount = activeClaims.size();
        int totalPriorityScore = activeClaims.stream()
                .mapToInt(Sinistre::getPriorityScore)
                .sum();

        expert.setWorkload(claimCount, totalPriorityScore);

        workloadDetails.put("activeClaimCount", claimCount);
        workloadDetails.put("totalPriorityScore", totalPriorityScore);
        workloadDetails.put("calculatedWorkload", expert.getWorkload());
        workloadDetails.put("workloadScore", calculateWorkloadScore(expert.getWorkload()));

        List<Map<String, Object>> claimDetails = activeClaims.stream()
                .map(claim -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", claim.getId());
                    details.put("numeroSinistre", claim.getNumeroSinistre());
                    details.put("type", claim.getType());
                    details.put("status", claim.getStatus());
                    details.put("priorityScore", claim.getPriorityScore());
                    details.put("lieu", claim.getLieu());
                    return details;
                })
                .collect(Collectors.toList());

        workloadDetails.put("activeClaims", claimDetails);

        return workloadDetails;
    }

    public Map<String, Object> getAssignmentAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        List<Expert> allExperts = expertRepository.findAllWithExpertRole();
        List<Map<String, Object>> expertWorkloads = new ArrayList<>();

        for (Expert expert : allExperts) {
            Map<String, Object> expertData = new HashMap<>();
            List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expert.getId());
            int claimCount = activeClaims.size();
            int totalPriorityScore = activeClaims.stream()
                    .mapToInt(Sinistre::getPriorityScore)
                    .sum();

            expert.setWorkload(claimCount, totalPriorityScore);

            expertData.put("expertId", expert.getId());
            expertData.put("expertName", expert.getNom());
            expertData.put("specialite", expert.getSpecialite().name());
            expertData.put("location", expert.getLocation());
            expertData.put("activeClaimCount", claimCount);
            expertData.put("workload", expert.getWorkload());

            expertWorkloads.add(expertData);
        }

        analytics.put("expertWorkloads", expertWorkloads);

        if (!expertWorkloads.isEmpty()) {
            double avgWorkload = expertWorkloads.stream()
                    .mapToInt(e -> (Integer) e.get("workload"))
                    .average()
                    .orElse(0);

            int minWorkload = expertWorkloads.stream()
                    .mapToInt(e -> (Integer) e.get("workload"))
                    .min()
                    .orElse(0);

            int maxWorkload = expertWorkloads.stream()
                    .mapToInt(e -> (Integer) e.get("workload"))
                    .max()
                    .orElse(0);

            double stdDeviation = calculateStandardDeviation(
                    expertWorkloads.stream()
                            .mapToInt(e -> (Integer) e.get("workload"))
                            .toArray()
            );

            analytics.put("averageWorkload", avgWorkload);
            analytics.put("minWorkload", minWorkload);
            analytics.put("maxWorkload", maxWorkload);
            analytics.put("workloadStdDeviation", stdDeviation);
            analytics.put("workloadVariance", stdDeviation * stdDeviation);
        }

        Map<String, Long> locationCounts = allExperts.stream()
                .collect(Collectors.groupingBy(
                        Expert::getLocation,
                        Collectors.counting()
                ));
        analytics.put("locationDistribution", locationCounts);

        Map<String, Long> specialiteCounts = allExperts.stream()
                .collect(Collectors.groupingBy(
                        expert -> expert.getSpecialite().name(),
                        Collectors.counting()
                ));
        analytics.put("specialiteDistribution", specialiteCounts);

        return analytics;
    }

    public List<Map<String, Object>> previewExpertScores(Sinistre sinistre) {
        List<Expert> availableExperts = expertRepository.findAllWithExpertRole();
        List<Expert> matchingExperts = availableExperts.stream()
                .filter(expert -> expert.getSpecialite().name().equalsIgnoreCase(sinistre.getType()))
                .filter(expert -> expert.getUser() != null &&
                        expert.getUser().getRoles().stream()
                                .anyMatch(role -> role.getName().equals("ROLE_EXPERT")))
                .toList();

        if (matchingExperts.isEmpty()) {
            return Collections.emptyList();
        }

        matchingExperts.forEach(expert -> {
            List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expert.getId());
            int claimCount = activeClaims.size();
            int totalPriorityScore = activeClaims.stream()
                    .mapToInt(Sinistre::getPriorityScore)
                    .sum();
            expert.setWorkload(claimCount, totalPriorityScore);
        });

        List<Map<String, Object>> expertScores = new ArrayList<>();
        for (Expert expert : matchingExperts) {
            Map<String, Object> scoreDetails = new HashMap<>();
            scoreDetails.put("expertId", expert.getId());
            scoreDetails.put("expertName", expert.getNom());
            scoreDetails.put("expertLocation", expert.getLocation());
            scoreDetails.put("expertWorkload", expert.getWorkload());

            double proximityScore = calculateProximityScore(sinistre, expert);
            double workloadScore = calculateWorkloadScore(expert.getWorkload());
            double complexityAdjustment = adjustForComplexity(sinistre.getPriorityScore());
            double totalScore = 0.5 * proximityScore + 0.4 * workloadScore + 0.1 * complexityAdjustment;

            scoreDetails.put("proximityScore", proximityScore);
            scoreDetails.put("workloadScore", workloadScore);
            scoreDetails.put("complexityAdjustment", complexityAdjustment);
            scoreDetails.put("totalScore", totalScore);

            String sinistreRegion = findRegion(sinistre.getLieu());
            String expertRegion = findRegion(expert.getLocation());

            Map<String, Object> locationDetails = new HashMap<>();
            locationDetails.put("sinistreLocation", sinistre.getLieu());
            locationDetails.put("sinistreRegion", sinistreRegion);
            locationDetails.put("expertLocation", expert.getLocation());
            locationDetails.put("expertRegion", expertRegion);

            if (sinistreRegion != null && expertRegion != null && !sinistreRegion.equals(expertRegion)) {
                int regionDistance = calculateRegionDistance(sinistreRegion, expertRegion);
                locationDetails.put("regionDistance", regionDistance);
            }

            scoreDetails.put("locationDetails", locationDetails);
            expertScores.add(scoreDetails);
        }

        expertScores.sort((a, b) -> Double.compare(
                (Double) b.get("totalScore"),
                (Double) a.get("totalScore")
        ));

        return expertScores;
    }

    private double calculateStandardDeviation(int[] values) {
        if (values.length == 0) return 0;

        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum() / values.length;

        return Math.sqrt(variance);
    }

    private static class ExpertScore {
        Expert expert;
        double score;

        ExpertScore(Expert expert, double score) {
            this.expert = expert;
            this.score = score;
        }
    }
}


/*package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpertService {

    private static final Logger logger = LoggerFactory.getLogger(ExpertService.class);
    private static final List<String> ASSURANCE_AGENCIES = Arrays.asList("Tunis", "Sfax", "Sousse", "Bizerte");

    private static final Map<String, List<String>> REGION_MAPPINGS = new HashMap<>();

    static {
        REGION_MAPPINGS.put("Tunis", Arrays.asList("Tunis", "Ariana", "Ben Arous", "Manouba", "La Marsa", "Carthage", "Sidi Bou Said"));
        REGION_MAPPINGS.put("Sfax", Arrays.asList("Sfax", "Sakiet Ezzit", "Sakiet Eddaier", "Gremda", "El Ain", "Thyna"));
        REGION_MAPPINGS.put("Sousse", Arrays.asList("Sousse", "Hammam Sousse", "Msaken", "Kalaa Kebira", "Enfidha", "Monastir"));
        REGION_MAPPINGS.put("Bizerte", Arrays.asList("Bizerte", "Menzel Bourguiba", "Mateur", "Ras Jebel", "Sejnane"));
    }

    private final ExpertRepository expertRepository;
    private final SinistreRepository sinistreRepository;
    private final ReportRepository reportRepository;
    private final MeetingRepository meetingRepository;
    private final NotificationRepository notificationRepository;
    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public ExpertService(ExpertRepository expertRepository, SinistreRepository sinistreRepository,
                         ReportRepository reportRepository, MeetingRepository meetingRepository,
                         NotificationRepository notificationRepository, ClientRepository clientRepository,
                         AdminRepository adminRepository) {
        this.expertRepository = expertRepository;
        this.sinistreRepository = sinistreRepository;
        this.reportRepository = reportRepository;
        this.meetingRepository = meetingRepository;
        this.notificationRepository = notificationRepository;
        this.clientRepository = clientRepository;
        this.adminRepository = adminRepository;
    }

    public Expert findExpertById(Long expertId) {
        return expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("Expert not found with id: " + expertId));
    }

    public Optional<Expert> findExpertByUserId(Long userId) {
        return expertRepository.findByUserId(userId);
    }

    public Expert saveExpert(Expert expert) {
        if (expert.getUser() == null || !expert.getUser().getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_EXPERT"))) {
            throw new IllegalArgumentException("Expert must be associated with a user having ROLE_EXPERT");
        }
        return expertRepository.save(expert);
    }
    

    public List<Expert> findExpertsBySpecialite(String specialite) {
        return expertRepository.findBySpecialite(specialite);
    }

    public void assignExpertToSinistre(Sinistre sinistre) {
        List<Expert> availableExperts = expertRepository.findAllWithExpertRole();
        logger.debug("Available experts from findAllWithExpertRole: {}", availableExperts.size());
        if (availableExperts.isEmpty()) {
            logger.warn("No experts found with ROLE_EXPERT, falling back to experts by specialty");
            availableExperts = expertRepository.findBySpecialite(sinistre.getType());
            logger.debug("Available experts from findBySpecialite: {}", availableExperts.size());
        }
        assignExpert(sinistre, availableExperts)
                .ifPresent(expert -> {
                    sinistre.setExpert(expert);
                    sinistreRepository.save(sinistre);
                    logger.info("Assigned expert {} to sinistre {}", expert.getNom(), sinistre.getNumeroSinistre());
                });
    }

    public Map<String, Object> assignExpertToSinistreWithDetails(Sinistre sinistre) {
        Map<String, Object> result = new HashMap<>();

        // Get all expert scores using previewExpertScores
        List<Map<String, Object>> expertScores = previewExpertScores(sinistre);
        result.put("allExpertScores", expertScores);

        if (expertScores.isEmpty()) {
            result.put("success", false);
            result.put("message", "No experts available for assignment");
            return result;
        }

        // Select the expert with the highest score
        Map<String, Object> selectedExpertDetails = expertScores.get(0); // Already sorted by totalScore descending
        Long selectedExpertId = (Long) selectedExpertDetails.get("expertId");
        Expert selectedExpert = findExpertById(selectedExpertId);

        // Assign the expert to the sinistre
        sinistre.setExpert(selectedExpert);
        sinistreRepository.save(sinistre);
        logger.info("Assigned expert {} to sinistre {}", selectedExpert.getNom(), sinistre.getNumeroSinistre());

        // Add assignment details to the result
        result.put("success", true);
        result.put("selectedExpert", selectedExpertDetails);
        result.put("message", "Expert " + selectedExpert.getNom() + " assigned to sinistre " + sinistre.getNumeroSinistre());

        return result;
    }

    public int calculateExpertWorkload(Long expertId) {
        Expert expert = findExpertById(expertId);
        List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expertId);
        int claimCount = activeClaims.size();
        int totalPriorityScore = activeClaims.stream()
                .mapToInt(Sinistre::getPriorityScore)
                .sum();

        expert.setWorkload(claimCount, totalPriorityScore);
        return expert.getWorkload();
    }

    private Optional<Expert> assignExpert(Sinistre sinistre, List<Expert> availableExperts) {
        if (sinistre == null) {
            logger.warn("Cannot assign expert: sinistre is null");
            return Optional.empty();
        }
        if (availableExperts.isEmpty()) {
            logger.warn("Cannot assign expert: no experts available");
            return Optional.empty();
        }

        logger.debug("Sinistre type: {}", sinistre.getType());

        List<Expert> matchingExperts = availableExperts.stream()
                .filter(expert -> {
                    boolean matchesSpecialite = expert.getSpecialite().equalsIgnoreCase(sinistre.getType());
                    logger.debug("Expert {} specialite: {}, matches: {}", expert.getNom(), expert.getSpecialite(), matchesSpecialite);
                    return matchesSpecialite;
                })
                .filter(expert -> {
                    boolean hasRoleExpert = expert.getUser() != null &&
                            expert.getUser().getRoles().stream()
                                    .anyMatch(role -> role.getName().equals("ROLE_EXPERT"));
                    logger.debug("Expert {} has ROLE_EXPERT: {}", expert.getNom(), hasRoleExpert);
                    return hasRoleExpert;
                })
                .toList();

        if (matchingExperts.isEmpty()) {
            logger.warn("No experts found for sinistre type: {}", sinistre.getType());
            return Optional.empty();
        }

        matchingExperts.forEach(expert -> {
            List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expert.getId());
            int claimCount = activeClaims.size();
            int totalPriorityScore = activeClaims.stream()
                    .mapToInt(Sinistre::getPriorityScore)
                    .sum();
            expert.setWorkload(claimCount, totalPriorityScore);
            logger.debug("Expert {} workload: {}", expert.getNom(), expert.getWorkload());
        });

        return matchingExperts.stream()
                .map(expert -> new ExpertScore(expert, calculateScore(sinistre, expert)))
                .peek(score -> logger.debug("Expert {} score: {}", score.expert.getNom(), score.score))
                .max((s1, s2) -> Double.compare(s1.score, s2.score))
                .map(expertScore -> {
                    logger.debug("Selected expert: {}", expertScore.expert.getNom());
                    return Optional.of(expertScore.expert);
                })
                .orElse(Optional.empty());
    }

    private double calculateScore(Sinistre sinistre, Expert expert) {
        double proximityScore = calculateProximityScore(sinistre, expert);
        double workloadScore = calculateWorkloadScore(expert.getWorkload());
        double complexityAdjustment = adjustForComplexity(sinistre.getPriorityScore());

        logger.debug("Expert {}: Proximity score: {}, Workload score: {}, Complexity adjustment: {}",
                expert.getNom(), proximityScore, workloadScore, complexityAdjustment);

        return 0.5 * proximityScore + 0.4 * workloadScore + 0.1 * complexityAdjustment;
    }

    private double calculateProximityScore(Sinistre sinistre, Expert expert) {
        if (sinistre.getLieu() == null || expert.getLocation() == null) {
            return 50.0;
        }

        String clientLocation = (sinistre.getPolicy() != null && sinistre.getPolicy().getClient() != null)
                ? sinistre.getPolicy().getClient().getAdresse()
                : null;

        double sinistreProximity = calculateIndividualProximityScore(sinistre.getLieu(), expert.getLocation());
        double clientProximity = (clientLocation != null)
                ? calculateIndividualProximityScore(clientLocation, expert.getLocation())
                : sinistreProximity;

        return (sinistreProximity + clientProximity) / 2.0;
    }

    private double calculateIndividualProximityScore(String sinistreLieu, String expertLocation) {
        if (sinistreLieu.equalsIgnoreCase(expertLocation)) {
            return 100.0;
        }

        String sinistreRegion = findRegion(sinistreLieu);
        String expertRegion = findRegion(expertLocation);

        if (sinistreRegion != null && expertRegion != null && sinistreRegion.equals(expertRegion)) {
            return 85.0;
        }

        if (sinistreLieu.toLowerCase().contains(expertLocation.toLowerCase()) ||
                expertLocation.toLowerCase().contains(sinistreLieu.toLowerCase())) {
            return 75.0;
        }

        if (sinistreRegion != null && expertRegion != null) {
            int regionDistance = calculateRegionDistance(sinistreRegion, expertRegion);
            return Math.max(25.0, 60.0 - (regionDistance * 10.0));
        }

        return 25.0;
    }

    private String findRegion(String location) {
        if (location == null) {
            return null;
        }

        String normalizedLocation = location.toLowerCase().trim();

        for (Map.Entry<String, List<String>> entry : REGION_MAPPINGS.entrySet()) {
            if (entry.getKey().toLowerCase().equals(normalizedLocation)) {
                return entry.getKey();
            }
        }

        for (Map.Entry<String, List<String>> entry : REGION_MAPPINGS.entrySet()) {
            if (entry.getValue().stream()
                    .anyMatch(city -> city.toLowerCase().equals(normalizedLocation) ||
                            normalizedLocation.contains(city.toLowerCase()))) {
                return entry.getKey();
            }
        }

        return null;
    }

    private int calculateRegionDistance(String region1, String region2) {
        Map<String, Map<String, Integer>> distanceMatrix = new HashMap<>();

        distanceMatrix.put("Tunis", new HashMap<String, Integer>() {{
            put("Tunis", 0);
            put("Bizerte", 1);
            put("Sousse", 2);
            put("Sfax", 3);
        }});

        distanceMatrix.put("Bizerte", new HashMap<String, Integer>() {{
            put("Tunis", 1);
            put("Bizerte", 0);
            put("Sousse", 3);
            put("Sfax", 4);
        }});

        distanceMatrix.put("Sousse", new HashMap<String, Integer>() {{
            put("Tunis", 2);
            put("Bizerte", 3);
            put("Sousse", 0);
            put("Sfax", 1);
        }});

        distanceMatrix.put("Sfax", new HashMap<String, Integer>() {{
            put("Tunis", 3);
            put("Bizerte", 4);
            put("Sousse", 1);
            put("Sfax", 0);
        }});

        return distanceMatrix.getOrDefault(region1, new HashMap<>()).getOrDefault(region2, 5);
    }

    private double calculateWorkloadScore(int workload) {
        if (workload <= 30) {
            return 100.0;
        } else if (workload <= 50) {
            return 90.0 - ((workload - 30) * 0.5);
        } else if (workload <= 100) {
            return 80.0 - ((workload - 50) * 0.6);
        } else {
            return Math.max(0, 50.0 - ((workload - 100) * 0.5));
        }
    }

    private double adjustForComplexity(Integer priorityScore) {
        if (priorityScore == null) {
            return 50.0;
        }

        if (priorityScore >= 80) {
            return 70.0;
        } else if (priorityScore >= 50) {
            return 60.0;
        } else {
            return 100.0 - priorityScore;
        }
    }

    public List<Sinistre> getAssignedClaims(Long expertId) {
        Expert expert = findExpertById(expertId);
        return sinistreRepository.findByExpertId(expertId);
    }

    public Sinistre getClaimDetails(Long expertId, Long sinistreId) {
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (expertId != null) {
            Expert expert = findExpertById(expertId);
            if (sinistre.getExpert() == null || !expert.equals(sinistre.getExpert())) {
                throw new RuntimeException("Expert with id " + expertId + " is not assigned to sinistre " + sinistreId);
            }
        }
        return sinistre;
    }

    public Report writeReport(Long expertId, Long sinistreId, String content) {
        Expert expert = findExpertById(expertId);
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (sinistre.getExpert() == null || !sinistre.getExpert().getId().equals(expertId)) {
            throw new RuntimeException("Expert not assigned to this sinistre");
        }

        Report report = new Report();
        report.setSinistre(sinistre);
        report.setExpert(expert);
        report.setContent(content);
        report.setCreatedAt(LocalDateTime.now());
        Report savedReport = reportRepository.save(report);

        notifyAdminAndClient(sinistre, "New report submitted by expert " + expert.getNom() + " for sinistre " + sinistre.getNumeroSinistre());

        return savedReport;
    }

    public Meeting scheduleMeeting(Long expertId, Long sinistreId, Meeting.MeetingType meetingType, LocalDateTime meetingDate, String requestedLocation) {
        Expert expert = findExpertById(expertId);
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (sinistre.getExpert() == null || !sinistre.getExpert().getId().equals(expertId)) {
            throw new RuntimeException("Expert not assigned to this sinistre");
        }

        String meetingLocation = findNearestAgency(sinistre.getLieu(), expert.getLocation(), requestedLocation);
        if (meetingLocation == null) {
            throw new RuntimeException("No suitable assurance agency found for meeting");
        }

        Meeting meeting = new Meeting();
        meeting.setSinistre(sinistre);
        meeting.setExpert(expert);
        meeting.setMeetingType(meetingType);
        meeting.setMeetingDate(meetingDate);
        meeting.setLocation(meetingLocation);
        meeting.setCreatedAt(LocalDateTime.now());
        Meeting savedMeeting = meetingRepository.save(meeting);

        Client client = clientRepository.findById(sinistre.getPolicy().getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Notification notification = new Notification();
        notification.setUser(client.getUser());
        notification.setSinistre(sinistre);
        notification.setMessage("Meeting scheduled: " + meetingType + " on " + meetingDate + " at " + meetingLocation);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);

        return savedMeeting;
    }

    public void approveClaim(Long expertId, Long sinistreId, String approvalNotes) {
        Expert expert = findExpertById(expertId);
        Sinistre sinistre = sinistreRepository.findById(sinistreId)
                .orElseThrow(() -> new RuntimeException("Sinistre not found with id: " + sinistreId));
        if (sinistre.getExpert() == null || !sinistre.getExpert().getId().equals(expertId)) {
            throw new RuntimeException("Expert not assigned to this sinistre");
        }

        sinistre.setStatus(ClaimStatus.APPROVED);
        sinistre.setMontantIndemnisation(sinistre.calculerMontantIndemnisation());
        sinistreRepository.save(sinistre);

        String message = "Claim " + sinistre.getNumeroSinistre() + " approved by expert " + expert.getNom() +
                ". Indemnification: " + sinistre.getMontantIndemnisation() + ". Notes: " + approvalNotes;
        notifyAdminAndClient(sinistre, message);
    }

    private void notifyAdminAndClient(Sinistre sinistre, String message) {
        Client client = clientRepository.findById(sinistre.getPolicy().getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        Notification clientNotification = new Notification();
        clientNotification.setUser(client.getUser());
        clientNotification.setSinistre(sinistre);
        clientNotification.setMessage(message);
        clientNotification.setCreatedAt(LocalDateTime.now());
        clientNotification.setRead(false);
        notificationRepository.save(clientNotification);

        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            Notification adminNotification = new Notification();
            adminNotification.setUser(admin.getUser());
            adminNotification.setSinistre(sinistre);
            adminNotification.setMessage(message);
            adminNotification.setCreatedAt(LocalDateTime.now());
            adminNotification.setRead(false);
            notificationRepository.save(adminNotification);
        }
    }

    private String findNearestAgency(String sinistreLieu, String expertLocation, String requestedLocation) {
        if (ASSURANCE_AGENCIES.contains(requestedLocation)) {
            return requestedLocation;
        }

        if (sinistreLieu != null) {
            for (String agency : ASSURANCE_AGENCIES) {
                if (sinistreLieu.toLowerCase().contains(agency.toLowerCase())) {
                    return agency;
                }
            }

            String sinistreRegion = findRegion(sinistreLieu);
            if (sinistreRegion != null && ASSURANCE_AGENCIES.contains(sinistreRegion)) {
                return sinistreRegion;
            }
        }

        if (expertLocation != null) {
            for (String agency : ASSURANCE_AGENCIES) {
                if (expertLocation.toLowerCase().contains(agency.toLowerCase())) {
                    return agency;
                }
            }

            String expertRegion = findRegion(expertLocation);
            if (expertRegion != null && ASSURANCE_AGENCIES.contains(expertRegion)) {
                return expertRegion;
            }
        }

        return ASSURANCE_AGENCIES.get(0);
    }

    public Map<String, Object> calculateExpertWorkloadWithDetails(Long expertId) {
        Expert expert = findExpertById(expertId);
        Map<String, Object> workloadDetails = new HashMap<>();

        workloadDetails.put("expertId", expert.getId());
        workloadDetails.put("expertName", expert.getNom());
        workloadDetails.put("specialite", expert.getSpecialite());
        workloadDetails.put("location", expert.getLocation());

        List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expertId);
        int claimCount = activeClaims.size();
        int totalPriorityScore = activeClaims.stream()
                .mapToInt(Sinistre::getPriorityScore)
                .sum();

        expert.setWorkload(claimCount, totalPriorityScore);

        workloadDetails.put("activeClaimCount", claimCount);
        workloadDetails.put("totalPriorityScore", totalPriorityScore);
        workloadDetails.put("calculatedWorkload", expert.getWorkload());
        workloadDetails.put("workloadScore", calculateWorkloadScore(expert.getWorkload()));

        List<Map<String, Object>> claimDetails = activeClaims.stream()
                .map(claim -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("id", claim.getId());
                    details.put("numeroSinistre", claim.getNumeroSinistre());
                    details.put("type", claim.getType());
                    details.put("status", claim.getStatus());
                    details.put("priorityScore", claim.getPriorityScore());
                    details.put("lieu", claim.getLieu());
                    return details;
                })
                .collect(Collectors.toList());

        workloadDetails.put("activeClaims", claimDetails);

        return workloadDetails;
    }

    public Map<String, Object> getAssignmentAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        List<Expert> allExperts = expertRepository.findAllWithExpertRole();
        List<Map<String, Object>> expertWorkloads = new ArrayList<>();

        for (Expert expert : allExperts) {
            Map<String, Object> expertData = new HashMap<>();
            List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expert.getId());
            int claimCount = activeClaims.size();
            int totalPriorityScore = activeClaims.stream()
                    .mapToInt(Sinistre::getPriorityScore)
                    .sum();

            expert.setWorkload(claimCount, totalPriorityScore);

            expertData.put("expertId", expert.getId());
            expertData.put("expertName", expert.getNom());
            expertData.put("specialite", expert.getSpecialite());
            expertData.put("location", expert.getLocation());
            expertData.put("activeClaimCount", claimCount);
            expertData.put("workload", expert.getWorkload());

            expertWorkloads.add(expertData);
        }

        analytics.put("expertWorkloads", expertWorkloads);

        if (!expertWorkloads.isEmpty()) {
            double avgWorkload = expertWorkloads.stream()
                    .mapToInt(e -> (Integer) e.get("workload"))
                    .average()
                    .orElse(0);

            int minWorkload = expertWorkloads.stream()
                    .mapToInt(e -> (Integer) e.get("workload"))
                    .min()
                    .orElse(0);

            int maxWorkload = expertWorkloads.stream()
                    .mapToInt(e -> (Integer) e.get("workload"))
                    .max()
                    .orElse(0);

            double stdDeviation = calculateStandardDeviation(
                    expertWorkloads.stream()
                            .mapToInt(e -> (Integer) e.get("workload"))
                            .toArray()
            );

            analytics.put("averageWorkload", avgWorkload);
            analytics.put("minWorkload", minWorkload);
            analytics.put("maxWorkload", maxWorkload);
            analytics.put("workloadStdDeviation", stdDeviation);
            analytics.put("workloadVariance", stdDeviation * stdDeviation);
        }

        Map<String, Long> locationCounts = allExperts.stream()
                .collect(Collectors.groupingBy(
                        Expert::getLocation,
                        Collectors.counting()
                ));
        analytics.put("locationDistribution", locationCounts);

        Map<String, Long> specialiteCounts = allExperts.stream()
                .collect(Collectors.groupingBy(
                        Expert::getSpecialite,
                        Collectors.counting()
                ));
        analytics.put("specialiteDistribution", specialiteCounts);

        return analytics;
    }

    public List<Map<String, Object>> previewExpertScores(Sinistre sinistre) {
        List<Expert> availableExperts = expertRepository.findAllWithExpertRole();
        List<Expert> matchingExperts = availableExperts.stream()
                .filter(expert -> expert.getSpecialite().equalsIgnoreCase(sinistre.getType()))
                .filter(expert -> expert.getUser() != null &&
                        expert.getUser().getRoles().stream()
                                .anyMatch(role -> role.getName().equals("ROLE_EXPERT")))
                .toList();

        if (matchingExperts.isEmpty()) {
            return Collections.emptyList();
        }

        matchingExperts.forEach(expert -> {
            List<Sinistre> activeClaims = sinistreRepository.findByExpertIdAndStatusNotClosed(expert.getId());
            int claimCount = activeClaims.size();
            int totalPriorityScore = activeClaims.stream()
                    .mapToInt(Sinistre::getPriorityScore)
                    .sum();
            expert.setWorkload(claimCount, totalPriorityScore);
        });

        List<Map<String, Object>> expertScores = new ArrayList<>();
        for (Expert expert : matchingExperts) {
            Map<String, Object> scoreDetails = new HashMap<>();
            scoreDetails.put("expertId", expert.getId());
            scoreDetails.put("expertName", expert.getNom());
            scoreDetails.put("expertLocation", expert.getLocation());
            scoreDetails.put("expertWorkload", expert.getWorkload());

            double proximityScore = calculateProximityScore(sinistre, expert);
            double workloadScore = calculateWorkloadScore(expert.getWorkload());
            double complexityAdjustment = adjustForComplexity(sinistre.getPriorityScore());
            double totalScore = 0.5 * proximityScore + 0.4 * workloadScore + 0.1 * complexityAdjustment;

            scoreDetails.put("proximityScore", proximityScore);
            scoreDetails.put("workloadScore", workloadScore);
            scoreDetails.put("complexityAdjustment", complexityAdjustment);
            scoreDetails.put("totalScore", totalScore);

            String sinistreRegion = findRegion(sinistre.getLieu());
            String expertRegion = findRegion(expert.getLocation());

            Map<String, Object> locationDetails = new HashMap<>();
            locationDetails.put("sinistreLocation", sinistre.getLieu());
            locationDetails.put("sinistreRegion", sinistreRegion);
            locationDetails.put("expertLocation", expert.getLocation());
            locationDetails.put("expertRegion", expertRegion);

            if (sinistreRegion != null && expertRegion != null && !sinistreRegion.equals(expertRegion)) {
                int regionDistance = calculateRegionDistance(sinistreRegion, expertRegion);
                locationDetails.put("regionDistance", regionDistance);
            }

            scoreDetails.put("locationDetails", locationDetails);
            expertScores.add(scoreDetails);
        }

        expertScores.sort((a, b) -> Double.compare(
                (Double) b.get("totalScore"),
                (Double) a.get("totalScore")
        ));

        return expertScores;
    }

    private double calculateStandardDeviation(int[] values) {
        if (values.length == 0) return 0;

        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum() / values.length;

        return Math.sqrt(variance);
    }

    private static class ExpertScore {
        Expert expert;
        double score;

        ExpertScore(Expert expert, double score) {
            this.expert = expert;
            this.score = score;
        }
    }
}*/

