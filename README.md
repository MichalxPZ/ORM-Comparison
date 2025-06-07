## Prerequisites

Przed rozpoczęciem upewnij się, że masz zainstalowane następujące narzędzia:
1. Minikube – lokalny klaster Kubernetes (zalecana wersja obsługująca Kubernetes 1.23+).
2. Kubectl – klient linii poleceń Kubernetes, skonfigurowany do komunikacji z klastrem Minikube.
3. Docker – środowisko do budowania obrazów Docker (Minikube dostarcza własny demon Dockera, z którego skorzystamy).
4. Helm – narzędzie do instalacji chartów Helm (pakietów aplikacji dla K8s).
5. Apache JMeter – narzędzie do testów wydajnościowych (może być uruchomione w trybie graficznym lub konsolowym).
6. Opcjonalnie: psql (klient PostgreSQL) lub inny klient SQL do weryfikacji zawartości bazy (można też użyć narzędzia kubectl exec/port-forward jak opisano w krokach).

## Uruchomienie klastra Minikube

Start Minikube – uruchom klaster poleceniem (opcjonalnie zwiększ pamięć/RAM jeśli planujesz testy obciążeniowe z dużymi batchami, np. do 4GB):
```bash
minikube start --memory=8192 --cpus=4 --driver=docker
```
Konfiguracja Docker w środowisku Minikube – wykonaj polecenie, które przełączy domyślny kontekst Dockera na demon Dockera działający wewnątrz Minikube:
```bash
eval $(minikube -p minikube docker-env)
```
Powyższe polecenie spowoduje, że kolejne komendy docker build/docker push będą operować na obrazie wewnątrz Minikube. Możesz zweryfikować, czy działa to poprawnie poleceniem docker images (powinno działać bez błędów).

## Budowa lokalnego obrazu Docker aplikacji 
Aby zbudować obraz, umieszczamy Dockerfile w folderze głównym projektu i wykonujemy komendę (np. w terminalu w katalogu projektu):
```bash
docker build -t spring-jdbc-app:latest .
```

## Konfiguracja bazy danych
W repozytorium znajdują się manifesty Kubernetes (pliki YAML) dla różnych baz danych. W zależności od wybranej bazy, zastosuj odpowiednie pliki:
```bash
kubectl apply -f k8s-manifests/databases/postgres.yaml
```
```bash
kubectl apply -f  k8s-manifests/databases/mysql.yaml
```
```bash
kubectl apply -f  k8s-manifests/databases/mariadb.yaml
```

## Inicjalizacja bazy danych
Aby zainicjalizować bazę danych, wykonaj następujące kroki:
1. Postgres
```bash
POD=$(kubectl get pods -l app=db,db=postgres -o jsonpath='{.items[0].metadata.name}')
kubectl cp k8s-manifests/databases/init-schema-scripts/schema-postgres.sql $POD:/tmp/schema.sql
kubectl exec -it $POD -- psql -U exampleuser -d exampledb -f /tmp/schema.sql
```
2. MySQL
```bash
POD=$(kubectl get pods -l app=db,db=mysql -o jsonpath='{.items[0].metadata.name}')
kubectl exec -i "$POD" -- mysql -u exampleuser -pexamplepass exampledb <<EOF
$(cat k8s-manifests/databases/init-schema-scripts/schema-mysql.sql)
EOF
```
3. MariaDB
```bash
POD=$(kubectl get pods -l app=db,db=mariadb -o jsonpath='{.items[0].metadata.name}')
kubectl exec -i "$POD" -- mariadb -u exampleuser -pexamplepass exampledb <<EOF
$(cat k8s-manifests/databases/init-schema-scripts/schema-mariadb.sql)
EOF
```

## Wdrażanie aplikacji
Po przygotowaniu bazy danych zainstaluj aplikację jdbc-app za pomocą Helm. 
Chart znajduje się w bieżącym katalogu (./).
Domyślnie w values.yaml ustawiona jest pewna baza (np. PostgreSQL).
Instalację wykonaj poleceniem:
```bash
helm upgrade --install jdbc-app-postgresql ./k8s-manifests/app/ \
  --set db=postgresql \
  --set database.port=5432 \
  --set database.host=postgres \
  --values k8s-manifests/app/values.yaml
```
To polecenie instaluje release o nazwie jdbc-app korzystając z pliku values.yaml.
Jeśli chcesz zmienić typ bazy danych na inny niż domyślny,
możesz użyć flagi --set db=.... Na przykład:
```bash
helm install jdbc-app ./ --set db=mysql
```

Inne aplikacje:
```bash
helm upgrade --install jdbc-app-mysql ./k8s-manifests/app/ \
  --set db=mysql \
  --set database.port=3306 \
  --set database.host=mysql \
  --values k8s-manifests/app/values.yaml
```
```bash
helm upgrade --install jdbc-app-mariadb ./k8s-manifests/app/ \
  --set db=mariadb \
  --set database.port=3306 \
  --set database.host=mariadb \
  --values k8s-manifests/app/values.yaml
```

Test:
- Sprawdź, czy aplikacja działa poprawnie:
```bash
kubectl port-forward svc/jdbc-app-postgres 8080:8080 &
curl -X POST \
     -v \
     'http://localhost:8080/api/orders/batchItems?count=500'
```

