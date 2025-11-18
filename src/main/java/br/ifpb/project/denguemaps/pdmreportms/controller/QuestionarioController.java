package br.ifpb.project.denguemaps.pdmreportms.controller;


import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioAtualizarDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioCriarDTO;
import br.ifpb.project.denguemaps.pdmreportms.dto.questionario.QuestionarioResponseDTO;
import br.ifpb.project.denguemaps.pdmreportms.service.QuestionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/questionario")
public class QuestionarioController {

    private final QuestionarioService questionarioService;

    @PostMapping
    public ResponseEntity<QuestionarioResponseDTO> registrarQuestionario(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody @Valid QuestionarioCriarDTO questionarioCriarDTO
            ){
        String token = authHeader.replace("Bearer ", "");
        return new ResponseEntity<>(
                questionarioService.registrarQuestionarioComRetorno(
                questionarioCriarDTO),
                HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<QuestionarioResponseDTO> atualizarQuestionario(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody @Valid QuestionarioAtualizarDTO questionarioAtualizarDTO
            ){
        String token = authHeader.replace("Bearer ", "");
        return new ResponseEntity<>(
                questionarioService.atualizarQuestionarioComRetorno(questionarioAtualizarDTO, token),
                HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<String> deletarQuestionarioEspecifico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam UUID uuid
            ){
        questionarioService.deletarQuestionario(uuid);
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/all")
    public ResponseEntity<List<QuestionarioResponseDTO>> retornarTodoQuestionario(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ){
        return new ResponseEntity<>(questionarioService.buscarTodoQuestionario(), HttpStatus.OK);
    }

    @GetMapping("/esp")
    public ResponseEntity<QuestionarioResponseDTO> retornarQuestionarioEspecifico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam UUID uuid
    ){
        return new ResponseEntity<>(questionarioService.buscarQuestionarioEspecifico(uuid), HttpStatus.OK);
    }

    @GetMapping("/esp-cidadao")
    public ResponseEntity<List<QuestionarioResponseDTO>> retornarTodoQuestionarioCidadaoEspecifico(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam UUID uuid
    ){
        return new ResponseEntity<>(
                questionarioService.buscarQuestionarioCidadaoEspecifico(uuid),
                HttpStatus.OK);
    }
}
