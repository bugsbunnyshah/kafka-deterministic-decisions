docker-compose exec broker kafka-console-consumer --bootstrap-server broker:9092 \
  --topic hello-world.dlq --from-beginning