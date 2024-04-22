backend-rest:
	(cd es.um.sisdist.backend.Service \
		&& mvn -DskipTests clean package)
	(cd es.um.sisdist.backend.ServiceExtern \
		&& mvn -DskipTests clean package)

containers:
	(cd es.um.sisdist.backend.Service \
		&& docker build -t dsevilla/ssdd-backend-rest:1.0 .)
	(cd es.um.sisdist.backend.ServiceExtern \
		&& docker build -t dllamachat/ssdd-backend-rest-extern:1.0 .)
		
clean:
	docker rmi -f dsevilla/ssdd-backend-rest:1.0
	docker rmi -f dllamachat/ssdd-backend-rest-extern:1.0 .