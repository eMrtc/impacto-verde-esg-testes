# Impacto Verde ESG: Testes Automatizados de API (BDD)

> *"O teste que separa o lixo do código."*
> Atividade avaliativa da fase **Testes Automatizados**: Cucumber + JUnit + REST Assured + JSON Schema.

---

## 1. O que é este projeto

O **Impacto Verde** é a plataforma ESG das atividades anteriores (Startup One, Cidades Inteligentes e ESG):

- **Ambiental (E):** famílias descartam corretamente resíduos domésticos perigosos (pilhas, isqueiros, eletrônicos, óleo de cozinha...).
- **Social (S):** cada descarte confirmado vira **créditos alimentares**, trocados por alimentos em parceiros.
- **Governança (G):** rastreabilidade, autenticação, separação de funções e indicadores auditáveis para PMEs.

Este repositório contém a **suíte de testes automatizados** que valida as funcionalidades críticas dessa API.

### ⚠️ Aviso importante: a API aqui é uma API de EXEMPLO

Na análise do workspace **não havia código-fonte do projeto** (nenhum `pom.xml`, `build.gradle`, `.csproj` ou controller). Só existia o documento *"Atividade MongoDB - Impacto Verde ESG"*, que descreve as collections `empresas`, `diagnosticos_esg`, `coletas_residuos`, `creditos_alimentares` e `parceiros`.

Em vez de inventar em silêncio, foi criada a pasta [`api-exemplo/`](api-exemplo): uma API **Java Spring Boot** coerente com esse documento (coletas de resíduos, créditos alimentares e indicadores ESG), usada como alvo dos testes. Os dados ficam **em memória** (sem MongoDB) para a suíte rodar com poucos comandos.

**Para usar a sua API real:** aponte `API_BASE_URL` para ela e adapte rotas, payloads e schemas (veja a seção 10). A suíte consome a API por HTTP, então não depende da implementação.

---

## 2. Stack e justificativa

| Item | Escolha | Por quê |
|---|---|---|
| Linguagem da API | Java 21 + Spring Boot 4.1.2 (Maven) | Mesma linguagem do projeto (Java Spring) |
| BDD | **Cucumber 8.0.4** (Gherkin em português) | Cenários legíveis por quem não programa; ótimo para critérios de aceite |
| Executor | **JUnit Platform 6.1.3** (`junit-platform-suite`) | Integra com Maven, IDEs e CI sem plugin extra |
| Cliente HTTP | **REST Assured 6.3.0** | DSL madura para APIs REST, timeouts e asserções |
| Contrato | **json-schema-validator 6.3.0** (do REST Assured) | Valida respostas contra JSON Schema (draft-07) |
| Asserções | AssertJ 3.27.7 | Mensagens de falha claras |
| Injeção de dependência | PicoContainer (Cucumber) | Uma instância de contexto **por cenário**, sem estado compartilhado |
| Relatórios | Cucumber HTML + JSON + XML (JUnit) | Evidências de execução |

Os testes ficam em **projeto separado** (`testes/`) e consomem a API **em execução**: é black-box de verdade, como um cliente real faria.

---

## 3. Estrutura de pastas

```
impacto-verde-esg-testes/
├── README.md                          ← você está aqui
├── docker-compose.yml                 ← sobe API + testes de forma reproduzível
├── .env.example                       ← modelo de variáveis (sem valores reais)
├── .gitignore / .dockerignore
├── .github/workflows/testes-api.yml   ← pipeline do GitHub Actions
├── scripts/
│   ├── configurar-ambiente.sh         ← gera .env com tokens ALEATÓRIOS
│   ├── executar-local.sh              ← sobe API, roda testes, desliga API
│   └── executar-docker.sh             ← roda tudo em contêineres e copia relatórios
├── docs/
│   └── DOCUMENTACAO_ENTREGA.md        ← conteúdo para o PDF/PPT da entrega
├── evidencias/                        ← relatórios reais (HTML/JSON/XML) da execução com 68 testes verdes
├── api-exemplo/                       ← API Spring Boot de exemplo (alvo dos testes)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/br/com/impactoverde/api/
│       ├── config/                    ← autenticação (Bearer) e perfis
│       ├── coleta/                    ← pilar Ambiental
│       ├── credito/                   ← pilar Social
│       ├── esg/                       ← indicadores consolidados e health check
│       └── erro/                      ← erro padronizado (sem stack trace)
└── testes/                            ← projeto de testes (Maven)
    ├── pom.xml
    ├── Dockerfile
    └── src/test/
        ├── java/br/com/impactoverde/testes/
        │   ├── executor/ExecutorCucumberTest.java   ← runner JUnit Platform
        │   ├── config/ConfiguracaoTeste.java        ← URL base/tokens (env, -D, properties)
        │   ├── http/ClienteApi.java                 ← REST Assured + timeouts
        │   ├── contexto/ContextoCenario.java        ← estado do cenário (1 por cenário)
        │   ├── dados/FabricaDados.java              ← massa de dados única por cenário
        │   ├── hooks/Hooks.java                     ← espera a API + limpeza pós-cenário
        │   └── steps/                               ← step definitions (Comuns, Coleta, Credito, Indicadores)
        └── resources/
            ├── features/              ← 4 arquivos .feature (pt)
            ├── schemas/               ← 8 JSON Schemas (contratos)
            ├── config/teste.properties
            └── junit-platform.properties            ← glue, plugins e relatórios
```

