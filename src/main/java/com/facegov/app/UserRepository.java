package com.facegov.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> userTable;

    @Autowired
    public UserRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.userTable = dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(User.class));
    }

    public User save(User user) {
        userTable.putItem(user);
        return user;
    }

    public User findById(String id) {
        return userTable.getItem(r -> r.key(k -> k.partitionValue(id)));
    }

    public void deleteById(String id) {
        userTable.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }
}