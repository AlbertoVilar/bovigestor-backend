ALTER TABLE bovine
    ADD COLUMN origin_creator_name VARCHAR(255) NOT NULL;

ALTER TABLE bovine
    ADD COLUMN origin_farm_name VARCHAR(255) NOT NULL;

ALTER TABLE bovine
    ADD COLUMN origin_farm_id BIGINT;

ALTER TABLE bovine
    ADD CONSTRAINT ck_bovine_origin_creator_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(origin_creator_name)) > 0);

ALTER TABLE bovine
    ADD CONSTRAINT ck_bovine_origin_farm_name_not_blank
        CHECK (CHAR_LENGTH(TRIM(origin_farm_name)) > 0);

ALTER TABLE bovine
    ADD CONSTRAINT ck_bovine_origin_farm_id_positive
        CHECK (origin_farm_id IS NULL OR origin_farm_id > 0);

ALTER TABLE bovine
    ADD CONSTRAINT fk_bovine_origin_farm
        FOREIGN KEY (origin_farm_id) REFERENCES farm (id);
