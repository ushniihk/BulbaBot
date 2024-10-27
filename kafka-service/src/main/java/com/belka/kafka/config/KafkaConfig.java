package com.belka.kafka.config;

import com.belka.core.CoreConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EntityScan("com.belka.kafka")
@EnableJpaRepositories("com.belka.kafka")
@Import(CoreConfig.class)
public class KafkaConfig {

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    /**
     * Creates a new Kafka topic.
     *
     * @return the NewTopic object
     */
    @Bean
    public NewTopic weatherTopic() {
        return TopicBuilder.name(topicName)
                .build();
    }
}
