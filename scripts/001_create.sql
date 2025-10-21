CREATE SCHEMA IF NOT EXISTS garch;
SET search_path TO garch, public;

CREATE TYPE role_type AS ENUM ('user', 'admin');
CREATE TYPE visibility_type AS ENUM ('private', 'public');
CREATE TYPE entity_type AS ENUM ('time_series', 'garch_configuration', 'calculation');
CREATE TYPE operation_type AS ENUM ('create', 'read', 'update', 'delete');


---------- users ----------
CREATE TABLE users
(
    user_id         BIGSERIAL PRIMARY KEY,
    username        TEXT            NOT NULL UNIQUE,
    password_hash   TEXT            NOT NULL,
    email           TEXT            NOT NULL UNIQUE,
    profile_pic_url TEXT,
    role            garch.role_type NOT NULL DEFAULT 'user',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

---------- time_series ----------
CREATE TABLE time_series
(
    time_series_id BIGSERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    name           TEXT        NOT NULL,
    mean           DOUBLE PRECISION,
    median         DOUBLE PRECISION,
    variance       DOUBLE PRECISION,
    skewness       DOUBLE PRECISION,
    kurtosis       DOUBLE PRECISION,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    visibility     TEXT        NOT NULL DEFAULT 'private'
);

---------- ----------
CREATE TABLE time_series_value
(
    value_id       BIGSERIAL PRIMARY KEY,
    time_series_id BIGINT           NOT NULL REFERENCES time_series (time_series_id) ON DELETE CASCADE,
    value          DOUBLE PRECISION NOT NULL,
    ts             INTEGER          NOT NULL
);

---------- GARCH configuration ----------
CREATE TABLE garch_configuration
(
    configuration_id  BIGSERIAL PRIMARY KEY,
    user_id           BIGINT           NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    name              TEXT             NOT NULL,
    start_variance    DOUBLE PRECISION NOT NULL,
    constant_variance DOUBLE PRECISION NOT NULL,
    created_at        TIMESTAMPTZ      NOT NULL DEFAULT now()
);

---------- configuration_shock_weight ----------
CREATE TABLE configuration_shock_weight
(
    configuration_shock_weight_id BIGSERIAL PRIMARY KEY,
    configuration_id              BIGINT           NOT NULL REFERENCES garch_configuration (configuration_id) ON DELETE CASCADE,
    order_no                      INTEGER          NOT NULL,
    value                         DOUBLE PRECISION NOT NULL,
    UNIQUE (configuration_id, order_no)
);

---------- configuration_variance_weight ----------
CREATE TABLE configuration_variance_weight
(
    configuration_variance_weight_id BIGSERIAL PRIMARY KEY,
    configuration_id                 BIGINT           NOT NULL REFERENCES garch_configuration (configuration_id) ON DELETE CASCADE,
    order_no                         INTEGER          NOT NULL,
    value                            DOUBLE PRECISION NOT NULL,
    UNIQUE (configuration_id, order_no)
);

---------- calculation ----------
CREATE TABLE calculation
(
    calculation_id        BIGSERIAL PRIMARY KEY,
    user_id               BIGINT           NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    configuration_id      BIGINT           REFERENCES garch_configuration (configuration_id) ON DELETE SET NULL,
    run_at                TIMESTAMPTZ      NOT NULL DEFAULT now(),
    -- vstupní a výsledná časová řada
    input_time_series_id  BIGINT           NOT NULL REFERENCES time_series (time_series_id) ON DELETE RESTRICT,
    result_time_series_id BIGINT           REFERENCES time_series (time_series_id) ON DELETE SET NULL,
    -- parametry použité při běhu (pro auditovatelnost, denormalizace)
    start_variance        DOUBLE PRECISION NOT NULL,
    constant_variance     DOUBLE PRECISION NOT NULL
);

---------- run_shock_weight ----------
CREATE TABLE run_shock_weight
(
    run_shock_weight_id BIGSERIAL PRIMARY KEY,
    calculation_id      BIGINT           NOT NULL REFERENCES calculation (calculation_id) ON DELETE CASCADE,
    order_no            INTEGER          NOT NULL,
    value               DOUBLE PRECISION NOT NULL,
    UNIQUE (calculation_id, order_no)
);

---------- run_variance_weight ----------
CREATE TABLE run_variance_weight
(
    run_variance_weight_id BIGSERIAL PRIMARY KEY,
    calculation_id         BIGINT           NOT NULL REFERENCES calculation (calculation_id) ON DELETE CASCADE,
    order_no               INTEGER          NOT NULL,
    value                  DOUBLE PRECISION NOT NULL,
    UNIQUE (calculation_id, order_no)
);

---------- audit_log ----------
CREATE TABLE audit_log
(
    audit_log_id BIGSERIAL PRIMARY KEY,
    occurred_at  TIMESTAMPTZ          NOT NULL DEFAULT now(),
    username     TEXT,
    user_id      BIGINT               REFERENCES users (user_id) ON DELETE SET NULL,
    entity_type  garch.entity_type    NOT NULL,
    entity_id    BIGINT               NOT NULL,
    operation    garch.operation_type NOT NULL,
    before_data  JSONB,
    after_data   JSONB,
    changed_keys TEXT[]
);

