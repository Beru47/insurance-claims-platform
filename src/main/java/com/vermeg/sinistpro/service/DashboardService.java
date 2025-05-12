package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.ClaimStatus;
import com.vermeg.sinistpro.model.DashboardDTO;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.model.SinistreDTO;
import com.vermeg.sinistpro.repository.SinistreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private SinistreRepository sinistreRepository;

    public DashboardDTO getDashboardData() {
        DashboardDTO dashboardDTO = new DashboardDTO();
        // Total sinistres
        long totalSinistres = sinistreRepository.count();
        dashboardDTO.setTotalSinistres(totalSinistres);

        // Pending sinistres
        long pendingSinistres = sinistreRepository.findByStatus(ClaimStatus.PENDING).size();
        dashboardDTO.setPendingSinistres(pendingSinistres);

        // Recent sinistres
        List<Sinistre> recentSinistres = sinistreRepository.findTop5ByOrderByDateDesc();
        List<SinistreDTO> recentSinistreDTOs = recentSinistres.stream().map(this::convertToDTO).collect(Collectors.toList());
        dashboardDTO.setRecentSinistres(recentSinistreDTOs);

        return dashboardDTO;
    }

    private SinistreDTO convertToDTO(Sinistre sinistre) {
        SinistreDTO dto = new SinistreDTO();
        dto.setId(sinistre.getId());
        dto.setDescription(sinistre.getDescription());
        dto.setDate(sinistre.getDate());
        return dto;
    }
}