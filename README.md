# bolha-musical-api

FIXME

## Usage

### Run the application locally

`lein ring server`

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`
### Create Migrations
```
lein migratus create "spotify-login-code"
```
### Run migrations
```
lein migratus migrate && lein with-profile test migratus migrate
```
## License

Copyright ©  FIXME
