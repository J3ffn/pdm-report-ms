package br.ifpb.project.denguemaps.pdmreportms.service;

import br.ifpb.project.denguemaps.pdmreportms.model.GeoEntidade;
import br.ifpb.project.denguemaps.pdmreportms.repository.GeoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por criar e persistir pontos geográficos com índices H3.
 *
 * <p>Centraliza o cálculo de H3 (res8 e res6) e a persistência na tabela tb_geo,
 * evitando duplicação entre {@code ReportFocusService} e {@code ReportSymptomsService}.
 */
@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeoRepository geoRepository;
    private final H3Service h3Service;

    /**
     * Cria e persiste um {@link GeoEntidade} com os índices H3 calculados.
     *
     * @param lat Latitude (-90 a 90)
     * @param lng Longitude (-180 a 180)
     * @return Entidade geo persistida com geo_id e índices H3 preenchidos
     */
    public GeoEntidade criarGeo(double lat, double lng) {
        GeoEntidade geo = new GeoEntidade();
        geo.setLat(lat);
        geo.setLng(lng);
        geo.setH3Res8(h3Service.calcularRes8(lat, lng));
        geo.setH3Res6(h3Service.calcularRes6(lat, lng));
        return geoRepository.save(geo);
    }
}