---

## 4. Pré-requisitos

| Para... | Você precisa de |
|---|---|
| Execução local | **JDK 21** e **Maven 3.9+** (`java -version`, `mvn -v`), `openssl` e `curl` |
| Execução via Docker | Docker + Docker Compose v2 e `docker login registry.redhat.io` (conta Red Hat gratuita) |
| Pipeline | Um repositório no GitHub (o workflow já está pronto) |

> Dica macOS: `brew install openjdk@21 maven`.

---

## 5. Configuração do ambiente

Nenhum segredo fica no código. As configurações vêm, em ordem de precedência, de **`-D` → variável de ambiente → `teste.properties`**.

| Variável | Para quê | Padrão |
|---|---|---|
| `API_BASE_URL` | URL base da API | `http://127.0.0.1:8080` |
| `API_TOKEN_OPERADOR` | Token do perfil operador (**obrigatório**) | n/a |
| `API_TOKEN_ADMIN` | Token do perfil administrador (**obrigatório**) | n/a |
| `API_ESPERA_MAXIMA_SEGUNDOS` | Quanto esperar a API acordar | `60` |

Gere tokens aleatórios e o arquivo `.env` (ignorado pelo Git):

```bash
./scripts/configurar-ambiente.sh
```

---

## 6. Como executar localmente

### Opção A: um comando só (sobe a API, testa e desliga)

```bash
./scripts/executar-local.sh                    # suíte completa
./scripts/executar-local.sh "@esg and @positivo"   # só uma fatia, por tag
```

### Opção B: passo a passo

```bash
# 1) tokens
./scripts/configurar-ambiente.sh && set -a && source .env && set +a

# 2) compilar e subir a API (porta 8080, somente loopback)
mvn -f api-exemplo/pom.xml -DskipTests clean package
IMPACTO_VERDE_TOKEN_OPERADOR=$API_TOKEN_OPERADOR \
IMPACTO_VERDE_TOKEN_ADMIN=$API_TOKEN_ADMIN \
  java -jar api-exemplo/target/impacto-verde-api.jar &

# 3) em outro terminal (com o .env carregado), rodar os testes
mvn -f testes/pom.xml clean test
```

Outras formas úteis:

```bash
mvn -f testes/pom.xml test -Dcucumber.filter.tags="@negativo"            # só cenários negativos
mvn -f testes/pom.xml test -Dcucumber.filter.tags="@contrato"            # só testes de contrato
mvn -f testes/pom.xml test -Dapi.base-url=http://127.0.0.1:9090          # outra URL
```

---

## 7. Como executar via Docker

```bash
docker login registry.redhat.io        # uma vez
./scripts/executar-docker.sh
```

O script gera o `.env`, constrói as imagens, sobe a API, roda os testes, copia os relatórios para `./relatorios/` e derruba tudo. Pontos de segurança aplicados: imagens da **Red Hat** (`registry.redhat.io`), **usuário não-root (185)**, `cap_drop: ALL`, `no-new-privileges`, sistema de arquivos da API **somente leitura** e **nenhuma porta publicada** (a API escuta apenas em loopback; os testes compartilham a rede da API).

> **Transparência:** o Docker não estava instalado na máquina onde esta suíte foi desenvolvida, então o `Dockerfile`/`docker-compose.yml` foram revisados (sintaxe YAML validada), mas **não executados**. A execução local e a lógica da suíte foram testadas de verdade (seção 11). Se algo precisar de ajuste no seu Docker, o ponto mais provável é o login no registry da Red Hat.

