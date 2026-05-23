import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('./views/Dashboard.vue'),
    meta: { title: '监督驾驶舱' },
  },
  {
    path: '/parameters',
    name: 'Parameters',
    component: () => import('./views/Parameters.vue'),
    meta: { title: '参数管理' },
  },
  {
    path: '/data-sources',
    name: 'DataSources',
    component: () => import('./views/DataSources.vue'),
    meta: { title: '数据源管理' },
  },
  {
    path: '/monitoring',
    name: 'Monitoring',
    component: () => import('./views/Monitoring.vue'),
    meta: { title: '监控规则' },
  },
  {
    path: '/alerts',
    name: 'Alerts',
    component: () => import('./views/Alerts.vue'),
    meta: { title: '告警事件' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
