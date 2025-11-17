package br.ifpb.project.denguemaps.pdmreportms.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "secretaria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Secretaria {

    @Id
    @Column(name = "secretaria_id")
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "fk_endereco_id")
    private UUID enderecoId;

    @Column(name = "fk_gestor_id", nullable = true)
    private UUID gestorId;
}
