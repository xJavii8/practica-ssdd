# Instrucciones para ejecutar Docker Swarm (laboratorio)
## Manager

Hay que poner el siguiente comando: 

```sh
docker swarm init --advertise-addr <IP-DEL-ORDENADOR>
```

Es necesario levantar los contenedores previamente en el manager con:

```sh 
docker-compose -f docker-compose-devel-mongo.yml up -d --build
```

Ahora creamos el servicio:

```sh
docker service create --name registry --publish published=5000,target=5000 registry:2
```

Cogemos el ID de las imágenes de:

```sh
docker images
```

Y les creamos un tag de la siguiente forma:

```sh
docker tag <ID-IMAGEN> 127.0.0.1:5000/ssdd-frontend
docker tag <ID-IMAGEN> 127.0.0.1:5000/ssdd_db-mongo
docker tag <ID-IMAGEN> 127.0.0.1:5000/ssdd_backend-rest
docker tag <ID-IMAGEN> 127.0.0.1:5000/ssdd_backend-rest-externo
docker tag <ID-IMAGEN> 127.0.0.1:5000/ssdd_backend-grpc
```

Les hacemos un push:

```sh
docker push 127.0.0.1:5000/ssdd-frontend
docker push 127.0.0.1:5000/ssdd_db-mongo
docker push 127.0.0.1:5000/ssdd_backend-rest
docker push 127.0.0.1:5000/ssdd_backend-rest-externo
docker push 127.0.0.1:5000/ssdd_backend-grpc
```

Hacemos el deploy:

```sh
docker stack deploy -c docker-compose-swarm.yml llamachat
```

## Worker

Nos unimos al manager con el comando que nos da el manager al hacer docker swarm init, tiene el siguiente aspecto:

```sh
docker swarm join --token <TOKEN> <IP>:2377
```

Nos metemos al navegador y ponemos la siguiente URL: ```localhost:5010```, y debería funcionar correctamente el frontend. Si sales del docker swarm, no funcionará

## Resolución de problemas

Si a la hora de descargar las dependencias del frontend da un error en el lab, reiniciar docker con:

```sh
sudo /etc/init.d/docker restart
```