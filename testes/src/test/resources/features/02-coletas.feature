# language: pt
@coletas
Funcionalidade: Gestão de coletas de resíduos perigosos
  Como operador de um EcoPonto
  Quero registrar, consultar, corrigir, confirmar e remover coletas de resíduos
  Para que cada pilha, isqueiro e litro de óleo tenha destino certo e rastreável (o E do ESG)

  Contexto:
    Dado que a API do Impacto Verde está disponível
    E que estou autenticado como "operador"

  # ---------- POST /api/v1/coletas ----------

  @positivo @contrato @esg @ambiental
  Cenário: Registrar uma coleta válida nasce pendente e com pontos calculados
    Dado que preparo uma coleta válida de "PILHAS" com 12 unidades e peso de "0.4" kg
    Quando eu envio "POST" para "/api/v1/coletas" com o corpo preparado
    Então o status da resposta deve ser 201
    E o cabeçalho "Location" deve conter "/api/v1/coletas/{coletaId}"
    E a resposta deve respeitar o schema "coleta.schema.json"
    E o campo "familia" deve ser "{familia}"
    E o campo "tipoResiduo" deve ser "PILHAS"
    E o campo "quantidadeUnidades" deve ser "12"
    E o campo "status" deve ser "PENDENTE"
    E o campo "pontosGerados" deve ser "40"
    E o campo "confirmadaEm" deve ser "null"

  @negativo @contrato
  Esquema do Cenário: Dados inválidos na criação são rejeitados com 400 e indicam o campo culpado
    Dado que preparo uma coleta válida de "PILHAS" com 12 unidades e peso de "0.4" kg
    E que altero o campo "<campo>" para "<valor>"
    Quando eu envio "POST" para "/api/v1/coletas" com o corpo preparado
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o schema "erro.schema.json"
    E os detalhes do erro devem citar o campo "<campo>"
    E o detalhe do campo "<campo>" deve ser "<mensagem>"

    Exemplos: Campos obrigatórios ausentes e valores fora do limite
      | campo              | valor      | mensagem                                   |
      | familia            | (ausente)  | A família é obrigatória                    |
      | tipoResiduo        | (ausente)  | O tipo de resíduo é obrigatório            |
      | quantidadeUnidades | (ausente)  | A quantidade de unidades é obrigatória     |
      | quantidadeUnidades | 0          | A quantidade deve ser no mínimo 1          |
      | quantidadeUnidades | 1001       | A quantidade deve ser no máximo 1000       |
      | pesoKg             | (ausente)  | O peso em kg é obrigatório                 |
      | pesoKg             | 0          | O peso deve ser maior que zero             |
      | pesoKg             | -1.5       | O peso deve ser maior que zero             |
      | pesoKg             | 501        | O peso deve ser no máximo 500 kg           |
      | pontoColeta        | (ausente)  | O ponto de coleta é obrigatório            |

  @negativo @contrato
  Cenário: Tipo de resíduo desconhecido (lixo comum não é perigoso) é rejeitado com 400
    Dado que preparo uma coleta válida de "LIXO_COMUM" com 1 unidades e peso de "1.0" kg
    Quando eu envio "POST" para "/api/v1/coletas" com o corpo preparado
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o schema "erro.schema.json"
    E o corpo não deve expor detalhes internos

  @negativo @contrato
  Cenário: JSON malformado é rejeitado com 400 sem vazar detalhes internos
    Quando eu envio "POST" para "/api/v1/coletas" com o corpo bruto:
      """
      { "familia": "isto não é um JSON completo",
      """
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o schema "erro.schema.json"
    E o corpo não deve expor detalhes internos

  # ---------- GET /api/v1/coletas ----------

  @positivo @contrato
  Cenário: Listar coletas filtrando pela família devolve apenas as dela
    Dado que existe uma coleta pendente de "PILHAS" com 8 unidades e peso de "0.27" kg
    Quando eu envio "GET" para "/api/v1/coletas?familia={familia}"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "lista-coletas.schema.json"
    E a lista deve ter exatamente 1 elemento
    E todos os itens da lista devem ter "familia" igual a "{familia}"
    E a lista "id" deve conter o valor "{coletaId}"

  @positivo @contrato
  Esquema do Cenário: Filtrar a listagem por status separa o pendente do confirmado
    Dado que existe uma coleta pendente de "ISQUEIROS" com 3 unidades e peso de "0.15" kg
    E que a coleta está confirmada
    Quando eu envio "GET" para "/api/v1/coletas?familia={familia}&status=<status>"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "lista-coletas.schema.json"
    E a lista deve ter exatamente <quantidade> elementos

    Exemplos:
      | status    | quantidade |
      | CONFIRMADA | 1          |
      | PENDENTE  | 0          |

  @positivo @contrato
  Cenário: Listar coletas de uma família sem registros devolve lista vazia
    Quando eu envio "GET" para "/api/v1/coletas?familia={familia}"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "lista-coletas.schema.json"
    E a lista deve ter exatamente 0 elementos

  @negativo @contrato
  Cenário: Filtro de status inválido é rejeitado com 400
    Quando eu envio "GET" para "/api/v1/coletas?status=INVENTADO"
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o schema "erro.schema.json"

  # ---------- GET /api/v1/coletas/{id} ----------

  @positivo @contrato
  Cenário: Consultar uma coleta existente pelo identificador
    Dado que existe uma coleta pendente de "ELETRONICOS_PEQUENOS" com 2 unidades e peso de "1.8" kg
    Quando eu envio "GET" para "/api/v1/coletas/{coletaId}"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "coleta.schema.json"
    E o campo "id" deve ser "{coletaId}"
    E o campo "tipoResiduo" deve ser "ELETRONICOS_PEQUENOS"
    E o campo "pontosGerados" deve ser "90"

  @negativo @contrato
  Cenário: Consultar coleta inexistente devolve 404
    Quando eu envio "GET" para "/api/v1/coletas/00000000-0000-0000-0000-00000000dead"
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o schema "erro.schema.json"
    E a mensagem de erro deve ser "Coleta não encontrada"

  @negativo @contrato
  Cenário: Identificador fora do formato UUID devolve 400
    Quando eu envio "GET" para "/api/v1/coletas/isto-nao-e-um-uuid"
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o schema "erro.schema.json"

  # ---------- PUT /api/v1/coletas/{id} ----------

  @positivo @contrato
  Cenário: Corrigir uma coleta pendente recalcula os pontos
    Dado que existe uma coleta pendente de "PILHAS" com 12 unidades e peso de "0.4" kg
    E que preparo uma atualização para 20 unidades e peso de "0.5" kg no ponto "EcoPonto Moema"
    Quando eu envio "PUT" para "/api/v1/coletas/{coletaId}" com o corpo preparado
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "coleta.schema.json"
    E o campo "quantidadeUnidades" deve ser "20"
    E o campo "pontoColeta" deve ser "EcoPonto Moema"
    E o campo "pontosGerados" deve ser "50"
    E o campo "status" deve ser "PENDENTE"

  @negativo @contrato
  Cenário: Atualização com peso inválido é rejeitada com 400
    Dado que existe uma coleta pendente de "PILHAS" com 12 unidades e peso de "0.4" kg
    E que preparo uma atualização para 20 unidades e peso de "0.5" kg no ponto "EcoPonto Moema"
    E que altero o campo "pesoKg" para "0"
    Quando eu envio "PUT" para "/api/v1/coletas/{coletaId}" com o corpo preparado
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o schema "erro.schema.json"
    E os detalhes do erro devem citar o campo "pesoKg"

  @negativo @contrato
  Cenário: Atualizar coleta inexistente devolve 404
    Dado que preparo uma atualização para 20 unidades e peso de "0.5" kg no ponto "EcoPonto Moema"
    Quando eu envio "PUT" para "/api/v1/coletas/00000000-0000-0000-0000-00000000dead" com o corpo preparado
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o schema "erro.schema.json"

  # ---------- PATCH /api/v1/coletas/{id}/confirmacao ----------

  @positivo @contrato @esg @rastreabilidade
  Cenário: Confirmar uma coleta registra o momento da confirmação e gera créditos
    Dado que existe uma coleta pendente de "PILHAS" com 12 unidades e peso de "0.4" kg
    Quando eu envio "PATCH" para "/api/v1/coletas/{coletaId}/confirmacao"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "coleta.schema.json"
    E o campo "status" deve ser "CONFIRMADA"
    E o campo "confirmadaEm" não deve ser nulo
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/saldo"
    Então o status da resposta deve ser 200
    E o campo "saldoPontos" deve ser "40"

  @negativo @contrato @esg @rastreabilidade
  Cenário: Coleta já confirmada não pode ser confirmada duas vezes (nada de crédito em dobro)
    Dado que existe uma coleta pendente de "PILHAS" com 12 unidades e peso de "0.4" kg
    E que a coleta está confirmada
    Quando eu envio "PATCH" para "/api/v1/coletas/{coletaId}/confirmacao"
    Então o status da resposta deve ser 409
    E a resposta deve respeitar o schema "erro.schema.json"
    E a mensagem de erro deve ser "Coleta já confirmada"
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/saldo"
    Então o campo "saldoPontos" deve ser "40"

  @negativo @contrato @esg @rastreabilidade
  Cenário: Coleta confirmada é imutável: tentar alterá-la devolve 409
    Dado que existe uma coleta pendente de "PILHAS" com 12 unidades e peso de "0.4" kg
    E que a coleta está confirmada
    E que preparo uma atualização para 99 unidades e peso de "9.9" kg no ponto "EcoPonto Fantasma"
    Quando eu envio "PUT" para "/api/v1/coletas/{coletaId}" com o corpo preparado
    Então o status da resposta deve ser 409
    E a resposta deve respeitar o schema "erro.schema.json"
    E a mensagem de erro deve ser "Coleta já confirmada não pode ser alterada (rastreabilidade)"

  @negativo @contrato
  Cenário: Confirmar coleta inexistente devolve 404
    Quando eu envio "PATCH" para "/api/v1/coletas/00000000-0000-0000-0000-00000000dead/confirmacao"
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o schema "erro.schema.json"

  # ---------- DELETE /api/v1/coletas/{id} ----------

  @positivo
  Cenário: Administrador remove uma coleta e ela deixa de existir (204 e depois 404)
    Dado que estou autenticado como "administrador"
    E que existe uma coleta pendente de "ISQUEIROS" com 3 unidades e peso de "0.15" kg
    Quando eu envio "DELETE" para "/api/v1/coletas/{coletaId}"
    Então o status da resposta deve ser 204
    E o corpo da resposta deve estar vazio
    Quando eu envio "GET" para "/api/v1/coletas/{coletaId}"
    Então o status da resposta deve ser 404

  @negativo @contrato
  Cenário: Remover coleta inexistente devolve 404
    Dado que estou autenticado como "administrador"
    Quando eu envio "DELETE" para "/api/v1/coletas/00000000-0000-0000-0000-00000000dead"
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o schema "erro.schema.json"

  @negativo @contrato
  Cenário: Operador não consegue remover uma coleta e ela continua lá (403)
    Dado que existe uma coleta pendente de "ISQUEIROS" com 3 unidades e peso de "0.15" kg
    Quando eu envio "DELETE" para "/api/v1/coletas/{coletaId}"
    Então o status da resposta deve ser 403
    E a resposta deve respeitar o schema "erro.schema.json"
    Quando eu envio "GET" para "/api/v1/coletas/{coletaId}"
    Então o status da resposta deve ser 200
