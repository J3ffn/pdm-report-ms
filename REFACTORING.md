# Guia de Refatoração Arquitetural — pdm-report-ms

Este documento resume a refatoração integral executada no repositório **`pdm-report-ms`** para sanar dívidas técnicas, eliminar riscos de concorrência e mensageria, alinhar as respostas aos padrões REST, unificar o controle de banco de dados com Flyway e estabelecer uma suíte de 30 testes automatizados.

---

## 📚 Documentação Técnica Completa (Pasta `/docs`)

Para entender em detalhes a motivação de cada decisão arquitetural, consulte os documentos dedicados:

| Documento | Descrição |
| :--- | :--- |
| **[docs/README.md](docs/README.md)** | Índice geral e visão sistêmica da nova arquitetura |
| **[docs/01-arquitetura-e-cqrs.md](docs/01-arquitetura-e-cqrs.md)** | Separação CQRS (`ReportQueryService` vs `ReportLifecycleService`), modelo rico e correção de polimorfismo JPA (`Hibernate.unproxy`) |
| **[docs/02-resiliencia-mensageria-e-transacoes.md](docs/02-resiliencia-mensageria-e-transacoes.md)** | Eliminação do *Dual-Write Hazard* no RabbitMQ via eventos transacionais pós-commit (`@TransactionalEventListener(AFTER_COMMIT)`) |
| **[docs/03-banco-de-dados-flyway-e-perfis.md](docs/03-banco-de-dados-flyway-e-perfis.md)** | Ativação do Flyway, transição de `ddl-auto: update` para `validate`, otimização `JSONB` no PostgreSQL e organização dos perfis YAML |
| **[docs/04-seguranca-rest-e-excecoes.md](docs/04-seguranca-rest-e-excecoes.md)** | Hierarquia de exceções REST (404, 403, 422, 400), `SecurityUtils` resiliente e Swagger com Bearer JWT |
| **[docs/05-observabilidade-e-testes.md](docs/05-observabilidade-e-testes.md)** | Endpoints de saúde do Actuator (`/actuator/health`) e suíte de 30 testes automatizados com 100% de aprovação |

---

## 🚀 Resumo Executivo das Mudanças

```
                                NOVA ARQUITETURA
                                
[Cliente HTTP] ──► [ReportController] 
                        │
       ┌────────────────┼────────────────┐
       ▼                ▼                ▼
[QueryService]    [LifecycleService]  [Focus / Symptoms Service]
(readOnly = true)   (desativar, visit)         │ (criar report)
       │                │                      ▼
       │                └──────────────► [Domain Events]
       │                                       │
       ▼                                       ▼ (@TransactionalEventListener)
[PostgreSQL 16] ◄── [Commit OK?] ────────► [RabbitMQ Publisher]
(Flyway V1 JSONB)                               (reports.events)
```

1. **CQRS Estrito**: `ReportQueryService` agora é 100% somente-leitura; `ReportLifecycleService` centraliza mutações de status.
2. **Mensageria Segura**: Eventos só chegam ao RabbitMQ se o PostgreSQL confirmar o commit com sucesso.
3. **Padrões REST**: Recursos inexistentes retornam `404 Not Found`, acessos negados retornam `403 Forbidden` e violações de regras retornam `422 Unprocessable Entity`.
4. **Fonte Única de Schema**: O Flyway gerencia o DDL do banco e o Hibernate opera com `ddl-auto: validate`.
5. **Cobertura de Testes**: Saímos de zero testes para 30 testes unitários e de integração cobrindo fluxos críticos.
