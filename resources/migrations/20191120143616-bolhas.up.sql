CREATE TABLE IF NOT EXISTS bolhas
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    apelido varchar(50) not null,
    cor varchar(20) not null,
    eh_fixa tinyint(1) default 0 not null,
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