---

## 8. Como executar no pipeline (GitHub Actions)

O workflow [`.github/workflows/testes-api.yml`](.github/workflows/testes-api.yml) roda a cada `push` na `main`, em `pull_request` e manualmente (*Run workflow*):

1. Instala JDK 21 (Temurin) com cache do Maven.
2. Gera **tokens aleatórios** na hora e os mascara nos logs (`::add-mask::`).
3. Compila e **sobe a API** em loopback, aguardando o `/api/v1/saude`.
4. Executa a suíte Cucumber.
5. Publica o resumo na aba do job e o **artefato `relatorios-cucumber`** (HTML, JSON, XML).

> Para o pipeline funcionar, o conteúdo desta pasta (`impacto-verde-esg-testes/`) deve estar na **raiz** do repositório GitHub.

---

## 9. Onde ficam os relatórios

| Onde | Caminho |
|---|---|
| Local | `testes/target/cucumber-reports/cucumber.html` (também `.json` e `.xml`) |
| Evidência pronta | `evidencias/cucumber.html` (cópia da execução validada na criação do projeto) |
| Docker | `relatorios/cucumber.html` |
| GitHub Actions | aba **Actions → execução → Artifacts → `relatorios-cucumber`** |

Cada cenário registra no relatório linhas de evidência como `GET /api/v1/coletas -> HTTP 200` e `Contrato validado contra schemas/coleta.schema.json`.

---

## 10. API testada e tabela de cobertura

Base: `/api/v1`. Autenticação: `Authorization: Bearer <token>` (exceto `/saude`). Perfil **OPERADOR** faz tudo, exceto `DELETE` (exige **ADMIN** → 403 para operador).

| # | Endpoint | Sucesso | Erros testados | Schema | Cenários (arquivo → cenário) |
|---|---|---|---|---|---|
| 1 | `GET /saude` | 200 | n/a (público) | `saude` | 01 → *Health check público* |
| 2 | `POST /coletas` | 201 | 400 (10 variações de campo + enum inválido + JSON malformado), 401, 405 | `coleta`, `erro` | 02 → *Registrar coleta válida*, *Dados inválidos* (Outline ×10), *Tipo desconhecido*, *JSON malformado*; 01 → *401 sem credenciais*; 04 → *Cada tipo de resíduo* (Outline ×6) |
| 3 | `GET /coletas` | 200 (+ filtros `familia`, `status`) | 400 (status inválido), 401 | `lista-coletas`, `erro` | 02 → *Listar por família*, *Filtrar por status* (Outline ×2), *Lista vazia*, *Filtro inválido* |
| 4 | `GET /coletas/{id}` | 200 | 404, 400 (UUID inválido), 401 | `coleta`, `erro` | 02 → *Consultar existente*, *Inexistente*, *Fora do formato UUID* |
| 5 | `PUT /coletas/{id}` | 200 | 400, 404, 409 (confirmada é imutável), 401 | `coleta`, `erro` | 02 → *Corrigir pendente*, *Peso inválido*, *Atualizar inexistente*, *Confirmada é imutável* |
| 6 | `PATCH /coletas/{id}/confirmacao` | 200 | 404, 409 (dupla confirmação), 401 | `coleta`, `erro` | 02 → *Confirmar registra momento e gera créditos*, *Confirmar duas vezes*, *Confirmar inexistente* |
| 7 | `DELETE /coletas/{id}` | 204 | 404, 403 (operador), 401 | `erro` | 02 → *Admin remove (204→404)*, *Remover inexistente*, *Operador recebe 403*; 01 → *403 separação de funções* |
| 8 | `GET /creditos/{familia}/saldo` | 200 | 404, 401 | `saldo`, `erro` | 03 → *Saldo reflete coletas*, *Família sem lançamentos* |
| 9 | `GET /creditos/{familia}/extrato` | 200 (inclui lista vazia) | 401 | `extrato`, `lancamento` | 03 → *Extrato aponta a coleta de origem*, *Extrato vazio* |
| 10 | `POST /creditos/resgates` | 201 | 400 (5 variações), 422 (saldo insuficiente / sem histórico), 401 | `lancamento`, `erro` | 03 → *Troca por cesta básica*, *Resgate maior que saldo*, *Dados inválidos* (Outline ×5), *Sem pontos* |
| 11 | `GET /esg/indicadores` | 200 | 401 | `indicadores` | 04 → *Contrato*, *Confirmada entra*, *Pendente não infla*, *Resgate aparece* |

