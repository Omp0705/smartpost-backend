package com.om.smartpost.shipment.prediction;

import com.om.smartpost.core.config.PredictionProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PredictionClient {

    private final RestClient predictionRestClient;
    private final PredictionProperties predictionProperties;

    public PredictionClient(@Qualifier("predictionRestClient") RestClient predictionRestClient,
                            PredictionProperties predictionProperties) {
        this.predictionRestClient = predictionRestClient;
        this.predictionProperties = predictionProperties;
    }

    public PredictionClientResponse predict(PredictionPayload payload) {
        try {
            PredictionClientResponse response = predictionRestClient.post()
                    .uri(predictionProperties.predictPath())
                    .body(payload)
                    .retrieve()
                    .body(PredictionClientResponse.class);

            if (response == null || response.resolveSlot() == null || response.resolveSlot().isBlank()) {
                throw new IllegalStateException("Prediction response did not contain a slot");
            }

            return response;
        } catch (RestClientException ex) {
            throw new IllegalStateException("Prediction request failed", ex);
        }
    }
}

