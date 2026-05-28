# Disbursement Service

Microservice handling disbursement creation and retrieval for MediAID.

## Port: 8081

## Endpoints
| Method | URL | Role | Description |
|--------|-----|------|-------------|
| POST   | /api/disbursement | OFFICER | Create a disbursement |
| GET    | /api/disbursement/{id} | CITIZEN, OFFICER | Get by disbursement ID |
| GET    | /api/disbursement/claim/{claimId} | CITIZEN, OFFICER | Get by claim ID |

## Startup Order
1. Start `service-registry` (port 8761)
2. Start `config-server` (port 8888)
3. Start `disbursement-service` (port 8081)

## Database
Create MySQL database: `mediaid_disbursement`
Update credentials in `application.yml`.

## Swagger UI
http://localhost:8081/swagger-ui.html
