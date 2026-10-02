<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { FieldDef } from '@/types'

const props = defineProps<{
  title: string
  items: Record<string, any>[]
  fields: FieldDef[]
}>()

const emit = defineEmits<{
  (e: 'add', payload: Record<string, any>): void
  (e: 'edit', id: number, payload: Record<string, any>): void
  (e: 'remove', id: number): void
}>()

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<Record<string, any>>({})

function primaryLabel(item: Record<string, any>): string {
  const f = props.fields[0]
  return item[f.key] || '（未填写）'
}

function secondaryLabel(item: Record<string, any>): string {
  const parts: string[] = []
  for (const f of props.fields.slice(1)) {
    if (f.type === 'textarea') continue
    if (item[f.key]) parts.push(item[f.key])
  }
  return parts.join(' · ')
}

function openAdd() {
  editingId.value = null
  props.fields.forEach((f) => (form[f.key] = ''))
  dialogVisible.value = true
}

function openEdit(item: Record<string, any>) {
  editingId.value = item.id
  props.fields.forEach((f) => (form[f.key] = item[f.key] ?? ''))
  dialogVisible.value = true
}

function onSubmit() {
  for (const f of props.fields) {
    if (f.required && !form[f.key]) return
  }
  const payload: Record<string, any> = {}
  props.fields.forEach((f) => (payload[f.key] = form[f.key] || undefined))
  if (editingId.value != null) {
    emit('edit', editingId.value, payload)
  } else {
    emit('add', payload)
  }
  dialogVisible.value = false
}

function onRemove(item: Record<string, any>) {
  emit('remove', item.id)
}
</script>

<template>
  <el-card shadow="never" class="section">
    <template #header>
      <div class="section-head">
        <span class="section-title">{{ title }}</span>
        <el-button type="primary" text :icon="Plus" @click="openAdd">添加</el-button>
      </div>
    </template>

    <el-empty v-if="items.length === 0" description="暂无记录" :image-size="60" />

    <div v-for="item in items" :key="item.id" class="item">
      <div class="item-main">
        <div class="item-title">{{ primaryLabel(item) }}</div>
        <div class="item-sub">{{ secondaryLabel(item) }}</div>
        <div v-if="item.description" class="item-desc">{{ item.description }}</div>
      </div>
      <div class="item-ops">
        <el-button text :icon="Edit" @click="openEdit(item)" />
        <el-button text :icon="Delete" class="danger" @click="onRemove(item)" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId != null ? `编辑${title}` : `添加${title}`" width="520px">
      <el-form label-position="top">
        <el-form-item v-for="f in fields" :key="f.key" :label="f.label" :required="f.required">
          <el-input
            v-if="!f.type || f.type === 'text'"
            v-model="form[f.key]"
            :maxlength="f.maxlength"
            :placeholder="f.placeholder"
          />
          <el-input
            v-else-if="f.type === 'textarea'"
            v-model="form[f.key]"
            type="textarea"
            :rows="3"
            :maxlength="f.maxlength"
            show-word-limit
            :placeholder="f.placeholder"
          />
          <el-date-picker
            v-else-if="f.type === 'date'"
            v-model="form[f.key]"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.section {
  margin-bottom: 16px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title {
  font-weight: 600;
}
.item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.item:last-child {
  border-bottom: none;
}
.item-title {
  font-weight: 600;
  font-size: 15px;
}
.item-sub {
  color: #86909c;
  font-size: 13px;
  margin-top: 2px;
}
.item-desc {
  color: #4e5969;
  font-size: 13px;
  margin-top: 6px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.item-ops {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
.danger {
  color: #f56c6c;
}
</style>
