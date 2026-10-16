CREATE SEQUENCE farm_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE farm (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code_series VARCHAR(4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_farm PRIMARY KEY (id),
    CONSTRAINT ck_farm_id_positive CHECK (id > 0),
    CONSTRAINT ck_farm_name_not_blank CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT uq_farm_code_series UNIQUE (code_series),
    CONSTRAINT ck_farm_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
