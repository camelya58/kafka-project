package com.github.camelya58.kafka_project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Class SenderService
 *
 * @author Kamila Meshcheryakova
 * created 21.06.2021
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SenderService {

    @Value("${kafka.topic}")
    private String kafkaTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String message) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(kafkaTopic, message);
        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.info("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Unable to send message=["
                        + message + "] due to : " + ex.getMessage());
            }
        });
    }
}
