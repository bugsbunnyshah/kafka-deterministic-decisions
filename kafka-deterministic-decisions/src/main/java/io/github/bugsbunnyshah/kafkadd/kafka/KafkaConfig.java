package io.github.bugsbunnyshah.kafkadd.kafka;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

public class KafkaConfig {

    private final TopicProperties topicProperties;

    // single source of truth: env var
    private static final String ENV_BOOTSTRAP = "KAFKA_BOOTSTRAP_SERVERS";

    public KafkaConfig(TopicProperties topicProperties) {
        this.topicProperties = topicProperties;
    }

    private String bootstrap() {
        String v = System.getenv(ENV_BOOTSTRAP);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    ENV_BOOTSTRAP + " env var missing. " +
                            "Use localhost:9092 for host JVM OR broker:29092 for container JVM."
            );
        }
        return v.trim();
    }

    @PostConstruct
    void dumpKafka() {
        // one line, no noise
        System.out.println("KAFKA_BOOTSTRAP_SERVERS=" + bootstrap());
    }

    // ---------------------------------------------------------------------
    // Producer configuration
    // ---------------------------------------------------------------------
    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // optional: keep it deterministic-ish for your demo
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ---------------------------------------------------------------------
    // Consumer configuration
    // ---------------------------------------------------------------------
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "idempotency-demo");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // keep it simple; your listener ack-mode can still be RECORD
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    // ---------------------------------------------------------------------
    // error-handling-for-pipeline-dlq
    // ---------------------------------------------------------------------
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> template) {

        String dlq = this.topicProperties.getDlq().getPipeline();

        return new DeadLetterPublishingRecoverer(
                template,
                (rec, ex) -> new TopicPartition(dlq, rec.partition())
        );
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        // 0 retries → DLQ immediately
        return new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 0));
    }
}
