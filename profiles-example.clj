{:dev-env-vars  {:env {:database-url             "postgres://bolha_musical_api_user:password1@127.0.0.1:5432/bolha_musical_api?stringtype=unspecified"
                       :spotify-client-id        ""
                       :spotify-client-secret    ""
                       :spotify-auth-encoded     ""
                       :spotify-redirect-uri     "http://10.0.0.108:3001:3001/api/v1/spotify/login/callback"
                       :sendinblue-user-login    "You@Something.com"
                       :sendinblue-user-password "sendinblue"
                       :auth-key                 "theSecretKeyUsedToCreateAndReadTokens"
                       :redis-url                "redis://redistogo:pass@panga.redistogo.com:9475/"}}
 :test-env-vars {:env {:database-url "postgres://bolha_musical_api_user:password1@127.0.0.1:5432/bolha_musical_api_test?stringtype=unspecified"
                       :auth-key     "theSecretKeyUsedToCreateAndReadTokens"}}}