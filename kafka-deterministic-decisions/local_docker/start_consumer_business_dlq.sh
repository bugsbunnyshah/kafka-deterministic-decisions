docker-compose exec broker kafka-console-consumer --bootstrap-server broker:29092 \
  --topic hello-world-business.dlq --from-beginning