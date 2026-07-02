# Spring Auth Server
Servidor de autenticação e autorização baseado em **JWT**, desenvolvido com **Kotlin + Spring Boot**. Permite gerenciar usuários, perfis de acesso, eventos e participantes. Suporta também fluxo iOS com login por telefone + UUID + confirmação por código.


> **Autor:** Ricardo Del Vecchio   
> **Apresentação Youtube:** https://youtu.be/ISIPWfMTb5U

---
## 🛠️ Tecnologias
| Tecnologia | Versão |
|---|---|
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.5 |
| Java | 21 |
| Spring Security | (gerenciado pelo Boot) |
| JJWT | 0.13.0 |
| Spring Data JPA | (gerenciado pelo Boot) |
| H2 Database | In-memory |
| PostgreSQL | Produção / dev |
| SpringDoc OpenAPI (Swagger) | 3.0.2 |
---
## 🚀 Como executar
```bash
# Perfil local (H2 in-memory)
./gradlew bootRun --args='--spring.profiles.active=local'

# Perfil dev (PostgreSQL via variáveis de ambiente)
./gradlew bootRun --args='--spring.profiles.active=dev'
```
A aplicação sobe em `http://localhost:8080`.
> **Swagger UI:** `http://localhost:8080`  
> **H2 Console (perfil local):** `http://localhost:8080/h2-console` (usuário: `sa`, senha: `sa`)

---

---
## 🧪 Testes da API pelo Insomnia

Esta seção documenta o fluxo completo de testes da collection **RDV WODBet Auth Server — Ordem Corrigida Insomnia**.

A execução deve seguir a ordem dos itens da collection, pois vários endpoints dependem de variáveis preenchidas por respostas anteriores.

> Sempre que um endpoint retornar um `id`, `token` ou código FakeSMS, copie o valor para o **Environment** antes de seguir para o próximo passo.

### Collection

- Arquivo de referência: `rdv-wodbet-auth-server-insomnia-v3.yaml`
- Ferramenta: **Insomnia**
- Base URL local: `http://localhost:8080`

### Environment do Insomnia

Configure o ambiente `Local` com as variáveis abaixo. Os valores vazios devem ser preenchidos durante a execução do fluxo.

```json
{
  "baseUrl": "http://localhost:8080",
  "token": "",
  "adminToken": "",
  "adminEmail": "admin@authserver.com",
  "adminPassword": "admin",
  "legacyEmail": "usuario@email.com",
  "legacyPassword": "Senha@123",
  "legacyName": "Usuário Teste",
  "phone": "11999999999",
  "uuid": "uuid-dispositivo-teste",
  "alternateUuid": "uuid-dispositivo-alternativo",
  "code": "",
  "userId": "",
  "athleteAId": "",
  "athleteBId": "",
  "betId": "",
  "betIdFinish": "",
  "betIdReject": "",
  "betIdCancel": "",
  "betIdResult": "",
  "eventId": "",
  "participantId": "",
  "roleName": "MODERATOR_TESTE_01"
}
```

### Captura do código FakeSMS

Nos itens **03** e **07**, o backend retorna `202 Accepted` e imprime o código de confirmação no log da aplicação. Exemplo:

```text
=== [FakeSMS] Sending confirmation code to phone 11999999999: CODE = 831920 ===
```
Copie somente o número do código e preencha a variável `code` no Environment antes de executar o endpoint de confirmação.

### Variáveis preenchidas manualmente durante o fluxo

| Variável | Quando preencher |
|---|---|
| `token` | Após o item 02, copiando o JWT do login legado. |
| `adminToken` | Após o item 50, copiando o JWT do login ADMIN. |
| `code` | Após os itens 03 e 07, copiando o código exibido no log FakeSMS. |
| `userId` | Após o item 04 ou 05, copiando o ID do usuário retornado. |
| `athleteAId` | Antes dos itens de apostas, informar o ID de um usuário existente. |
| `athleteBId` | Antes dos itens de apostas, informar o ID de outro usuário existente. |
| `participantId` | Após o item 18. |
| `eventId` | Após o item 23. |
| `betId` | Após o item 31. |
| `betIdFinish` | Após o item 34. |
| `betIdReject` | Após o item 39. |
| `betIdCancel` | Após o item 42. |
| `betIdResult` | Após o item 44. |

