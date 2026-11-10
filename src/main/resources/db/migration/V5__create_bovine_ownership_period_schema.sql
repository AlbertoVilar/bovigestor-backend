CREATE SEQUENCE bovine_ownership_period_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE bovine_ownership_period (
    id BIGINT NOT NULL,
    bovine_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_bovine_ownership_period PRIMARY KEY (id),
    CONSTRAINT ck_bovine_ownership_period_id_positive CHECK (id > 0),
    CONSTRAINT fk_bovine_ownership_period_bovine
        FOREIGN KEY (bovine_id) REFERENCES bovine (id),
    CONSTRAINT fk_bovine_ownership_period_farm
        FOREIGN KEY (farm_id) REFERENCES farm (id),
    CONSTRAINT ck_bovine_ownership_period_end_after_start
        CHECK (ended_at IS NULL OR ended_at > started_at)
);

CREATE INDEX ix_bovine_ownership_period_bovine_started_at
    ON bovine_ownership_period (bovine_id, started_at);
