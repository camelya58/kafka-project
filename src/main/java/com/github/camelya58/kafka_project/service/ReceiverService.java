package com.github.camelya58.kafka_project.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * Class ReceiverService
 *
 * @author Kamila Meshcheryakova
 * created 21.06.2021
 */
@Slf4j
@Service
public class ReceiverService {

    private final CountDownLatch latch = new CountDownLatch(3);

    @KafkaListener(topics = "${kafka.topic}", groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void receive(ConsumerRecord<Long, Object> record) {
        log.info("Receive message= [{}] from topic = [{}] ", record.value(), record.topic());
        latch.countDown();
    }
}
