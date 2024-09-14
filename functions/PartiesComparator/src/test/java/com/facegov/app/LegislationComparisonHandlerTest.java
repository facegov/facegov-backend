package com.facegov.app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LegislationComparisonHandlerTest {

    @Mock
    private SsmClient ssmClient;

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Context context;

    private LegislationComparisonHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LegislationComparisonHandler(ssmClient, dynamoDbClient, httpClient);
    }

    @Test
    void testHandleRequest() throws Exception {
        // Mock SSM Parameter Store response
        when(ssmClient.getParameter(any(GetParameterRequest.class)))
                .thenReturn(GetParameterResponse.builder()
                        .parameter(Parameter.builder().value("mock-api-key").build())
                        .build());

        // Mock Anthropic API response
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{\"content\":[{\"text\":\"Mock comparison result\"}]}");

        // Use this line instead of the previous one
        when(httpClient.send(any(HttpRequest.class), any(BodyHandler.class))).thenReturn(mockResponse);

        // Create a mock request
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("{\"country1\":\"USA\",\"country2\":\"Canada\",\"topic\":\"Healthcare\"}");

        // Call the handler
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);

        // Assert the response
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Mock comparison result"));
        assertTrue(response.getBody().contains("USA"));
        assertTrue(response.getBody().contains("Canada"));
        assertTrue(response.getBody().contains("Healthcare"));
    }
}