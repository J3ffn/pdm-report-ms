# 05. Observabilidade e Suíte de Testes Automatizados

## 1. Observabilidade com Spring Boot Actuator

Em ambientes conteinerizados (Docker, Kubernetes) e pipelines de integração contínua (CI/CD), a aplicação precisa comunicar de forma padronizada o seu estado de saúde e métricas internas.

### 1.1 Por que o Actuator foi adicionado?
No código original da [`SecurityConfig`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/config/SecurityConfig.java), havia a regra:
```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
```
Contudo, o `pom.xml` **não continha** o starter do Actuator. Logo, qualquer chamada para `/actuator/health` retornava HTTP 404 Not Found.

### 1.2 Configuração Implementada
1. Adicionamos a dependência `spring-boot-starter-actuator` no `pom.xml`.
2. Configuramos a exposição controlada dos endpoints no [`application.yml`](../src/main/resources/application.yml):
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,metrics
     endpoint:
       health:
         show-details: when_authorized
   ```
3. **Benefícios imediatos**:
   - **`GET /actuator/health`**: Probes de *liveness* e *readiness* do Kubernetes/Docker para reiniciar o container automaticamente se o banco ou RabbitMQ caírem.
   - **`GET /actuator/metrics`**: Permite raspagem (*scraping*) de métricas de JVM, pool de conexões HikariCP e requisições HTTP pelo Prometheus.

---

## 2. Da Ausência Total para 30 Testes Automatizados

O microsserviço original **não possuía um único teste automatizado** no diretório `src/test/java`.

Para garantir que a aplicação possa ser mantida, evoluída e colocada em produção com segurança, criamos uma suíte completa de testes unitários e de integração de slice (`@WebMvcTest`).

### 2.1 Perfil de Testes Dedicado ([`application-test.yml`](../src/test/resources/application-test.yml))
Criamos uma configuração isolada para os testes:
- Banco de dados **H2 em memória** no modo de compatibilidade PostgreSQL (`MODE=PostgreSQL`).
- Flyway desativado nos testes rápidos de slice (`spring.flyway.enabled: false`).
- RabbitMQ mockado ou com auto-startup desativado para testes sem dependência de infraestrutura externa.

---

## 3. Matriz de Cobertura dos Testes

Abaixo estão os 30 testes desenvolvidos e o que cada um valida:

### A. Serviços Geoespaciais e H3 ([`H3ServiceTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/service/H3ServiceTest.java) & [`GeoServiceTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/service/GeoServiceTest.java))
| Teste | Objetivo |
| :--- | :--- |
| `deveCalcularRes8ComSucesso` | Valida cálculo do índice H3 resolução 8 (~0,7 km²) para latitude/longitude reais |
| `deveCalcularRes6ComSucesso` | Valida cálculo do índice H3 resolução 6 (~36 km²) para agregação de mapa |
| `coordenadasProximasDevemCompartilharMesmoHexagonoRes6` | Garante que coordenadas no mesmo bairro agrupam no mesmo hexágono H3 |
| `deveCriarGeoComSucesso` | Garante que `GeoService` persiste a entidade `tb_geo` com ambos os índices calculados |

### B. Criação de Reports e Mensageria ([`ReportFocusServiceTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/service/ReportFocusServiceTest.java) & [`ReportSymptomsServiceTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/service/ReportSymptomsServiceTest.java))
| Teste | Objetivo |
| :--- | :--- |
| `deveCriarReportFocoComSucesso` | Valida persistência do foco e captura a emissão de `ReportCreatedEvent` com routing key `"report.focus.created"` |
| `scoreMaiorOuIgual50DeveMarcarDoenca` | Valida a regra de negócio do limiar: score $\ge$ 50 define `isDisease = true` e emite `"report.symptom.created"` |
| `scoreMenor50DeveMarcarDoencaComoFalse` | Valida a regra do limiar: score $<$ 50 define `isDisease = false` |

### C. Ciclo de Vida e Auditoria ([`ReportLifecycleServiceTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/service/ReportLifecycleServiceTest.java))
| Teste | Objetivo |
| :--- | :--- |
| `cidadaoDonoDeveDesativarComSucesso` | Cidadão autenticado desativa seu próprio report (`is_enabled = false`) e dispara `ReportDisabledEvent` |
| `adminDeveDesativarReportDeQualquerUm` | Administrador desativa qualquer report de terceiros com sucesso |
| `naoDonoNemAdminDeveFalharAoDesativar` | Usuário comum tentando desativar report de outro cidadão recebe `AccessDeniedException` (HTTP 403) |
| `desativarInexistenteDeveLancarResourceNotFoundException` | Tentativa de desativar report que não existe lança `ResourceNotFoundException` (HTTP 404) |
| `desativarJaInativoDeveLancarBusinessRuleException` | Tentativa de desativar report que já estava inativo lança `BusinessRuleException` (HTTP 422) |
| `marcarVisitadoComSucesso` | Agente de saúde marca report como visitado e dispara `ReportVisitedEvent` |
| `marcarVisitadoInexistenteDeveLancarResourceNotFoundException` | Marcar visita em report inativo ou inexistente lança `ResourceNotFoundException` (HTTP 404) |

