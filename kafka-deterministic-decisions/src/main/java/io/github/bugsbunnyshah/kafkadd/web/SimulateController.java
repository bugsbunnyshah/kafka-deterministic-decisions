package io.github.bugsbunnyshah.kafkadd.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bugsbunnyshah.kafkadd.kafka.core.IdempotencyStore;
import io.github.bugsbunnyshah.kafkadd.kafka.service.BusinessProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulateController {
    private static Logger logger = LoggerFactory.getLogger(SimulateController.class);

    private final IdempotencyStore store;
    private final BusinessProcessor processor;

    public SimulateController(IdempotencyStore store, BusinessProcessor processor) {
        this.store = store;
        this.processor = processor;
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> simulate(@RequestBody String req) {
        JsonObject requestJson = JsonParser.parseString(req).getAsJsonObject();
        JsonObject responseJson = new JsonObject();

        if (req == null || req.trim().length() == 0) {
            responseJson.addProperty("failure", "request_body_is_empty");
            ResponseEntity<String> responseEntity = ResponseEntity.accepted().body(responseJson.toString());
            return responseEntity;
        }

        String payloadStr = requestJson.get("payload").getAsString();

        processor.process(payloadStr);

        responseJson.addProperty("success", "true");

        ResponseEntity<String> responseEntity = ResponseEntity.accepted().body(responseJson.toString());
        return responseEntity;
    }
}
