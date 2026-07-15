# Codibly Backend Service

This is a backend service for the Codibly recruitment task. It provides an API for retrieving current and forecasted energy generation data and calculating optimal electric vehicle charging windows based on clean energy availability.

## Technologies
* Java 21
* Spring Boot 3
* Maven
* Docker

## Configuration
To run the application, you must provide the following environment variables. If you are deploying the application (e.g., on Render), set these in the "Environment" section of your service dashboard:

* `CARBON_API_URL`: The base URL for the Carbon Intensity API.
* `APP_FRONTEND_URL`: The URL of the deployed frontend application (required for CORS configuration).

Note: If you are running the application locally, you can set these variables in your shell or add them to your IDE run configuration. The application also provides default values in application.properties for development purposes, but it is recommended to set them explicitly in production environments.

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/generation` | Returns a list of daily energy generation mixes. |
| `GET` | `/api/v1/generation/window?hours={n}` | Calculates the optimal charging window of `n` hours. |

## Testing
The project includes both unit and integration tests covering business logic and API endpoints. To execute the tests, run:
```
./mvnw test
```

## Deployment
The application is containerized using Docker. A Dockerfile is provided in the root directory, enabling deployment on cloud platforms like Render.
