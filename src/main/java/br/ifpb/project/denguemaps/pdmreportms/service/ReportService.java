package br.ifpb.project.denguemaps.pdmreportms.service;

import java.util.UUID;

/**
 * Contrato base para criação de qualquer tipo de report.
 *
 * <p>Cada tipo de report define seu próprio retorno:
 * <ul>
 *   <li>{@link ReportFocusService} retorna {@code ReportResponseDTO} — frontend confirma ponto no mapa</li>
 *   <li>{@link ReportSymptomsService} retorna {@code void} — frontend já calculou e exibiu o resultado</li>
 * </ul>
 *
 * <p>Princípio ISP (Interface Segregation): não forçamos um retorno único que não se aplica
 * a todos os tipos. Cada implementação usa o tipo de retorno correto para o seu contrato HTTP.
 *
 * @param <T> DTO de entrada específico do tipo de report
 * @param <R> Tipo de retorno (use {@link Void} para operações sem resposta ao cliente)
 */
public interface ReportService<T, R> {
    R criar(T dto, UUID cidadaoId);
}
