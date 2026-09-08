-- ============================================================================
-- SCRIPT DE MIGRAÇÃO FLYWAY V1 — pdm-report-ms
-- PostgreSQL 16 — Indexação geoespacial via H3 (Uber), sem PostGIS
-- Herança JPA: JOINED — subtypes compartilham o PK do report pai
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ----------------------------------------------------------------------------
-- Tabela: tb_geo
-- Ponto geográfico com índices H3 pré-calculados pelo H3Service
-- ----------------------------------------------------------------------------
CREATE TABLE tb_geo (
    geo_id  UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
    lat     DOUBLE PRECISION NOT NULL,
    lng     DOUBLE PRECISION NOT NULL,
    h3_res8 BIGINT,   -- Índice H3 resolução 8 (~0,7 km²)
    h3_res6 BIGINT,   -- Índice H3 resolução 6 (~36 km²)

    CONSTRAINT chk_lat CHECK (lat BETWEEN -90  AND  90),
    CONSTRAINT chk_lng CHECK (lng BETWEEN -180 AND 180)
);

-- Índices B-Tree para GROUP BY no heatmap (pdm-geo-ms)
CREATE INDEX idx_geo_h3_res8 ON tb_geo (h3_res8);
CREATE INDEX idx_geo_h3_res6 ON tb_geo (h3_res6);

-- ----------------------------------------------------------------------------
-- Tabela: tb_reports  (pai da herança JOINED)
-- report_type = discriminador gerenciado pelo JPA (@DiscriminatorColumn)
-- ----------------------------------------------------------------------------
CREATE TABLE tb_reports (
    report_id    UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_type  VARCHAR(20)  NOT NULL,   -- 'FOCUS' | 'SYMPTOM'
    description  TEXT         NOT NULL,
    is_enabled   BOOLEAN      NOT NULL DEFAULT FALSE,
    is_disease   BOOLEAN      NOT NULL DEFAULT FALSE,
    is_visited   BOOLEAN      NOT NULL DEFAULT FALSE,
    fk_person_id UUID,                   -- NULL = cidadão anônimo
    cpf_hash     VARCHAR(64),
    fk_geo_id    UUID         NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_report_geo   FOREIGN KEY (fk_geo_id) REFERENCES tb_geo (geo_id) ON DELETE RESTRICT,
    CONSTRAINT chk_report_type CHECK (report_type IN ('FOCUS', 'SYMPTOM'))
);

CREATE INDEX idx_reports_type    ON tb_reports (report_type);
CREATE INDEX idx_reports_geo     ON tb_reports (fk_geo_id);
CREATE INDEX idx_reports_person  ON tb_reports (fk_person_id);
CREATE INDEX idx_reports_disease ON tb_reports (is_disease);

-- ----------------------------------------------------------------------------
-- Tabela: tb_report_focus  (subtype JOINED)
-- report_id é PK e FK para tb_reports — padrão JPA @Inheritance(JOINED)
-- ----------------------------------------------------------------------------
CREATE TABLE tb_report_focus (
    report_id         UUID PRIMARY KEY,
    local_description TEXT NOT NULL,

    CONSTRAINT fk_focus_report FOREIGN KEY (report_id) REFERENCES tb_reports (report_id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------------
-- Tabela: tb_report_symptoms  (subtype JOINED)
-- report_id é PK e FK para tb_reports — padrão JPA @Inheritance(JOINED)
-- ----------------------------------------------------------------------------
CREATE TABLE tb_report_symptoms (
    report_id           UUID    PRIMARY KEY,
    respostas           JSON,               -- { "perguntaId": "opcaoId" }
    score_total         INTEGER NOT NULL,   -- 0–100, calculado pelo frontend
    fk_questionnaire_id UUID    NOT NULL,

    CONSTRAINT fk_symptoms_report FOREIGN KEY (report_id) REFERENCES tb_reports (report_id) ON DELETE CASCADE,
    CONSTRAINT chk_score          CHECK (score_total BETWEEN 0 AND 100)
);
