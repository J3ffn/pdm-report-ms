# 03. Banco de Dados, Migrações Flyway e Perfis de Configuração

## 1. O Problema das Migrações e `ddl-auto`

No início do projeto, coexistiam dois modelos conflitantes de gerenciamento de banco de dados:
1. Um script SQL formal de migração: [`V1__init_unified_schema.sql`](../src/main/resources/db/migration/V1__init_unified_schema.sql).
2. O Hibernate configurado com `spring.jpa.hibernate.ddl-auto: update` no [`application.yml`](../src/main/resources/application.yml) e [`application-dev.yml`](../src/main/resources/application-dev.yml).

### 1.1 Por que essa combinação era problemática?
- **Falta das dependências**: O `pom.xml` não declarava `flyway-core` nem `flyway-database-postgresql`. Portanto, o script `V1__init_unified_schema.sql` era **completamente ignorado** pelo Spring Boot durante a inicialização.
- **Limitações do Hibernate `ddl-auto: update`**:
  O Hibernate em modo `update` apenas cria tabelas e colunas básicas faltantes. Ele **NÃO** cria:
  - Constraints de validação `CHECK` complexas (como `chk_lat BETWEEN -90 AND 90`, `chk_lng BETWEEN -180 AND 180`, `chk_score BETWEEN 0 AND 100`).
  - Índices secundários especializados para heatmap (como `idx_geo_h3_res8` e `idx_geo_h3_res6`).
  - Extensões do Postgres como `uuid-ossp`.
- **Divergência Silenciosa de Schemas**: O schema gerado pelo Hibernate em dev ficava totalmente diferente do schema pretendido pelo script SQL de produção.

---

## 2. Decisão Arquitetural: Flyway como Fonte Única da Verdade

```
                         ┌────────────────────────────────────┐
                         │   V1__init_unified_schema.sql      │
                         │   (DDL, Índices H3, Constraints)   │
                         └─────────────────┬──────────────────┘
                                           │
                                           ▼ (Flyway executa no startup)
                         ┌────────────────────────────────────┐
                         │      Banco PostgreSQL 16           │
                         │      (Schema 100% Determinístico)   │
                         └─────────────────▲──────────────────┘
                                           │
                                           │ (Hibernate apenas confere)
                         ┌─────────────────┴──────────────────┐
                         │     Hibernate: ddl-auto: validate  │
                         │     (Mapeamento das Entidades)     │
                         └────────────────────────────────────┘
```

### 2.1 Alterações Implementadas no `pom.xml`
Adicionamos o driver oficial do Flyway para PostgreSQL:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>10.10.0</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <version>10.10.0</version>
</dependency>
```

### 2.2 Alterações no `application.yml`
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: true
```

**Por que `ddl-auto: validate`?**
- O Flyway executa todas as migrações primeiro.
- Em seguida, o Hibernate verifica se os nomes de colunas, tipos e relacionamentos das entidades Java coincidem com as tabelas criadas.
- Se houver qualquer divergência (ex: coluna renomeada sem migração), a aplicação recusa-se a subir imediatamente, evitando corrupção de dados em runtime.

---

## 3. Otimização PostgreSQL: `JSON` ➔ `JSONB`

No script de migração original e na entidade [`ReportSymptomsEntidade`](../src/main/java/br/ifpb/project/denguemaps/pdmreportms/model/ReportSymptomsEntidade.java), a coluna de respostas estava declarada como `JSON` puro:

```sql
-- Antes:
respostas JSON,
```

### Por que alteramos para `JSONB`?

1. **Eficiência de Armazenamento e Leitura**:
   - O tipo `JSON` armazena o texto literal exato. Toda vez que a coluna é lida ou filtrada, o PostgreSQL precisa re-fazer o *parse* sintático do JSON inteiro.
   - O tipo `JSONB` (*Binary JSON*) armazena os dados decompostos em formato binário otimizado. A escrita tem custo insignificante a mais de validação inicial, mas as leituras são ordens de grandeza mais rápidas.
2. **Capacidade de Indexação GIN**:
   - Apenas colunas `JSONB` aceitam índices GIN (*Generalized Inverted Index*) no PostgreSQL, permitindo que futuras queries analíticas busquem por chaves e valores específicos dentro do questionário com alto desempenho.

---

## 4. Revisão dos Arquivos de Perfil (`application*.yml`)

O projeto possui 4 arquivos YAML:

| Arquivo | Finalidade Original | Como foi configurado |
| :--- | :--- | :--- |
| **`application.yml`** | Configuração base carregada para todos os perfis | Porta 8082, Flyway ativo, JPA `validate`, Actuator exposto e RabbitMQ |
| **`application-dev.yml`** | Perfil de desenvolvimento local | Sincronizado para `validate`, com `logging.level.org.springframework.security: DEBUG` |
| **`application-prod.yml`** | Perfil de produção | Já continha `validate`, `show-sql: false` e logs restritos a WARN/INFO |
| **`application-common.yml`** | Configurações comuns legadas | Mantido para compatibilidade com outros módulos |
| **`application-test.yml`** *(Novo)* | Perfil exclusivo para suíte de testes | H2 em memória, Flyway desligado, sem dependência de broker externo |
