# Spring Auth Server
Servidor de autenticação e autorização baseado em **JWT**, desenvolvido com **Kotlin + Spring Boot**. Permite gerenciar usuários, perfis de acesso, eventos e participantes. Suporta também fluxo de login por **telefone + UUID** (utilizado pelo app iOS).


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
## 📋 Regras de Negócio
### Usuários
- **Login por telefone:** o número é normalizado (removendo caracteres não numéricos) antes de buscar/salvar.
- **UUID único por dispositivo:** se o telefone existir mas com UUID diferente, um novo código é gerado.
- **Código de confirmação:** 6 dígitos, válido por **10 minutos**, marcado como usado após consumo.
- **Criação automática:** ao confirmar o código, se o telefone não estiver cadastrado, um novo usuário é criado automaticamente.
- **E-mail único (legado):** não é possível cadastrar dois usuários com o mesmo e-mail.
- **Senha (legado):** mínimo 8 caracteres, obrigatório conter letra, número e caractere especial (`@$!%*#?&`).
- **Atualização (legado):** apenas o próprio usuário ou um ADMIN pode alterar o nome via `PATCH`.
- **Remoção:** um ADMIN não pode ser deletado se for o único com essa role no sistema.
### Roles
- O nome da role é sempre convertido e armazenado em **maiúsculas**.
- Roles duplicadas não são permitidas.
- Somente ADMINs podem criar e listar roles.
### Eventos
- Um evento pode ter vários participantes (relação `1:N`).
- Um participante pode ser associado a apenas um evento por vez.
- Associar um participante já vinculado ao mesmo evento retorna `409 Conflict`.
- Desassociar um participante não vinculado ao evento retorna `400 Bad Request`.
- Buscar evento inexistente retorna `404 Not Found`.
### Participantes
- **E-mail único:** não é possível cadastrar dois participantes com o mesmo e-mail.
- Um participante pode existir sem estar vinculado a nenhum evento.
- Buscar participante inexistente retorna `404 Not Found`.
### Inicialização automática (Bootstrapper)
Ao subir a aplicação, são criados automaticamente:

| Recurso | Valor padrão |
|---|---|
| Role | `USER` |
| Role | `ADMIN` |
| Usuário admin | `admin@authserver.com` / `admin` |
> ⚠️ **Atenção:** altere as credenciais padrão antes de usar em produção.  
> 💡 Todo novo usuário criado via `POST /users` recebe automaticamente a role `USER`.

---
## 🔒 Regras de Segurança
- Sessão **stateless** (sem cookies/sessão no servidor).
- **CORS** liberado para todas as origens (configurar restrições em produção).
- **CSRF** desabilitado.
- Endpoints `GET`, `POST /users`, `POST /users/login`, `POST /users/confirm` e `PUT /users/{id}` são públicos.
- Ações de criação e atualização (legado) requerem autenticação JWT.
- Ações destrutivas (`DELETE`) requerem perfil **ADMIN**.

