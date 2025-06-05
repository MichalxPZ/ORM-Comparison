#!/bin/bash

set -e

ORMS=("jdbc" "hibernate" "jooq" "mybatis")
DBS=("postgresql" "mysql" "mariadb")

CHART_PATH="./k8s-manifests/app"
VALUES_FILE="$CHART_PATH/values.yaml"

for ORM in "${ORMS[@]}"; do
  for DB in "${DBS[@]}"; do
    echo "=============================="
    echo "🔄 Deploying app for ORM=$ORM, DB=$DB"
    echo "=============================="
    RELEASE_NAME="${ORM}-app-${DB}"

    # Determine DB_HOST based on DB type
    if [ "$DB" == "postgresql" ]; then
      DB_HOST="postgres"
      DB_PORT=5432
    else
      DB_HOST=$DB
      DB_PORT=3306
    fi

    # Deploy with Helm
    helm upgrade --install $RELEASE_NAME $CHART_PATH \
      --set orm=$ORM \
      --set db=$DB \
      --set database.port=$DB_PORT \
      --set database.host=$DB_HOST \
      --values $VALUES_FILE

    echo "⏳ Waiting for pod to be ready..."
    kubectl wait --for=condition=ready pod -l app=${ORM}-app-${DB} --timeout=60s || echo "⚠️ Timeout or no pod found"

    sleep 12

    LOCAL_PORT=$(jot -r 1 30000 40000)

    echo "🚪 Setting up port-forwarding: $LOCAL_PORT -> 8080"
    nohup kubectl port-forward svc/$RELEASE_NAME $LOCAL_PORT:8080 > /dev/null 2>&1 &
    PF_PID=$!

    echo "⏳ Waiting for app HTTP readiness..."
    until curl -s --fail "http://localhost:$LOCAL_PORT/actuator/health/readiness" > /dev/null; do
      printf '.'
      sleep 1
    done
    echo "✅ App is ready!"

    echo "📡 Sending /api/init request..."
    curl -X POST "http://localhost:$LOCAL_PORT/api/init?users=1000&categories=5&products=100&orders=2000&itemsPerOrder=5"

    sleep 5

    echo "🧹 Cleaning up..."
    kill $PF_PID || echo "⚠️ Port-forward already terminated"
#    helm uninstall $RELEASE_NAME || echo "⚠️ Could not uninstall release"

    echo "✅ Finished ORM=$ORM, DB=$DB"
    echo ""
    sleep 3
  done
done

echo "🎉 All ORM × DB benchmark combinations complete!"
