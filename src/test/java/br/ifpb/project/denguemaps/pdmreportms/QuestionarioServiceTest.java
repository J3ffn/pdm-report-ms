package br.ifpb.project.denguemaps.pdmreportms;

import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioAtualizarDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioCriarDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmreportms.entity.Questionario;
import br.ifpb.project.denguemaps.pdmreportms.repository.CidadaoRepository;
import br.ifpb.project.denguemaps.pdmreportms.repository.QuestionarioRepository;
import br.ifpb.project.denguemaps.pdmreportms.service.QuestionarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Permite stubs definidos no setup que não são usados em todos os testes.
class QuestionarioServiceTest {

    @Mock
    private QuestionarioRepository questionarioRepository;

    @Mock
    private CidadaoRepository cidadaoRepository;

    @InjectMocks
    private QuestionarioService questionarioService;

    // Dados de teste
    private final UUID CIDADAO_ID = UUID.randomUUID();
    private final UUID QUESTIONARIO_ID = UUID.randomUUID();
    private Cidadao mockCidadao;
    private Questionario mockQuestionario;
    private QuestionarioCriarDTO mockCriarDTO;
    private QuestionarioAtualizarDTO mockAtualizarDTO;

    @BeforeEach
    void setup() {
        // 1. Inicialização de Cidadao usando .set()
        mockCidadao = new Cidadao();
        mockCidadao.setId(CIDADAO_ID);
        mockCidadao.setNome("Joao Teste");
        mockCidadao.setCpf("000.111.222-33");

        // 2. Inicialização de Questionario (Entidade JPA, geralmente usa construtor completo)
        mockQuestionario = new Questionario(
                QUESTIONARIO_ID,
                "{\"q1\": \"Perg 1\"}", // perguntas
                "{\"q1\": \"Resp 1\"}",  // respostas
                mockCidadao,
                OffsetDateTime.now().minusDays(2),
                OffsetDateTime.now().minusDays(1)
        );

        // 3. Inicialização de DTOs de Criação usando .set()
        mockCriarDTO = new QuestionarioCriarDTO();
        mockCriarDTO.setPerguntas("{\"q1\": \"Nova Perg 1\"}");
        mockCriarDTO.setRespostas("{\"q1\": \"Nova Resp 1\"}");
        mockCriarDTO.setFkCidadaoId(CIDADAO_ID);

        // 4. Inicialização de DTOs de Atualização usando .set()
        mockAtualizarDTO = new QuestionarioAtualizarDTO();
        mockAtualizarDTO.setId(QUESTIONARIO_ID);
        mockAtualizarDTO.setPerguntas("{\"q1\": \"Perg ATUALIZADA\"}");
        mockAtualizarDTO.setRespostas("{\"q1\": \"Resp ATUALIZADA\"}");
        mockAtualizarDTO.setFkCidadaoId(CIDADAO_ID);

        // Configuração Padrão de Repositório (Reutilizada em vários testes)
        when(cidadaoRepository.findById(CIDADAO_ID)).thenReturn(Optional.of(mockCidadao));
    }

    // --- Testes para registrarQuestionarioComRetorno ---

    @Test
    void registrarQuestionarioComRetorno_shouldSaveAndReturnResponseDTO_onSuccess() {
        // Arrange
        ArgumentCaptor<Questionario> questionarioCaptor = ArgumentCaptor.forClass(Questionario.class);

        when(questionarioRepository.save(questionarioCaptor.capture())).thenAnswer(invocation -> {
            Questionario capturedQuestionario = invocation.getArgument(0);
            capturedQuestionario.setId(QUESTIONARIO_ID);
            // Simula a data de criação/atualização sendo preenchida
            capturedQuestionario.setCreatedAt(OffsetDateTime.now());
            capturedQuestionario.setUpdatedBy(OffsetDateTime.now());
            return capturedQuestionario;
        });

        // Act
        QuestionarioResponseDTO result = questionarioService.registrarQuestionarioComRetorno(mockCriarDTO);

        // Assert
        Questionario capturedQuestionario = questionarioCaptor.getValue();

        verify(questionarioRepository, times(1)).save(any(Questionario.class));

        assertEquals(mockCriarDTO.getPerguntas(), capturedQuestionario.getPerguntas());
        assertEquals(CIDADAO_ID, result.getFkCidadaoId());
    }

