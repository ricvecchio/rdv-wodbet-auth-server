# Spring Auth Server
Servidor de autenticação e autorização baseado em **JWT**, desenvolvido com **Kotlin + Spring Boot**. Permite gerenciar usuários, perfis de acesso, eventos e participantes. Suporta também fluxo iOS com login por telefone + UUID + confirmação por código.


> **Autor:** Ricardo Del Vecchio   
> **Apresentação Youtube:** https://youtu.be/WYS0-PJBDR8

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
## 🧪 Testes da API com Postman ou Insomnia

Este projeto possui uma collection para facilitar os testes dos principais endpoints do backend do RDV WODBet Auth Server.

A collection cobre os seguintes módulos:

- Login iOS por telefone + UUID
- Usuários
- Fluxo legado com e-mail, senha e JWT
- Roles
- Apostas / Bets
- Eventos
- Participantes

A collection completa está no arquivo `rdv-wodbet-auth-server.postman_collection.json` na raiz do projeto. Importe-a no **Postman** ou **Insomnia**.

---

### 1. Configuração inicial

Antes de iniciar os testes, execute a aplicação localmente.

A API deve estar disponível em:

```text
http://localhost:8080
```

Na collection, configure as variáveis:

```json
{
  "baseUrl": "http://localhost:8080",
  "token": "",
  "userId": "1",
  "phone": "11999999999",
  "uuid": "uuid-dispositivo-teste",
  "code": "123456",
  "betId": "1",
  "eventId": "1",
  "participantId": "1"
}
```

A variável `code` deve ser atualizada manualmente com o código exibido no log do backend após o login por telefone retornar `202 Accepted`.

---

### 2. Fluxo iOS — Login por telefone + UUID

Este é o fluxo principal usado pelo app iOS.

#### 2.1 Login por telefone

```http
POST /users/login
```

Body:

```json
{
  "phone": "{{phone}}",
  "uuid": "{{uuid}}"
}
```

Resultados esperados:

| Status | Situação |
|---|---|
| `202 Accepted` | Telefone ainda não confirmado ou UUID diferente — código gerado no log via FakeSms |
| `200 OK` | Usuário ativo com mesmo telefone e UUID — retorna o usuário diretamente |

---

#### 2.2 Confirmar código

```http
POST /users/confirm
```

Body:

```json
{
  "phone": "{{phone}}",
  "uuid": "{{uuid}}",
  "code": "{{code}}"
}
```

Antes de executar, copie o código exibido no log do backend e atualize a variável `code`.

| Status | Situação |
|---|---|
| `200 OK` | Código válido — backend cria ou atualiza o usuário e retorna seus dados |
| `404 Not Found` | Nenhum código pendente para o telefone + UUID |
| `400 Bad Request` | Código inválido ou expirado |

---

#### 2.3 Login direto após confirmação

```http
POST /users/login
```

Body:

```json
{
  "phone": "{{phone}}",
  "uuid": "{{uuid}}"
}
```

Após confirmar o código com sucesso, repetir o login com o mesmo telefone e UUID deve retornar `200 OK` sem solicitar novo código.

---

#### 2.4 Atualizar perfil iOS

```http
PUT /users/{{userId}}
```

Body:

```json
{
  "name": "Ricardo",
  "description": "Atleta e desenvolvedor",
  "phone": null,
  "photoUrl": null
}
```

Resultado esperado: `200 OK` com o usuário atualizado.

---

### 3. Fluxo legado — E-mail, senha e JWT

#### 3.1 Criar usuário

```http
POST /users
```

Body:

```json
{
  "email": "usuario@email.com",
  "password": "Senha@123",
  "name": "Nome do Usuário"
}
```

Resultado esperado: `201 Created`

---

#### 3.2 Login com e-mail e senha

```http
POST /users/login
```

Body:

```json
{
  "email": "admin@authserver.com",
  "password": "admin"
}
```

Resultado esperado: `200 OK`

Resposta esperada:

```json
{
  "token": "...",
  "user": {
    "id": 1,
    "name": "Admin"
  }
}
```

Após o login, copie o token retornado e atualize a variável `token`. Nos endpoints protegidos, use o header:

```http
Authorization: Bearer {{token}}
```

| Perfil | Expiração do token |
|---|---|
| Usuário comum | 48 horas |
| ADMIN | 1 hora |

---

