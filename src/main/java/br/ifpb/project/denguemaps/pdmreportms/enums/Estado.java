package br.ifpb.project.denguemaps.pdmreportms.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum Estado {
    AC("AC", "Acre"),
    AL("AL", "Alagoas"),
    AP("AP", "Amapá"),
    AM("AM", "Amazonas"),
    BA("BA", "Bahia"),
    CE("CE", "Ceará"),
    DF("DF", "Distrito Federal"),
    ES("ES", "Espírito Santo"),
    GO("GO", "Goiás"),
    MA("MA", "Maranhão"),
    MT("MT", "Mato Grosso"),
    MS("MS", "Mato Grosso do Sul"),
    MG("MG", "Minas Gerais"),
    PA("PA", "Pará"),
    PB("PB", "Paraíba"),
    PR("PR", "Paraná"),
    PE("PE", "Pernambuco"),
    PI("PI", "Piauí"),
    RJ("RJ", "Rio de Janeiro"),
    RN("RN", "Rio Grande do Norte"),
    RS("RS", "Rio Grande do Sul"),
    RO("RO", "Rondônia"),
    RR("RR", "Roraima"),
    SC("SC", "Santa Catarina"),
    SP("SP", "São Paulo"),
    SE("SE", "Sergipe"),
    TO("TO", "Tocantins");

    private final String sigla;
    /**
     * -- GETTER --
     *  Retorna o nome completo do estado (Ex: "Paraíba").
     */
    @Getter
    private final String nome;

    Estado(String sigla, String nome) {
        this.sigla = sigla;
        this.nome = nome;
    }

    /**
     * Retorna a sigla do estado (Ex: "PB").
     */
    @JsonValue
    public String getSigla() {
        return sigla;
    }

    /**
     * Método estático para buscar um Estado pela sua sigla.
     * Útil para deserialização (receber a sigla em JSON e converter para o Enum).
     * * @param sigla A sigla do estado.
     * @return O enum Estado correspondente.
     * @throws IllegalArgumentException se a sigla não for encontrada.
     */
    @JsonCreator
    public static Estado fromSigla(String sigla) {
        for (Estado estado : Estado.values()) {
            if (estado.sigla.equalsIgnoreCase(sigla)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Sigla de estado inválida: " + sigla);
    }
}
