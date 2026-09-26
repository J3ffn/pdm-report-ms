# Documentação de Refatoração Arquitetural — pdm-report-ms

Este diretório contém a documentação técnica completa de todas as decisões, padrões arquiteturais e refatorações aplicadas no microsserviço **`pdm-report-ms`** (Mapeamento e Registro de Focos e Sintomas de Dengue).

---

## 📌 Índice dos Documentos

1. [**01. Arquitetura, CQRS e Domínio Rico**](01-arquitetura-e-cqrs.md)
   - Separação entre Consulta e Comando (CQRS) no service layer.
   - Desacoplamento entre `ReportQueryService` e `ReportLifecycleService`.
   - Superação do modelo anêmico: invariantes e regras encapsuladas em `ReportEntidade`.
   - Solução do problema de proxies ByteBuddy em herança JPA `JOINED` via `Hibernate.unproxy`.

2. [**02. Resiliência de Mensageria e Consistência Transacional**](02-resiliencia-mensageria-e-transacoes.md)
   - O perigo do *Dual-Write Hazard* (disparo precoce para RabbitMQ antes do commit no PostgreSQL).
   - Adoção de Spring Domain Events com `@TransactionalEventListener(phase = AFTER_COMMIT)`.
   - Estrutura dos eventos de domínio (`ReportCreatedEvent`, `ReportDisabledEvent`, `ReportVisitedEvent`).

3. [**03. Banco de Dados, Migrações Flyway e Perfis de Configuração**](03-banco-de-dados-flyway-e-perfis.md)
   - Ativação do Flyway e papel do script `V1__init_unified_schema.sql`.
   - Por que substituir `ddl-auto: update` por `ddl-auto: validate`.
   - Migração de tipo PostgreSQL: `JSON` para `JSONB` (compressão e índices GIN).
   - Diagnóstico e boas práticas de organização dos perfis (`application.yml`, `dev`, `prod`, `common`).

4. [**04. Segurança Declarativa, Padronização REST e Exceções**](04-seguranca-rest-e-excecoes.md)
   - Eliminação de HTTP 422 genérico e criação da hierarquia REST (`404 Not Found`, `403 Forbidden`, `422 Unprocessable Entity`).
   - Criação do `SecurityUtils` e extração segura do `sub` do JWT contra falhas 500.
   - Habilitação do `@EnableMethodSecurity`.
   - Documentação viva com OpenAPI 3.0 e autenticação Bearer JWT no Swagger UI.

5. [**05. Observabilidade e Suíte de Testes Automatizados**](05-observabilidade-e-testes.md)
   - Integração do Spring Boot Actuator para probes de liveness/readiness no Docker e Kubernetes.
   - Suíte com 30 testes automatizados cobrindo serviços, segurança, controladores (`MockMvc`), exceções e H3.
   - Matriz de testes e resultados de execução.

---

## 🗺️ Diagrama da Arquitetura Refatorada

```mermaid
graph TD
    subgraph REST API & Segurança
        A[Cliente / Frontend] -->|Requisições HTTP| B[ReportController]
        B -->|Bearer JWT Claims| SU[SecurityUtils]
        B -->|Swagger / OpenAPI Docs| OA[OpenApiConfig]
    end

    subgraph Service Layer - CQRS
        B -->|Leituras| QS[ReportQueryService]
        B -->|Mutações de Ciclo de Vida| LS[ReportLifecycleService]
        B -->|Registro de Foco| FS[ReportFocusService]
        B -->|Registro de Sintomas| SS[ReportSymptomsService]
    end

    subgraph Geoespacial & H3
        FS --> GS[GeoService]
        SS --> GS
        GS --> H3[H3Service - Uber H3]
    end

    subgraph Persistência - PostgreSQL 16
        QS -->|@Transactional readOnly| RR[ReportRepository]
        LS -->|@Transactional write| RR
        FS -->|@Transactional write| RR
        SS -->|@Transactional write| RR
        GS --> GR[GeoRepository]
        DB[(PostgreSQL - tb_geo, tb_reports)]
        RR & GR --> DB
        FW[Flyway Migrations V1] -.->|Gerencia DDL| DB
    end

    subgraph Eventos Pós-Commit & RabbitMQ
        LS -.->|Domain Event| E1[ReportDisabledEvent / ReportVisitedEvent]
        FS -.->|Domain Event| E2[ReportCreatedEvent]
        SS -.->|Domain Event| E3[ReportCreatedEvent]
        E1 & E2 & E3 -->|@TransactionalEventListener AFTER_COMMIT| REL[ReportEventListener]
        REL --> RP[ReportPublisher]
        RP -->|AMQP JSON| EX[Exchange: reports.events]
    end
```