---

### 00 — Preparação sem erro

#### 01 — Criar usuário legado

**Descrição:** Cria um usuário legado com e-mail e senha para obter JWT comum.

**Request:**

```http
POST {{ _.baseUrl }}/users
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "email": "{{ _.legacyEmail }}",
  "password": "{{ _.legacyPassword }}",
  "name": "{{ _.legacyName }}"
}
```

**Retorno esperado:** `201 Created`


#### 02 — Login legado e-mail + senha

**Descrição:** Realiza login legado com e-mail e senha para obter token JWT.

**Request:**

```http
POST {{ _.baseUrl }}/users/login
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "email": "{{ _.legacyEmail }}",
  "password": "{{ _.legacyPassword }}"
}
```

**Retorno esperado:** `200 OK`


**Ações obrigatórias / observações:**

- Copiar o JWT retornado e preencher a variável `token` no Environment.


---

### 01 — Fluxo iOS: telefone + UUID

#### 03 — Login por telefone — primeiro acesso espera 202

**Descrição:** Solicita o código de confirmação do fluxo iOS usando telefone + UUID.

**Request:**

```http
POST {{ _.baseUrl }}/users/login
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}"
}
```

**Retorno esperado:** `202 Accepted` no primeiro acesso; `200 OK` se telefone + UUID já estiverem confirmados


**Ações obrigatórias / observações:**

- Consultar o log do backend para capturar o código FakeSMS.
- Exemplo de log: `=== [FakeSMS] Sending confirmation code to phone 11999999999: CODE = 831920 ===`
- Copiar somente o número do código e preencher a variável `code` antes de executar o item 04.


#### 04 — Confirmar código iOS

**Descrição:** Confirma o código recebido via FakeSMS e ativa/cria/atualiza o usuário.

**Request:**

```http
POST {{ _.baseUrl }}/users/confirm
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}",
  "code": "{{ _.code }}"
}
```

**Retorno esperado:** `200 OK`


**Ações obrigatórias / observações:**

- Copiar o `id` do usuário retornado e preencher a variável `userId`.


#### 05 — Login direto após confirmação

**Descrição:** Valida que o mesmo telefone + UUID já entra diretamente após confirmação.

**Request:**

```http
POST {{ _.baseUrl }}/users/login
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}"
}
```

**Retorno esperado:** `200 OK`


**Ações obrigatórias / observações:**

- Confirmar que o usuário retornado é o mesmo telefone + UUID.
- Se o retorno possuir token, manter/copiar para `token`; para o fluxo atual, o principal é manter `userId` preenchido.


#### 06 — Atualizar perfil iOS

**Descrição:** Atualiza nome, descrição, telefone e foto do usuário no fluxo iOS.

**Request:**

```http
PUT {{ _.baseUrl }}/users/{{ _.userId }}
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "name": "Ricardo",
  "description": "Atleta e desenvolvedor",
  "phone": null,
  "photoUrl": null
}
```

**Retorno esperado:** `200 OK`


#### 07 — Login com UUID diferente — espera 202

**Descrição:** Simula troca de dispositivo usando o mesmo telefone com outro UUID.

**Request:**

```http
POST {{ _.baseUrl }}/users/login
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.alternateUuid }}"
}
```

**Retorno esperado:** `202 Accepted`


**Ações obrigatórias / observações:**

- Consultar novamente o log FakeSMS.
- Atualizar a variável `code` com o novo código antes de executar o item 08.


#### 08 — Confirmar UUID diferente

**Descrição:** Confirma o novo UUID com o código recebido via FakeSMS.

