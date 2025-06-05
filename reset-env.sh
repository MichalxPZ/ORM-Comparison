#!/bin/bash

set -e

# Lista aplikacji ORM × baza danych
ORMS=("jdbc" "hibernate" "jooq" "mybatis")
DBS=("postgresql" "mysql" "mariadb")

# Usuwanie aplikacji
for ORM in "${ORMS[@]}"; do
  for DB in "${DBS[@]}"; do
    RELEASE_NAME="${ORM}-app-${DB}"
    echo "🗑️  Uninstalling app: $RELEASE_NAME..."
    helm uninstall "$RELEASE_NAME" || echo "⚠️  $RELEASE_NAME not found or already removed"
  done
done

# Usuwanie baz danych (manifesty YAML)
echo "🗑️  Deleting database deployments..."
kubectl delete -f k8s-manifests/databases/postgres.yaml --ignore-not-found
kubectl delete -f k8s-manifests/databases/mysql.yaml --ignore-not-found
kubectl delete -f k8s-manifests/databases/mariadb.yaml --ignore-not-found

echo "✅ Środowisko zostało wyczyszczone."

echo "Setting up empty databases"
kubectl apply -f k8s-manifests/databases/postgres.yaml
kubectl apply -f  k8s-manifests/databases/mysql.yaml
kubectl apply -f  k8s-manifests/databases/mariadb.yaml
sleep 10
kubectl wait --for=condition=ready pod -l db=postgres --timeout=60s || echo "⚠️ Timeout or no pod found"
POD=$(kubectl get pods -l app=db,db=postgres -o jsonpath='{.items[0].metadata.name}')
kubectl cp k8s-manifests/databases/init-schema-scripts/schema-postgres.sql $POD:/tmp/schema.sql
kubectl exec -it $POD -- psql -U exampleuser -d exampledb -f /tmp/schema.sql

kubectl wait --for=condition=ready pod -l db=mysql --timeout=60s || echo "⚠️ Timeout or no pod found"
POD=$(kubectl get pods -l app=db,db=mysql -o jsonpath='{.items[0].metadata.name}')
kubectl exec -i "$POD" -- mysql -u exampleuser -pexamplepass exampledb <<EOF
$(cat k8s-manifests/databases/init-schema-scripts/schema-mysql.sql)
EOF

kubectl wait --for=condition=ready pod -l db=mariadb --timeout=60s || echo "⚠️ Timeout or no pod found"
POD=$(kubectl get pods -l app=db,db=mariadb -o jsonpath='{.items[0].metadata.name}')
kubectl exec -i "$POD" -- mariadb -u exampleuser -pexamplepass exampledb <<EOF
$(cat k8s-manifests/databases/init-schema-scripts/schema-mariadb.sql)
EOF

echo "✅ Bazy danych zostały zainicjalizowane."