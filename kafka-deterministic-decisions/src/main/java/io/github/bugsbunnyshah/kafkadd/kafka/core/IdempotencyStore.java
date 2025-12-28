package io.github.bugsbunnyshah.kafkadd.kafka.core;

public interface IdempotencyStore {
    boolean seen(String eventId);
    void markProcessed(String eventId);
}
