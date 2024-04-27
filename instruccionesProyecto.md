# Instrucciones para poner en funcionamiento el proyecto

Este proyecto actualmente está hecho para funcionar únicamente con MongoDB, las funciones relacionadas con MySQL no están implementadas. Para poner en marcha el proyecto, simplemente hay que ejecutar el archivo ```build-mongo.bat``` en Windows, el cual ejecutará el comando ```make``` y levantará el proyecto en Docker con el archivo ```docker-compose-devel-mongo.yml```. Si se ejecuta desde otro sistema operativo, se deben ejecutar los siguientes comandos para levantar el proyecto:

```sh
make
docker-compose -f docker-compose-devel-mongo.yml up -d --build
```