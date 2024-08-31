#!/bin/bash

# Function to create directory if it doesn't exist
create_dir() {
    if [ ! -d "$1" ]; then
        mkdir -p "$1"
        echo "Created directory: $1"
    else
        echo "Directory already exists: $1"
    fi
}

# Function to create file if it doesn't exist
create_file() {
    if [ ! -f "$1" ]; then
        touch "$1"
        echo "Created file: $1"
    else
        echo "File already exists: $1"
    fi
}

# Create main project directory
create_dir "facegov-project"
cd facegov-project

# Create common directory and subdirectories
create_dir "common/src/main/java/com/facegov/common/models"
create_dir "common/src/main/java/com/facegov/common/utils"
create_dir "common/src/main/java/com/facegov/common/config"
create_file "common/build.gradle"

# Create functions directory and subdirectories
create_dir "functions"

# User Management
create_dir "functions/user-management/src/main/java/com/facegov/user"
create_file "functions/user-management/src/main/java/com/facegov/user/CreateUserHandler.java"
create_file "functions/user-management/src/main/java/com/facegov/user/UpdateUserHandler.java"
create_file "functions/user-management/src/main/java/com/facegov/user/DeleteUserHandler.java"
create_file "functions/user-management/build.gradle"

# Post Management
create_dir "functions/post-management/src/main/java/com/facegov/post"
create_file "functions/post-management/src/main/java/com/facegov/post/CreatePostHandler.java"
create_file "functions/post-management/src/main/java/com/facegov/post/DeletePostHandler.java"
create_file "functions/post-management/src/main/java/com/facegov/post/ListPostsHandler.java"
create_file "functions/post-management/build.gradle"

# Friend Management
create_dir "functions/friend-management/src/main/java/com/facegov/friend"
create_file "functions/friend-management/src/main/java/com/facegov/friend/SendFriendRequestHandler.java"
create_file "functions/friend-management/src/main/java/com/facegov/friend/AcceptFriendRequestHandler.java"
create_file "functions/friend-management/src/main/java/com/facegov/friend/ListFriendsHandler.java"
create_file "functions/friend-management/build.gradle"

# Notification
create_dir "functions/notification/src/main/java/com/facegov/notification"
create_file "functions/notification/src/main/java/com/facegov/notification/SendNotificationHandler.java"
create_file "functions/notification/src/main/java/com/facegov/notification/ListNotificationsHandler.java"
create_file "functions/notification/build.gradle"

# Analytics
create_dir "functions/analytics/src/main/java/com/facegov/analytics"
create_file "functions/analytics/src/main/java/com/facegov/analytics/TrackUserActivityHandler.java"
create_file "functions/analytics/src/main/java/com/facegov/analytics/GenerateUserInsightsHandler.java"
create_file "functions/analytics/build.gradle"

# Create tests directory
create_dir "tests/integration"
create_dir "tests/unit"

# Create infrastructure directory
create_dir "infrastructure/terraform"

# Create scripts directory
create_dir "scripts"

# Create root level files
create_file ".gitignore"
create_file "README.md"
create_file "build.gradle"

echo "FaceGov project structure creation/verification completed successfully!"
