## Konfiguracja minikube

Start minikube
```bash
minikube start --cpus=4 --memory=4g
```

Dodaj repozytorium i zainstaluj Postgresa z prostą konfiguracją:
```bash
kubectl create configmap pg-init --from-file=k8s/jdbc_init.sql
```

```bash 
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install pg bitnami/postgresql \
  --set auth.username=myuser,auth.password=mypassword,auth.database=mydatabase \
  --set primary.resources.requests.cpu=100m,primary.resources.requests.memory=256Mi \
  --set primary.resources.limits.cpu=500m,primary.resources.limits.memory=512Mi \
  --set initdbScripts.initdbScriptsFile=init.sql \
  --set initdbScripts.configMap=pg-init
 ```
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install prometheus prometheus-community/prometheus \
  --set server.persistentVolume.enabled=false \
  --set alertmanager.persistentVolume.enabled=false
```
```bash
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
helm install grafana grafana/grafana \
  --set persistence.enabled=false  
```

Sprawdź status:
```bash
   kubectl get pods -o wide
```

Uruchom aplikację jdbc
```bash
kubectl apply -f k8s/jdbc.yaml
```
Sprawdź status:
```bash
   kubectl get pods -l app=jdbc-app
```

Logowanie do Grafany
```bash
kubectl port-forward svc/grafana 3000:80
```
W innym terminalu:
```bash
kubectl get secret grafana -o jsonpath="{.data.admin-password}" | base64 -d ; echo
```

Dodaj źródło danych w Grafanie
W Grafanie:
1. Configuration → Data Sources → Add data source → Prometheus
2. URL: http://prometheus-server.default.svc.cluster.local:80
3. Save & Test → powinieneś zobaczyć zielony komunikat.

Sprawdź liste targetów Prometheusa
```bash
kubectl port-forward svc/prometheus-server 9090:80
```
Otwórz http://localhost:9090/targets
Powinieneś zobaczyć wśród targetów adres jdbc-app.default.svc.cluster.local:8080 z endpointem /actuator/prometheus.

Uruchom krótki test
```bash
curl http://$(minikube ip):$(kubectl get svc jdbc-app -o jsonpath='{.spec.ports[0].nodePort}')/api/orders/1
```
lub:
```bash
curl http://localhost:8080/api/orders/1
```

Potem w Grafanie na zakładce “Explore” wpisz np.:
```bash
http_server_requests_seconds_count{uri="/api/orders/1"}
```