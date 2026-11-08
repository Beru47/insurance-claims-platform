package com.vermeg.sinistpro.service;

import com.vermeg.sinistpro.model.Client;
import com.vermeg.sinistpro.model.Policy;
import com.vermeg.sinistpro.model.Sinistre;
import com.vermeg.sinistpro.repository.SinistreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndemnificationServiceTest {

    @Mock
    SinistreRepository repo;

    IndemnificationService service;
    Policy policy;

    @BeforeEach
    void setUp() {
        service = new IndemnificationService(repo);
        Client client = new Client();
        client.setId(1L);
        policy = new Policy();
        policy.setValid(true);
        policy.setGuarantees(List.of("incendie", "accident"));
        policy.setClient(client);
    }

    private Sinistre claim(String type, String description) {
        Sinistre s = new Sinistre();
        s.setType(type);
        s.setDescription(description);
        s.setPolicy(policy);
        s.setPriorityScore(10);
        when(repo.findById(1L)).thenReturn(Optional.of(s));
        when(repo.save(any(Sinistre.class))).thenAnswer(i -> i.getArgument(0));
        return s;
    }

    private static void assertAmount(String expected, Sinistre s) {
        assertEquals(0, new BigDecimal(expected).compareTo(s.getMontantIndemnisation()),
                "got " + s.getMontantIndemnisation());
    }

    @Test
    void fireClaimWithFewPastClaimsGetsBasePlusTenPercent() {
        claim("incendie", "kitchen fire");
        when(repo.findByPolicyClientId(1L)).thenReturn(Collections.emptyList());
        assertAmount("5500", service.calculateIndemnity(1L));
    }

    @Test
    void severeDescriptionMultipliesBaseByOnePointFive() {
        claim("accident", "severe collision");
        when(repo.findByPolicyClientId(1L)).thenReturn(Collections.emptyList());
        assertAmount("1650", service.calculateIndemnity(1L));
    }

    @Test
    void clientWithMoreThanThreePastClaimsGetsTenPercentReduction() {
        claim("incendie", "fire");
        when(repo.findByPolicyClientId(1L)).thenReturn(List.of(new Sinistre(), new Sinistre(), new Sinistre(), new Sinistre()));
        assertAmount("4500", service.calculateIndemnity(1L));
    }

    @Test
    void invalidPolicyYieldsZero() {
        policy.setValid(false);
        claim("incendie", "fire");
        when(repo.findByPolicyClientId(1L)).thenReturn(Collections.emptyList());
        assertAmount("0", service.calculateIndemnity(1L));
    }

    @Test
    void unknownClaimThrows() {
        when(repo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.calculateIndemnity(2L));
    }
}
