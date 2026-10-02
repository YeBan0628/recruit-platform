import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/JobseekerLayout.vue'),
    meta: { requiresAuth: true, role: 1 },
    children: [
      { path: '', redirect: '/jobs' },
      { path: 'jobs', name: 'jobs', component: () => import('@/views/jobseeker/JobListView.vue') },
      { path: 'jobs/:id', name: 'job-detail', component: () => import('@/views/jobseeker/JobDetailView.vue') },
      { path: 'my/resume', name: 'my-resume', component: () => import('@/views/jobseeker/MyResumeView.vue') },
      { path: 'my/deliveries', name: 'my-deliveries', component: () => import('@/views/jobseeker/MyDeliveriesView.vue') },
      { path: 'notifications', name: 'jobseeker-notifications', component: () => import('@/views/shared/NotificationsView.vue') },
    ],
  },
  {
    path: '/recruiter',
    component: () => import('@/layouts/RecruiterLayout.vue'),
    meta: { requiresAuth: true, role: 2 },
    children: [
      { path: '', redirect: '/recruiter/positions' },
      { path: 'positions', name: 'recruiter-positions', component: () => import('@/views/recruiter/PositionManageView.vue') },
      { path: 'positions/new', name: 'position-new', component: () => import('@/views/recruiter/PositionEditView.vue') },
      { path: 'positions/:id/edit', name: 'position-edit', component: () => import('@/views/recruiter/PositionEditView.vue') },
      { path: 'positions/:id/applications', name: 'position-applications', component: () => import('@/views/recruiter/ApplicationsView.vue') },
      { path: 'notifications', name: 'recruiter-notifications', component: () => import('@/views/shared/NotificationsView.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/jobs' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

function homeByRole(role: number | null): string {
  return role === 2 ? '/recruiter/positions' : '/jobs'
}

router.beforeEach((to) => {
  const auth = useAuthStore()
  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
  const requiredRole = to.matched.find((r) => r.meta.role)?.meta.role as number | undefined

  if (!requiresAuth) {
    // 已登录用户访问登录/注册页，直接回各自首页
    if (auth.isLogin && (to.path === '/login' || to.path === '/register')) {
      return homeByRole(auth.role)
    }
    return true
  }

  if (!auth.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (requiredRole && auth.role !== requiredRole) {
    return homeByRole(auth.role)
  }

  return true
})

export default router
