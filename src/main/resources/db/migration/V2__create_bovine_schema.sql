CREATE SEQUENCE bovine_id_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE bovine (
    id BIGINT NOT NULL,
    internal_code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    sex VARCHAR(20) NOT NULL,
    breed VARCHAR(255),
    coat_color VARCHAR(255),
    birth_date DATE,
    status VARCHAR(20) NOT NULL,
    ear_tag VARCHAR(255),
    registration_number VARCHAR(255),
    registration_association VARCHAR(255),
    registration_category VARCHAR(20) NOT NULL,
    father_id BIGINT,
    mother_id BIGINT,
    father_registration_number VARCHAR(255),
    mother_registration_number VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_bovine PRIMARY KEY (id),
    CONSTRAINT ck_bovine_id_positive CHECK (id > 0),
    CONSTRAINT uq_bovine_internal_code UNIQUE (internal_code),
    CONSTRAINT ck_bovine_name_not_blank CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_bovine_sex CHECK (sex IN ('MALE', 'FEMALE')),
    CONSTRAINT ck_bovine_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DECEASED', 'SOLD')),
    CONSTRAINT ck_bovine_registration_category CHECK (
        registration_category IN ('PO', 'PC', 'PA', 'UNREGISTERED')
    ),
    CONSTRAINT ck_bovine_birth_date_not_after_created_at CHECK (
        birth_date IS NULL OR birth_date <= CAST(created_at AT TIME ZONE 'UTC' AS DATE)
    ),
    CONSTRAINT ck_bovine_father_not_self CHECK (father_id IS NULL OR father_id <> id),
    CONSTRAINT ck_bovine_mother_not_self CHECK (mother_id IS NULL OR mother_id <> id),
    CONSTRAINT ck_bovine_registered_details_required CHECK (
        registration_category = 'UNREGISTERED'
        OR (
            registration_number IS NOT NULL
            AND CHAR_LENGTH(TRIM(registration_number)) > 0
            AND registration_association IS NOT NULL
            AND CHAR_LENGTH(TRIM(registration_association)) > 0
        )
    ),
    CONSTRAINT ck_bovine_unregistered_without_details CHECK (
        registration_category <> 'UNREGISTERED'
        OR (
            (registration_number IS NULL OR CHAR_LENGTH(TRIM(registration_number)) = 0)
            AND (
                registration_association IS NULL
                OR CHAR_LENGTH(TRIM(registration_association)) = 0
            )
        )
    ),
    CONSTRAINT ck_bovine_po_pc_parentage_required CHECK (
        registration_category NOT IN ('PO', 'PC')
        OR (
            (father_id IS NOT NULL OR (
                father_registration_number IS NOT NULL
                AND CHAR_LENGTH(TRIM(father_registration_number)) > 0
            ))
            AND (mother_id IS NOT NULL OR (
                mother_registration_number IS NOT NULL
                AND CHAR_LENGTH(TRIM(mother_registration_number)) > 0
            ))
        )
    ),
    CONSTRAINT fk_bovine_father FOREIGN KEY (father_id) REFERENCES bovine (id),
    CONSTRAINT fk_bovine_mother FOREIGN KEY (mother_id) REFERENCES bovine (id)
);