### 4. Usuários

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `GET` | `/users` | Lista usuários ativos | Público |
| `GET` | `/users?role=ADMIN` | Filtra usuários por role | Público |
| `GET` | `/users?sortDir=ASC\|DESC` | Lista usuários ordenados por nome | Público |
| `GET` | `/users/{id}` | Busca usuário por ID | Público |
| `POST` | `/users` | Cria novo usuário (legado) | Público |
| `POST` | `/users/login` | Login por telefone+UUID (iOS) ou e-mail+senha (legado) | Público |
| `POST` | `/users/confirm` | Confirma código e retorna usuário (iOS) | Público |
| `PUT` | `/users/{id}` | Atualiza perfil — name, description, phone, photoUrl (iOS) | Público |
| `PATCH` | `/users/{id}` | Atualiza nome do usuário (legado) | Autenticado (próprio usuário ou ADMIN) |
| `PUT` | `/users/{id}/roles/{role}` | Adiciona role ao usuário | ADMIN |
| `DELETE` | `/users/{id}` | Remove usuário | ADMIN |

#### 4.1 Atualizar nome do usuário — fluxo legado

```http
PATCH /users/{{userId}}
```

Headers:

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "name": "Novo Nome"
}
```

---

### 5. Roles

Todos os endpoints de roles exigem autenticação com perfil **ADMIN**.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/roles` | Cria nova role |
| `GET` | `/roles` | Lista todas as roles |

#### 5.1 Criar role

```http
POST /roles
```

Headers:

```http
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "name": "MODERATOR",
  "description": "Moderador de conteúdo"
}
```

Resultado esperado: `201 Created`

---

### 6. Apostas — Bets

Os endpoints de apostas representam o fluxo principal do app RDV WODBet. Todos são públicos.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/bets` | Lista apostas ordenadas por createdAt DESC |
| `GET` | `/bets/{id}` | Busca aposta por ID |
| `POST` | `/bets` | Cria nova aposta |
| `PUT` | `/bets/{id}/vote` | Registra ou atualiza voto em atleta |
| `PUT` | `/bets/{id}/winner` | Propõe vencedor |
| `PUT` | `/bets/{id}/confirm` | Confirma vencedor proposto |
| `PUT` | `/bets/{id}/reject` | Rejeita vencedor e marca como disputa |
| `PUT` | `/bets/{id}/cancel` | Cancela aposta |
| `PUT` | `/bets/{id}/result` | Atualiza resultados e finaliza aposta |

#### 6.1 Criar aposta

```http
POST /bets
```

Body:

```json
{
  "createdByUserId": "1",
  "athleteAUserId": "2",
  "athleteBUserId": "3",
  "wodTitle": "Fran 21-15-9",
  "prizeType": "gatorade",
  "prizeOtherDescription": null,
  "expiresAt": "2026-12-31T23:59:00Z"
}
```

Resultado esperado: `201 Created`

Valores aceitos para `prizeType`:

| Valor | Descrição |
|---|---|
| `water` | Água |
| `gatorade` | Gatorade |
| `beer` | Cerveja |
| `shake` | Shake |
| `other` | Outro — campo `prizeOtherDescription` obrigatório |

Após criar, copie o `id` retornado e atualize a variável `betId`.

Status possíveis de uma aposta:

| Status | Descrição |
|---|---|
| `open` | Aposta em aberto |
| `finished` | Finalizada com vencedor confirmado |
| `canceled` | Cancelada pelo criador |
| `disputed` | Vencedor rejeitado por um dos atletas |
| `expired` | expiresAt ultrapassado sem finalização |

#### 6.2 Votar em atleta

```http
PUT /bets/{{betId}}/vote
```

Body:

```json
{
  "voterUserId": "1",
  "votedAthleteUserId": "2"
}
```

Permitido apenas em apostas com status `open` ou `disputed`. Voto anterior do mesmo usuário é substituído.

#### 6.3 Propor vencedor

```http
PUT /bets/{{betId}}/winner
```

Body:

```json
{
  "requesterUserId": "1",
  "proposedWinnerUserId": "2"
}
```

#### 6.4 Confirmar vencedor

```http
PUT /bets/{{betId}}/confirm
```

Body:

```json
{
  "confirmerUserId": "2"
}
```

Apenas os atletas da aposta podem confirmar. Quando os dois confirmam o mesmo vencedor, a aposta é finalizada com `status = finished`.

#### 6.5 Rejeitar vencedor

```http
PUT /bets/{{betId}}/reject
```

Body:

```json
{
  "rejectorUserId": "2"
}
```

Altera a aposta para `status = disputed` e limpa o vencedor proposto.

#### 6.6 Cancelar aposta

```http
PUT /bets/{{betId}}/cancel
```

Body:

```json
{
  "requesterUserId": "1"
}
```

Apenas o criador pode cancelar. Não é possível cancelar apostas `finished`, `canceled` ou `expired`.

#### 6.7 Atualizar resultado

```http
PUT /bets/{{betId}}/result
```

Body:

```json
{
  "requesterUserId": "1",
  "athleteAResult": "5:30",
  "athleteBResult": "6:10",
  "winnerUserId": "2"
}
```

Atualiza os resultados dos atletas, define o vencedor e finaliza a aposta diretamente.

---

### 7. Eventos

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/events` | Cria novo evento | Autenticado |
| `GET` | `/events` | Lista eventos com filtros e ordenação | Público |
| `GET` | `/events/{id}` | Busca evento por ID | Público |
| `PATCH` | `/events/{id}` | Atualiza dados do evento | Autenticado |
| `DELETE` | `/events/{id}` | Remove evento | ADMIN |
| `POST` | `/events/{eventId}/participants/{participantId}` | Associa participante ao evento | Autenticado |
| `DELETE` | `/events/{eventId}/participants/{participantId}` | Remove participante do evento | ADMIN |
| `GET` | `/events/{eventId}/participants` | Lista participantes do evento | Público |

