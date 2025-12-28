package io.github.bugsbunnyshah.kafkadd.kafka.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.bugsbunnyshah.kafkadd.kafka.TopicProperties;
import io.github.bugsbunnyshah.kafkadd.kafka.core.IdempotencyStore;

import org.apache.kafka.clients.producer.ProducerRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BusinessProcessor {
    private static final Logger log = LoggerFactory.getLogger(BusinessProcessor.class);

    private KafkaTemplate<Object, Object> kafkaTemplate;

    private TopicProperties topicProperties;

    private final IdempotencyStore store;

    public BusinessProcessor(KafkaTemplate<Object, Object> kafkaTemplate, TopicProperties topicProperties,
                             IdempotencyStore store) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
        this.store = store;
    }

    public void process(String payload) {
        JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
        String domain = "user";
        String email = payloadJson.get("email").getAsString();

        String key = domain + ":" + email;

        // send
        String inputTopic = this.topicProperties.getInput();
        kafkaTemplate.send(new ProducerRecord<>(inputTopic, key, payload));
    }

    public void doWork(String payload) {
        try {
            // commit-safe business logic
            log.info("execute_business_logic " + payload);

            JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
            String eventId = payloadJson.get("email").getAsString();

            if (this.store.seen(eventId)) {
                // if logic_error, then business dlq
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("business_error", "duplicate_event");
                errorJson.addProperty("payload", payload);

                this.handleError(errorJson.toString());
            } else {
                this.store.markProcessed(eventId);
            }
        } catch (Exception e) {
            // if system_error, then business dlq
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("system_error", "system_error");
            errorJson.addProperty("system_error", e.getMessage());
            errorJson.addProperty("payload", payload);

            this.handleError(errorJson.toString());
        }
    }

    public void handleError(String payload) {
        String domain = "user";
        String key = domain + ": error";

        // send
        String dlqBusinessTopic = this.topicProperties.getDlq().getBusiness();
        kafkaTemplate.send(new ProducerRecord<>(dlqBusinessTopic, key, payload));
    }
}