### D. Consultas e Isolamento CQRS ([`ReportQueryServiceTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/service/ReportQueryServiceTest.java))
| Teste | Objetivo |
| :--- | :--- |
| `deveBuscarPorIdQuandoAtivo` | Retorna DTO com detalhes do report ativo |
| `deveLancarExceptionQuandoReportInativo` | Garante que reports soft-deleted não apareçam em consultas de ID |
| `deveListarTodosPaginado` | Testa paginação de reports ativos para agentes e admins |
| `deveListarMeusReports` | Testa histórico paginado de reports do cidadão autenticado |
| `deveRetornarVazioParaAnonimo` | Garante que cidadão sem ID (anônimo) receba página vazia segura sem estourar exceção |

### E. Tratamento Global de Erros ([`GlobalExceptionHandlerTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/exception/GlobalExceptionHandlerTest.java))
| Teste | Objetivo |
| :--- | :--- |
| `deveRetornar404ParaResourceNotFound` | Verifica conversão para HTTP 404 e geração de `traceId` |
| `deveRetornar403ParaAccessDenied` | Verifica conversão para HTTP 403 Forbidden |
| `deveRetornar422ParaBusinessRule` | Verifica conversão para HTTP 422 Unprocessable Entity |
| `deveRetornar400ParaIllegalArgument` | Verifica conversão para HTTP 400 Bad Request |
| `deveRetornar500ParaExceptionGenerica` | Verifica conversão para HTTP 500 sem vazar stack trace no body |

### F. Camada Web e Endpoints REST ([`ReportControllerTest`](../src/test/java/br/ifpb/project/denguemaps/pdmreportms/controller/ReportControllerTest.java))
| Teste | Endpoint Testado | Resposta Esperada |
| :--- | :--- | :---: |
| `deveRegistrarFocoComSucesso` | `POST /api/reports/focus` | 201 Created |
| `deveRegistrarSintomasComSucesso` | `POST /api/reports/symptoms` | 201 Created |
| `deveBuscarPorIdComSucesso` | `GET /api/reports/{id}` | 200 OK |
| `deveRetornar404QuandoNaoEncontrado` | `GET /api/reports/{id}` | 404 Not Found |
| `deveDesativarReportComSucesso` | `DELETE /api/reports/{id}` | 204 No Content |
| `deveMarcarVisitadoComSucesso` | `PATCH /api/reports/{id}/visit` | 200 OK |

---

## 4. Evidência de Execução

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.controller.ReportControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.exception.GlobalExceptionHandlerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.service.GeoServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.service.H3ServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.service.ReportFocusServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.service.ReportLifecycleServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.service.ReportQueryServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running br.ifpb.project.denguemaps.pdmreportms.service.ReportSymptomsServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
