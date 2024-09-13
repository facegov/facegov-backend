package com.facegov.app.registration_notifier;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

public class App implements RequestHandler<DynamodbEvent, String> {

    private static final String SENDER = "info@facegov.com";
    private static final String RECIPIENT = "puglieseweb@gmail.com";
    private static final String SUBJECT = "New Facegov User Registration";

    @Override
    public String handleRequest(DynamodbEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Facegov Send Email. Function started");

        try {
            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.defaultClient();

            for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
                if ("INSERT".equals(record.getEventName())) {


                    String emailBody = "<html>"
                            + "<head></head>"
                            + "<body>"
                            + "<h1>New User Registration</h1>"
                            + "<p>"
                            + "A new item has been inserted into DynamoDB:\n" + record.getDynamodb().getNewImage().toString()
                            + "</p>"
                            + "</body>"
                            + "</html>";

                    SendEmailRequest request = new SendEmailRequest()
                            .withDestination(new Destination().withToAddresses(RECIPIENT))
                            .withMessage(new Message()
                                    .withBody(new Body()
                                            .withHtml(new Content().withCharset("UTF-8").withData(emailBody))
                                            .withText(new Content().withCharset("UTF-8").withData(emailBody)))
                                    .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
                            .withSource(SENDER);

                    client.sendEmail(request);
                    context.getLogger().log("Email sent successfully for new DynamoDB entry.");
                }
            }
            return "Processed " + event.getRecords().size() + " records.";
        } catch (Exception ex) {
            context.getLogger().log("Error processing request: " + ex.getMessage());
            logger.log("Facegov Email Sender: ".concat(ex.getMessage()));

            throw new RuntimeException(ex);
        }
    }
}
