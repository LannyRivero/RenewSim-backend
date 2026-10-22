CREATE TABLE simulation_result_revisions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    simulation_id BIGINT NOT NULL,
    revision_number INT NOT NULL,
    input_snapshot LONGTEXT NOT NULL,
    result_snapshot LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sim_result_revisions_simulation
        FOREIGN KEY (simulation_id) REFERENCES simulations (id) ON DELETE CASCADE,
    CONSTRAINT uk_sim_result_revisions_number UNIQUE (simulation_id, revision_number)
);

ALTER TABLE simulations
    ADD COLUMN current_revision_id BIGINT NULL;
