package br.ifpb.project.denguemaps.pdmreportms.model.enums;

/**
 * Nível de resolução H3 usado para agregação geoespacial do heatmap.
 *
 * RAW      → coordenada exata (zoom >= 13) — apenas para usuários logados ou agentes
 * H3_RES8  → hexágono ~0,5 km² (zoom >= 9) — quarteirão/rua
 * H3_RES6  → hexágono ~36 km² (zoom < 9)  — bairro/cidade
 *
 * Usado como parâmetro de query no pdm-geo-ms ao gerar o GeoJSON para o mapa.
 * Não é salvo no banco — é calculado em runtime com base no zoom do frontend.
 */
public enum H3Resolution {
    RAW,
    H3_RES8,
    H3_RES6
}
