CREATE TABLE IF NOT EXISTS bolhas
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    referencia varchar(36) NOT NULL,
    apelido varchar(50) not null,
    rocket_chat_canal_id varchar(100) null,
    cor varchar(20) null,
    eh_fixa tinyint(1) default 0 not null,
    apenas_lider_adiciona_track tinyint(1) default 0 not null,
    user_id_referencia_raio int null, /* se for fixo não precisa */
    referencia_raio_fixo POINT null, /* utilizado quando for bolha fixa*/
    tamanho_bolha_referencia_id int default 1,
    user_lider_id int null,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);

/*
CREATE TABLE geom (g GEOMETRY);
ALTER TABLE geom ADD pt POINT;
ALTER TABLE geom DROP pt;
INSERT INTO geom VALUES (GeomFromText('POINT(1 1)'));

SET @g = 'POINT(1 1)';
INSERT INTO geom VALUES (GeomFromText(@g));

SELECT AsText(g) FROM geom;
*/