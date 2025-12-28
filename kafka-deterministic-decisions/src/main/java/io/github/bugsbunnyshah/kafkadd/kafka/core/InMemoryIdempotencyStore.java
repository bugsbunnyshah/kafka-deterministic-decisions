package io.github.bugsbunnyshah.kafkadd.kafka.core;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile({"default","kafka"})
public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    public boolean seen(String eventId) { 
        return processed.contains(eventId); 
    }
    
    public void markProcessed(String eventId) { 
        processed.add(eventId); 
    }
}
