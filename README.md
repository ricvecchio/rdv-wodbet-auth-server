# Spring Auth Server

Servidor de autenticação e autorização baseado em **JWT**, desenvolvido com **Kotlin + Spring Boot**. Permite gerenciar usuários e perfis de acesso com controle de permissões via roles.

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
| SpringDoc OpenAPI (Swagger) | 3.0.2 |

---

## 🚀 Como executar

```bash
./gradlew bootRun
```

A aplicação sobe em `http://localhost:8080/api`.

> **Swagger UI:** `http://localhost:8080/api`  
> **H2 Console:** `http://localhost:8080/api/h2-console` (usuário: `sa`, senha: `sa`)

---

## 🔐 Autenticação

A API utiliza **JWT Bearer Token**. Para acessar endpoints protegidos:

1. Faça login em `POST /api/users/login`
2. Use o token retornado no header: `Authorization: Bearer <token>`

| Perfil | Expiração do token |
|---|---|
| Usuário comum | 48 horas |
| ADMIN | 1 hora |

---

## 👤 Endpoints — Usuários (`/api/users`)

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `GET` | `/users` | Lista todos os usuários | Público |
| `GET` | `/users?role={role}` | Lista usuários por role | Público |
| `GET` | `/users?sortDir=ASC\|DESC` | Lista usuários ordenados por nome | Público |
| `GET` | `/users/{id}` | Busca usuário por ID | Público |
| `POST` | `/users` | Cria novo usuário | Público |
| `POST` | `/users/login` | Realiza login e retorna JWT | Público |
| `PATCH` | `/users/{id}` | Atualiza nome do usuário | Autenticado (próprio usuário ou ADMIN) |
| `DELETE` | `/users/{id}` | Remove usuário | ADMIN |
| `PUT` | `/users/{id}/roles/{role}` | Adiciona role ao usuário | ADMIN |

### Criar usuário — `POST /users`

```json
{
  "email": "usuario@email.com",
  "password": "Senha@123",
  "name": "Nome do Usuário"
}
```

### Login — `POST /users/login`

```json
{
  "email": "usuario@email.com",
  "password": "Senha@123"
}
```

**Resposta:**
```json
{
  "token": "<jwt>",
  "user": { "id": 1, "name": "Nome do Usuário", "email": "usuario@email.com" }
}
```

### Atualizar nome — `PATCH /users/{id}`

```json
{
  "name": "Novo Nome"
}
```

---

## 🏷️ Endpoints — Roles (`/api/roles`)

> Todos os endpoints de roles exigem autenticação com perfil **ADMIN**.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/roles` | Cria nova role |
| `GET` | `/roles` | Lista todas as roles |

### Criar role — `POST /roles`

```json
{
  "name": "PREMIUM",
  "description": "Usuário premium"
}
```

---

## 📋 Regras de Negócio

### Usuários
- **E-mail único:** não é possível cadastrar dois usuários com o mesmo e-mail.
- **Senha:** mínimo 8 caracteres, obrigatório conter letra, número e caractere especial (`@$!%*#?&`).
- **Atualização:** apenas o próprio usuário ou um ADMIN pode alterar o nome.
- **Remoção:** um ADMIN não pode ser deletado se for o único com essa role no sistema.

### Roles
- O nome da role é sempre convertido e armazenado em **maiúsculas**.
- Roles duplicadas não são permitidas.
- Somente ADMINs podem criar e listar roles.

### Inicialização automática (Bootstrapper)
Ao subir a aplicação, são criados automaticamente:

| Recurso | Valor padrão |
|---|---|
| Role | `ADMIN` |
| Role | `PREMIUM` |
| Usuário admin | `admin@authserver.com` / `admin` |

> ⚠️ **Atenção:** altere as credenciais padrão antes de usar em produção.

---

## 🔒 Regras de Segurança

- Sessão **stateless** (sem cookies/sessão no servidor).
- **CORS** liberado para todas as origens (configurar restrições em produção).
- **CSRF** desabilitado.
- Endpoints `GET` e criação de usuário/login são públicos; demais requerem autenticação.

---

## 📁 Estrutura do Projeto

```
src/main/kotlin/br/pucpr/authserver/
├── AuthserverApplication.kt      # Entry point
├── Bootstrapper.kt               # Dados iniciais (roles e admin padrão)
├── exceptions/                   # Exceções customizadas (400, 401, 403, 404)
├── roles/
│   ├── Role.kt                   # Entidade Role
│   ├── RoleController.kt         # Endpoints /roles
│   ├── RoleRepository.kt         # Repositório JPA
│   ├── RoleService.kt            # Regras de negócio de roles
│   ├── requests/CreateRoleRequest.kt
│   └── responses/RoleResponse.kt
├── security/
│   ├── JWT.kt                    # Geração e validação de tokens JWT
│   ├── JwtTokenFilter.kt         # Filtro de autenticação via JWT
│   ├── SecurityConfig.kt         # Configuração do Spring Security
│   └── UserToken.kt              # Representação do usuário no token
└── users/
    ├── User.kt                   # Entidade User
    ├── UserController.kt         # Endpoints /users
    ├── UserRepository.kt         # Repositório JPA
    ├── UserService.kt            # Regras de negócio de usuários
    ├── SortDir.kt                # Enum de direção de ordenação
    ├── requests/                 # DTOs de entrada
    └── responses/                # DTOs de saída
```

---

## ⚙️ Configuração (`application.yaml`)

```yaml
server:
  servlet:
    context-path: /api   # Todas as rotas prefixadas com /api

spring:
  datasource:
    url: jdbc:h2:mem:db  # Banco em memória (dados perdidos ao reiniciar)
```

> Para ambientes produtivos, substitua o H2 por um banco persistente (PostgreSQL, MySQL, etc.) e atualize as variáveis de conexão.
