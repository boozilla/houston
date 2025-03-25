CREATE TABLE `data`
(
    `id`        BIGINT       NOT NULL AUTO_INCREMENT,
    `commit_id` VARCHAR(40)  NOT NULL,
    `scope`     VARCHAR(8)   NOT NULL,
    `name`      VARCHAR(128) NOT NULL,
    `path`     VARCHAR(255) NOT NULL,
    `checksum` VARCHAR(64)  NOT NULL,
    `apply_at`  DATETIME(6)  NULL,
    PRIMARY KEY (`id`),
    INDEX      `IDX_data_findByName` (`scope` ASC, `name` ASC, `apply_at` ASC) VISIBLE,
    UNIQUE INDEX `UNIQ_data` (`commit_id` ASC, `scope` ASC, `name` ASC, `checksum` ASC) VISIBLE
);

CREATE TABLE `manifest`
(
    `name`      VARCHAR(128) NOT NULL,
    `commit_id` VARCHAR(45)  NOT NULL,
    `data`      BLOB         NOT NULL,
    PRIMARY KEY (`name`)
);
