# language: pt
@seguranca
Funcionalidade: Segurança e disponibilidade da API
  Como responsável pela governança da plataforma Impacto Verde
  Quero que apenas pessoas autenticadas e autorizadas acessem os dados de resíduos e créditos
  Para garantir conformidade, rastreabilidade e proteção dos dados das famílias (o G do ESG)

  Contexto:
    Dado que a API do Impacto Verde está disponível

  @positivo @contrato @saude
  Cenário: Health check público responde sem exigir credenciais
    Dado que não envio credenciais
    Quando eu envio "GET" para "/api/v1/saude"
    Então o status da resposta deve ser 200
    E o campo "status" deve ser "UP"
    E a resposta deve respeitar o schema "saude.schema.json"

  @negativo @contrato @governanca
  Esquema do Cenário: Acesso sem credenciais é barrado com 401 em todos os endpoints protegidos
    Dado que não envio credenciais
    Quando eu envio "<metodo>" para "<caminho>"
    Então o status da resposta deve ser 401
    E o cabeçalho "WWW-Authenticate" deve conter "Bearer"
    E a resposta deve respeitar o schema "erro.schema.json"
    E o corpo não deve expor detalhes internos

    Exemplos: Cada endpoint protegido precisa de porteiro (lixo perigoso não entra sem crachá)
      | metodo | caminho                                                         |
      | GET    | /api/v1/coletas                                                 |
      | POST   | /api/v1/coletas                                                 |
      | GET    | /api/v1/coletas/00000000-0000-0000-0000-000000000001            |
      | PUT    | /api/v1/coletas/00000000-0000-0000-0000-000000000001            |
      | PATCH  | /api/v1/coletas/00000000-0000-0000-0000-000000000001/confirmacao |
      | DELETE | /api/v1/coletas/00000000-0000-0000-0000-000000000001            |
      | GET    | /api/v1/creditos/familia-qa/saldo                               |
      | GET    | /api/v1/creditos/familia-qa/extrato                             |
      | POST   | /api/v1/creditos/resgates                                       |
      | GET    | /api/v1/esg/indicadores                                         |

  @negativo @contrato @governanca
  Cenário: Token inválido também é barrado com 401
    Dado que envio um token inválido
    Quando eu envio "GET" para "/api/v1/coletas"
    Então o status da resposta deve ser 401
    E a resposta deve respeitar o schema "erro.schema.json"
    E a mensagem de erro deve ser "Credenciais ausentes ou inválidas"

  @negativo @contrato @governanca
  Cenário: Operador não pode apagar registros (separação de funções) e recebe 403
    Dado que estou autenticado como "operador"
    Quando eu envio "DELETE" para "/api/v1/coletas/00000000-0000-0000-0000-000000000001"
    Então o status da resposta deve ser 403
    E a resposta deve respeitar o schema "erro.schema.json"
    E a mensagem de erro deve ser "Perfil sem permissão para esta operação"

  @negativo @contrato
  Cenário: Método HTTP não suportado devolve 405 com erro padronizado
    Dado que estou autenticado como "operador"
    Quando eu envio "PATCH" para "/api/v1/coletas"
    Então o status da resposta deve ser 405
    E a resposta deve respeitar o schema "erro.schema.json"
    E o corpo não deve expor detalhes internos

  @negativo @contrato
  Cenário: Rota inexistente devolve 404 com erro padronizado
    Dado que estou autenticado como "operador"
    Quando eu envio "GET" para "/api/v1/rota-que-nao-existe"
    Então o status da resposta deve ser 404
    E a resposta deve respeitar o schema "erro.schema.json"
