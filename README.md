# RSO: Template microservice


## Build and run commands
```bash
mvn clean package
cd api/target
java -jar scraper-api-1.0.0-SNAPSHOT.jar
```
Available at: localhost:8082/v1/scraper

## Run in IntelliJ IDEA
Add new Run configuration and select the Application type. In the next step, select the module api and for the main class com.kumuluz.ee.EeApplication.

Available at: localhost:8082/v1/scraper

## Docker commands
```bash
docker build -t template-image .   
docker images
docker run template-image    
docker tag template-image barbaralipnik/scraper:latest  
docker push barbaralipnik/scraper
docker ps
```

```bash
docker network ls  
docker network rm rso
docker network create rso
docker inspect pg-scraper
docker run -p 8081:8081 --network rso -e KUMULUZEE_DATASOURCES0_CONNECTIONURL=jdbc:postgresql://pg-scraper:5432/scraper barbaralipnik/scraper:latest
```

## Consul
```bash
consul agent -dev
```
Available at: localhost:8500

Key: environments/dev/services/scraper-service/1.0.0/config/rest-properties/maintenance-mode

Value: true or false

## Kubernetes
```bash
kubectl version
kubectl --help
kubectl get nodes
kubectl create -f k8s/scraper-deployment.yaml 
kubectl apply -f k8s/scraper-deployment.yaml 
kubectl get services 
kubectl get deployments
kubectl get pods
kubectl logs scraper-deployment-6f59c5d96c-rjz46
kubectl delete pod scraper-deployment-6f59c5d96c-rjz46
```
Secrets: https://kubernetes.io/docs/concepts/configuration/secret/

