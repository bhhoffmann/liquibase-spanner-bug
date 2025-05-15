-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE test_table (
                            test_id STRING(MAX) NOT NULL,
                            test_column STRING(MAX) NOT NULL,
) PRIMARY KEY (test_id);