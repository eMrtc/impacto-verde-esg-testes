# language: pt
@esg-indicadores
Funcionalidade: Indicadores ESG consolidados
  Como gestor de sustentabilidade
  Quero indicadores confiáveis de resíduos coletados, pontos distribuídos e famílias atendidas
  Para comprovar o impacto ambiental, social e de governança com números auditáveis

  Contexto:
    Dado que a API do Impacto Verde está disponível
    E que estou autenticado como "operador"

  # ---------- GET /api/v1/esg/indicadores ----------

  @positivo @contrato @esg
  Cenário: Consultar os indicadores ESG respeita o contrato
    Quando eu envio "GET" para "/api/v1/esg/indicadores"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "indicadores.schema.json"
    E o campo "totalColetas" deve ser do tipo "numero"
    E o campo "porTipoResiduo" deve ser do tipo "lista"
    E o campo "geradoEm" deve ser do tipo "texto"

  @positivo @contrato @esg @ambiental @rastreabilidade
  Cenário: Coleta confirmada entra nos indicadores ambientais e sociais
    Dado que registro a fotografia atual dos indicadores ESG
    E que existe uma coleta pendente de "PILHAS" com 12 unidades e peso de "0.4" kg
    E que a coleta está confirmada
    Quando eu envio "GET" para "/api/v1/esg/indicadores"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "indicadores.schema.json"
    E o indicador "totalColetas" deve ter aumentado em "1"
    E o indicador "coletasConfirmadas" deve ter aumentado em "1"
    E o indicador "pesoTotalKg" deve ter aumentado em "0.4"
    E o indicador "pontosDistribuidos" deve ter aumentado em "40"
    E o indicador "familiasAtendidas" deve ter aumentado em "1"

  @positivo @esg @governanca @conformidade
  Cenário: Coleta pendente não infla o impacto (só o que foi auditado conta)
    Dado que registro a fotografia atual dos indicadores ESG
    E que existe uma coleta pendente de "PILHAS" com 12 unidades e peso de "0.4" kg
    Quando eu envio "GET" para "/api/v1/esg/indicadores"
    Então o status da resposta deve ser 200
    E o indicador "totalColetas" deve ter aumentado em "1"
    E o indicador "coletasPendentes" deve ter aumentado em "1"
    E o indicador "coletasConfirmadas" não deve ter mudado
    E o indicador "pesoTotalKg" não deve ter mudado
    E o indicador "pontosDistribuidos" não deve ter mudado

  @positivo @esg @social @inclusao
  Cenário: Resgate de alimentos aparece como pontos resgatados nos indicadores
    Dado que a família possui 100 pontos de crédito
    E que registro a fotografia atual dos indicadores ESG
    E que preparo um resgate de 30 pontos no parceiro "Padaria Trigo Dourado" para o item "Vale-pão semanal"
    E que envio o resgate preparado
    Quando eu envio "GET" para "/api/v1/esg/indicadores"
    Então o status da resposta deve ser 200
    E o indicador "pontosResgatados" deve ter aumentado em "30"
    E o indicador "pontosDistribuidos" não deve ter mudado

  @positivo @esg @ambiental @eficiencia
  Esquema do Cenário: Cada tipo de resíduo rende pontos proporcionais ao peso (eficiência de recursos)
    Dado que preparo uma coleta válida de "<tipo>" com 1 unidades e peso de "<peso>" kg
    Quando eu envio "POST" para "/api/v1/coletas" com o corpo preparado
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o schema "coleta.schema.json"
    E o campo "pontosGerados" deve ser "<pontos>"

    Exemplos: Tabela de pontos por quilo (a mesma usada no projeto do MongoDB)
      | tipo                   | peso | pontos |
      | PILHAS                 | 0.4  | 40     |
      | ISQUEIROS              | 0.15 | 15     |
      | ELETRONICOS_PEQUENOS   | 1.8  | 90     |
      | OLEO_DE_COZINHA        | 2.0  | 60     |
      | LAMPADAS_FLUORESCENTES | 0.6  | 48     |
      | BATERIAS_DE_CELULAR    | 0.25 | 35     |