**Request:**

```http
POST {{ _.baseUrl }}/users/confirm
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.alternateUuid }}",
  "code": "{{ _.code }}"
}
```

**Retorno esperado:** `200 OK`


---

### 02 — Usuários

#### 09 — Listar usuários ativos

**Request:**

```http
GET {{ _.baseUrl }}/users
```

**Retorno esperado:** `200 OK`


#### 10 — Listar usuários por role USER

**Request:**

```http
GET {{ _.baseUrl }}/users?role=USER
```

**Retorno esperado:** `200 OK`


#### 11 — Listar usuários por role ADMIN

**Request:**

```http
GET {{ _.baseUrl }}/users?role=ADMIN
```

**Retorno esperado:** `200 OK`


#### 12 — Listar usuários ASC

**Request:**

```http
GET {{ _.baseUrl }}/users?sortDir=ASC
```

**Retorno esperado:** `200 OK`


#### 13 — Listar usuários DESC

**Request:**

```http
GET {{ _.baseUrl }}/users?sortDir=DESC
```

**Retorno esperado:** `200 OK`


#### 14 — Buscar usuário por ID

**Request:**

```http
GET {{ _.baseUrl }}/users/{{ _.userId }}
```

**Retorno esperado:** `200 OK`


#### 15 — Buscar usuário RAW por ID

**Request:**

```http
GET {{ _.baseUrl }}/users/{{ _.userId }}/raw
```

**Retorno esperado:** `200 OK`


#### 16 — Atualizar perfil iOS por PUT

**Request:**

```http
PUT {{ _.baseUrl }}/users/{{ _.userId }}
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "name": "Ricardo Atualizado",
  "description": "Perfil atualizado via PUT",
  "phone": null,
  "photoUrl": null
}
```

**Retorno esperado:** `200 OK`


#### 17 — Atualizar nome legado por PATCH — somente próprio usuário do token

**Descrição:** Atualiza nome pelo fluxo legado. Requer token do próprio usuário ou de ADMIN.

**Request:**

```http
PATCH {{ _.baseUrl }}/users/{{ _.userId }}
```

**Headers:**

```http
Content-Type: application/json
Authorization: Bearer {{ _.token }}
```

**Body:**

```json
{
  "name": "Novo Nome Legado"
}
```

**Retorno esperado:** `200 OK`; pode retornar `403 Forbidden` se `userId` não for o usuário do token ou se o token não for ADMIN


---

### 03 — Participantes

#### 18 — Criar participante

**Descrição:** Cria participante autenticado usando o JWT comum.

**Request:**

```http
POST {{ _.baseUrl }}/participants
```

**Headers:**

```http
Content-Type: application/json
Authorization: Bearer {{ _.token }}
```

**Body:**

```json
{
  "name": "João Silva Teste",
  "email": "joao.teste@email.com",
  "phone": "(41) 99999-0000"
}
```

**Retorno esperado:** `201 Created`


**Ações obrigatórias / observações:**

- Copiar o `id` retornado e preencher a variável `participantId`.


#### 19 — Listar participantes

**Request:**

```http
GET {{ _.baseUrl }}/participants
```

**Retorno esperado:** `200 OK`


#### 20 — Listar participantes DESC

**Request:**

```http
GET {{ _.baseUrl }}/participants?sortDir=DESC
```

**Retorno esperado:** `200 OK`


#### 21 — Buscar participante por ID

**Request:**

```http
GET {{ _.baseUrl }}/participants/{{ _.participantId }}
```

**Retorno esperado:** `200 OK`


#### 22 — Atualizar participante

**Request:**

```http
PATCH {{ _.baseUrl }}/participants/{{ _.participantId }}
```

**Headers:**

```http
Content-Type: application/json
Authorization: Bearer {{ _.token }}
```

**Body:**

```json
{
  "name": "João Santos",
  "phone": "(41) 98888-1111"
}
```

**Retorno esperado:** `200 OK`


