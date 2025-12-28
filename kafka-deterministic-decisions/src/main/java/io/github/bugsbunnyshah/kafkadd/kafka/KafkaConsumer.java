package io.github.bugsbunnyshah.kafkadd.kafka;


import io.github.bugsbunnyshah.kafkadd.kafka.service.BusinessProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final BusinessProcessor processor;

    public KafkaConsumer(BusinessProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "${topics.input}", groupId = "idempotency-demo")
    public void onMessage(
            @Payload String value,
            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) {
        // String id = (key != null && key.trim().length() > 0) ? key : "" +
        // value.hashCode();
        log.info("consumed o={} partition={} t={} key={} value={}", offset, partition, ts, key, value);

        try {
            processor.doWork(value.toString());
        } catch (Exception e) {
            // Transport must not fail; log only.
            log.error("callback-unexpected err={}", e.toString());
        }
    }
}
