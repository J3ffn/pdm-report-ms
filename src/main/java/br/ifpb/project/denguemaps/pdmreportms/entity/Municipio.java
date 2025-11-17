package br.ifpb.project.denguemaps.pdmreportms.entity;

import br.ifpb.project.denguemaps.pdmreportms.enums.Estado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "municipio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Municipio {

    @Id
    @Column(name = "municipio_id")
    private UUID id;

    @Column(nullable = false)
    private String nome;

    private String geolocalizacao;

    @Column(nullable = false, length = 2)
    @Enumerated(EnumType.STRING)
    private Estado estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_secretaria_id", nullable = true)
    private Secretaria secretaria;
}
