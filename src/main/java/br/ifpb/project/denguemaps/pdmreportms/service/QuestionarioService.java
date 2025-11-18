package br.ifpb.project.denguemaps.pdmreportms.service;


import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioAtualizarDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioCriarDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmreportms.entity.Questionario;
import br.ifpb.project.denguemaps.pdmreportms.repository.CidadaoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.QuestionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionarioService {
    private final QuestionarioRepository questionarioRepository;
    private final CidadaoRepository cidadaoRepository;

    public QuestionarioResponseDTO registrarQuestionarioComRetorno(
            QuestionarioCriarDTO questionarioCriarDTO){
        Questionario questionario = new Questionario();
        aplicarCriacaoEntidadeQuestionarioSemRetorno(questionario, questionarioCriarDTO);
        questionario.setCreatedAt(OffsetDateTime.now());
        questionario.setUpdatedBy(OffsetDateTime.now());
        questionario = salvarQuestionario(questionario);
        return retornarResponseDTO(questionario);
    }

    public QuestionarioResponseDTO atualizarQuestionarioComRetorno(
            QuestionarioAtualizarDTO questionarioAtualizarDTO,
            String token){
        Questionario questionario = buscarQuestionarioPorId(questionarioAtualizarDTO.getId());
        aplicarMudancaEntidadeQuestionarioSemRetorno(questionario, questionarioAtualizarDTO, token);
        questionario.setUpdatedBy(OffsetDateTime.now());
        salvarQuestionario(questionario);
        return retornarResponseDTO(questionario);
    }

    public void deletarQuestionario(UUID idQuestionario){
        deletarQuestionarioEspecifico(idQuestionario);
    }

    public QuestionarioResponseDTO buscarQuestionarioEspecifico(UUID uuid){
        Questionario questionario = questionarioRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Questionario não encontrado"));
        return retornarResponseDTO(questionario);
    }

    public List<QuestionarioResponseDTO> buscarQuestionarioCidadaoEspecifico(UUID idCidadao){
        List<Questionario> Listaquestionario = questionarioRepository.findAllByCidadaoId(idCidadao);
        return mapearReportsResponseDTO(Listaquestionario);
    }

    public List<QuestionarioResponseDTO> buscarTodoQuestionario(){
        List<Questionario> listaQuestionario = questionarioRepository.findAll();
        return mapearReportsResponseDTO(listaQuestionario);
    }



    // Metodos auxiliares:
    public void deletarTodoQuestionarioCidadaoEspecifico(UUID idCidadao){
        questionarioRepository.deleteAllByCidadaoId(idCidadao);
    }


    private Questionario salvarQuestionario(Questionario questionario){
        return questionarioRepository.save(questionario);
    }

    private void deletarQuestionarioEspecifico(UUID idQuestionario){
        questionarioRepository.deleteById(idQuestionario);
    }

    private Questionario buscarQuestionarioPorId(UUID idQuestionario){
        return questionarioRepository.findById(idQuestionario)
                .orElseThrow(() -> new IllegalArgumentException("Questionario não encontrado"));
    }

    private List<QuestionarioResponseDTO> mapearReportsResponseDTO(List<Questionario> questionario) {
        return questionario.stream()
                .map(this::retornarResponseDTO)
                .collect(Collectors.toList());
    }

    private QuestionarioResponseDTO retornarResponseDTO(Questionario questionario){
        QuestionarioResponseDTO questionarioResponseDTO = new QuestionarioResponseDTO();
        questionarioResponseDTO.setId(questionario.getId());
        questionarioResponseDTO.setPerguntas(questionario.getPerguntas());
        questionarioResponseDTO.setRespostas(questionario.getRespostas());
        questionarioResponseDTO.setCreatedAt(questionario.getCreatedAt());
        questionarioResponseDTO.setUpdatedBy(questionario.getUpdatedBy());
        questionarioResponseDTO.setFkCidadaoId(questionario.getCidadao().getId());
        return questionarioResponseDTO;
    }
    private void aplicarMudancaEntidadeQuestionarioSemRetorno(
            Questionario questionario,
            QuestionarioAtualizarDTO questionarioAtualizarDTO,
            String token){
        questionario.setPerguntas(questionarioAtualizarDTO.getPerguntas());
        questionario.setRespostas(questionarioAtualizarDTO.getRespostas());
        questionario.setCidadao(buscarCidadao(questionarioAtualizarDTO.getFkCidadaoId()));
    }

    private void aplicarCriacaoEntidadeQuestionarioSemRetorno(
            Questionario questionario,
            QuestionarioCriarDTO questionarioCriarDTO){
        questionario.setPerguntas(questionarioCriarDTO.getPerguntas());
        questionario.setRespostas(questionarioCriarDTO.getRespostas());
        questionario.setCidadao(buscarCidadao(questionarioCriarDTO.getFkCidadaoId()));
    }

    private Cidadao buscarCidadao(UUID idCidadao){
        return cidadaoRepository.findById(idCidadao)
                .orElseThrow(() -> new IllegalArgumentException("Cidadao não encontrado"));
    }
}
