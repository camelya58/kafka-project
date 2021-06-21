# kafka_project

Simple Spring Boot project with Apache Kafka (for windows), SSL certificate and Swagger.

## Step 1
Install [Apache Kafka](https://kafka.apache.org/downloads) for Windows.
Then run zookeeper in terminal:
```
cd C:\kafka_2.13-2.8.0\bin
.\windows\zookeeper-server-start.bat ..\config\zookeeper.properties
```
Run kafka in other terminal:
```
cd C:\kafka_2.13-2.8.0\bin
.\windows\kafka-server-start.bat ..\config\server.properties
```

## Step 2
Generate an SSL certificate in a keystore.
Open other terminal.
```
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365 -storepass password
```
These are the command we can run:

* *genkeypair*: generates a key pair;
* *alias*: the alias name for the item we are generating;
* *keyalg*: the cryptographic algorithm to generate the key pair;
* *keysize*: the size of the key. We have used 2048 bits, but 4096 would be a better choice for production;
* *storetype*: the type of keystore;
* *keystore*: the name of the keystore;
* *validity*: validity number of days;
* *storepass*: a password for the keystore.

When running the previous command, we will be asked to input some information.
```
What is your first and last name? 
    [Unknown]: 
What is the name of your organizational unit? 
    [Unknown]: 
What is the name of your organization? 
    [Unknown]: 
What is the name of your City or Locality? 
    [Unknown]: 
What is the name of your State or Province? 
    [Unknown]: 
What is the two-letter country code for this unit? 
    [Unknown]: 
Is CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct? 
    [no]: yes 

Enter key password for <tomcat> 
    (RETURN if same as keystore password):
```

## Step 3
Add dependencies.
```xml
 <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.26</version>
        </dependency>

        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.9.2</version>
        </dependency>

        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.9.2</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>
```
Create main class.
```java
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
```

## Step 4
Create application.properties and fill it.
```properties
server.port=8443
http.port=8080

kafka.url=localhost:9092
kafka.topic=registrations
kafka.consumer.group-id=group

server.ssl.key-store=../../keystore.p12
server.ssl.key-store-password=Keypass
server.ssl.key-store-type=pkcs12
server.ssl.key-alias=tomcat
server.ssl.enabled=true
```

If you would store your cert in project you could use *classpath:keystore.p12*.

* *server.port*: the port on which the server is listening. We have used 8443 rather than the default 8080 port.
* *server.ssl.key-store*: the path to the key store which contains the SSL certificate. In our example, we want Spring Boot to look for it in the classpath.
* *server.ssl.key-store-password*: the password used to access the key store.
* *server.ssl.key-store-type*: the type of the key store (JKS or PKCS12).
* *server.ssl.key-alias*: the alias that identifies the key in the key store.

## Step 5
Create configuration class for Swagger.
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("kafka_project.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /* Describe APIs */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Kafka Rest APIs")
                .version("1.0-SNAPSHOT")
                .build();
    }
}
```

## Step 6
Create configuration class for kafka producer.
```java
@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.url}")
    private String kafkaUrl;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        //list of host:port used for establishing the initial connections to the Kafka cluster
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```
Create service to send messages.
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SenderService {

    @Value("${kafka.topic}")
    private String kafkaTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String message) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(kafkaTopic, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

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
```
## Step 7
Create configuration class for kafka consumer.
```java
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${kafka.url}")
    private String kafkaUrl;

    @Value("${kafka.consumer.group-id}")
    private String groupId;


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```
Create service to receive messages.
```java
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
```

## Step 8
For convenience create controller to print messages.
```java
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
```

## Step 9
When using Spring Security, we can configure it to require automatically block any request coming from a non-secure HTTP channel.

We need to extend the WebSecurityConfigurerAdapter class, since the security.require-ssl property has been deprecated.
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**",
                "/ws/**"
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .requiresChannel()
                .anyRequest()
                .requiresSecure();
    }
}
```

## Step 10
Redirect HTTP requests to HTTPS.

Now that we have enabled HTTPS in our Spring Boot application and blocked any HTTP request, 
we want to redirect all traffic to HTTPS.

Spring allows defining just one network connector in application.properties.
 Since we have used it for HTTPS, we have to set the HTTP connector programmatically for our Tomcat web server.
```java
@Configuration
public class ServerConfig {

    @Value("${http.port}")
    int httpPort;

    @Value("${server.port}")
    int redirectPort;

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(getHttpConnector());
        return tomcat;
    }

    private Connector getHttpConnector() {
        var connector = new Connector();
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(redirectPort);
        return connector;
    }

}
```

## Step 11
Run the application.
Send messages and receive.
