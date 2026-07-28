package com.ibatulanand.notificationservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics notificationTopics(
            @Value("${app.kafka.topics.notification}") String notificationTopic,
            @Value("${app.kafka.topics.notification-retry}") String notificationRetryTopic,
            @Value("${app.kafka.topics.notification-dlt}") String notificationDltTopic) {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(notificationTopic).partitions(1).replicas(1).build(),
                TopicBuilder.name(notificationRetryTopic).partitions(1).replicas(1).build(),
                TopicBuilder.name(notificationDltTopic).partitions(1).replicas(1).build()
        );
    }
}
