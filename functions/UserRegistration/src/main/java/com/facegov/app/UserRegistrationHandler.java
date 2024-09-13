package com.facegov.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;

public class UserRegistrationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson gson = new Gson();
    private static final DynamoDbClient dynamoDb = DynamoDbClient.builder()
            .region(Region.US_EAST_1) // Replace with your region
            .build();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Facegov UserRegistration. Function started");

        try {
            logger.log("Facegov UserRegistration. Input: " + gson.toJson(input));

            if (input.getBody() == null || input.getBody().isEmpty()) {
                logger.log("Facegov UserRegistration. Request body is missing or empty");
                return createResponse(400, "Request body is missing or empty");
            }

            String rawBody = input.getBody();
            UserData userData;

            try {
                // Try to parse the body as a JSON string
                userData = gson.fromJson(rawBody, UserData.class);
            } catch (JsonSyntaxException e) {
                // If parsing fails, the body might already be a JSON object
                // Try to parse it directly
                userData = gson.fromJson(gson.fromJson(rawBody, String.class), UserData.class);
            }

            logger.log("Facegov UserRegistration. Parsed UserData: " + userData);
            if (isNullOrEmpty(userData.username) || isNullOrEmpty(userData.email) || isNullOrEmpty(userData.password)) {
                logger.log("Facegov UserRegistration. Missing required fields");
                return createResponse(400, "Missing required fields");
            }

            logger.log("Facegov UserRegistration. Preparing DynamoDB item");
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("userId", AttributeValue.builder().s(userData.username).build());
            item.put("email", AttributeValue.builder().s(userData.email).build());
            item.put("password", AttributeValue.builder().s(userData.password).build());

            logger.log("Facegov UserRegistration. Creating PutItemRequest");
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName("Users") // Replace with your table name
                    .item(item)
                    .build();

            logger.log("Facegov UserRegistration. Putting item in DynamoDB");
            dynamoDb.putItem(putItemRequest);

            logger.log("Facegov UserRegistration. Item successfully put in DynamoDB");
            return createResponse(200, "User registered successfully");
        } catch (Exception e) {
            logger.log("Facegov UserRegistration. Error: " + e.getMessage());
            return createResponse(500, "Failed to register user: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(gson.toJson(Map.of("message", "Facegov UserRegistration. " + body)));

        // Add CORS headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "https://facegov.com");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        headers.put("Access-Control-Allow-Methods", "POST");
        response.setHeaders(headers);
        return response;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private static class UserData {
        String username;
        String email;
        String password;

        @Override
        public String toString() {
            return "UserData{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", password='[REDACTED]'" +
                    '}';
        }
    }
}