DROP TABLE IF EXISTS bugs;
DROP TABLE IF EXISTS service_status;

CREATE TABLE bugs (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    component VARCHAR(64)  NOT NULL,
    summary   VARCHAR(512) NOT NULL,
    severity  VARCHAR(16)  NOT NULL,
    resolution VARCHAR(512)
);

CREATE TABLE service_status (
    component VARCHAR(64) PRIMARY KEY,
    status    VARCHAR(32) NOT NULL,
    detail    VARCHAR(512)
);
