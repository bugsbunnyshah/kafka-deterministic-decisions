curl -X POST http://localhost/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "payload": "{'name': 'test', 'email': 'test@test.com'}"
  }'