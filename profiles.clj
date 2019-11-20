{:dev-env-vars  {:env {:database-url  "postgres://bolha_musical_api_user:password1@127.0.0.1:5432/bolha_musical_api?stringtype=unspecified"
                       :sendinblue-user-login    "You@Something.com"
                       :sendinblue-user-password "sendinblue"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}
 :test-env-vars {:env {:database-url  "postgres://bolha_musical_api_user:password1@127.0.0.1:5432/bolha_musical_api_test?stringtype=unspecified"
                       :auth-key      "theSecretKeyUsedToCreateAndReadTokens"}}}