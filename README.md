# Projet intégration SOC
Projet intégration SOC 3a 2017

# Application androïd
Faite un build et un launch ensuite vous pouvez l'utilisé
  https://github.com/florian-texier/Projet_integration_SOC/tree/master/Application

# Base de donner
Pour installer la base de donnée couchdb, rentré cette ligne de commande dans un terminal.
```
docker pull gauthier66/couchdb
```
Ensuite créer le ficher docker-compose.yml et y incorporer le yml suivant:
```
version: "3"
     services:
couchdb:
       	    container_name: couchdb 
 		    image: klaemo/couchdb:latest
    networks: [interne]
    labels:
      - traefik.backend=couchdb
      - traefik.frontend.rule=Host:couchdb.mignolet.fr
      - traefik.port=5984
networks:
  interne:
    external:
      name: interne

```
Ensuite lancer le docker
```
docker-compose up -d
```

# Rancher
Pour installer rancher suiver le tutoriel présent sur le site.
  http://rancher.com/

# Création du swarm avec rancher
## Installer le service router
```
docker pull gauthier66/routerint
```
Vous pouvez consulter les sources du python à cette adresse:
  https://github.com/mignolet/scavengerRouter.git

Ensuite créer le ficher docker-compose.yml et y incorporer le yml suivant:
```
version: '2'
			services:
  				router:
   				   image: gauthier66/routerint
    				environment:
  				    LOGINCOUCHDB: admin
  				    PWDCOUCHDB: adminsoc
  				  ports:
 				   - 4000:5000/tcp
```
Ensuite lancer le docker
```
docker-compose up -d
```
## Installer le service instance equipe
```
docker pull gauthier66/instanceequipe_web
```
Vous pouvez consulter les sources du python à cette adresse:
  https://github.com/mignolet/instanceEquipe.git

Ensuite créer le ficher docker-compose.yml et y incorporer le yml suivant:
```
version: '2'
			services:
  			 web:
  			  image: gauthier66/instanceequipe_web
```
Ensuite lancer le docker
```
docker-compose up -d
```

# Topic MQTTCLOUD
Ce topic va permettre le retour d'affichage sur la raspberry pi.
Vous pouvez retrouver le code pythona cette adresse:
  https://github.com/florian-texier/Projet_integration_SOC/tree/master/pythonRasp
