<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'

const menus = [{ to: '/recruiter/positions', label: '职位管理' }]
</script>

<template>
  <div>
    <AppHeader title="智能招聘 · 招聘端" :menus="menus" notification-to="/recruiter/notifications" />
    <div class="page-container">
      <router-view />
    </div>
  </div>
</template>
