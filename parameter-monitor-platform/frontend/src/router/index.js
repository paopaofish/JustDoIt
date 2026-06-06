import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/parameters',
    name: 'Parameters',
    component: () => import('../views/Parameters.vue')
  },
  {
    path: '/datasources',
    name: 'DataSources',
    component: () => import('../views/DataSources.vue')
  },
  {
    path: '/rules',
    name: 'Rules',
    component: () => import('../views/Rules.vue')
  },
  {
    path: '/alarms',
    name: 'Alarms',
    component: () => import('../views/Alarms.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
