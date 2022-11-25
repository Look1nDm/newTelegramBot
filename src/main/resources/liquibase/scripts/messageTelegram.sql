--liquibase formatted sql


-- changeSet lukinD:1
CREATE TABLE notification_task(
id INTEGER,
datetime date,
message TEXT
);