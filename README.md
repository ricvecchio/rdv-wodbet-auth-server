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
## 📦 Coleção de testes e roteiro no Insomnia
A collection `spring-auth-server.postman_collection.json` documenta os fluxos do projeto e já traz os retornos esperados para validação manual.

### Variáveis da collection
- `baseUrl` → `http://localhost:8080`
- `token` → preenchida automaticamente no login legado via script de teste
- `userId` → usado nos endpoints de usuário
- `eventId` → usado nos endpoints de evento
- `participantId` → usado nos endpoints de participante

### Como importar
1. Importe o arquivo `spring-auth-server.postman_collection.json` no Postman.
2. Crie o environment no Insomnia com as variáveis abaixo:

```json
{
  "baseUrl": "http://localhost:8080",
  "phone": "11999999999",
  "uuid": "uuid-dispositivo-teste",
  "code": "123456",
  "userId": "1",
  "token": "",
  "eventId": "1",
  "participantId": "1"
}
```

### Fluxos e retornos documentados
| Fluxo / Endpoint | Método | Resultado esperado |
|---|---|---|
| `POST /users/login` (iOS) | `POST` | `200 OK` se o usuário já existir e estiver ativo; `202 Accepted` quando um novo código é gerado e enviado no log (`FakeSms`) |
| `POST /users/confirm` (iOS) | `POST` | `200 OK` retorna o usuário; `404 Not Found` quando não existe código pendente; `400 Bad Request` quando o código é inválido ou expirou |
| `PUT /users/{id}` (iOS) | `PUT` | `200 OK` com o perfil atualizado |
| `POST /users` (legado) | `POST` | `201 Created` com role `USER` atribuída automaticamente |
| `POST /users/login` (legado) | `POST` | `200 OK` com `{ token, user }` e script salvando `{{token}}` |
| `POST /roles` | `POST` | `201 Created` com a role criada |
| `POST /events` | `POST` | `201 Created` com o evento criado |
| `POST /events/{eventId}/participants/{participantId}` | `POST` | `409 Conflict` se o participante já estiver vinculado ao mesmo evento |
| `DELETE /events/{eventId}/participants/{participantId}` | `DELETE` | `400 Bad Request` se o participante não estiver vinculado ao evento |
| `POST /participants` | `POST` | `201 Created` com o participante criado |

---
## 🔐 Autenticação e roteiros de teste

### Fluxo iOS — telefone + UUID + código
O app iOS utiliza um fluxo sem senha baseado em **telefone + UUID + código de confirmação**. Nenhum JWT é retornado nesse fluxo; a API retorna o usuário diretamente.

#### Roteiro no Insomnia
**1. Configurar Environment**

Use o environment acima e mantenha `phone`, `uuid`, `code`, `userId` e `token` atualizáveis durante o teste.

**2. Testar fluxo iOS**

**Request 1 — Login por telefone**
- `POST {{ _.baseUrl }}/users/login`
- Header: `Content-Type: application/json`
- Body:
```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}"
}
```
- Resultado esperado: `202 Accepted`
- Depois, copie o código do log do backend e atualize a variável `code` no Insomnia.

**Request 2 — Confirmar código**
- `POST {{ _.baseUrl }}/users/confirm`
- Header: `Content-Type: application/json`
- Body:
```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}",
  "code": "{{ _.code }}"
}
```
- Resultado esperado: `200 OK`
- A resposta deve retornar o usuário. Copie o `id` retornado e atualize a variável `userId`.

**Request 3 — Login direto**
- Repita `POST {{ _.baseUrl }}/users/login` com o mesmo `phone` e `uuid`.
- Resultado esperado: `200 OK`
- Agora não deve pedir código novamente.

**Request 4 — Atualizar perfil**
- `PUT {{ _.baseUrl }}/users/{{ _.userId }}`
- Header: `Content-Type: application/json`
- Body:
```json
{
  "name": "Ricardo",
  "description": "Atleta e desenvolvedor",
  "phone": null
}
```
- Resultado esperado: `200 OK`

#### Testes de erro importantes
**Código inválido**
```json
{
  "phone": "{{ _.phone }}",
  "uuid": "{{ _.uuid }}",
  "code": "000000"
}
```
Esperado: `400 Bad Request`

**Código inexistente**

Use outro UUID:
```json
{
  "phone": "{{ _.phone }}",
  "uuid": "uuid-sem-codigo",
  "code": "123456"
}
```
Esperado: `404 Not Found`

**UUID diferente**

No login, use:
```json
{
  "phone": "{{ _.phone }}",
  "uuid": "novo-uuid-teste"
}
```
Esperado: `202 Accepted`

Depois confirme com o novo código do log.

#### Ordem recomendada
1. Login telefone → `202`
2. Copiar código do log
3. Confirmar código → `200`
4. Copiar `userId`
5. Login telefone novamente → `200`
6. Atualizar perfil → `200`
7. Testar código inválido → `400`
8. Testar código inexistente → `404`
9. Testar UUID diferente → `202`

### Fluxo legado — JWT
O fluxo clássico usa **e-mail + senha + JWT Bearer Token**.

1. Crie um usuário em `POST /users`.
2. Faça login em `POST /users/login` com e-mail e senha.
3. Se retornar `token`, salve-o na variável `token`.
4. Use o header `Authorization: Bearer {{token}}` nos endpoints protegidos.

| Perfil | Expiração do token |
|---|---|
| Usuário comum | 48 horas |
| ADMIN | 1 hora |

