package com.github.camelya58.kafka_project.controller;

import com.github.camelya58.kafka_project.service.SenderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class KafkaProducerController
 *
 * @author Kamila Meshcheryakova
 * created 21.06.2021
 */
@RestController
@RequestMapping("/kafka")
@Api(value = "/kafka", produces = "application/json")
@RequiredArgsConstructor
public class KafkaProducerController {

    private final SenderService senderService;

    @ApiOperation(value = "Send message to Kafka")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Message delivered"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "Entry not found")
    })
    @PostMapping(value = "/publish")
    public String send(@RequestParam String message) {
        senderService.send(message);
        return "Сообщение опубликовано: " + message;
    }

}
