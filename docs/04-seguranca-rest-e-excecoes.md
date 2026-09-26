# 04. Segurança Declarativa, Padronização REST e Exceções

## 1. O Problema das Respostas de Erro REST

Na versão anterior, o tratamento de erros do microsserviço violava padrões fundamentais da especificação HTTP e do modelo REST:

```java
// Código anterior:
@ExceptionHandler(ReportNegocioException.class)
public ResponseEntity<ErrorResponseDTO> handleNegocio(...) {
    // Retornava HTTP 422 (Unprocessable Entity) para QUALQUER erro de negócio
    return ResponseEntity.unprocessableEntity().body(body);
}
```

Isso causava os seguintes problemas:
1. **Recurso Inexistente retornava 422 em vez de 404**:
   Quando alguém buscava `GET /api/reports/{id}` e o ID não existia, a resposta era `422 Unprocessable Entity` com mensagem `"Report não encontrado ou inativo"`. Clientes HTTP, gateways e frontends esperam estritamente `404 Not Found`.
2. **Acesso Negado retornava 422 em vez de 403**:
   Quando um cidadão tentava desativar o report de outro cidadão, a resposta era `422` com mensagem `"Sem permissão para desativar este report"`. O código correto pelo RFC 9110 é `403 Forbidden`.
3. **Payload Malformado causava 500 em vez de 400**:
   Quando o JSON da requisição continha sintaxe inválida ou UUID corrompido, a exceção `IllegalArgumentException` caía no `handleErroInterno`, gerando `500 Internal Server Error` (sugerindo erro de código/infraestrutura em vez de falha do cliente).

---

## 2. Nova Hierarquia de Exceções RESTful

Reestruturamos as exceções do pacote `exception` com mapeamento semântico 1:1 para códigos HTTP:

```
                            ┌────────────────────────┐
                            │    RuntimeException    │
                            └───────────┬────────────┘
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             ▼                          ▼                          ▼
┌─────────────────────────┐┌─────────────────────────┐┌─────────────────────────┐
│ResourceNotFoundException││  AccessDeniedException  ││  BusinessRuleException  │
│      (HTTP 404)         ││      (HTTP 403)         ││      (HTTP 422)         │
└─────────────────────────┘└─────────────────────────┘└────────────┬────────────┘
                                                                   ▼
                                                      ┌─────────────────────────┐
                                                      │  ReportNegocioException │
                                                      │  (Legado Retrocompat.)  │
                                                      └─────────────────────────┘
```

### 2.1 Mapeamento no [`GlobalExceptionHandler`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/exception/GlobalExceptionHandler.java)

| Exceção | Status HTTP | Significado REST |
| :--- | :---: | :--- |
| `MethodArgumentNotValidException` | **400 Bad Request** | Validações de anotações `@Valid` (`@NotNull`, `@Min`, `@Max`, `@NotBlank`) |
| `IllegalArgumentException` / `HttpMessageNotReadableException` | **400 Bad Request** | Sintaxe JSON corrompida, tipo incompatível ou UUID malformado |
| `ResourceNotFoundException` | **404 Not Found** | Report não localizado por ID ou inativo na base |
| `AccessDeniedException` | **403 Forbidden** | Cidadão tentando alterar dados que pertencem a outro usuário |
| `BusinessRuleException` | **422 Unprocessable Entity** | Violação de estado de negócio (ex: tentar inativar report já inativo) |
| `Exception` (genérica) | **500 Internal Server Error** | Erro não previsto, gerando `traceId` sem vazar detalhes internos ao cliente |

---

## 3. Segurança e Utilitários de Autenticação

### 3.1 O Problema do `UUID.fromString(jwt.getSubject())`
No controller original, havia o seguinte método privado:
```java
private UUID extrairId(Jwt jwt) {
    return jwt != null ? UUID.fromString(jwt.getSubject()) : null;
}
```
**O Bug**: Se o token JWT do Keycloak contivesse no claim `sub` um formato diferente de UUID (ou uma string com espaços/caracteres inválidos), `UUID.fromString` lançava `IllegalArgumentException`, gerando erro 500 para uma requisição legítima do cliente.

### 3.2 Criação do [`SecurityUtils`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/security/SecurityUtils.java)
Centralizamos e tornamos a extração resiliente com `Optional<UUID>`:
```java
public static Optional<UUID> extractUserId(Jwt jwt) {
    if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
        return Optional.empty();
    }
    try {
        return Optional.of(UUID.fromString(jwt.getSubject()));
    } catch (IllegalArgumentException e) {
        log.warn("Subject do JWT não pôde ser convertido para UUID: {}", jwt.getSubject());
        return Optional.empty();
    }
}
```

Além disso, substituímos a checagem imperativa de authorities que existia no controller por métodos utilitários:
- `SecurityUtils.isAdmin(authentication)`
- `SecurityUtils.isHealthAgent(authentication)`
- `SecurityUtils.hasRole(authentication, role)`

### 3.3 Habilitação de `@EnableMethodSecurity`
Na [`SecurityConfig`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/config/SecurityConfig.java), adicionamos `@EnableMethodSecurity`, habilitando o uso de anotações declarativas como `@PreAuthorize("hasRole('admin')")` diretamente nos métodos da aplicação quando desejado.

---

## 4. Documentação OpenAPI 3.0 e Swagger com Bearer JWT

Embora a biblioteca `springdoc-openapi` estivesse no `pom.xml`, a interface do Swagger UI não conseguia autenticar requisições porque o esquema de segurança JWT não estava configurado.

### 4.1 Criação do [`OpenApiConfig`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/config/OpenApiConfig.java)
Configuramos o esquema `bearerAuth` no OpenAPI 3.0:
- Permite colar o token JWT Bearer diretamente no botão **"Authorize"** do Swagger UI (`http://localhost:8082/swagger-ui.html`).
- Todas as rotas autenticadas passam a receber o token automaticamente.

### 4.2 Anotações no [`ReportController`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/controller/ReportController.java)
Todos os endpoints foram anotados com:
- `@Tag`: agrupamento temático da API.
- `@Operation`: resumo claro e propósito de cada rota.
- `@ApiResponses` e `@ApiResponse`: documentação dos status de sucesso (200, 201, 204) e dos cenários de erro possíveis (400, 403, 404).
