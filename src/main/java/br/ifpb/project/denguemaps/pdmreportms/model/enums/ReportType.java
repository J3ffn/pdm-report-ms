package br.ifpb.project.denguemaps.pdmreportms.model.enums;

/**
 * Tipo de report enviado pelo cidadão.
 *
 * FOCUS -> Foco de dengue identificado no ambiente (água parada, etc.)
 * SYMPTOM -> Cidadão reportou sintomas de saúde via questionário dinâmico
 *
 * Armazenado como VARCHAR no banco via @Enumerated(EnumType.STRING).
 * Nunca use ORDINAL, se a ordem dos valores mudar, corrompe o banco.
 */
public enum ReportType {
    FOCUS,
    SYMPTOM
}
