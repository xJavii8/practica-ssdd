@echo off
make
docker-compose -f .\docker-compose-devel.yml up -d --build