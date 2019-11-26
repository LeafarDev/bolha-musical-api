# bolha-musical-api

Api da bolhamusical, uma rede social para pessoas próximas compartilharem suas experiências musicais.
## Usage

### Rodando a aplicação local

`lein ring server`

### Rodando como jar
```
lein do clean, ring uberjar
java -jar target/server.jar
```
### Kibit para para tornar o código mais idiomático
`lein kibit --replace`
### Cljfmt para formatar seu código
`lein cljfmt fix`
### Gerando o war
`lein ring uberwar`
### Crie migrations com o Migratus
```
lein migratus create "spotify-login-code"
```
### Execute migrations com o Miratus
```
lein migratus migrate && lein with-profile test migratus migrate
```
## Licença
MIT
