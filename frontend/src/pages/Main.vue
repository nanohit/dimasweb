<template>
  <div class="main-page">
    <div class="grain" aria-hidden="true"></div>

    <header class="main-header">
      <div>
        <p class="eyebrow">MAIN PAGE</p>
        <h1>Проверка точек</h1>
      </div>

      <div class="header-actions">
        <span class="user-chip">Пользователь: {{ currentUser }}</span>
        <button class="logout-btn" type="button" @click="logout">Завершить сессию</button>
      </div>
    </header>

    <main class="main-content">
      <section class="control-panel">
        <h2>Параметры точки</h2>

        <fieldset class="field-block">
          <legend>X</legend>
          <div class="radio-grid x-grid">
            <label v-for="value in xValues" :key="`x-${value}`">
              <input v-model.number="form.x" type="radio" name="x" :value="value" />
              <span>{{ value }}</span>
            </label>
          </div>
        </fieldset>

        <div class="field-block">
          <label for="y-input">Y (текст, от -3 до 3)</label>
          <input
            id="y-input"
            v-model.trim="form.y"
            type="text"
            inputmode="decimal"
            placeholder="Например: 1.5"
            @input="touchY = true"
          />
          <p v-if="touchY && yError" class="field-error">{{ yError }}</p>
        </div>

        <fieldset class="field-block">
          <legend>R</legend>
          <div class="radio-grid r-grid">
            <label v-for="value in rValues" :key="`r-${value}`">
              <input v-model.number="form.r" type="radio" name="r" :value="value" />
              <span>{{ value }}</span>
            </label>
          </div>
          <p v-if="rError" class="field-error">{{ rError }}</p>
        </fieldset>

        <div class="button-row">
          <button type="button" class="primary-btn" :disabled="submitting" @click="checkPoint">
            {{ submitting ? 'Отправка...' : 'Проверить точку' }}
          </button>
          <button type="button" class="ghost-btn" :disabled="clearing" @click="clearHistory">
            {{ clearing ? 'Очистка...' : 'Очистить историю' }}
          </button>
        </div>

        <p v-if="statusMessage" class="status-message">{{ statusMessage }}</p>
      </section>

      <section class="visual-panel">
        <article class="graph-card">
          <header>
            <h2>Область и точки</h2>
            <p>Клик по графику отправляет новую точку</p>
          </header>

          <canvas
            ref="canvas"
            :width="canvasSize"
            :height="canvasSize"
            @click="handleCanvasClick"
          ></canvas>

          <div class="legend">
            <span><i class="dot hit"></i>Попадание</span>
            <span><i class="dot miss"></i>Промах</span>
          </div>
        </article>

        <article class="table-card">
          <header>
            <h2>История проверок</h2>
            <p>Все сохранённые результаты текущего пользователя</p>
          </header>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>X</th>
                  <th>Y</th>
                  <th>R</th>
                  <th>Результат</th>
                  <th>Время</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="point in points" :key="point.id">
                  <td>{{ formatNumber(point.x) }}</td>
                  <td>{{ formatNumber(point.y) }}</td>
                  <td>{{ formatNumber(point.r) }}</td>
                  <td :class="point.result ? 'hit-text' : 'miss-text'">
                    {{ point.result ? 'Попадание' : 'Промах' }}
                  </td>
                  <td>{{ formatDate(point.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
            <p v-if="!points.length" class="empty-state">История пока пуста</p>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script>
import { authService, pointService, extractErrorMessage } from '../services/restAPI'

export default {
  name: 'MainPage',
  data() {
    return {
      currentUser: localStorage.getItem('username') || '',
      xMin: -3,
      xMax: 5,
      yMin: -3,
      yMax: 3,
      xValues: [-3, -2, -1, 0, 1, 2, 3, 4, 5],
      rValues: [-3, -2, -1, 0, 1, 2, 3, 4, 5],
      form: {
        x: 0,
        y: '',
        r: 1
      },
      touchY: false,
      points: [],
      canvasSize: 450,
      submitting: false,
      clearing: false,
      statusMessage: ''
    }
  },
  computed: {
    normalizedY() {
      const source = String(this.form.y).replace(',', '.')
      if (!source.length) {
        return null
      }
      const parsed = Number(source)
      return Number.isFinite(parsed) ? parsed : null
    },
    yError() {
      if (this.form.y === '') {
        return 'Введите значение Y'
      }
      if (this.normalizedY === null) {
        return 'Y должен быть числом'
      }
      if (this.normalizedY < this.yMin || this.normalizedY > this.yMax) {
        return `Y должен быть в диапазоне от ${this.yMin} до ${this.yMax}`
      }
      return ''
    },
    rError() {
      if (typeof this.form.r !== 'number' || Number.isNaN(this.form.r)) {
        return 'Выберите значение R'
      }
      if (this.form.r < 0) {
        return 'R не может быть отрицательным'
      }
      return ''
    },
    canSubmit() {
      return !this.yError && !this.rError && this.form.x >= this.xMin && this.form.x <= this.xMax
    }
  },
  watch: {
    'form.r'() {
      this.drawGraph()
    },
    points() {
      this.drawGraph()
    }
  },
  async mounted() {
    await this.syncUser()
    this.updateCanvasSize()
    window.addEventListener('resize', this.updateCanvasSize)
    await this.loadPoints()
    this.drawGraph()
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.updateCanvasSize)
  },
  methods: {
    async syncUser() {
      try {
        const response = await authService.me()
        this.currentUser = response?.data?.login || this.currentUser
        if (this.currentUser) {
          localStorage.setItem('username', this.currentUser)
        }
      } catch (error) {
        localStorage.removeItem('username')
        await this.$router.replace('/')
      }
    },
    updateCanvasSize() {
      const width = window.innerWidth
      if (width >= 1204) {
        this.canvasSize = 450
      } else if (width >= 878) {
        this.canvasSize = 380
      } else {
        this.canvasSize = 300
      }
      this.$nextTick(() => this.drawGraph())
    },
    async loadPoints() {
      try {
        const response = await pointService.getPoints()
        this.points = response?.data || []
      } catch (error) {
        this.statusMessage = extractErrorMessage(error, 'Не удалось загрузить историю точек')
        if (error?.response?.status === 401) {
          await this.$router.replace('/')
        }
      }
    },
    async checkPoint() {
      this.touchY = true
      this.statusMessage = ''

      if (!this.canSubmit) {
        this.statusMessage = 'Исправьте ошибки в форме перед отправкой'
        return
      }

      this.submitting = true

      try {
        await this.submitPoint(this.form.x, this.normalizedY, this.form.r)
      } catch (error) {
        this.statusMessage = extractErrorMessage(error, 'Не удалось проверить точку')
      } finally {
        this.submitting = false
      }
    },
    async clearHistory() {
      this.statusMessage = ''
      this.clearing = true

      try {
        await pointService.clearPoints()
        this.points = []
      } catch (error) {
        this.statusMessage = extractErrorMessage(error, 'Не удалось очистить историю')
      } finally {
        this.clearing = false
      }
    },
    async logout() {
      try {
        await authService.logout()
      } catch (error) {
        // Локально завершаем сессию на клиенте даже при сетевой ошибке.
      } finally {
        localStorage.removeItem('username')
        await this.$router.push('/')
      }
    },
    async submitPoint(x, y, r) {
      const response = await pointService.addPoint(x, y, r)
      this.points = [response.data, ...this.points]
      this.drawGraph()
      return response
    },
    handleCanvasClick(event) {
      const r = Number(this.form.r)
      if (!Number.isFinite(r) || r <= 0) {
        this.statusMessage = 'Для клика по графику выберите R больше 0'
        return
      }

      const canvas = this.$refs.canvas
      const rect = canvas.getBoundingClientRect()
      const center = this.canvasSize / 2
      const scale = this.canvasSize / 12

      const canvasX = event.clientX - rect.left
      const canvasY = event.clientY - rect.top

      const x = Number(((canvasX - center) / scale).toFixed(3))
      const y = Number(((center - canvasY) / scale).toFixed(3))

      if (x < this.xMin || x > this.xMax || y < this.yMin || y > this.yMax) {
        this.statusMessage = `Координаты клика должны быть в диапазонах X:[${this.xMin}, ${this.xMax}], Y:[${this.yMin}, ${this.yMax}]`
        return
      }

      this.form.y = y.toFixed(2)
      this.touchY = true
      this.statusMessage = ''

      this.submitting = true
      this.submitPoint(x, y, r)
        .catch((error) => {
          this.statusMessage = extractErrorMessage(error, 'Не удалось отправить точку по клику')
        })
        .finally(() => {
          this.submitting = false
        })
    },
    drawGraph() {
      const canvas = this.$refs.canvas
      if (!canvas) {
        return
      }

      const ctx = canvas.getContext('2d')
      const size = this.canvasSize
      const center = size / 2
      const scale = size / 12
      const r = Number(this.form.r)

      ctx.clearRect(0, 0, size, size)
      ctx.fillStyle = '#fffef8'
      ctx.fillRect(0, 0, size, size)

      this.drawAxes(ctx, size, center, scale)

      if (Number.isFinite(r) && r > 0) {
        this.drawArea(ctx, center, scale, r)
      }

      this.drawPoints(ctx, center, scale)
    },
    drawAxes(ctx, size, center, scale) {
      ctx.strokeStyle = '#1f2430'
      ctx.lineWidth = 1

      ctx.beginPath()
      ctx.moveTo(0, center)
      ctx.lineTo(size, center)
      ctx.moveTo(center, 0)
      ctx.lineTo(center, size)
      ctx.stroke()

      ctx.fillStyle = '#1f2430'
      ctx.beginPath()
      ctx.moveTo(size - 12, center - 4)
      ctx.lineTo(size, center)
      ctx.lineTo(size - 12, center + 4)
      ctx.fill()

      ctx.beginPath()
      ctx.moveTo(center - 4, 12)
      ctx.lineTo(center, 0)
      ctx.lineTo(center + 4, 12)
      ctx.fill()

      ctx.font = "11px 'IBM Plex Mono', monospace"
      ctx.fillStyle = '#5f6570'
      for (let i = -5; i <= 5; i += 1) {
        if (i === 0) continue

        const x = center + i * scale
        const y = center - i * scale

        ctx.beginPath()
        ctx.moveTo(x, center - 4)
        ctx.lineTo(x, center + 4)
        ctx.moveTo(center - 4, y)
        ctx.lineTo(center + 4, y)
        ctx.stroke()

        ctx.textAlign = 'center'
        ctx.textBaseline = 'top'
        ctx.fillText(String(i), x, center + 7)

        ctx.textAlign = 'right'
        ctx.textBaseline = 'middle'
        ctx.fillText(String(i), center - 8, y)
      }
    },
    drawArea(ctx, center, scale, r) {
      const areaColor = 'rgba(233, 111, 47, 0.28)'
      const strokeColor = 'rgba(190, 86, 30, 0.95)'

      ctx.fillStyle = areaColor
      ctx.strokeStyle = strokeColor
      ctx.lineWidth = 1.5

      ctx.beginPath()

      // I quadrant: rectangle x in [0, r], y in [0, r/2].
      ctx.rect(center, center - (r * scale) / 2, r * scale, (r * scale) / 2)

      // II quadrant: quarter circle with radius r.
      ctx.moveTo(center, center)
      ctx.arc(center, center, r * scale, Math.PI, Math.PI * 1.5, false)
      ctx.closePath()

      // III quadrant: triangle with points (0,0), (-r,0), (0,-r/2).
      ctx.moveTo(center, center)
      ctx.lineTo(center - r * scale, center)
      ctx.lineTo(center, center + (r * scale) / 2)
      ctx.closePath()

      // IV quadrant: triangle with points (0,0), (r,0), (0,-r).
      ctx.moveTo(center, center)
      ctx.lineTo(center + r * scale, center)
      ctx.lineTo(center, center + r * scale)
      ctx.closePath()

      ctx.fill()
      ctx.stroke()
    },
    drawPoints(ctx, center, scale) {
      this.points.forEach((point) => {
        const px = center + Number(point.x) * scale
        const py = center - Number(point.y) * scale

        ctx.beginPath()
        ctx.arc(px, py, 4, 0, Math.PI * 2)
        ctx.fillStyle = point.result ? '#16a34a' : '#dc2626'
        ctx.fill()
        ctx.strokeStyle = '#ffffff'
        ctx.lineWidth = 1.2
        ctx.stroke()
      })
    },
    formatNumber(value) {
      return Number(value).toFixed(2)
    },
    formatDate(value) {
      if (!value) {
        return '—'
      }
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      return date.toLocaleString('ru-RU')
    }
  }
}
</script>

<style scoped>
.main-page {
  --ink: #1f2430;
  --paper: #fffdf8;
  --panel: #fff9ef;
  --line: rgba(31, 36, 48, 0.18);
  --accent: #e96f2f;
  --accent-dark: #b44f1d;
  --ok: #15803d;
  --bad: #b42318;

  min-height: 100vh;
  padding: 20px;
  color: var(--ink);
  background:
    radial-gradient(circle at 85% 10%, rgba(59, 98, 188, 0.18), transparent 30%),
    radial-gradient(circle at 12% 72%, rgba(233, 111, 47, 0.16), transparent 36%),
    linear-gradient(155deg, #f7f2e6, #f0e8d8 48%, #f7f0e2);
}

.grain {
  position: fixed;
  inset: 0;
  pointer-events: none;
  opacity: 0.12;
  background-image: radial-gradient(#1f2430 0.4px, transparent 0.4px);
  background-size: 4px 4px;
}

.main-header {
  position: relative;
  z-index: 1;
  width: min(1280px, 100%);
  margin: 0 auto 16px;
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.eyebrow {
  margin: 0;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 12px;
  color: #6a7180;
  letter-spacing: 0.14em;
}

.main-header h1 {
  margin: 8px 0 0;
  font-size: clamp(24px, 3.5vw, 34px);
  line-height: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-chip {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 7px 12px;
  background: #ffffff;
  font-size: 14px;
}

.logout-btn {
  border: none;
  border-radius: 10px;
  padding: 9px 14px;
  font-weight: 700;
  color: #ffffff;
  background: linear-gradient(120deg, var(--accent), var(--accent-dark));
  cursor: pointer;
}

.main-content {
  position: relative;
  z-index: 1;
  width: min(1280px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 16px;
}

.control-panel,
.graph-card,
.table-card {
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(21, 20, 14, 0.07);
}

.control-panel {
  padding: 18px;
}

.control-panel h2,
.graph-card h2,
.table-card h2 {
  margin: 0 0 8px;
  font-size: 20px;
}

.field-block {
  margin: 14px 0;
  border: none;
  padding: 0;
}

.field-block legend,
.field-block label {
  font-family: 'IBM Plex Mono', monospace;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
  color: #646c7d;
  margin-bottom: 8px;
  display: block;
}

.field-block input[type='text'] {
  width: 100%;
  border: 1px solid rgba(31, 36, 48, 0.25);
  border-radius: 10px;
  padding: 12px;
  font-size: 15px;
  background: #fffefb;
}

.field-block input[type='text']:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(233, 111, 47, 0.16);
}

.radio-grid {
  display: grid;
  gap: 8px;
}

.x-grid,
.r-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.radio-grid label {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: #fffaf0;
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}

.radio-grid input {
  margin: 0;
}

.field-error {
  margin: 8px 0 0;
  color: var(--bad);
  font-size: 14px;
}

.button-row {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.primary-btn,
.ghost-btn {
  border: none;
  border-radius: 10px;
  padding: 11px 14px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn {
  color: #ffffff;
  background: linear-gradient(120deg, var(--accent), var(--accent-dark));
}

.ghost-btn {
  border: 1px solid var(--line);
  background: #fff9ef;
  color: var(--ink);
}

.primary-btn:disabled,
.ghost-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.status-message {
  margin: 12px 0 0;
  font-size: 14px;
  color: var(--bad);
}

.visual-panel {
  display: grid;
  gap: 16px;
}

.graph-card,
.table-card {
  padding: 16px;
}

.graph-card header p,
.table-card header p {
  margin: 0 0 12px;
  color: #596172;
  font-size: 14px;
}

canvas {
  display: block;
  margin: 0 auto;
  border: 1px solid var(--line);
  border-radius: 12px;
  cursor: crosshair;
  background: #fffef8;
}

.legend {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 14px;
  color: #48505e;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.dot.hit {
  background: var(--ok);
}

.dot.miss {
  background: var(--bad);
}

.table-wrap {
  max-height: 320px;
  overflow: auto;
  border: 1px solid var(--line);
  border-radius: 10px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 10px;
  text-align: center;
  border-bottom: 1px solid var(--line);
  font-size: 14px;
}

th {
  position: sticky;
  top: 0;
  background: #fff7ea;
  font-family: 'IBM Plex Mono', monospace;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.hit-text {
  color: var(--ok);
  font-weight: 700;
}

.miss-text {
  color: var(--bad);
  font-weight: 700;
}

.empty-state {
  margin: 14px;
  text-align: center;
  color: #6b7381;
  font-size: 14px;
}

@media (min-width: 878px) and (max-width: 1203px) {
  .main-page {
    padding: 14px;
  }

  .main-content {
    grid-template-columns: 280px 1fr;
    gap: 12px;
  }

  .x-grid,
  .r-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  th,
  td {
    padding: 8px;
    font-size: 13px;
  }
}

@media (max-width: 877px) {
  .main-page {
    padding: 10px;
  }

  .main-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 14px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .main-content {
    grid-template-columns: 1fr;
  }

  .x-grid,
  .r-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .control-panel,
  .graph-card,
  .table-card {
    border-radius: 14px;
    padding: 14px;
  }

  th,
  td {
    padding: 7px;
    font-size: 12px;
  }
}
</style>
