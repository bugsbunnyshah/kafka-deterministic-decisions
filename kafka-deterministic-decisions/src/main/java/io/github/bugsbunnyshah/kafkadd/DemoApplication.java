package io.github.bugsbunnyshah.kafkadd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "io.github.bugsbunnyshah.kafkadd")
@Import(io.github.bugsbunnyshah.kafkadd.kafka.KafkaConfig.class)
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
