package com.turism.users.services;

import com.google.gson.Gson;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.turism.users.dtos.UserMessageDTO;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MessageQueueService {
    private static final String queueName = "usersQueue";
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public MessageQueueService() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    public void sendMessage(UserMessageDTO user) {
        Gson gson = new Gson();

        kafkaTemplate.send(queueName, gson.toJson(user));
    }
}
