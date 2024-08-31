package com.facegov.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .build();
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
    }
}
