# create input & output topics (inside docker)
docker-compose exec broker kafka-topics --bootstrap-server broker:9092 \
  --create --topic hello-world.dlq --partitions 1 --replication-factor 1
