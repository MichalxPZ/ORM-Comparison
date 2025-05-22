Aby zbudować obraz, umieszczamy Dockerfile w folderze głównym projektu i wykonujemy komendę (np. w terminalu w katalogu projektu):
```bash
docker build -t spring-jdbc-app .
```

Po zbudowaniu obrazu, aplikację można uruchomić poleceniem Docker, przekazując parametry połączenia do bazy jako zmienne środowiskowe lub wykorzystując domyślne z application.properties. Przykładowo:
```bash
docker run -e DB_HOST=postgres-host -e DB_NAME=mydatabase \
           -e DB_USER=myuser -e DB_PASS=mypassword \
           -p 8080:8080 spring-jdbc-app
```