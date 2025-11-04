package br.ifpb.project.denguemaps.pdmreportms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/private/secure")
public class PrivateController {

    @GetMapping("/hello")
    @PreAuthorize("hasAnyRole('agente_saude')")
    public ResponseEntity<String> helloSecure() {
        return ResponseEntity.ok("Hello World / Olá Mundo");
    }
}