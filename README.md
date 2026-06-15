# Calculadora de Calorias 


Projeto desenvolvido em Clojure para a disciplina de Programação Funcional.

A aplicação permite cadastrar um usuário, registrar consumo de alimentos, registrar realização de exercícios físicos, consultar extrato de transações e consultar saldo calórico por período.

https://github.com/Zuimbra/clojure-calories-conversor

## Funcionalidades

* Cadastrar usuário
* Consultar usuário cadastrado
* Registrar alimento consumido
* Registrar exercício realizado
* Consultar extrato de transações
* Consultar saldo calórico
* Consultar extrato e saldo por período
* Consumir APIs externas para obter calorias

## Tecnologias utilizadas

* Clojure
* Leiningen
* Ring / Jetty
* Compojure
* Cheshire
* clj-http
* USDA FoodData Central API
* API Ninjas Calories Burned API

## Estrutura do projeto

```txt
src
├── conversor
│   ├── core.clj
│   ├── api.clj
│   ├── state.clj
│   └── external.clj
│
└── frontend
    └── cli.clj
```

## Descrição dos arquivos

### `conversor.core`

Arquivo principal do back-end. Inicia o servidor da API.

### `conversor.api`

Define as rotas HTTP da aplicação e recebe requisições em JSON.

Rotas principais:

```txt
POST /user
GET  /user
POST /foods
POST /exercises
GET  /extract
GET  /balance
POST /reset
```

### `conversor.state`

Concentra o estado da aplicação usando `atom`.

Também possui funções para criar usuário, criar transações, armazenar dados, calcular calorias ganhas, calorias perdidas, saldo e filtrar transações por período.

### `conversor.external`

Responsável por acessar APIs externas.

* USDA FoodData Central API: usada para obter calorias de alimentos.
* API Ninjas Calories Burned API: usada para obter calorias gastas em exercícios.

### `frontend.cli`

Front-end por linha de comando. Ele não acessa diretamente o estado interno do back-end. A comunicação é feita por HTTP/JSON.

## Variáveis de ambiente

Antes de iniciar a API, configure as chaves das APIs externas:

```powershell
$env:USDA_API_KEY="SUA_CHAVE_USDA"
$env:API_NINJAS_KEY="SUA_CHAVE_API_NINJAS"
```

As chaves não devem ser colocadas diretamente no código-fonte.

## Como rodar o back-end

Na raiz do projeto:

```powershell
lein run
```

A API será iniciada em:

```txt
http://localhost:3000
```

## Como rodar o front-end

Em outro terminal, com a API já rodando:

```powershell
lein run -m frontend.cli
```

## Fluxo recomendado de uso

No front-end, utilize a seguinte ordem:

```txt
7 - Resetar dados
1 - Cadastrar usuario
2 - Consultar usuario
3 - Registrar alimento
4 - Registrar exercicio
5 - Consultar extrato
6 - Consultar saldo
```

## Observação sobre alimentos

No registro de alimentos, o campo `quantity` representa a quantidade em gramas.

Exemplo:

```json
{
  "food": "apple",
  "quantity": 200,
  "date": "2026-05-28"
}
```

Esse exemplo representa 200 gramas de maçã.

## Exemplos de chamadas HTTP

### Cadastrar usuário

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:3000/user" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"name":"Mateus","email":"mateus@email.com","age":19,"weight":85,"height":1.75,"gender":"Masculino"}'
```

### Registrar alimento

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:3000/foods" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"food":"apple","quantity":200,"date":"2026-05-28"}'
```

### Registrar exercício

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:3000/exercises" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"exercise":"running","duration":30,"date":"2026-05-28"}'
```

### Consultar extrato

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:3000/extract" `
  -Method GET
```

### Consultar saldo

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:3000/balance" `
  -Method GET
```

### Consultar saldo por período

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:3000/balance?start=2026-05-01&end=2026-05-31" `
  -Method GET
```