#### Filtros disponíveis — `GET /events`

| Parâmetro | Tipo | Descrição | Exemplo |
|---|---|---|---|
| `name` | string | Filtra por nome (parcial, case-insensitive) | `name=java` |
| `status` | enum | `SCHEDULED` \| `CANCELLED` \| `FINISHED` | `status=SCHEDULED` |
| `location` | string | Filtra por local (parcial, case-insensitive) | `location=curitiba` |
| `startDate` | ISO datetime | Eventos a partir desta data | `startDate=2026-01-01T00:00:00` |
| `endDate` | ISO datetime | Eventos até esta data | `endDate=2026-12-31T23:59:59` |
| `sortBy` | string | `name` \| `eventDate` \| `createdAt` \| `location` | `sortBy=eventDate` |
| `direction` | string | `ASC` \| `DESC` | `direction=ASC` |

Exemplo completo:

```http
GET /events?name=java&status=SCHEDULED&location=curitiba&sortBy=eventDate&direction=ASC
```

#### Criar evento

```http
POST /events
```

Body:

```json
{
  "name": "Java Summit 2026",
  "description": "Conferência sobre Java e JVM",
  "location": "Curitiba, PR",
  "eventDate": "2026-09-15T09:00:00",
  "status": "SCHEDULED"
}
```

#### Atualizar evento

```http
PATCH /events/{{eventId}}
```

Body (todos os campos são opcionais):

```json
{
  "name": "Novo Nome do Evento",
  "location": "São Paulo, SP",
  "status": "CANCELLED"
}
```

---