---

### 04 — Eventos

#### 23 — Criar evento

**Descrição:** Cria evento autenticado usando o JWT comum.

**Request:**

```http
POST {{ _.baseUrl }}/events
```

**Headers:**

```http
Content-Type: application/json
Authorization: Bearer {{ _.token }}
```

**Body:**

```json
{
  "name": "Java Summit 2026",
  "description": "Conferência sobre Java e JVM",
  "location": "Curitiba, PR",
  "eventDate": "2026-09-15T09:00:00",
  "status": "SCHEDULED"
}
```

**Retorno esperado:** `201 Created`


**Ações obrigatórias / observações:**

- Copiar o `id` retornado e preencher a variável `eventId`.


#### 24 — Listar eventos

**Request:**

```http
GET {{ _.baseUrl }}/events
```

**Retorno esperado:** `200 OK`


#### 25 — Listar eventos com filtros

**Request:**

```http
GET {{ _.baseUrl }}/events?name=java&status=SCHEDULED&sortBy=eventDate&direction=ASC
```

**Retorno esperado:** `200 OK`


#### 26 — Buscar evento por ID

**Request:**

```http
GET {{ _.baseUrl }}/events/{{ _.eventId }}
```

**Retorno esperado:** `200 OK`


#### 27 — Atualizar evento

**Request:**

```http
PATCH {{ _.baseUrl }}/events/{{ _.eventId }}
```

**Headers:**

```http
Content-Type: application/json
Authorization: Bearer {{ _.token }}
```

**Body:**

```json
{
  "name": "Novo Nome do Evento",
  "location": "São Paulo, SP",
  "status": "CANCELLED"
}
```

**Retorno esperado:** `200 OK`


#### 28 — Associar participante ao evento

**Descrição:** Associa um participante já criado a um evento já criado.

**Request:**

```http
POST {{ _.baseUrl }}/events/{{ _.eventId }}/participants/{{ _.participantId }}
```

**Headers:**

```http
Authorization: Bearer {{ _.token }}
```

**Retorno esperado:** `200 OK`


**Ações obrigatórias / observações:**

- Executar somente depois de `participantId` e `eventId` estarem preenchidos.


#### 29 — Listar participantes do evento

**Request:**

```http
GET {{ _.baseUrl }}/events/{{ _.eventId }}/participants
```

**Retorno esperado:** `200 OK`


---

### 05 — Apostas: consultas e criação

#### 30 — Listar apostas

**Request:**

```http
GET {{ _.baseUrl }}/bets
```

**Retorno esperado:** `200 OK`


#### 31 — Criar aposta padrão

**Descrição:** Cria aposta padrão usando os IDs de usuários existentes.

**Request:**

```http
POST {{ _.baseUrl }}/bets
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "createdByUserId": "{{ _.userId }}",
  "athleteAUserId": "{{ _.athleteAId }}",
  "athleteBUserId": "{{ _.athleteBId }}",
  "wodTitle": "Fran 21-15-9",
  "prizeType": "gatorade",
  "prizeOtherDescription": null,
  "expiresAt": "2026-12-31T23:59:00Z"
}
```

**Retorno esperado:** `201 Created`


**Ações obrigatórias / observações:**

- Copiar o `id` retornado e preencher a variável `betId`.

**Pré-requisito:** `userId`, `athleteAId` e `athleteBId` precisam estar preenchidos com usuários existentes. `athleteAId` e `athleteBId` devem ser diferentes.


#### 32 — Buscar aposta por ID

**Request:**

```http
GET {{ _.baseUrl }}/bets/{{ _.betId }}
```

**Retorno esperado:** `200 OK`


