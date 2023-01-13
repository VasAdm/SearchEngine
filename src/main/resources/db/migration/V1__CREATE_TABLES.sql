CREATE
    TYPE status_type AS ENUM ('INDEXING', 'INDEXED', 'FAILED');
CREATE TABLE sites
(
    id          INT GENERATED ALWAYS AS IDENTITY,
    status status_type       NOT NULL,
    status_time timestamp    NOT NULL,
    last_error  TEXT,
    url         VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE pages
(
    id INT GENERATED ALWAYS AS IDENTITY,
    site_id INT ,
    path    TEXT       NOT NULL,
    code    INT        NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_site_id
        FOREIGN KEY(site_id)
            REFERENCES sites(id)
            ON DELETE CASCADE
    );
CREATE INDEX path
    ON pages (site_id, path);