/*package com.vermeg.sinistpro.event;

import com.vermeg.sinistpro.model.Sinistre;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClaimEventPublisher {
    private final KafkaTemplate<String, Sinistre> kafkaTemplate;

    public ClaimEventPublisher(KafkaTemplate<String, Sinistre> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierÉvénement(Sinistre sinistre) {
        kafkaTemplate.send("sinistre-events", sinistre);
    }
}*/