    @Test
    void registrarQuestionarioComRetorno_shouldThrowIllegalArgumentException_whenCidadaoNotFound() {
        // Arrange
        UUID nonExistingCidadaoId = UUID.randomUUID();
        QuestionarioCriarDTO dto = new QuestionarioCriarDTO();
        dto.setPerguntas("{}");
        dto.setRespostas("{}");
        dto.setFkCidadaoId(nonExistingCidadaoId); // ID que não existe

        when(cidadaoRepository.findById(nonExistingCidadaoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionarioService.registrarQuestionarioComRetorno(dto),
                "Deve lançar IllegalArgumentException quando o Cidadao não for encontrado.");

        verify(questionarioRepository, never()).save(any(Questionario.class));
    }

    // --- Testes para atualizarQuestionarioComRetorno ---

    @Test
    void atualizarQuestionarioComRetorno_shouldUpdateAndReturnResponseDTO_onSuccess() {
        // Arrange
        String mockToken = "VALID_TOKEN_123";

        // Simulação 1: Encontrar o Questionário existente
        when(questionarioRepository.findById(QUESTIONARIO_ID)).thenReturn(Optional.of(mockQuestionario));

        // Simulação 2: Salvar o Questionário atualizado (o mockQuestionario será modificado no serviço)
        when(questionarioRepository.save(any(Questionario.class))).thenReturn(mockQuestionario);

        // Act
        QuestionarioResponseDTO result = questionarioService.atualizarQuestionarioComRetorno(mockAtualizarDTO, mockToken);

        // Assert
        verify(questionarioRepository, times(1)).findById(QUESTIONARIO_ID);
        verify(cidadaoRepository, times(1)).findById(CIDADAO_ID); // Busca do Cidadao para atualização
        verify(questionarioRepository, times(1)).save(mockQuestionario);

        // Verifica se os campos foram atualizados no objeto mockQuestionario
        assertEquals(mockAtualizarDTO.getPerguntas(), mockQuestionario.getPerguntas());
        assertEquals(mockAtualizarDTO.getRespostas(), mockQuestionario.getRespostas());

        // Verifica o DTO de resposta
        assertEquals(mockAtualizarDTO.getPerguntas(), result.getPerguntas());
    }

    @Test
    void atualizarQuestionarioComRetorno_shouldThrowIllegalArgumentException_whenQuestionarioNotFound() {
        // Arrange
        UUID nonExistingQuestionarioId = UUID.randomUUID();
        QuestionarioAtualizarDTO dto = new QuestionarioAtualizarDTO();
        dto.setId(nonExistingQuestionarioId);
        dto.setPerguntas("{}");
        dto.setRespostas("{}");
        dto.setFkCidadaoId(CIDADAO_ID);

        when(questionarioRepository.findById(nonExistingQuestionarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionarioService.atualizarQuestionarioComRetorno(dto, "token"),
                "Deve lançar IllegalArgumentException quando o Questionario não for encontrado.");

        verify(questionarioRepository, never()).save(any(Questionario.class));
    }

    @Test
    void atualizarQuestionarioComRetorno_shouldThrowIllegalArgumentException_whenCidadaoNotFound() {
        // Arrange
        UUID nonExistingCidadaoId = UUID.randomUUID();
        QuestionarioAtualizarDTO dto = new QuestionarioAtualizarDTO();
        dto.setId(QUESTIONARIO_ID);
        dto.setPerguntas("{}");
        dto.setRespostas("{}");
        dto.setFkCidadaoId(nonExistingCidadaoId); // ID do Cidadao que não existe

        // Simula o Questionário encontrado
        when(questionarioRepository.findById(QUESTIONARIO_ID)).thenReturn(Optional.of(mockQuestionario));

        // Simula a falha na busca do Cidadao
        when(cidadaoRepository.findById(nonExistingCidadaoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionarioService.atualizarQuestionarioComRetorno(dto, "token"),
                "Deve lançar IllegalArgumentException quando o Cidadao (para atualização) não for encontrado.");

        verify(questionarioRepository, never()).save(any(Questionario.class));
    }

    // --- Testes para Métodos de Deleção ---

    @Test
    void deletarQuestionario_shouldCallDeleteById() {
        // Arrange
        UUID questionarioToDeleteId = UUID.randomUUID();

        // Act
        questionarioService.deletarQuestionario(questionarioToDeleteId);

        // Assert
        verify(questionarioRepository, times(1)).deleteById(questionarioToDeleteId);
    }

    @Test
    void deletarTodoQuestionarioCidadaoEspecifico_shouldCallDeleteAllByCidadaoId() {
        // Arrange
        UUID cidadaoIdToDeleteQuestionarios = UUID.randomUUID();

        // Act
        questionarioService.deletarTodoQuestionarioCidadaoEspecifico(cidadaoIdToDeleteQuestionarios);

        // Assert
        verify(questionarioRepository, times(1)).deleteAllByCidadaoId(cidadaoIdToDeleteQuestionarios);
    }

    // --- Testes para Métodos de Busca ---

    @Test
    void buscarQuestionarioEspecifico_shouldReturnSingleQuestionario() {
        // Arrange
        when(questionarioRepository.findById(QUESTIONARIO_ID)).thenReturn(Optional.of(mockQuestionario));

        // Act
        QuestionarioResponseDTO result = questionarioService.buscarQuestionarioEspecifico(QUESTIONARIO_ID);

        // Assert
        assertEquals(QUESTIONARIO_ID, result.getId());
        assertEquals(mockQuestionario.getPerguntas(), result.getPerguntas());
        assertEquals(CIDADAO_ID, result.getFkCidadaoId());
        verify(questionarioRepository, times(1)).findById(QUESTIONARIO_ID);
    }

    @Test
    void buscarQuestionarioEspecifico_shouldThrowException_whenNotFound() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(questionarioRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> questionarioService.buscarQuestionarioEspecifico(nonExistingId));
    }

    @Test
    void buscarQuestionarioCidadaoEspecifico_shouldReturnReportsByCidadaoId() {
        // Arrange
        Questionario mockQuestionario2 = new Questionario(
                UUID.randomUUID(),
                "{}",
                "{}",
                mockCidadao,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        List<Questionario> mockList = List.of(mockQuestionario, mockQuestionario2);

        when(questionarioRepository.findAllByCidadaoId(CIDADAO_ID)).thenReturn(mockList);

        // Act
        List<QuestionarioResponseDTO> results = questionarioService.buscarQuestionarioCidadaoEspecifico(CIDADAO_ID);

        // Assert
        assertEquals(2, results.size());
        assertEquals(CIDADAO_ID, results.get(0).getFkCidadaoId());
        verify(questionarioRepository, times(1)).findAllByCidadaoId(CIDADAO_ID);
    }

    @Test
    void buscarTodoQuestionario_shouldReturnAllQuestionarios() {
        // Arrange
        Questionario mockQuestionario2 = new Questionario(
                UUID.randomUUID(),
                "{}",
                "{}",
                mockCidadao,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        List<Questionario> mockList = List.of(mockQuestionario, mockQuestionario2);

        when(questionarioRepository.findAll()).thenReturn(mockList);

        // Act
        List<QuestionarioResponseDTO> results = questionarioService.buscarTodoQuestionario();

        // Assert
        assertEquals(2, results.size());
        assertEquals(QUESTIONARIO_ID, results.get(0).getId());
        verify(questionarioRepository, times(1)).findAll();
    }
}