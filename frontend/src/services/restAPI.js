import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

export function extractErrorMessage(error, fallback = 'Произошла ошибка') {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    fallback
  )
}

export const authService = {
  register(login, password) {
    return api.post('/auth/register', { login, password })
  },

  login(login, password) {
    return api.post('/auth/login', { login, password })
  },

  logout() {
    return api.post('/auth/logout')
  },

  me() {
    return api.get('/auth/me')
  }
}

export const pointService = {
  addPoint(x, y, r) {
    return api.post('/points', { x, y, r })
  },

  getPoints() {
    return api.get('/points')
  },

  clearPoints() {
    return api.delete('/points')
  }
}

export default api
