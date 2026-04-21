# API Types

Spring generates the API contract at runtime through Springdoc OpenAPI.

Start the backend, then generate TypeScript in the frontend repo from:

```bash
npx openapi-typescript http://localhost:8080/v3/api-docs -o src/types/studi-api.ts
```

For a generated fetch client instead of only types:

```bash
npx @openapitools/openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-fetch \
  -o src/api/studi
```

The generated OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

## Auth Flow

Register or log in first:

```ts
const auth = await fetch("http://localhost:8080/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ username, password }),
}).then((res) => res.json());
```

The response includes:

```ts
type AuthResponse = {
  token: string;
  userId: number;
  username: string;
};
```

Use that token for every authenticated request:

```ts
const plan = await fetch("http://localhost:8080/study-plans", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${auth.token}`,
  },
  body: JSON.stringify({ goal: "Learn Spring Boot authentication" }),
}).then((res) => res.json());
```

Steps are created through study plan creation. There is no standalone create-step endpoint because a step only makes sense inside a plan.

Each authenticated user only sees their own study plans and steps. The backend gets the user id from the JWT, not from frontend request params.

## Mock Study Plan Workflow

Create a hardcoded study plan:

```ts
const plan = await fetch("http://localhost:8080/study-plans", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${auth.token}`,
  },
  body: JSON.stringify({ goal: "Learn Spring Boot authentication" }),
}).then((res) => res.json());
```

The backend currently uses a hardcoded mock generator in place of future inference. It creates:

- one `StudyPlan`
- multiple `Step` records linked to that plan
- a full response containing the plan and its steps
