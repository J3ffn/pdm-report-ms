package br.ifpb.project.denguemaps.pdmreportms.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_geo")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GeoEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "geo_id")
    private UUID id;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "h3_res8")
    private Long h3Res8;

    @Column(name = "h3_res6")
    private Long h3Res6;
}
