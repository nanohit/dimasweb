import { createRouter, createWebHistory } from 'vue-router'
import Login from './pages/Login.vue'
import Main from './pages/Main.vue'
import { authService } from './services/restAPI'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login
  },
  {
    path: '/main',
    name: 'Main',
    component: Main,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (!to.meta.requiresAuth && to.path !== '/') {
    return true
  }

  try {
    const response = await authService.me()
    const login = response?.data?.login
    if (login) {
      localStorage.setItem('username', login)
    }

    if (to.path === '/') {
      return '/main'
    }
    return true
  } catch (error) {
    localStorage.removeItem('username')

    if (to.meta.requiresAuth) {
      return '/'
    }
    return true
  }
})

export default router
