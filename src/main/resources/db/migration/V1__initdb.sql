alter table if exists indexes
    drop constraint if exists FKcdoxdsq7pjupiunh1kfy9k1b0;

alter table if exists indexes
    drop constraint if exists FK2hiof9yd10bubcuhs8ap4mfwr;

alter table if exists lemmas
    drop constraint if exists FK4bs8lla1jmyeg250o2g3focej;

alter table if exists pages
    drop constraint if exists FK33gexkhrwd3yvnxy0usw9y3p1;

drop table if exists indexes cascade;

drop table if exists lemmas cascade;


drop table if exists pages cascade;


drop table if exists sites cascade;

create type status_type as enum (
    'INDEXING',
    'INDEXED',
    'FAILED'
    );

create table indexes
(
    id       serial  not null,
    page_id  integer not null,
    lemma_id integer not null,
    rank     FLOAT   NOT NULL,

    primary key (id)
);


create table lemmas
(
    id        serial       not null,
    site_id   integer      not null,
    lemma     VARCHAR(255) NOT NULL,
    frequency INT          NOT NULL,
    primary key (id)
);


create table pages
(
    id      serial       not null,
    site_id integer      not null,
    path    VARCHAR(255) NOT NULL,
    code    INT          NOT NULL,
    content TEXT         NOT NULL,
    primary key (id)
);


create table sites
(
    id          serial       not null,
    status      status_type  not null,
    status_time timestamp(6) not null,
    last_error  TEXT,
    url         VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    primary key (id)
);
create index path on pages (path);


alter table if exists indexes
    add constraint indexes_lemma_fk
        foreign key (lemma_id)
            references lemmas;


alter table if exists indexes
    add constraint indexes_page_fk
        foreign key (page_id)
            references pages;


alter table if exists lemmas
    add constraint lemmas_site_fk
        foreign key (site_id)
            references sites;


alter table if exists pages
    add constraint pages_site_fk
        foreign key (site_id)
            references sites;