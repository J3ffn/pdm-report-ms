# 01. Arquitetura, CQRS e Domínio Rico

## 1. Contexto do Problema

No design inicial do microsserviço, a divisão de responsabilidades apresentava inconsistências entre leitura e escrita, além de expor fragilidades no mapeamento polimórfico de entidades JPA.

### 1.1 Violação de CQRS e SRP no `ReportQueryService`
O serviço [`ReportQueryService`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/service/ReportQueryService.java) tinha em seu nome e propósito a responsabilidade de realizar **consultas** (*queries*). No entanto, ele acumulava métodos de mutação de estado com efeitos colaterais e envio de mensagens para mensageria:
- `desativar(UUID id, UUID cidadaoId, boolean isAdmin)` — alterava `is_enabled = false`, salvava no banco e disparava evento `report.disabled`.
- `marcarVisitado(UUID id)` — alterava `is_visited = true`, salvava no banco e disparava evento `report.visited`.

Essa mistura gerava:
- **Violação do Princípio da Responsabilidade Única (SRP)**: Uma mesma classe gerenciava consultas paginadas e regras de auditoria/modificação.
- **Quebra do padrão CQRS (Command Query Responsibility Segregation)**: Dificultava otimizações no banco, como aplicar transações somente-leitura (`readOnly = true`) em nível de classe.

---

## 2. Decisão Arquitetural: Segregação CQRS

Dividimos claramente as responsabilidades em dois serviços especializados:

```
                              ┌─────────────────────────┐
                              │    ReportController     │
                              └────────────┬────────────┘
                                           │
                    ┌──────────────────────┴──────────────────────┐
                    ▼                                             ▼
       ┌─────────────────────────┐                   ┌─────────────────────────┐
       │   ReportQueryService    │                   │ ReportLifecycleService  │
       │  (Somente Leitura)      │                   │  (Mutações de Estado)   │
       ├─────────────────────────┤                   ├─────────────────────────┤
       │ + buscarPorId(UUID)     │                   │ + desativar(...)        │
       │ + listarTodos(Pageable) │                   │ + marcarVisitado(UUID)  │
       │ + listarMeus(UUID, ...) │                   └─────────────────────────┘
       └─────────────────────────┘
```

### 2.1 Benefícios de `ReportQueryService` com `@Transactional(readOnly = true)`
Ao aplicar `@Transactional(readOnly = true)` no `ReportQueryService`:
- O Hibernate define a sessão como *FlushMode.MANUAL*, desativando a verificação de sujeira (*dirty checking*) e economizando ciclos de CPU e memória.
- Drivers JDBC e pools de conexão (HikariCP) podem rotear consultas para réplicas de leitura (*read-only replicas*) no PostgreSQL.

### 2.2 Isolamento de Ciclo de Vida no `ReportLifecycleService`
O novo serviço [`ReportLifecycleService`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/service/ReportLifecycleService.java) passou a ser a única autoridade responsável pela alteração de ciclo de vida dos reports, operando sob transações de escrita e emitindo eventos de domínio.

---

## 3. Superação do Modelo Anêmico (Rich Domain Model)

No modelo anterior, a entidade [`ReportEntidade`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/model/ReportEntidade.java) era anêmica — apenas uma casca com *getters* e *setters*. A lógica que protegia as invariantes do negócio ficava solta e duplicada nos serviços.

### Refatoração Aplicada:
Encapsulamos as regras de transição de estado diretamente na entidade:

```java
public abstract class ReportEntidade {
    // ...

    /**
     * Regra de negócio: desativa o report protegendo invariantes de domínio.
     * Cidadão desativa apenas o seu próprio; admin desativa qualquer um.
     */
    public void desativar(UUID executorId, boolean isAdmin) {
        if (Boolean.FALSE.equals(this.isEnabled)) {
            throw new BusinessRuleException("Report já está inativo.");
        }
        boolean ehDono = this.fkPersonId != null && this.fkPersonId.equals(executorId);
        if (!isAdmin && !ehDono) {
            throw new AccessDeniedException("Sem permissão para desativar este report.");
        }
        this.isEnabled = false;
    }

    /**
     * Regra de negócio: marca o report como visitado por agente de saúde.
     */
    public void marcarVisitado() {
        if (Boolean.FALSE.equals(this.isEnabled)) {
            throw new BusinessRuleException("Report não encontrado ou inativo.");
        }
        this.isVisited = true;
    }
}
```

**Por que essa mudança ocorreu assim?**
1. **Invariantes protegidas**: É impossível desativar um report já inativo, ou desativar o report de terceiros sem privilégio de admin, independente de onde o método seja chamado.
2. **Alta testabilidade**: As regras de domínio podem ser testadas unitariamente de forma isolada, sem necessidade de levantar contexto do Spring ou banco de dados.

---

## 4. Polimorfismo Seguro em Herança JPA (`JOINED`)

O modelo de dados utiliza herança relacional com tabela base e tabelas filhas:
- Tabela base: `tb_reports`
- Tabelas filhas: `tb_report_focus` e `tb_report_symptoms`
- Estratégia JPA: `@Inheritance(strategy = InheritanceType.JOINED)`

### 4.1 O Perigo Oculto dos Proxies do Hibernate (ByteBuddy)
Ao executar `reportRepository.findById(id)`, o JPA/Hibernate muitas vezes instancia um **proxy ByteBuddy** da classe abstrata `ReportEntidade` para viabilizar lazy-loading e polimorfismo.

No [`ReportMapper`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/mapper/ReportMapper.java), havia o código:
```java
// Código anterior (problemático com proxies Hibernate):
if (report instanceof ReportFocusEntidade foco) {
    localDescription = foco.getLocalDescription();
} else if (report instanceof ReportSymptomsEntidade sintomas) {
    respostas = sintomas.getRespostas();
    // ...
}
```
**O Bug Silencioso:** Se o objeto `report` for um Proxy gerenciado de `ReportEntidade`, a instrução `report instanceof ReportFocusEntidade` avalia para `false`! O DTO retornado continha `localDescription = null` e `respostas = null`, mesmo que a linha existisse no banco.

### 4.2 A Solução Adotada: `Hibernate.unproxy`
Refatoramos o mapper para desempacotar o proxy antes da checagem de tipos polimórficos:

```java
public static ReportDetailResponseDTO toDetailDTO(ReportEntidade report) {
    ReportEntidade unproxied = (ReportEntidade) org.hibernate.Hibernate.unproxy(report);

    String localDescription = null;
    java.util.Map<String, String> respostas = null;
    Integer scoreTotal = null;
    java.util.UUID questionnaireId = null;

    if (unproxied instanceof ReportFocusEntidade foco) {
        localDescription = foco.getLocalDescription();
    } else if (unproxied instanceof ReportSymptomsEntidade sintomas) {
        respostas = sintomas.getRespostas();
        scoreTotal = sintomas.getScoreTotal();
        questionnaireId = sintomas.getFkQuestionnaireId();
    }
    // ...
}
```

Com o `Hibernate.unproxy(report)`, garantimos que a instância real subjacente seja inspecionada, tornando o mapeamento 100% determinístico e à prova de falhas com qualquer fetch strategy do JPA.
