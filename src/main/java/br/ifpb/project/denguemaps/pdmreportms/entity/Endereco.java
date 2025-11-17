package br.ifpb.project.denguemaps.pdmreportms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Id
    @Column(name = "endereco_id")
    @GeneratedValue
    private UUID id;

    private String logradouro;

    private String cep;

    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_municipio_id", nullable = true)
    private Municipio municipio;
}
