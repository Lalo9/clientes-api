# Clientes API

API REST e tela web para cadastro de clientes (empresas). Basta informar **nome**, **CNPJ** e **CEP**: o sistema consulta serviços externos e preenche sozinho a **razão social** e o **endereço**, valida os dados e salva no MySQL.

## Tecnologias

- Java 25, Spring Boot 4 (Web, Data JPA, Validation, Cache)
- MySQL 8.4 (via Docker Compose)
- ViaCEP (endereço) e BrasilAPI (dados do CNPJ), consumidas com `RestClient`
- Tela web em HTML e JavaScript puro, servida pela própria aplicação
- JUnit 5 e Mockito

## O que o projeto mostra

- Consumo de APIs externas com timeout de conexão e de leitura
- Tratamento de falhas: CEP/CNPJ inexistente devolve 422, serviço externo fora do ar devolve 502
- Validação do CNPJ pelos dígitos verificadores, antes de qualquer chamada externa
- Cache em memória dos CEPs já consultados
- Respostas de erro padronizadas em JSON
- Testes automatizados com as APIs externas simuladas (mocks)

## Como rodar

Pré-requisitos: JDK 25 e Docker.

```bash
git clone https://github.com/Lalo9/clientes-api.git
cd clientes-api
docker compose up -d
./mvnw spring-boot:run
```

Abra `http://localhost:8080` para usar a tela. A API fica em `http://localhost:8080/clientes`. O MySQL usa a porta 3307 do seu computador.

Para encerrar: `Ctrl+C` na aplicação e `docker compose down` para desligar o banco (os dados continuam guardados no volume do Docker).

## Endpoints

| Método | Rota | Ação |
|---|---|---|
| POST | `/clientes` | Cadastra (consulta CNPJ e CEP) |
| GET | `/clientes` | Lista todos |
| GET | `/clientes/{id}` | Busca um |
| PUT | `/clientes/{id}` | Edita (refaz as consultas) |
| DELETE | `/clientes/{id}` | Exclui |

CNPJ e CEP são enviados só com números, sem pontos nem traços.

### Exemplo

```bash
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Banco do Brasil","cnpj":"00000000000191","cep":"01001000"}'
```

Resposta:

```json
{
  "id": 1,
  "nome": "Banco do Brasil",
  "cnpj": "00000000000191",
  "razaoSocial": "BANCO DO BRASIL SA",
  "cep": "01001000",
  "logradouro": "Praça da Sé",
  "bairro": "Sé",
  "cidade": "São Paulo",
  "uf": "SP"
}
```

### Erros

Os erros voltam neste formato:

```json
{ "status": 422, "mensagem": "CEP não encontrado" }
```

| Código | Quando |
|---|---|
| 400 | Campos ausentes ou com formato errado (a resposta lista os campos) |
| 404 | Cliente não encontrado |
| 409 | CNPJ já cadastrado |
| 422 | CNPJ inválido, CNPJ ou CEP não encontrado |
| 502 | ViaCEP ou BrasilAPI indisponível |

## Decisões de projeto

- **Nada é salvo com dados incompletos.** Se o CEP ou o CNPJ não existirem, ou se uma API externa estiver fora do ar, o cadastro falha antes de gravar.
- **Validação local primeiro.** Um CNPJ com dígitos verificadores errados é recusado sem gastar uma chamada externa.
- **Timeouts.** Conexão de 3 segundos e leitura de 5 segundos, para a API não travar por causa de um serviço externo lento.
- **Cache.** O resultado do ViaCEP fica em memória. Falhas não entram no cache, então uma nova tentativa consulta de novo.
- **DTO de entrada.** Quem chama a API só envia nome, CNPJ e CEP; `id`, razão social e endereço não podem ser definidos de fora.

## Testes

```bash
./mvnw test
```

São 13 testes. Os do controller usam versões simuladas do ViaCEP, da BrasilAPI e do repositório, então rodam sem internet e cobrem os casos de erro (CEP inexistente, API fora do ar, CNPJ duplicado). O teste de contexto da aplicação precisa do MySQL ligado.

## Limitações conhecidas

- A senha do banco (`root123`) está no `docker-compose.yml` e no `application.properties` porque o projeto é para uso local. Numa versão de produção ela viria de variável de ambiente.
- O cache é em memória e zera ao reiniciar a aplicação.
- Não há autenticação nem paginação na listagem.
- Não há testes de integração com o banco e o servidor HTTP reais.