#### 33 — Votar em atleta

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betId }}/vote
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "voterUserId": "{{ _.userId }}",
  "votedAthleteUserId": "{{ _.athleteAId }}"
}
```

**Retorno esperado:** `200 OK`


---

### 06 — Apostas: finalizar por confirmação dupla

#### 34 — Criar aposta para finalizar

**Descrição:** Cria aposta nova exclusivamente para testar finalização por dupla confirmação.

**Request:**

```http
POST {{ _.baseUrl }}/bets
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "createdByUserId": "{{ _.userId }}",
  "athleteAUserId": "{{ _.athleteAId }}",
  "athleteBUserId": "{{ _.athleteBId }}",
  "wodTitle": "Grace - fluxo finalizar",
  "prizeType": "gatorade",
  "prizeOtherDescription": null,
  "expiresAt": "2026-12-31T23:59:00Z"
}
```

**Retorno esperado:** `201 Created`


**Ações obrigatórias / observações:**

- Copiar o `id` retornado e preencher a variável `betIdFinish`.

**Pré-requisito:** `userId`, `athleteAId` e `athleteBId` preenchidos com usuários existentes.


#### 35 — Propor vencedor

**Descrição:** Define o vencedor proposto para a aposta de finalização.

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betIdFinish }}/winner
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "requesterUserId": "{{ _.userId }}",
  "proposedWinnerUserId": "{{ _.athleteAId }}"
}
```

**Retorno esperado:** `200 OK`


#### 36 — Confirmar vencedor pelo atleta A

**Descrição:** Confirma o vencedor pelo atleta A.

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betIdFinish }}/confirm
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "confirmerUserId": "{{ _.athleteAId }}"
}
```

**Retorno esperado:** `200 OK`


#### 37 — Confirmar vencedor pelo atleta B

**Descrição:** Confirma o vencedor pelo atleta B. Após as duas confirmações, a aposta deve finalizar.

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betIdFinish }}/confirm
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "confirmerUserId": "{{ _.athleteBId }}"
}
```

**Retorno esperado:** `200 OK`


#### 38 — Buscar aposta finalizada

**Request:**

```http
GET {{ _.baseUrl }}/bets/{{ _.betIdFinish }}
```

**Retorno esperado:** `200 OK`, com `status = finished`


---

### 07 — Apostas: fluxos alternativos

#### 39 — Criar aposta para rejeição

**Descrição:** Cria aposta nova exclusivamente para testar rejeição de vencedor.

**Request:**

```http
POST {{ _.baseUrl }}/bets
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "createdByUserId": "{{ _.userId }}",
  "athleteAUserId": "{{ _.athleteAId }}",
  "athleteBUserId": "{{ _.athleteBId }}",
  "wodTitle": "Isabel - fluxo rejeição",
  "prizeType": "gatorade",
  "prizeOtherDescription": null,
  "expiresAt": "2026-12-31T23:59:00Z"
}
```

**Retorno esperado:** `201 Created`


**Ações obrigatórias / observações:**

- Copiar o `id` retornado e preencher a variável `betIdReject`.

**Pré-requisito:** `userId`, `athleteAId` e `athleteBId` preenchidos com usuários existentes.


#### 40 — Propor vencedor para rejeição

**Descrição:** Propõe vencedor para a aposta que será rejeitada.

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betIdReject }}/winner
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "requesterUserId": "{{ _.userId }}",
  "proposedWinnerUserId": "{{ _.athleteAId }}"
}
```

**Retorno esperado:** `200 OK`


#### 41 — Rejeitar vencedor

**Descrição:** Rejeita o vencedor proposto e coloca a aposta em disputa.

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betIdReject }}/reject
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "rejectorUserId": "{{ _.athleteBId }}"
}
```

**Retorno esperado:** `200 OK`, com `status = disputed`


#### 42 — Criar aposta para cancelamento

**Descrição:** Cria aposta nova exclusivamente para testar cancelamento.

**Request:**

```http
POST {{ _.baseUrl }}/bets
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "createdByUserId": "{{ _.userId }}",
  "athleteAUserId": "{{ _.athleteAId }}",
  "athleteBUserId": "{{ _.athleteBId }}",
  "wodTitle": "Helen - fluxo cancelamento",
  "prizeType": "gatorade",
  "prizeOtherDescription": null,
  "expiresAt": "2026-12-31T23:59:00Z"
}
```

