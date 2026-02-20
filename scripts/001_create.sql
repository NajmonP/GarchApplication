CREATE SCHEMA IF NOT EXISTS garch;
SET
search_path TO garch, public;

CREATE TYPE role_type AS ENUM ('USER', 'ADMIN');
CREATE TYPE visibility_type AS ENUM ('private', 'public');
CREATE TYPE entity_type AS ENUM ('time_series', 'configuration', 'calculation');
CREATE TYPE operation_type AS ENUM ('create', 'read', 'update', 'delete');
CREATE TYPE calculation_status AS ENUM ('OK', 'MISSING_INPUT_SERIES', 'MISSING_OUTPUT_SERIES', 'BROKEN');


---------- users ----------
CREATE TABLE users
(
    user_id         BIGSERIAL PRIMARY KEY,
    username        TEXT            NOT NULL UNIQUE,
    password_hash   TEXT            NOT NULL,
    email           TEXT            NOT NULL UNIQUE,
    profile_pic_url TEXT,
    role            garch.role_type NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

---------- time_series ----------
CREATE TABLE time_series
(
    time_series_id BIGSERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    name           TEXT        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    visibility     TEXT        NOT NULL DEFAULT 'private',
    UNIQUE (user_id, name)
);

---------- time_series_value ----------
CREATE TABLE time_series_value
(
    value_id       BIGSERIAL PRIMARY KEY,
    time_series_id BIGINT           NOT NULL REFERENCES time_series (time_series_id) ON DELETE CASCADE,
    value          DOUBLE PRECISION NOT NULL,
    order_no       INTEGER          NOT NULL
);

---------- configuration ----------
CREATE TABLE configuration
(
    configuration_id BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    name             TEXT        NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

---------- GARCH model ----------
CREATE TABLE garch_model
(
    model_id          BIGSERIAL PRIMARY KEY,
    configuration_id  BIGINT           NOT NULL REFERENCES configuration (configuration_id) ON DELETE CASCADE,
    name              TEXT             NOT NULL,
    start_variance    DOUBLE PRECISION NOT NULL,
    constant_variance DOUBLE PRECISION NOT NULL,
    UNIQUE (configuration_id, name)
);

---------- model_shock_weight ----------
CREATE TABLE model_shock_weight
(
    model_shock_weight_id BIGSERIAL PRIMARY KEY,
    model_id              BIGINT           NOT NULL REFERENCES garch_model (model_id) ON DELETE CASCADE,
    order_no              INTEGER          NOT NULL,
    value                 DOUBLE PRECISION NOT NULL,
    UNIQUE (model_id, order_no)
);

---------- model_variance_weight ----------
CREATE TABLE model_variance_weight
(
    model_variance_weight_id BIGSERIAL PRIMARY KEY,
    model_id                 BIGINT           NOT NULL REFERENCES garch_model (model_id) ON DELETE CASCADE,
    order_no                 INTEGER          NOT NULL,
    value                    DOUBLE PRECISION NOT NULL,
    UNIQUE (model_id, order_no)
);

---------- calculation ----------
CREATE TABLE calculation
(
    calculation_id        BIGSERIAL PRIMARY KEY,
    user_id               BIGINT                   NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    status                garch.calculation_status NOT NULL DEFAULT 'OK',
    run_at                TIMESTAMPTZ              NOT NULL DEFAULT now(),
    input_time_series_id  BIGINT                   REFERENCES time_series (time_series_id) ON DELETE SET NULL,
    result_time_series_id BIGINT                   REFERENCES time_series (time_series_id) ON DELETE SET NULL,
    forecast              BIGINT                   NOT NULL,
    start_variance        DOUBLE PRECISION         NOT NULL,
    constant_variance     DOUBLE PRECISION         NOT NULL
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

