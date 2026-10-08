# Nivora Backend

Spring Boot REST API backend for Nivora. This repository contains the backend only; the React frontend is maintained separately in `nivora-frontend`.

## Technologies

- Java 21
- Spring Boot 3.5.0
- Maven
- Spring Web and Bean Validation
- Spring Data JPA
- H2 for local development and tests
- MySQL for the MySQL deployment profile
- OpenAI Responses API for health guidance

## Architecture

```text
React Frontend (separate repository)
              |
              v
         REST API
              |
              v
     Spring Boot Controllers
              |
              v
           Services
          /        \
         v          v
      Database   OpenAI API
```

## Project Structure

- `src/main/java/com/nivora/api/` - application, REST controllers, DTOs, services, configuration, and exception handling
- `src/main/java/com/nivora/api/story/` - story entities, repositories, services, and endpoints
- `src/main/resources/` - default, local H2, and MySQL Spring configuration
- `src/test/java/` - backend unit and integration tests
- `pom.xml` - Maven dependencies, Java version, and Spring Boot build configuration

## Requirements

- JDK 21
- Maven 3.6.3 or newer
- No separate database is required for local development; the default `local` profile uses file-backed H2 under `data/`
- A MySQL server and credentials are required only when using the `mysql` profile
- An OpenAI API key is required to use the health guidance endpoint

## Environment Variables

Copy `.env.example` as a reference, then set the variables in your shell or deployment environment. Spring Boot does not automatically load a `.env` file. Never commit a populated `.env` file.

- `OPENAI_API_KEY` - required for `POST /api/health/guidance`
- `OPENAI_BASE_URL` - optional; defaults to `https://api.openai.com/v1`
- `OPENAI_MODEL` - optional; defaults to `gpt-4.1-mini`
- `NIVORA_CORS_ALLOWED_ORIGIN_PATTERNS` - optional; defaults to `http://localhost:5173`
- `PORT` - optional; defaults to `8080`
- `SPRING_PROFILES_ACTIVE` - optional; defaults to `local`; set to `mysql` for MySQL
- `DB_URL` - optional for the `mysql` profile; defaults to a local MySQL URL for database `nivora`
- `DB_USERNAME` and `DB_PASSWORD` - required for the `mysql` profile

## Run the Backend

From the repository root:

```bash
mvn clean install
mvn spring-boot:run
```

The default local profile uses H2. To run with MySQL, configure `DB_USERNAME` and `DB_PASSWORD`, optionally set `DB_URL`, and activate the profile, for example:

```bash
# PowerShell
$env:SPRING_PROFILES_ACTIVE = "mysql"
mvn spring-boot:run
```

The service listens at `http://localhost:8080` by default. The API base URL is `http://localhost:8080/api`.

## API Endpoints

### Health Guidance

`POST /api/health/guidance`

Gets conversational health information using the OpenAI Responses API. The request requires a nonblank `message` (up to 2,000 characters); optional `previousMessages` and `conversationHistory` provide context.

### Stories

`GET /api/stories?visitorId={uuid}&category={category}`

Lists stories for a visitor. `visitorId` is required; `category` is optional.

`PUT /api/stories/{storyId}/likes`

Likes a story for a visitor. The JSON body contains `visitorId`.

`DELETE /api/stories/{storyId}/likes?visitorId={uuid}`

Removes a visitor's like from a story. `visitorId` is required.

All endpoints are under `/api` and accept requests from the configured frontend origin.

## Frontend Connection

The React frontend is hosted in the separate GitHub repository `nivora-frontend` and communicates with this backend through the REST API. The default CORS origin is `http://localhost:5173`; set `NIVORA_CORS_ALLOWED_ORIGIN_PATTERNS` when the frontend runs at a different origin.
