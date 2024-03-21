@echo off
make
docker-compose -f .\docker-compose-devel-mongo.yml up -d --build