package com.facegov.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LegislationComparisonHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String PARAMETER_NAME = "/myapp/anthropic-api-key";
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/chat/completions";
    private static final String DYNAMODB_TABLE_NAME = "LegislationComparisons";

    private final SsmClient ssmClient;
    private final DynamoDbClient dynamoDbClient;
    private final HttpClient client;

    public LegislationComparisonHandler() {
        this.client = HttpClient.newHttpClient();
        this.ssmClient = SsmClient.builder().region(Region.US_EAST_1).build();
        this.dynamoDbClient = DynamoDbClient.builder().region(Region.US_EAST_1).build();
    }

    // Constructor for dependency injection (used in tests)
    public LegislationComparisonHandler(SsmClient ssmClient, DynamoDbClient dynamoDbClient, HttpClient httpClient) {
        this.ssmClient = ssmClient;
        this.dynamoDbClient = dynamoDbClient;
        this.client = httpClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            // Retrieve Anthropic API key from Parameter Store
            String apiKey = getParameterValue(PARAMETER_NAME);

            // Parse input
            JSONObject inputJson = new JSONObject(input.getBody());
            String country1 = inputJson.getString("country1");
            String country2 = inputJson.getString("country2");
            String legislationTopic = inputJson.getString("topic");

            // Prepare prompt for Anthropic API
            String prompt = String.format(
                "Compare the legislation on %s between %s and %s. Provide a concise summary of key similarities and differences.",
                legislationTopic, country1, country2
            );

            // Call Anthropic API
            String comparisonResult = callAnthropicAPI(apiKey, prompt);

            // Store result in DynamoDB
            String id = storeInDynamoDB(country1, country2, legislationTopic, comparisonResult);

            // Prepare and return response
            JSONObject outputJson = new JSONObject();
            outputJson.put("id", id);
            outputJson.put("country1", country1);
            outputJson.put("country2", country2);
            outputJson.put("topic", legislationTopic);
            outputJson.put("comparison", comparisonResult);

            response.setStatusCode(200);
            response.setBody(outputJson.toString());

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("Error: " + e.getMessage());
        }

        return response;
    }

    private String getParameterValue(String parameterName) {
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(parameterName)
                .withDecryption(true)
                .build();

        return ssmClient.getParameter(parameterRequest).parameter().value();
    }

    private String callAnthropicAPI(String apiKey, String prompt) throws Exception {


        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "claude-3-sonnet-20240229");
        requestBody.put("max_tokens", 1000);
        requestBody.put("messages", new JSONObject[]{ new JSONObject().put("role", "user").put("content", prompt) });

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_API_URL))
                .header("Content-Type", "application/json")
                .header("X-API-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getJSONArray("content").getJSONObject(0).getString("text");
    }

    private String storeInDynamoDB(String country1, String country2, String topic, String comparison) {
        String id = UUID.randomUUID().toString();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        item.put("country1", AttributeValue.builder().s(country1).build());
        item.put("country2", AttributeValue.builder().s(country2).build());
        item.put("topic", AttributeValue.builder().s(topic).build());
        item.put("comparison", AttributeValue.builder().s(comparison).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(DYNAMODB_TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(putItemRequest);

        return id;
    }
}
