# Payment Service

Microservice handling payment creation and retrieval for MediAID.
Communicates with `disbursement-service` via OpenFeign for disbursement validation.

## Port: 8082

## Endpoints
| Method | URL | Role | Description |
|--------|-----|------|-------------|
| POST   | /api/payment | OFFICER | Create a payment |
| GET    | /api/payment/{id} | CITIZEN, OFFICER | Get by payment ID |
| GET    | /api/payment/disbursement/{id} | CITIZEN, OFFICER | Get by disbursement ID |

## Startup Order
1. Start `service-registry` (port 8761)
2. Start `config-server` (port 8888)
3. Start `disbursement-service` (port 8081)
4. Start `payment-service` (port 8082)

## Database
Create MySQL database: `mediaid_payment`
Update credentials in `application.yml`.

## Swagger UI
http://localhost:8082/swagger-ui.html
