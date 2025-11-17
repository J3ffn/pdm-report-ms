package br.ifpb.project.denguemaps.pdmreportms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "cidadao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cidadao {

    @Id
    @Column(name = "cidadao_id")
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "fk_endereco_id", nullable = true)
    private Endereco endereco;

    @Column(name = "ref_keycloak_id",nullable = false, unique = true)
    private UUID refKeycloakId;
}
