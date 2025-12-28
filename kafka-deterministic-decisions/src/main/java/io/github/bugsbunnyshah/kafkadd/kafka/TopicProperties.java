package io.github.bugsbunnyshah.kafkadd.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "topics")
public class TopicProperties {
    private String input;
    private Dlq dlq;

    public static class Dlq {
        private String pipeline;
        private String business;

        public String getPipeline() {
            return pipeline;
        }

        public void setPipeline(String pipeline) {
            this.pipeline = pipeline;
        }

        public String getBusiness() {
            return business;
        }

        public void setBusiness(String business) {
            this.business = business;
        }
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Dlq getDlq() {
        return dlq;
    }

    public void setDlq(Dlq dlq) {
        this.dlq = dlq;
    }
}