drop index path;

create unique index site_path
    on pages (site_id, path);

drop index site_lemma_index;

create unique index site_lemma_index
    on lemmas (site_id, lemma);