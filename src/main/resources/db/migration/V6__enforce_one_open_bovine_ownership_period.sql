CREATE UNIQUE INDEX uq_bovine_one_open_ownership
    ON bovine_ownership_period (bovine_id)
    WHERE ended_at IS NULL;
