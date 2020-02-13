CREATE TABLE IF NOT EXISTS bolhas_membros
(
    id         int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    bolha_id int not null,
    user_id int not null,
    checkin datetime not null,
    checkout datetime null,
    foi_expulso tinyint(1) default 0 not null,
    created_by int null,
    deleted_at TIMESTAMP       null,
    created_at TIMESTAMP       null,
    updated_at TIMESTAMP       null
);
