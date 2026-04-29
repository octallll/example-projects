package backend.academy.linktracker.scrapper;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class IntegrationTest {
    protected static final PostgreSQLContainer<?> POSTGRES;
    private static final KafkaContainer KAFKA_CONTAINER;
    private static final GenericContainer<?> VALKEY;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("scrapper")
                .withUsername("user")
                .withPassword("pass");
        POSTGRES.start();

        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

        KAFKA_CONTAINER.start();

        VALKEY = new GenericContainer<>(DockerImageName.parse("valkey/valkey:8.0")).withExposedPorts(6379);
        VALKEY.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/master.xml");

        registry.add("app.scheduler.enable", () -> "false");
        registry.add("logging.level.org.liquibase", () -> "DEBUG");

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("app.kafka.topic", () -> "test-topic");

        registry.add(
                "spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.LongSerializer");
        registry.add(
                "spring.kafka.producer.value-serializer",
                () -> "org.apache.kafka.common.serialization.ByteArraySerializer");

        registry.add("spring.kafka.consumer.properties.specific.avro.reader", () -> "true");

        registry.add("spring.data.redis.host", VALKEY::getHost);
        registry.add("spring.data.redis.port", () -> VALKEY.getMappedPort(6379).toString());
        registry.add("app.redis.ttl", () -> "10m");
    }
}
