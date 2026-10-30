# 🏢 Clientes API: Cadastro com Preenchimento Automático

> Desenvolvido como projeto de portfólio para estágio em desenvolvimento.

![Java](https://img.shields.io/badge/Java_25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

Sistema de cadastro de clientes (empresas) com API REST e tela web. Você informa apenas **nome**, **CNPJ** e **CEP**, e o sistema consulta serviços externos, preenche sozinho a **razão social** e o **endereço**, valida tudo e salva no MySQL. O banco roda em um container com **Docker Compose**.

---

## ✨ Funcionalidades

* CRUD completo de clientes (cadastrar, listar, buscar, editar e excluir)
* Razão social obtida pela [BrasilAPI](https://brasilapi.com.br) a partir do CNPJ
* Endereço obtido pelo [ViaCEP](https://viacep.com.br) a partir do CEP
* Validação do CNPJ pelos dígitos verificadores, antes de qualquer chamada externa
* Cache em memória dos CEPs já consultados
* Tratamento de falhas das APIs externas, com timeouts e mensagens claras
* Respostas de erro padronizadas em JSON
* Tela web em HTML e JavaScript, servida pela própria aplicação

---

## 📦 Estrutura do Projeto

```text
clientes-api/
├── docker-compose.yml          ← Sobe o MySQL
├── pom.xml                     ← Dependências (Maven)
└── src/
    ├── main/
    │   ├── java/com/lalo9/clientes/
    │   │   ├── ClientesApiApplication.java   ← Entry point Spring Boot
    │   │   ├── StatusController.java         ← Rota de saúde (/status)
    │   │   ├── cliente/
    │   │   │   ├── Cliente.java              ← Entidade (tabela clientes)
    │   │   │   ├── ClienteRequest.java       ← DTO de entrada + validações
    │   │   │   ├── ClienteRepository.java    ← Acesso ao banco (JPA)
    │   │   │   ├── ClienteController.java    ← Rotas REST do CRUD
    │   │   │   └── CnpjValidador.java        ← Dígitos verificadores
    │   │   ├── externo/
    │   │   │   ├── ViaCepClient.java         ← Consome o ViaCEP (com cache)
    │   │   │   └── BrasilApiClient.java      ← Consome a BrasilAPI
    │   │   ├── config/
    │   │   │   └── RestClientConfig.java     ← Timeouts das chamadas
    │   │   └── erro/
    │   │       └── TratadorDeErros.java      ← Respostas de erro em JSON
    │   └── resources/
    │       ├── application.properties        ← Configurações e banco
    │       └── static/
    │           └── index.html                ← Tela web
    └── test/java/com/lalo9/clientes/
        ├── ClientesApiApplicationTests.java  ← Teste de contexto
        └── cliente/
            ├── ClienteControllerTest.java    ← Controller com APIs simuladas
            └── CnpjValidadorTest.java        ← Validador de CNPJ
```

---

## 🚀 Como Rodar Localmente

### Pré-requisitos

* [JDK 25](https://adoptium.net/) instalado (`java -version` deve mostrar 25).
* [Docker](https://www.docker.com/) instalado e em execução.
* Conexão com a internet (as APIs externas são consultadas de verdade).

### 1. Clone e acesse a pasta do projeto

```bash
git clone https://github.com/Lalo9/clientes-api.git
cd clientes-api
```

### 2. Suba o banco de dados

```bash
docker compose up -d
```

> 💡 **Na primeira execução**, o Docker baixa a imagem do MySQL 8.4 e cria o banco `clientes`. Aguarde uns 20 segundos antes do próximo passo.

### 3. Suba a aplicação

```bash
./mvnw spring-boot:run
```

> 💡 **Na primeira execução**, o Maven baixa as bibliotecas do projeto, o que pode levar alguns minutos. A tabela `clientes` é criada automaticamente. Aguarde a mensagem `Started ClientesApiApplication`.

### 4. Acesse no navegador

| Serviço | URL |
| --- | --- |
| Tela web | http://localhost:8080 |
| API | http://localhost:8080/clientes |
| Rota de saúde | http://localhost:8080/status |
| MySQL | localhost:3307 |

---

## 🧪 Dados para Testar

CNPJs reais de empresas grandes, que a BrasilAPI reconhece, e dois CEPs:

| Empresa | CNPJ |
| --- | --- |
| Banco do Brasil | 00000000000191 |
| Petrobras | 33000167000101 |
| Bradesco | 60746948000112 |
| Caixa Econômica Federal | 00360305000104 |

| Local | CEP |
| --- | --- |
| Praça da Sé, São Paulo | 01001000 |
| Asa Norte, Brasília | 70040010 |

Para ver os erros funcionando, tente o CNPJ `11111111111111` (inválido) ou o CEP `99999999` (inexistente).

---

## 🔌 Endpoints da API

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/clientes` | Cadastra (consulta CNPJ e CEP) |
| GET | `/clientes` | Lista todos |
| GET | `/clientes/{id}` | Busca um cliente |
| PUT | `/clientes/{id}` | Edita (refaz as consultas) |
| DELETE | `/clientes/{id}` | Exclui |
| GET | `/status` | Confirma que a API está no ar |

CNPJ e CEP são enviados **só com números**, sem pontos nem traços. A tela web cuida dessa limpeza sozinha.

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
| --- | --- |
| 400 | Campos ausentes ou com formato errado (a resposta lista os campos) |
| 404 | Cliente não encontrado |
| 409 | CNPJ já cadastrado |
| 422 | CNPJ inválido, CNPJ não encontrado ou CEP não encontrado |
| 502 | ViaCEP ou BrasilAPI indisponível |

---

## 🧠 Decisões de Projeto

* **Nada é salvo com dados incompletos.** Se o CEP ou o CNPJ não existirem, ou se uma API externa estiver fora do ar, o cadastro falha antes de gravar.
* **Validação local primeiro.** Um CNPJ com dígitos verificadores errados é recusado sem gastar uma chamada externa.
* **Timeouts.** Conexão de 3 segundos e leitura de 5 segundos, para a API não travar por causa de um serviço externo lento.
* **Cache.** O resultado do ViaCEP fica em memória. Falhas não entram no cache, então uma nova tentativa consulta de novo.
* **DTO de entrada.** Quem chama a API só envia nome, CNPJ e CEP; `id`, razão social e endereço não podem ser definidos de fora.

---

## 🗃️ Banco de Dados

Uma tabela, criada automaticamente pelo Hibernate na primeira execução:

| Coluna | Tipo | Observação |
| --- | --- | --- |
| `id` | bigint | Chave primária, auto incremento |
| `nome` | varchar(150) | Obrigatório |
| `cnpj` | varchar(14) | Obrigatório e **único** |
| `razao_social` | varchar(200) | Preenchido pela BrasilAPI |
| `cep` | varchar(8) | Obrigatório |
| `logradouro`, `bairro`, `cidade` | varchar(255) | Preenchidos pelo ViaCEP |
| `uf` | varchar(2) | Preenchido pelo ViaCEP |

---

## ✅ Testes

```bash
./mvnw test
```

São **13 testes**. Os do controller usam versões simuladas (mocks) do ViaCEP, da BrasilAPI e do repositório, então rodam sem internet e cobrem os casos de erro (CEP inexistente, API fora do ar, CNPJ duplicado). O teste de contexto da aplicação precisa do MySQL ligado.

---

## 🛑 Gerenciamento do Docker

Para desligar o banco sem perder os dados:

```bash
docker compose down
```

Para "resetar" o projeto inteiro (apagando todos os clientes cadastrados):

```bash
docker compose down -v
```

Se ligar o computador e a aplicação reclamar que não conecta no banco, rode `docker compose up -d` de novo.

---

## 🏗️ Arquitetura do Sistema

```text
┌──────────────┐        ┌──────────────────────────┐        ┌──────────────────┐
│  Navegador   │───────▶│   Spring Boot  :8080     │───────▶│  MySQL 8.4       │
│  (tela web)  │◀───────│  API REST + tela estática│◀───────│  Docker  :3307   │
└──────────────┘        └─────────┬────────────────┘        └──────────────────┘
                                  │
                      ┌───────────┴───────────┐
                      ▼                       ▼
               ┌─────────────┐         ┌─────────────┐
               │  ViaCEP     │         │ BrasilAPI   │
               │ (endereço)  │         │ (CNPJ)      │
               └─────────────┘         └─────────────┘
```

---

## ⚠️ Limitações Conhecidas

* A senha do banco (`root123`) está no `docker-compose.yml` e no `application.properties` porque o projeto é para uso local. Numa versão de produção ela viria de variável de ambiente.
* O cache é em memória e zera ao reiniciar a aplicação.
* Não há autenticação nem paginação na listagem.
* Não há testes de integração com o banco e o servidor HTTP reais.

---

## 👨‍💻 Autor

> **Aviso:** Este é um projeto de portfólio com foco no aprendizado de Java, Spring Boot, consumo de APIs externas e containers. Não recomendado para uso em produção sem implementações extras de segurança.
