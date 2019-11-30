CREATE TABLE IF NOT EXISTS referencias_tamanhos_bolhas
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    raio float default 100,
    min int null,
    max int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