**Retorno esperado:** `201 Created`


**Ações obrigatórias / observações:**

- Copiar o `id` retornado e preencher a variável `betIdCancel`.

**Pré-requisito:** `userId`, `athleteAId` e `athleteBId` preenchidos com usuários existentes.


#### 43 — Cancelar aposta

**Descrição:** Cancela aposta pelo criador.

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betIdCancel }}/cancel
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "requesterUserId": "{{ _.userId }}"
}
```

**Retorno esperado:** `200 OK`, com `status = canceled`


#### 44 — Criar aposta para resultado direto

**Descrição:** Cria aposta nova exclusivamente para testar resultado direto.

**Request:**

```http
POST {{ _.baseUrl }}/bets
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "createdByUserId": "{{ _.userId }}",
  "athleteAUserId": "{{ _.athleteAId }}",
  "athleteBUserId": "{{ _.athleteBId }}",
  "wodTitle": "Diane - fluxo resultado",
  "prizeType": "gatorade",
  "prizeOtherDescription": null,
  "expiresAt": "2026-12-31T23:59:00Z"
}
```

**Retorno esperado:** `201 Created`


**Ações obrigatórias / observações:**

- Copiar o `id` retornado e preencher a variável `betIdResult`.

**Pré-requisito:** `userId`, `athleteAId` e `athleteBId` preenchidos com usuários existentes.


#### 45 — Atualizar resultado direto

**Descrição:** Atualiza resultados dos atletas e finaliza a aposta diretamente.

**Request:**

```http
PUT {{ _.baseUrl }}/bets/{{ _.betIdResult }}/result
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "requesterUserId": "{{ _.userId }}",
  "athleteAResult": "5:30",
  "athleteBResult": "6:10",
  "winnerUserId": "{{ _.athleteAId }}"
}
```

**Retorno esperado:** `200 OK`, com `status = finished`


---

### 08 — Cenários de erro iOS

#### 46 — Confirmar código inválido — espera 400

**Request:**

```http
POST {{ _.baseUrl }}/users/confirm
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}",
  "code": "000000"
}
```

**Retorno esperado:** `400 Bad Request`


#### 47 — Confirmar código inexistente — espera 404

**Request:**

```http
POST {{ _.baseUrl }}/users/confirm
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "uuid-sem-codigo",
  "code": "123456"
}
```

**Retorno esperado:** `404 Not Found`


#### 48 — Login telefone sem phone — espera 400

**Request:**

```http
POST {{ _.baseUrl }}/users/login
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "",
  "uuid": "{{ _.uuid }}"
}
```

**Retorno esperado:** `400 Bad Request`


#### 49 — Login telefone sem uuid — espera 400

**Request:**

```http
POST {{ _.baseUrl }}/users/login
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "phone": "{{ _.phone }}",
  "uuid": ""
}
```

**Retorno esperado:** `400 Bad Request`


---

### 09 — ADMIN opcional

#### 50 — Login ADMIN e-mail + senha

**Descrição:** Realiza login ADMIN para obter `adminToken` usado nos endpoints administrativos.

**Request:**

```http
POST {{ _.baseUrl }}/users/login
```

**Headers:**

```http
Content-Type: application/json
```

**Body:**

```json
{
  "email": "{{ _.adminEmail }}",
  "password": "{{ _.adminPassword }}"
}
```

**Retorno esperado:** `200 OK`


**Ações obrigatórias / observações:**

- Copiar o JWT retornado e preencher a variável `adminToken`.


#### 51 — Criar role

**Descrição:** Cria uma nova role. Requer token ADMIN.

**Request:**

```http
POST {{ _.baseUrl }}/roles
```

**Headers:**

```http
Content-Type: application/json
Authorization: Bearer {{ _.adminToken }}
```

**Body:**

```json
{
  "name": "MODERATOR_TESTE_01",
  "description": "Moderador de conteúdo"
}
```

**Retorno esperado:** `201 Created`; pode retornar conflito/erro se a role já existir


**Ações obrigatórias / observações:**

- Se a role já existir, alterar o valor fixo `MODERATOR_TESTE_01` no body antes de reenviar.

**Pré-requisito:** executar o item 50 e preencher `adminToken`.


#### 52 — Listar roles

**Descrição:** Lista roles cadastradas. Requer token ADMIN.

**Request:**

```http
GET {{ _.baseUrl }}/roles
```

**Headers:**

```http
Authorization: Bearer {{ _.adminToken }}
```

**Retorno esperado:** `200 OK`

**Pré-requisito:** executar o item 50 e preencher `adminToken`.


#### 53 — Adicionar role ADMIN ao usuário

**Descrição:** Adiciona role ADMIN ao usuário informado em `userId`. Requer token ADMIN.

**Request:**

```http
PUT {{ _.baseUrl }}/users/{{ _.userId }}/roles/ADMIN
```

**Headers:**

```http
Authorization: Bearer {{ _.adminToken }}
```

**Retorno esperado:** `200 OK` ou `204 No Content`


**Ações obrigatórias / observações:**

- Garantir que `userId` esteja preenchido com um usuário existente.

**Pré-requisito:** executar o item 50 e preencher `adminToken`.


#### 54 — Remover participante do evento

**Descrição:** Remove participante de um evento. Requer token ADMIN.

**Request:**

```http
DELETE {{ _.baseUrl }}/events/{{ _.eventId }}/participants/{{ _.participantId }}
```

**Headers:**

```http
Authorization: Bearer {{ _.adminToken }}
```

**Retorno esperado:** `204 No Content`


**Ações obrigatórias / observações:**

- Executar somente depois dos itens 18, 23 e 28. `eventId` e `participantId` precisam estar preenchidos e associados.

**Pré-requisito:** executar o item 50 e preencher `adminToken`.


#### 55 — Remover evento

**Descrição:** Remove evento. Requer token ADMIN.

**Request:**

```http
DELETE {{ _.baseUrl }}/events/{{ _.eventId }}
```

**Headers:**

```http
Authorization: Bearer {{ _.adminToken }}
```

**Retorno esperado:** `204 No Content`


**Ações obrigatórias / observações:**

- Executar somente depois do item 54, se quiser remover primeiro o vínculo com participante.

**Pré-requisito:** executar o item 50 e preencher `adminToken`.


#### 56 — Remover participante

**Descrição:** Remove participante. Requer token ADMIN.

**Request:**

```http
DELETE {{ _.baseUrl }}/participants/{{ _.participantId }}
```

**Headers:**

```http
Authorization: Bearer {{ _.adminToken }}
```

**Retorno esperado:** `204 No Content`

**Pré-requisito:** executar o item 50 e preencher `adminToken`.


#### 57 — Remover usuário

**Descrição:** Remove usuário. Requer token ADMIN.

**Request:**

```http
DELETE {{ _.baseUrl }}/users/{{ _.userId }}
```

**Headers:**

```http
Authorization: Bearer {{ _.adminToken }}
```

**Retorno esperado:** `204 No Content`

**Pré-requisito:** executar o item 50 e preencher `adminToken`.


---

### Ordem resumida de execução

```text
00 — Preparação sem erro
01 — Fluxo iOS: telefone + UUID
02 — Usuários
03 — Participantes
04 — Eventos
05 — Apostas: consultas e criação
06 — Apostas: finalizar por confirmação dupla
07 — Apostas: fluxos alternativos
08 — Cenários de erro iOS
09 — ADMIN opcional
```

> Para reiniciar o fluxo do zero usando banco em memória H2, reinicie a aplicação no perfil `local`. Lembre-se de limpar ou atualizar as variáveis do Environment quando IDs antigos deixarem de existir.
