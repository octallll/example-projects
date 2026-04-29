package backend.academy.linktracker.scrapper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic}")
    private String topicName;

    @Value("${app.kafka.partitions}")
    private int partitions;

    @Value("${app.kafka.replicas}")
    private int replicas;

    @Bean
    public NewTopic linkUpdatesTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
