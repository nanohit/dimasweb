# Web4

Учебное приложение на `Spring Boot + Vue.js`:
- backend: REST API + H2 + Spring Data JPA;
- frontend: Vue 3, 2 страницы (стартовая и основная);
- авторизация через HTTP-сессию;
- проверка точки на попадание в область и история результатов.

## Требования

- Java **17+ JDK** (нужен именно JDK, не только JRE)
- Node.js **18+**
- npm

## Запуск backend

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd spring-boot:run
```

Backend по умолчанию: `http://localhost:8080`

## Запуск frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend по умолчанию: `http://localhost:5173`

## Тесты backend

```bash
./mvnw test
```

Windows:

```powershell
mvnw.cmd test
```
