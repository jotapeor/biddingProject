# EditaisGOV Backend

REST API for EditaisGOV, a government procurement (bidding) platform. Handles authentication, tender (edital) management, and bid (lance) processing. Companion service for the [EditaisGOV Frontend](https://github.com/jotapeor/biddingFrontEnd).

## Tech Stack

- Java 21
- Spring Boot 4.0.6 (Web MVC)
- MySQL
- JWT (jjwt) for stateless authentication
- Maven

## Architecture

Layered structure: `controller` -> `service` -> `repository` -> `model`. Authentication is stateless via JWT; user identity and role are extracted directly from the token payload (`TokenService.extrairClaim`), avoiding a database lookup on every request.

Roles: `FORNECEDOR` (supplier, places bids) and a buyer/admin role that creates and manages tenders.

## API Reference

### Authentication — `/api/autenticar`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/registrar` | Registers a new user |
| POST | `/logar` | Authenticates a user and returns a JWT |
| GET | `/verificar-email` | Checks if an email is already registered |
| GET | `/verificar-nome` | Checks if a username is already taken |

### Tenders — `/api/editais`

All routes require an `Authorization: Bearer <token>` header.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/criar` | Creates a new tender |
| GET | `/` | Lists tenders; `?urgente=true` filters tenders closing within 48 hours |
| GET | `/{id}` | Retrieves a single tender |
| PUT | `/{id}` | Updates a tender |
| DELETE | `/{id}` | Deletes a tender |
| POST | `/{id}/lances` | Submits a bid on a tender |
| GET | `/{id}/lances` | Lists bids on a tender |

### Bids — `/api/lances`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/meus-lances` | Lists bids placed by the authenticated user |
| DELETE | `/{id}` | Deletes a bid |

## Data Model

**UserDTO** — id, nome, email, senha (hashed), role.

**EditalDTO** — id, titulo, descricao, data_fechamento, status, vencedor.

**LanceDTO** — id, valor, data_lance (server-assigned), id_edital, id_usuario, nome_fornecedor, vencedor.

## Requirements

- JDK 21+
- Maven 3.9+
- MySQL 8

## Setup

1. Create the database and import the schema:
   ```bash
   mysql -u root -p < dump.sql
   ```
2. Set the required environment variables (see below).
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The API starts on `http://localhost:8080`.

## Environment Variables

| Variable | Description | Required |
|----------|--------------|----------|
| `JWT_SECRET` | Secret key used to sign JWTs | Yes |

> Database connection details are currently hardcoded in `repository/Conexao.java` (URL, username, and password), separate from the JPA/env-based configuration used elsewhere. This must be externalized before the credentials are committed to version control again — see the security note in the related repository.

## Related Project

[biddingFrontEnd](https://github.com/jotapeor/biddingFrontEnd) — Thymeleaf client consuming this API.
