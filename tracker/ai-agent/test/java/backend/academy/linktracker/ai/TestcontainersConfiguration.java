package backend.academy.linktracker.ai;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    private static final KafkaContainer KAFKA_CONTAINER;

    static {
        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

        KAFKA_CONTAINER.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("app.kafka.topic", () -> "test-topic");

        registry.add(
                "spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.LongSerializer");
        registry.add(
                "spring.kafka.producer.value-serializer",
                () -> "org.apache.kafka.common.serialization.ByteArraySerializer");

        registry.add(
                "spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.LongDeserializer");

        registry.add(
                "spring.kafka.consumer.value-deserializer",
                () -> "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        registry.add("spring.kafka.consumer.properties.specific.avro.reader", () -> "true");
    }
}
