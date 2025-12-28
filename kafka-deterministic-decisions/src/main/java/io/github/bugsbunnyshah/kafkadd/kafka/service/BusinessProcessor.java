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
            log.info("execute_business_logic " + payload);
            /*if(true) {
                throw new RuntimeException("error_dlq_test");
            }*/

            JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
            String eventId = payloadJson.get("email").getAsString();

            if (this.store.seen(eventId)) {
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("business_error", "duplicate_event");
                errorJson.addProperty("payload", payload);

                this.publishBusinessReject(errorJson.toString());
                return; // important: handled
            }

            this.store.markProcessed(eventId);

        } catch (Exception e) {
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("system_error", "system_error");
            errorJson.addProperty("message", e.getMessage()); // don't overwrite
            errorJson.addProperty("payload", payload);

            this.publishSystemDlq(errorJson.toString());
        }
    }

    public void publishBusinessReject(String payload) {
        String domain = "user";
        String key = domain + ":reject";
        String topic = this.topicProperties.getDlq().getBusiness(); // hello-world-business.dlq
        kafkaTemplate.send(new ProducerRecord<>(topic, key, payload));
    }

    public void publishSystemDlq(String payload) {
        String domain = "user";
        String key = domain + ":system";
        String topic = this.topicProperties.getDlq().getPipeline(); // hello-world.dlq
        kafkaTemplate.send(new ProducerRecord<>(topic, key, payload));
    }

}