## Instalacja narzędzi do monitoringu
### Prometheus
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```
```bash
helm install prometheus prometheus-community/prometheus --wait
```
Test:
```bash
kubectl port-forward svc/prometheus-server 9090:80 &
```
Otwórz
```
http://localhost:9090/targets
```
Query PromQL:
```bash
http_server_requests_seconds_count{uri="/api/orders/batchItems"}
```

### Grafana
```bash
helm repo add grafana https://grafana.github.io/helm-charts   # jeśli repo nie jest dodane
helm repo update
helm install grafana grafana/grafana --set adminPassword=admin123 --wait
```
Portforwarding:
```bash
kubectl port-forward svc/grafana 3000:80 &
```
Logowanie do Grafany:
```
http://localhost:3000/login
```
```bash
kubectl get secret grafana -o jsonpath="{.data.admin-password}" | base64 -d && echo
```
Dodanie datasource Prometheus:
1. Zaloguj się do Grafany (domyślnie admin/admin123).
2. Przejdź do Configuration -> Data Sources.
3. Dodaj nowy data source i wybierz Prometheus.
4. W polu URL wpisz:
```
http://prometheus-server.default.svc.cluster.local:80
```
5. Wybierz opcję Access: Server (default).
6. Kliknij Save & Test.
Tworzenie dashoardu testowego:
Tworzenie dashboardu testowego – przejdź do Create -> Dashboard i dodaj nowy Panel. W polu Query wybierz nasz data source Prometheus i wpisz jedno z zapytań PromQL, np.:
1. Dla obserwacji czasu odpowiedzi:
```
rate(
    http_server_requests_seconds_sum{uri="/api/orders/batchItems",method="POST",status="200"}[5m]
  )
```
2. Dodaj kolejny Panel na tym samym dashboardzie, tym razem np. z zapytaniem:
```
rate(http_server_requests_seconds_count{uri="/api/orders/batchItems",status="200"}[1m])
```

## Query PromQL
1. Scenariusz inicjalizacji bazy danych:
```promql
(
  sum by (application) (last_over_time(http_server_requests_seconds_sum{uri="/api/init"}[5m]))
  /
  sum by (application) (last_over_time(http_server_requests_seconds_count{uri="/api/init"}[5m]))
) * 1000
```
2. Scenariusz get by id
```promql
(
  sum by (application) (last_over_time(http_server_requests_seconds_sum{uri="/api/orders/{id}"}[5m]))
  /
  sum by (application) (last_over_time(http_server_requests_seconds_count{uri="/api/orders/{id}"}[5m]))
) * 1000
```
3. Scenariusz get products
```promql
(
  sum by (application) (last_over_time(http_server_requests_seconds_sum{uri="/api/products"}[5m]))
  /
  sum by (application) (last_over_time(http_server_requests_seconds_count{uri="/api/products"}[5m]))
) * 1000
```
4. Scenariusz batch items
```promql
sum by (orm_db, param) (
  label_join(last_over_time(batch_insert_duration_seconds_sum[5m]), "orm_db", "-", "orm", "db")
)
/
sum by (orm_db, param) (
  label_join(last_over_time(batch_insert_duration_seconds_count[5m]), "orm_db", "-", "orm", "db")
) * 1000
```
Transformacje: Join by labels, convert field type, filter data by values, sort by

5. Scenariusz batch update
```promql
   sum by (orm_db, param) (
   label_join(last_over_time(updatePrices_duration_seconds_sum[5m]), "orm_db", "-", "orm", "db")
   )
   /
   sum by (orm_db, param) (
   label_join(last_over_time(updatePrices_duration_seconds_count[5m]), "orm_db", "-", "orm", "db")
   ) * 1000
```
Transformacje: Join by labels, convert field type, filter data by values, sort by

6. Scenariusz złożonej transakcji
```promql
(
  sum by (application) (last_over_time(http_server_requests_seconds_sum{uri="/api/orders/complex"}[5m]))
  /
  sum by (application) (last_over_time(http_server_requests_seconds_count{uri="/api/orders/complex"}[5m]))
) * 1000
```
7. Dodatkowe zapytania
Zapytanie mierzące obciążenie CPU:
```promql
sum by (app_deployment) (
  label_replace(
    rate(container_cpu_usage_seconds_total{pod=~".*-app-.*"}[5m]),
    "app_deployment", "$1", "pod", "(.*)-[a-z0-9]{9,}-[a-z0-9]{5}$"
  )
) * 1000
```
Zapytanie mierzące liczbę wykonanych zapytań na sekundę:
```promql
sum by (application) (
  rate(http_server_requests_seconds_count[5m])
)
```
Zapytanie mierzące obciążenie w procentach całkowitej mocy procesora:
```promql
label_join(
  (
    sum by (orm, db) (last_over_time(_api_orders_batchItems_cpu_usage_sum[5m]))
    /
    sum by (orm, db) (last_over_time(_api_orders_batchItems_cpu_usage_count[5m]))
  ),
  "orm_db", "-", "orm", "db"
) * 100
```


## Czyszczenie środowiska
Aby usunąć aplikację i wszystkie zasoby, które zostały utworzone, użyj polecenia:
```bash
helm uninstall jdbc-app
```

