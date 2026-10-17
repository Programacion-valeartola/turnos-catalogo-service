-- Categorías profesionales (copia local del catálogo de la cátedra)
CREATE TABLE professional_category (
    id          BIGINT       PRIMARY KEY,          -- mismo ID que en la cátedra
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    enabled     BOOLEAN      NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

-- Profesionales
CREATE TABLE professional (
    id          BIGINT       PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES professional_category (id),
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    enabled     BOOLEAN      NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_professional_category ON professional (category_id);

-- Horarios semanales de atención
CREATE TABLE weekly_schedule (
    id                    BIGINT      PRIMARY KEY,
    professional_id       BIGINT      NOT NULL REFERENCES professional (id),
    day_of_week           VARCHAR(9)  NOT NULL CHECK (day_of_week IN
                          ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    start_time            TIME        NOT NULL,
    end_time              TIME        NOT NULL,
    slot_duration_minutes INTEGER     NOT NULL CHECK (slot_duration_minutes > 0),
    enabled               BOOLEAN     NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    CHECK (end_time > start_time)
);
CREATE INDEX idx_weekly_schedule_professional ON weekly_schedule (professional_id);

-- Estado de sincronización: una única fila con la versión local aplicada
CREATE TABLE catalog_sync_state (
    id             SMALLINT    PRIMARY KEY CHECK (id = 1),
    local_version  BIGINT,                 -- NULL = base vacía, requiere snapshot
    last_sync_at   TIMESTAMPTZ,
    last_sync_type VARCHAR(20),            -- SNAPSHOT o INCREMENTAL
    last_error     TEXT,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
INSERT INTO catalog_sync_state (id) VALUES (1);

-- Eventos Kafka ya procesados (idempotencia por eventId)
CREATE TABLE processed_event (
    event_id     VARCHAR(255) PRIMARY KEY,
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);