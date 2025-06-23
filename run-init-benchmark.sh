#!/bin/bash

set -e

ORMS=(
  "jdbc"
  "hibernate"
  "jooq"
  "mybatis"
)
DBS=(
  "postgresql"
  "mysql"
  "mariadb"
)

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
      HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQLDialect"
      JOOQ_DIALECT="POSTGRES"
    elif [ "$DB" == "mysql" ]; then
      DB_HOST="mysql"
      DB_PORT=3306
      HIBERNATE_DIALECT="org.hibernate.dialect.MySQLDialect"
      JOOQ_DIALECT="MYSQL"
    elif [ "$DB" == "mariadb" ]; then
      DB_HOST="mariadb"
      DB_PORT=3306
      HIBERNATE_DIALECT="org.hibernate.dialect.MariaDBDialect"
      JOOQ_DIALECT="MARIADB"
    else
      DB_HOST="localhost"
      DB_PORT=3306
      HIBERNATE_DIALECT="org.hibernate.dialect.H2Dialect"
      JOOQ_DIALECT="H2"
    fi

    # Deploy with Helm
    helm upgrade --install $RELEASE_NAME $CHART_PATH \
      --set orm=$ORM \
      --set db=$DB \
      --set database.port=$DB_PORT \
      --set database.host=$DB_HOST \
      --set hibernate.dialect=$HIBERNATE_DIALECT \
      --set jooq.dialect=$JOOQ_DIALECT \
      --values $VALUES_FILE

    echo "⏳ Waiting for pod to be ready..."
    kubectl wait --for=condition=ready pod -l app=${ORM}-app-${DB} --timeout=60s || echo "⚠️ Timeout or no pod found"

    sleep 30

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

    sleep 2

    echo "📨 Sending scenario 1 requests (/api/orders/{id})..."
    for i in {1..10}; do
      curl -s "http://localhost:$LOCAL_PORT/api/orders/$i" > /dev/null
      echo "🔁 Request $i sent to /api/orders/$i"
    done

  sleep 2

  echo "📨 Scenario 2: /api/products filtered by category, price, and keyword"
  curl -s "http://localhost:$LOCAL_PORT/api/products?categoryId=1&minPrice=5&maxPrice=400&keyword=5" > /dev/null

    sleep 2

    echo "📨 Scenario 3: /api/orders/batchItems with varying count"
    for count in 5000 50000 75000 100000 200000 300000 400000 500000; do
      echo "🔁 Inserting order with $count items"
      curl -s -X POST "http://localhost:$LOCAL_PORT/api/orders/batchItems?count=$count" > /dev/null
      sleep 1
    done

    sleep 2
    echo "📨 Scenario 4: Updating prices for selected products using modulo filtering"

    for mod in 10 5 3 2 1; do
      percent=$((mod * 2))
      echo "🔁 Updating prices by ${percent}% for products with id % ${mod} == 0"
      curl -s -X PUT "http://localhost:$LOCAL_PORT/api/products/prices?mod=${mod}"
      echo ""
    done

    sleep 2

    echo "📨 Scenario 5: Creating transactional order"
    curl -s -X POST "http://localhost:$LOCAL_PORT/api/orders/complex" \
         -H "Content-Type: application/json" \
         -d '{"userId": 1, "productIds": [1, 2, 3]}'
    echo -e "\n✅ Transactional order complete"


    echo "🧹 Cleaning up..."
    kill $PF_PID || echo "⚠️ Port-forward already terminated"
#    helm uninstall $RELEASE_NAME || echo "⚠️ Could not uninstall release"

    echo "✅ Finished ORM=$ORM, DB=$DB"
    echo ""
    sleep 3
  done
done

echo "🎉 All ORM × DB benchmark combinations complete!"