### Endpoints e exemplos de teste
#### Usuários (`/users`)
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `GET` | `/users` | Lista todos os usuários | Público |
| `GET` | `/users?role={role}` | Lista usuários por role | Público |
| `GET` | `/users?sortDir=ASC\|DESC` | Lista usuários ordenados por nome | Público |
| `GET` | `/users/{id}` | Busca usuário por ID | Público |
| `POST` | `/users` | Cria novo usuário (fluxo legado) | Público |
| `POST` | `/users/login` | Login por telefone + uuid (iOS) ou e-mail + senha (legado) | Público |
| `POST` | `/users/confirm` | Confirma código e retorna usuário (iOS) | Público |
| `PUT` | `/users/{id}` | Atualiza nome e descrição do perfil (iOS) | Público |
| `PATCH` | `/users/{id}` | Atualiza nome do usuário (legado) | Autenticado (próprio usuário ou ADMIN) |
| `DELETE` | `/users/{id}` | Remove usuário | ADMIN |
| `PUT` | `/users/{id}/roles/{role}` | Adiciona role ao usuário | ADMIN |

##### Criar usuário — `POST /users` (legado)
```json
{
  "email": "usuario@email.com",
  "password": "Senha@123",
  "name": "Nome do Usuário"
}
```
- Resultado esperado: `201 Created`
- A role `USER` é atribuída automaticamente.

##### Login por telefone — `POST /users/login` (iOS)
```json
{
  "phone": "11999999999",
  "uuid": "uuid-dispositivo"
}
```
- `200 OK` → usuário já cadastrado e ativo, retorna `BackendUserResponse`
- `202 Accepted` → código de confirmação enviado via log (`FakeSms`)

##### Confirmar código — `POST /users/confirm` (iOS)
```json
{
  "phone": "11999999999",
  "uuid": "uuid-dispositivo",
  "code": "123456"
}
```
- `200 OK` → cria/atualiza o usuário e retorna `BackendUserResponse`
- `404 Not Found` → nenhum código pendente para `phone + uuid`
- `400 Bad Request` → código inválido ou expirado

##### Atualizar perfil — `PUT /users/{id}` (iOS)
```json
{
  "name": "Ricardo",
  "description": "texto opcional",
  "phone": null
}
```
- `200 OK` → mesmo formato de `BackendUserResponse`

##### Atualizar nome — `PATCH /users/{id}` (legado)
```json
{
  "name": "Novo Nome"
}
```

##### Resposta legada de usuário — `UserResponse`
```json
{
  "id": 1,
  "email": "usuario@email.com",
  "name": "Nome do Usuário",
  "roles": ["USER"]
}
```

#### Roles (`/roles`)
Todos os endpoints de roles exigem autenticação com perfil **ADMIN**.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/roles` | Cria nova role |
| `GET` | `/roles` | Lista todas as roles |

##### Criar role — `POST /roles`
```json
{
  "name": "MODERATOR",
  "description": "Moderador de conteúdo"
}
```
- Resultado esperado: `201 Created`

---
## 📅 Endpoints — Eventos (`/events`)

---
## 🏷️ Endpoints — Roles (`/roles`)
Todos os endpoints de roles exigem autenticação com perfil **ADMIN**.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/roles` | Cria nova role |
| `GET` | `/roles` | Lista todas as roles |

### Criar role — `POST /roles`
```json
{
  "name": "MODERATOR",
  "description": "Moderador de conteúdo"
}
```
---
## 📅 Endpoints — Eventos (`/events`)

### Tabela no banco: `events`
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

### Filtros e ordenação — `GET /events`
| Parâmetro | Tipo | Descrição | Exemplo |
|---|---|---|---|
| `name` | string | Filtra por nome (parcial, case-insensitive) | `name=java` |
| `status` | enum | Filtra por status | `status=SCHEDULED` |
| `location` | string | Filtra por local (parcial, case-insensitive) | `location=curitiba` |
| `startDate` | ISO datetime | Eventos a partir desta data | `startDate=2026-01-01T00:00:00` |
| `endDate` | ISO datetime | Eventos até esta data | `endDate=2026-12-31T23:59:59` |
| `sortBy` | string | Campo de ordenação (`name`, `eventDate`, `createdAt`, `location`) | `sortBy=eventDate` |
| `direction` | string | Direção da ordenação (`ASC` ou `DESC`) | `direction=ASC` |

**Exemplo completo:**
```
GET /events?name=java&status=SCHEDULED&location=curitiba&sortBy=eventDate&direction=ASC
```

### Status do evento
| Status | Descrição |
|---|---|
| `SCHEDULED` | Evento agendado (padrão) |
| `CANCELLED` | Evento cancelado |
| `FINISHED` | Evento finalizado |

### Criar evento — `POST /events`
```json
{
  "name": "Java Summit 2026",
  "description": "Conferência sobre Java e JVM",
  "location": "Curitiba, PR",
  "eventDate": "2026-09-15T09:00:00",
  "status": "SCHEDULED"
}
```
### Atualizar evento — `PATCH /events/{id}`
Todos os campos são opcionais:
```json
{
  "name": "Novo Nome",
  "location": "São Paulo, SP",
  "status": "CANCELLED"
}
```
---
## 👥 Endpoints — Participantes (`/participants`)

### Tabela no banco: `participants`
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/participants` | Cria novo participante | Autenticado |
| `GET` | `/participants` | Lista todos os participantes | Público |
| `GET` | `/participants?sortDir=ASC\|DESC` | Lista participantes ordenados por nome | Público |
| `GET` | `/participants/{id}` | Busca participante por ID | Público |
| `PATCH` | `/participants/{id}` | Atualiza dados do participante | Autenticado |
| `DELETE` | `/participants/{id}` | Remove participante | ADMIN |

### Criar participante — `POST /participants`
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "phone": "(41) 99999-0000"
}
```
### Atualizar participante — `PATCH /participants/{id}`
Todos os campos são opcionais:
```json
{
  "name": "João Santos",
  "phone": "(41) 98888-1111"
}
```
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
