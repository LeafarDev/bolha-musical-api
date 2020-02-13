create TABLE IF NOT EXISTS password_reset_key
(
    id           SERIAL PRIMARY KEY NOT NULL,
    reset_key    char(255)               NOT NULL UNIQUE,
    already_used BOOLEAN            NOT NULL DEFAULT FALSE,
    user_id      int,
    valid_until   DATETIME
);

--;;
create TRIGGER refleshtokentime
BEFORE insert on password_reset_key
FOR EACH ROW BEGIN
    IF new.`valid_until` is null THEN
        SET new.`valid_until` = DATE_ADD(NOW(), INTERVAL 1 month);
    END IF;
END;