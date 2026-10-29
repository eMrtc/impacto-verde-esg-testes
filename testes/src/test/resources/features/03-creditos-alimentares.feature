# language: pt
@creditos
Funcionalidade: Créditos alimentares das famílias
  Como família que descarta resíduos corretamente
  Quero acompanhar meu saldo e trocar meus pontos por alimentos
  Para que o descarte correto vire comida na mesa (o S do ESG: impacto social de verdade)

  Contexto:
    Dado que a API do Impacto Verde está disponível
    E que estou autenticado como "operador"

  # ---------- GET /api/v1/creditos/{familia}/saldo ----------

  @positivo @contrato @esg @social
  Cenário: Saldo reflete os pontos das coletas confirmadas
    Dado que a família possui 100 pontos de crédito
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/saldo"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "saldo.schema.json"
    E o campo "familia" deve ser "{familia}"
    E o campo "saldoPontos" deve ser "100"

  @negativo @contrato
  Cenário: Saldo de família sem lançamentos devolve 404
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/saldo"
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o schema "erro.schema.json"
    E a mensagem de erro deve ser "Família sem lançamentos"

  # ---------- GET /api/v1/creditos/{familia}/extrato ----------

  @positivo @contrato @esg @rastreabilidade
  Cenário: Extrato lista o crédito gerado e aponta a coleta de origem
    Dado que a família possui 100 pontos de crédito
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/extrato"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "extrato.schema.json"
    E a lista deve ter exatamente 1 elemento
    E todos os itens da lista devem ter "tipo" igual a "GERADO"
    E a lista "origem" deve conter o valor "COLETA:{coletaId}"

  @positivo @contrato
  Cenário: Extrato de família sem lançamentos devolve lista vazia
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/extrato"
    Então o status da resposta deve ser 200
    E a resposta deve respeitar o schema "extrato.schema.json"
    E a lista deve ter exatamente 0 elementos

  # ---------- POST /api/v1/creditos/resgates ----------

  @positivo @contrato @esg @social @inclusao
  Cenário: Família troca pontos por uma cesta básica e o saldo é debitado
    Dado que a família possui 100 pontos de crédito
    E que preparo um resgate de 40 pontos no parceiro "Mercado Bom Preço" para o item "Cesta básica pequena"
    Quando eu envio "POST" para "/api/v1/creditos/resgates" com o corpo preparado
    Então o status da resposta deve ser 201
    E a resposta deve respeitar o schema "lancamento.schema.json"
    E o campo "tipo" deve ser "RESGATADO"
    E o campo "valorPontos" deve ser "-40"
    E o campo "saldoApos" deve ser "60"
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/saldo"
    Então o campo "saldoPontos" deve ser "60"

  @negativo @contrato @esg @social
  Cenário: Resgate maior que o saldo é recusado com 422 e o saldo permanece intacto
    Dado que a família possui 100 pontos de crédito
    E que preparo um resgate de 101 pontos no parceiro "Hortifruti Raiz" para o item "Kit de hortaliças"
    Quando eu envio "POST" para "/api/v1/creditos/resgates" com o corpo preparado
    Então o status da resposta deve ser 422
    E a resposta deve respeitar o schema "erro.schema.json"
    E a mensagem de erro deve ser "Saldo insuficiente para o resgate"
    Quando eu envio "GET" para "/api/v1/creditos/{familia}/saldo"
    Então o campo "saldoPontos" deve ser "100"

  @negativo @contrato
  Esquema do Cenário: Resgate com dados inválidos é rejeitado com 400
    Dado que preparo um resgate de 10 pontos no parceiro "Mercado Bom Preço" para o item "Cesta básica pequena"
    E que altero o campo "<campo>" para "<valor>"
    Quando eu envio "POST" para "/api/v1/creditos/resgates" com o corpo preparado
    Então o status da resposta deve ser 400
    E a resposta deve respeitar o schema "erro.schema.json"
    E os detalhes do erro devem citar o campo "<campo>"
    E o detalhe do campo "<campo>" deve ser "<mensagem>"

    Exemplos:
      | campo    | valor     | mensagem                        |
      | familia  | (ausente) | A família é obrigatória         |
      | parceiro | (ausente) | O parceiro é obrigatório        |
      | item     | (ausente) | O item é obrigatório            |
      | pontos   | 0         | Os pontos devem ser no mínimo 1 |
      | pontos   | (ausente) | Os pontos são obrigatórios      |

  @negativo @contrato @esg @social
  Cenário: Resgatar sem ter pontos (família sem histórico) também é recusado com 422
    Dado que preparo um resgate de 10 pontos no parceiro "Mercado Bom Preço" para o item "Cesta básica pequena"
    Quando eu envio "POST" para "/api/v1/creditos/resgates" com o corpo preparado
    Então o status da resposta deve ser 422
    E a resposta deve respeitar o schema "erro.schema.json"