---
## 📁 Estrutura do Projeto
```
src/main/kotlin/br/pucpr/authserver/
├── AuthserverApplication.kt          # Entry point
├── Bootstrapper.kt                   # Dados iniciais (roles e admin padrão)
├── exceptions/                       # Exceções globais (400, 401, 403, 404)
├── security/
│   ├── Jwt.kt                        # Geração e validação de tokens JWT
│   ├── JwtTokenFilter.kt             # Filtro de autenticação via JWT
│   ├── SecurityConfig.kt             # Configuração do Spring Security
│   └── UserToken.kt                  # Representação do usuário no token
├── roles/
│   ├── controllers/
│   │   └── RoleController.kt         # Endpoints /roles
│   ├── dtos/
│   │   ├── requests/
│   │   │   └── CreateRoleRequest.kt
│   │   └── responses/
│   │       └── RoleResponse.kt
│   ├── entities/
│   │   └── Role.kt                   # Entidade Role
│   ├── repositories/
│   │   └── RoleRepository.kt         # Repositório JPA
│   └── services/
│       └── RoleService.kt            # Regras de negócio de roles
├── users/
│   ├── controllers/
│   │   └── UserController.kt         # Endpoints /users
│   ├── dtos/
│   │   ├── requests/
│   │   │   ├── CreateUserRequest.kt
│   │   │   ├── LoginRequest.kt       # Legado: email + password
│   │   │   ├── PhoneLoginRequest.kt  # iOS: phone + uuid
│   │   │   ├── PhoneConfirmRequest.kt# iOS: phone + uuid + code
│   │   │   ├── UpdateUserRequest.kt  # Legado: name
│   │   │   └── UpdateUserProfileRequest.kt # iOS: name + description
│   │   └── responses/
│   │       ├── LoginResponse.kt      # Legado: token + UserResponse
│   │       ├── UserResponse.kt       # Legado: id, email, name, roles
│   │       └── BackendUserResponse.kt# iOS: id, name, phone, uuid, active, description, createdAt
│   ├── entities/
│   │   ├── User.kt                   # Entidade User (tabela UserTable)
│   │   └── ConfirmationCode.kt       # Entidade de código de confirmação (tabela ConfirmationCode)
│   ├── enums/
│   │   └── SortDir.kt                # Enum de direção de ordenação (ASC/DESC)
│   ├── repositories/
│   │   ├── UserRepository.kt         # Repositório JPA
│   │   └── ConfirmationCodeRepository.kt # Repositório JPA de códigos
│   └── services/
│       ├── UserService.kt            # Regras de negócio de usuários
│       ├── ConfirmationCodeService.kt# Geração e validação de códigos
│       └── FakeSmsService.kt         # Simulação de envio de SMS via log
├── events/
│   ├── controllers/
│   │   └── EventController.kt        # Endpoints /events e /events/{id}/participants
│   ├── dtos/
│   │   ├── requests/
│   │   │   ├── CreateEventRequest.kt
│   │   │   └── UpdateEventRequest.kt
│   │   └── responses/
│   │       └── EventResponse.kt
│   ├── entities/
│   │   └── Event.kt                  # Entidade Event — tabela `events` (1:N com Participant)
│   ├── enums/
│   │   └── EventStatus.kt            # Enum: SCHEDULED, CANCELLED, FINISHED
│   ├── exceptions/
│   │   └── EventNotFoundException.kt # Exceção 404 customizada
│   ├── repositories/
│   │   └── EventRepository.kt        # Repositório JPA com query de filtros dinâmicos
│   └── services/
│       └── EventService.kt           # Regras de negócio com logs
└── participants/
    ├── controllers/
    │   └── ParticipantController.kt  # Endpoints /participants
    ├── dtos/
    │   ├── requests/
    │   │   ├── CreateParticipantRequest.kt
    │   │   └── UpdateParticipantRequest.kt
    │   └── responses/
    │       └── ParticipantResponse.kt
    ├── entities/
    │   └── Participant.kt            # Entidade Participant — tabela `participants` (N:1 com Event)
    ├── exceptions/
    │   ├── ParticipantNotFoundException.kt       # Exceção 404 customizada
    │   ├── ParticipantAlreadyLinkedException.kt  # Exceção 409 customizada
    │   └── ParticipantNotLinkedException.kt      # Exceção 400 customizada
    ├── repositories/
    │   └── ParticipantRepository.kt  # Repositório JPA
    └── services/
        └── ParticipantService.kt     # Regras de negócio com logs
```
---
## ⚙️ Configuração (`application.yaml`)
```yaml
server:
  port: ${PORT:8080}   # Sem context-path — rotas acessadas diretamente em /
spring:
  datasource:
    url: jdbc:h2:mem:db  # Banco em memória (perfil local) — dados perdidos ao reiniciar
```
> Para o perfil **dev** (AWS RDS / PostgreSQL), as variáveis de conexão são lidas de:  
> `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD`  
> Para ambientes produtivos, substitua o H2 por um banco persistente e atualize as variáveis de conexão.

---
## 📨 FakeSmsService — Simulação de envio de código
Durante o desenvolvimento, o código de confirmação **não é enviado por SMS real**. Ele é impresso no log da aplicação:
```
=== [FakeSMS] Sending confirmation code to phone 11999999999: CODE = 482931 ===
```
Basta verificar o terminal ou o arquivo `logs/spring.log` para obter o código durante os testes.
# rdv-wodbet-auth-server
