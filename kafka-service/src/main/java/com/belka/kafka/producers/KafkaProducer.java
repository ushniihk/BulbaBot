package com.belka.kafka.producers;

import com.belka.weather_core.models.WeatherHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducer {

    @Value("${spring.kafka.topic.name}")
    private String topic;

    private final KafkaTemplate<String, WeatherHistory> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, WeatherHistory> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Sends a message to the Kafka topic.
     *
     * @param data the WeatherHistory data to send
     */
    public void sendMessage(WeatherHistory data) {
        log.info(String.format("Message sent -> %s", data.toString()));
        Message<WeatherHistory> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        kafkaTemplate.send(message);
    }
}
