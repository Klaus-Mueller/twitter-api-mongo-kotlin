# Twitter API with MongoDB and Kotlin

## Overview

The `twitter-api-mongo-kotlin` project is a backend API that simulates basic functionalities of Twitter. It allows users to perform operations like user authentication, tweeting, following/unfollowing users, and viewing timelines. The API is built using Kotlin and leverages MongoDB for data storage.

## Purpose

The purpose of this API is to provide a backend service that supports typical social media functionalities. It can be used as a learning tool, a base for further development, or as part of a larger application that requires social media features.

## Key Features

- **User Authentication**: Login and logout functionalities to manage user sessions.
- **Tweet Management**: Create, like, unlike, and delete tweets.
- **User Interaction**: Follow and unfollow other users.
- **Timeline**: View tweets from followed users, sorted by relevance.

## Technologies Used

- **Kotlin**: The primary programming language used for development.
- **MongoDB**: A NoSQL database used for storing user and tweet data.
- **Spark Framework**: A micro web framework for handling HTTP requests.
- **Gson**: A library for converting Java/Kotlin objects to JSON and vice versa.
- **Docker**: For containerizing the application to ensure consistent development and production environments.
- **Maven**: The build tool used for this project.

## Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Ensure you have JDK 8 or higher installed.
- **MongoDB**: Install MongoDB and ensure it is running.
- **Docker**: For running the application in a containerized environment.
- **Maven**: The build tool used for this project.

### Installation

1. **Clone the repository**:

   ```sh
   git clone https://github.com/yourusername/twitter-api-mongo-kotlin.git
   cd twitter-api-mongo-kotlin

2. **Build the Project**:

   ```sh
   mvn clean install

3. **Run the application:**
   - **Locally:**

      ```sh
      mvn exec:java -Dexec.mainClass="com.twitter.Main"

   - **Using Docker:**
       ```sh
         docker build -t twitter-api .
         docker run -p 4567:4567 twitter-api

## API Endpoints

### User Routes:

- `POST /login`: Login a user.
- `GET /logout`: Logout the current user.
- `GET /getUser`: Retrieve user details.
- `POST /saveUser`: Create a new user.
- `POST /followUser`: Follow another user.
- `POST /unfollowUser`: Unfollow a user.
- `GET /getFollowers`: Get followers of the current user.
- `GET /getFollowing`: Get users followed by the current user.
- `POST /updateUser`: Update user details.
- `GET /searchUsers`: Search for users.

### Tweet Routes:

- `POST /saveTweet`: Create a new tweet.
- `GET /getUserTweets`: Get tweets by a specific user.
- `POST /likeTweet`: Like a tweet.
- `POST /unlikeTweet`: Unlike a tweet.
- `POST /deleteTweet`: Delete a tweet.
- `GET /timeline`: Get the timeline of the current user.

## Configuration

The application can be configured using environment variables:

- `PORT`: The port on which the application will run (default: `4567`).
- `MONGO_URI`: The URI for connecting to the MongoDB instance.
- `ALLOWED_ORIGIN`: The allowed origin for CORS.