> **Todos os 11 endpoints** têm validação de status code, de corpo JSON e de contrato (JSON Schema). O 401 para cada endpoint protegido é exercitado pelo Scenario Outline *"Acesso sem credenciais é barrado com 401"* (10 exemplos, um por rota protegida).

### Schemas (`testes/src/test/resources/schemas/`)

`coleta`, `lista-coletas`, `saldo`, `lancamento`, `extrato`, `indicadores`, `saude` e `erro`. Todos definem `required`, `type`, `enum`/limites e `additionalProperties: false` (se alguém adicionar um campo sem avisar, o teste de contrato acusa).

### Adaptando à sua API real

1. Ajuste `API_BASE_URL` e os tokens.
2. Edite os caminhos nas `.feature` e os payloads em [`FabricaDados`](testes/src/test/java/br/com/impactoverde/testes/dados/FabricaDados.java).
3. Atualize os schemas em `resources/schemas/` conforme o seu JSON de resposta.
4. Os passos genéricos (`PassosComuns`) servem para qualquer API REST.

---

## 11. Resultado da execução (validado de verdade)

Execução feita com `./scripts/executar-local.sh` (JDK 21, Maven 3.10):

```
Tests run: 68, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Feature | Cenários executados |
|---|---|
| 01 Segurança e disponibilidade | 15 |
| 02 Gestão de coletas | 31 |
| 03 Créditos alimentares | 12 |
| 04 Indicadores ESG | 10 |
| **Total** | **68 cenários / 541 passos, todos passando** |

Verificações extras de qualidade:

- **Repetibilidade:** a suíte foi executada várias vezes seguidas contra a mesma API; após cada rodada a API ficou com **0 coletas residuais** (o `@After` recolhe o lixo de teste).
- **Contrato de verdade:** alterar propositalmente um schema (`CONFIRMADA` removido do `enum`) **derrubou** 2 testes, provando que a validação não é decorativa. O schema foi restaurado.
- **Filtro por tags:** `@esg and @positivo` executa apenas a fatia correspondente.

---

## 12. Cenários BDD e tags

Gherkin em português (`# language: pt`) com `Contexto` (Background), `Esquema do Cenário` + `Exemplos` e tags:

| Tag | Significado |
|---|---|
| `@positivo` / `@negativo` | Caminho feliz / cenário de erro |
| `@contrato` | Valida JSON Schema |
| `@esg` | Evidencia o vínculo com ESG |
| `@ambiental` `@social` `@governanca` | Pilar ESG |
| `@rastreabilidade` `@conformidade` `@inclusao` `@eficiencia` | Valor ESG específico |

Exemplo de vínculo ESG (rastreabilidade): *"Coleta confirmada é imutável"* e *"Coleta pendente não infla o impacto (só o que foi auditado conta)"*.

---

## 13. Segurança aplicada

- Tokens **somente por variável de ambiente**; `.env` no `.gitignore`; nenhum segredo no repositório.
- API escuta em **127.0.0.1** (nunca `0.0.0.0`); comparação de token em tempo constante.
- Erros **sem stack trace** (há um passo que verifica que o corpo não vaza `Exception`, `springframework` etc.).
- Entradas validadas no servidor (Bean Validation) e `401` aplicado **antes** do roteamento (um endpoint inexistente sem credencial também não entrega informação).
- Timeouts de conexão e resposta configurados no cliente de teste.
- Dependências em versões estáveis atuais (verificadas no Maven Central na criação).

---

## 14. Lembretes acadêmicos (leia com carinho, antes de dar *Enviar*)

- **Confira o arquivo certo antes do upload.** Depois que a atividade fechar ou for corrigida, **não dá para reenviar**. Um `.zip` errado é lixo que nem a reciclagem salva.
- **Não deixe para os últimos minutos.** Quem entrega às 23h59 compete com o Wi-Fi da faculdade e a lei de Murphy.
- **Entregue apenas pela plataforma.** E-mail, print no chat e "mandei por fora" não valem.
- **Não compartilhe a resolução** em grupos de WhatsApp, Discord ou Microsoft Teams: isso configura plágio e pode **anular a atividade**. Compartilhe conhecimento, não o gabarito.
- Você tem **até 15 dias após a publicação da nota** para solicitar **revisão**.

---

*Feito com café, JSON Schema e a certeza de que bug também precisa de coleta seletiva.*