### 8. Participantes

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/participants` | Cria novo participante | Autenticado |
| `GET` | `/participants` | Lista participantes ordenados por nome (ASC) | Público |
| `GET` | `/participants?sortDir=DESC` | Lista participantes em ordem decrescente | Público |
| `GET` | `/participants/{id}` | Busca participante por ID | Público |
| `PATCH` | `/participants/{id}` | Atualiza dados do participante | Autenticado |
| `DELETE` | `/participants/{id}` | Remove participante | ADMIN |

#### Criar participante

```http
POST /participants
```

Body:

```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "phone": "(41) 99999-0000"
}
```

#### Atualizar participante

```http
PATCH /participants/{{participantId}}
```

Body (todos os campos são opcionais):

```json
{
  "name": "João Santos",
  "phone": "(41) 98888-1111"
}
```

---

### 9. Ordem recomendada de execução dos testes

Para validar o backend completo, execute a collection nesta ordem:

```text
1.  Login por telefone
2.  Confirmar código
3.  Login direto após confirmação
4.  Atualizar perfil iOS
5.  Criar usuário legado
6.  Login legado para obter JWT
7.  Listar usuários
8.  Buscar usuário por ID
9.  Criar aposta
10. Listar apostas
11. Buscar aposta por ID
12. Votar em atleta
13. Propor vencedor
14. Confirmar vencedor com atleta A
15. Confirmar vencedor com atleta B
16. Criar role
17. Listar roles
18. Criar participante
19. Listar participantes
20. Criar evento
21. Listar eventos
22. Associar participante ao evento
23. Listar participantes do evento
24. Atualizar evento
25. Atualizar participante
```

Para testar cenários alternativos de apostas, crie novas apostas e execute separadamente:

```text
- Rejeitar vencedor  → status: disputed
- Cancelar aposta    → status: canceled
- Atualizar resultado → status: finished (via resultado direto)
```

> ⚠️ Esses fluxos alteram o status da aposta e podem impedir novas operações sobre a mesma aposta.

---

## 🧪 Testes da API pelo Insomnia

Use a collection do Insomnia **exatamente na ordem dos endpoints**. Sempre que um endpoint retornar um **ID** ou um **JWT**, copie o valor para a variável correspondente no **Environment**.

Nos fluxos iOS, após executar o login por telefone, consulte o log do backend para capturar o código enviado pelo FakeSMS.

Exemplo de log:

```text
=== [FakeSMS] Sending confirmation code to phone 11999999999: CODE = 831920 ===
```

Copie o valor retornado para a variável `code` antes de executar a confirmação:

```json
{
  "code": "831920"
}
```

### Environment

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

### 00 — Preparação sem erro

#### 01 — Criar usuário legado

**POST** `{{ _.baseUrl }}/users`

Body:

```json
{
  "email": "{{ _.legacyEmail }}",
  "password": "{{ _.legacyPassword }}",
  "name": "{{ _.legacyName }}"
}
```

**Retorno esperado:** `201 Created`

#### 02 — Login legado e-mail + senha

**POST** `{{ _.baseUrl }}/users/login`

Body:

```json
{
  "email": "{{ _.legacyEmail }}",
  "password": "{{ _.legacyPassword }}"
}
```

**Retorno esperado:** `200 OK`

**Ação obrigatória:** copiar o JWT retornado para a variável `token`.

### 01 — Fluxo iOS: telefone + UUID

#### 03 — Login por telefone — primeiro acesso espera 202

**POST** `{{ _.baseUrl }}/users/login`

Body:

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}"
}
```

**Retorno esperado:** `202 Accepted`

**Ação obrigatória:** consultar o log do backend, copiar o código do FakeSMS e preencher a variável `code`.

#### 04 — Confirmar código iOS

**POST** `{{ _.baseUrl }}/users/confirm`

Body:

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}",
  "code": "{{ _.code }}"
}
```

**Retorno esperado:** `200 OK`

#### 05 — Login direto após confirmação

**POST** `{{ _.baseUrl }}/users/login`

Body:

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}"
}
```

**Retorno esperado:** `200 OK`

**Ações obrigatórias:**
- copiar o JWT para `token`;
- copiar o ID do usuário para `userId`;
- identificar dois usuários válidos para apostas e preencher `athleteAId` e `athleteBId`.

Esses IDs serão utilizados a partir do item 31.

#### 06 — Atualizar perfil iOS

**PUT** `{{ _.baseUrl }}/users/{{ _.userId }}`

Body:

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

**POST** `{{ _.baseUrl }}/users/login`

Body:

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.alternateUuid }}"
}
```

**Retorno esperado:** `202 Accepted`

**Ação obrigatória:** consultar novamente o log FakeSMS e atualizar a variável `code`.

#### 08 — Confirmar UUID diferente

**POST** `{{ _.baseUrl }}/users/confirm`

Body:

```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.alternateUuid }}",
  "code": "{{ _.code }}"
}
```

**Retorno esperado:** `200 OK`

### Variáveis que precisam ser preenchidas manualmente ao longo do fluxo

| Variável | Obtida em |
|---|---|
| `token` | 02 ou 05 |
| `adminToken` | 50 |
| `code` | log FakeSMS (03 e 07) |
| `userId` | 05 |
| `participantId` | 18 |
| `eventId` | 23 |
| `betId` | 31 |
| `betIdFinish` | 34 |
| `betIdReject` | 39 |
| `betIdCancel` | 42 |
| `betIdResult` | 44 |
| `athleteAId` | usuário existente |
| `athleteBId` | usuário existente |
