curl -X POST http://localhost:8080/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "payload": "{'name': 'test', 'email': 'test@test.com'}"
  }'