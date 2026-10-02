<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'

const menus = [
  { to: '/jobs', label: '职位' },
  { to: '/my/resume', label: '我的简历' },
  { to: '/my/deliveries', label: '我的投递' },
]
</script>

<template>
  <div>
    <AppHeader title="智能招聘 · 求职端" :menus="menus" notification-to="/notifications" />
    <div class="page-container">
      <router-view />
    </div>
  </div>
</template>
