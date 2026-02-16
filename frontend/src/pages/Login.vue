<template>
  <div class="login-page">
    <div class="ambient" aria-hidden="true"></div>

    <main class="login-card">
      <header class="hero">
        <p class="label">START PAGE</p>
        <h1>Проверка попадания точки в область</h1>
        <p class="subtitle">
          Авторизуйтесь, чтобы перейти к основной странице приложения.
        </p>
      </header>

      <section class="student-header">
        <h2>Информация о студенте</h2>
        <dl>
          <div>
            <dt>ФИО</dt>
            <dd>{{ studentInfo.fullName }}</dd>
          </div>
          <div>
            <dt>Группа</dt>
            <dd>{{ studentInfo.group }}</dd>
          </div>
          <div>
            <dt>Вариант</dt>
            <dd>{{ studentInfo.variant }}</dd>
          </div>
        </dl>
      </section>

      <section class="auth-box">
        <div class="auth-switch">
          <button
            type="button"
            :class="{ active: mode === 'login' }"
            @click="mode = 'login'"
          >
            Вход
          </button>
          <button
            type="button"
            :class="{ active: mode === 'register' }"
            @click="mode = 'register'"
          >
            Регистрация
          </button>
        </div>

        <form class="auth-form" @submit.prevent="submit">
          <label for="login">Логин</label>
          <input
            id="login"
            v-model.trim="form.login"
            type="text"
            minlength="3"
            maxlength="32"
            required
            autocomplete="username"
            placeholder="Введите логин"
          />

          <label for="password">Пароль</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            minlength="4"
            maxlength="128"
            required
            autocomplete="current-password"
            placeholder="Введите пароль"
          />

          <button class="submit-btn" type="submit" :disabled="loading">
            {{ loading ? 'Подождите...' : mode === 'login' ? 'Войти' : 'Создать аккаунт' }}
          </button>

          <p v-if="error" class="error-message">{{ error }}</p>
        </form>
      </section>
    </main>
  </div>
</template>

<script>
import { authService, extractErrorMessage } from '../services/restAPI'

export default {
  name: 'LoginPage',
  data() {
    return {
      mode: 'login',
      loading: false,
      error: '',
      form: {
        login: '',
        password: ''
      },
      studentInfo: {
        fullName: 'Укажите ФИО студента',
        group: 'Укажите номер группы',
        variant: 'Укажите номер варианта'
      }
    }
  },
  methods: {
    async submit() {
      this.loading = true
      this.error = ''

      try {
        if (this.mode === 'login') {
          await authService.login(this.form.login, this.form.password)
        } else {
          await authService.register(this.form.login, this.form.password)
        }

        localStorage.setItem('username', this.form.login)
        await this.$router.push('/main')
      } catch (error) {
        this.error = extractErrorMessage(
          error,
          this.mode === 'login'
            ? 'Не удалось выполнить вход'
            : 'Не удалось зарегистрировать пользователя'
        )
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  --paper: #fffdf9;
  --ink: #1f2430;
  --accent: #e96f2f;
  --accent-dark: #c8521b;
  --line: rgba(31, 36, 48, 0.16);
  --error: #d92d20;

  min-height: 100vh;
  padding: 32px 20px;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at 12% 18%, rgba(233, 111, 47, 0.28), transparent 28%),
    radial-gradient(circle at 88% 12%, rgba(59, 98, 188, 0.24), transparent 25%),
    linear-gradient(130deg, #f4f0e7, #efe7d7 58%, #f6ebdd);
  color: var(--ink);
}

.ambient {
  position: fixed;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(
      to right,
      rgba(31, 36, 48, 0.03) 1px,
      transparent 1px
    ),
    linear-gradient(to bottom, rgba(31, 36, 48, 0.03) 1px, transparent 1px);
  background-size: 34px 34px;
}

.login-card {
  position: relative;
  z-index: 1;
  width: min(980px, 100%);
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 26px;
  box-shadow:
    0 24px 40px rgba(20, 16, 12, 0.14),
    0 4px 10px rgba(20, 16, 12, 0.08);
  padding: 36px;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  grid-template-areas:
    'hero auth'
    'student auth';
  gap: 22px 34px;
}

.hero {
  grid-area: hero;
}

.label {
  margin: 0;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #6d7480;
}

.hero h1 {
  margin: 10px 0 12px;
  font-size: clamp(30px, 4.5vw, 44px);
  line-height: 1.05;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  max-width: 50ch;
  color: #545d6d;
}

.student-header {
  grid-area: student;
  padding: 16px 18px;
  border-radius: 16px;
  border: 1px dashed rgba(31, 36, 48, 0.25);
  background: #fff7ef;
}

.student-header h2 {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 700;
}

dl {
  margin: 0;
  display: grid;
  gap: 10px;
}

dl div {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 12px;
}

dt {
  font-family: 'IBM Plex Mono', monospace;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #6d7480;
}

dd {
  margin: 0;
  font-weight: 600;
}

.auth-box {
  grid-area: auth;
  border: 1px solid var(--line);
  border-radius: 18px;
  padding: 16px;
  background: #ffffff;
  align-self: start;
}

.auth-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 14px;
}

.auth-switch button {
  border: 1px solid var(--line);
  background: #f5f4f1;
  border-radius: 999px;
  padding: 10px 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.auth-switch button.active {
  background: var(--ink);
  color: #ffffff;
  border-color: var(--ink);
}

.auth-form {
  display: grid;
  gap: 8px;
}

.auth-form label {
  margin-top: 6px;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6d7480;
}

.auth-form input {
  width: 100%;
  border: 1px solid rgba(31, 36, 48, 0.22);
  border-radius: 10px;
  padding: 12px;
  font-size: 15px;
  color: var(--ink);
  background: #fffdfb;
}

.auth-form input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(233, 111, 47, 0.16);
}

.submit-btn {
  margin-top: 10px;
  border: none;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 15px;
  font-weight: 700;
  color: #ffffff;
  background: linear-gradient(120deg, var(--accent), var(--accent-dark));
  cursor: pointer;
  transition: transform 0.16s ease;
}

.submit-btn:hover:enabled {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.error-message {
  margin: 8px 0 0;
  color: var(--error);
  font-size: 14px;
}

@media (min-width: 878px) and (max-width: 1203px) {
  .login-card {
    width: min(840px, 100%);
    grid-template-columns: 1fr;
    grid-template-areas:
      'hero'
      'student'
      'auth';
    padding: 28px;
    gap: 16px;
  }

  .hero h1 {
    font-size: clamp(28px, 4.8vw, 36px);
  }
}

@media (max-width: 877px) {
  .login-page {
    padding: 14px;
  }

  .login-card {
    width: 100%;
    border-radius: 18px;
    padding: 18px;
    grid-template-columns: 1fr;
    grid-template-areas:
      'hero'
      'student'
      'auth';
    gap: 14px;
  }

  .hero h1 {
    font-size: clamp(24px, 8.5vw, 30px);
  }

  dl div {
    grid-template-columns: 1fr;
    gap: 4px;
  }

  dt {
    font-size: 11px;
  }
}
</style>
