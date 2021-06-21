package com.github.camelya58.kafka_project;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class KafkaProjectApp
 *
 * @author Kamila Meshcheryakova
 * created 21.06.2021
 */
@Slf4j
@SpringBootApplication
public class KafkaProjectApp {

    public static void main(String[] args) throws UnknownHostException {

        Environment environment = SpringApplication.run(KafkaProjectApp.class, args).getEnvironment();
        String protocol = "http";
        if (environment.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://127.0.0.1:{}/swagger-ui.html\n\t" +
                        "External: \t{}://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                "KafkaProjectApp",
                protocol,
                environment.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                environment.getProperty("server.port"),
                environment.getActiveProfiles());

    }

}
