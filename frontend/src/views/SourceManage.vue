<template>
  <div class="source-manage">
    <div class="header">
      <h2>书源管理 ({{ sources.length }})</h2>
      <div class="actions">
        <el-button type="primary" @click="refreshList">刷新</el-button>
        <el-button @click="router.push('/bookSource')">调试书源</el-button>
        <el-button @click="router.push('/')">返回书架</el-button>
      </div>
    </div>
    <el-input
      v-model="searchKey"
      placeholder="搜索书源名称/分组/URL"
      clearable
      class="search"
    />
    <el-table :data="filteredSources" style="width: 100%" stripe>
      <el-table-column label="启用" width="70" align="center">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            @change="val => toggleEnabled(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="bookSourceName" label="名称" min-width="160" />
      <el-table-column prop="bookSourceGroup" label="分组" width="130" />
      <el-table-column prop="bookSourceUrl" label="URL" min-width="200" show-overflow-tooltip />
      <el-table-column prop="bookSourceType" label="类型" width="70" align="center">
        <template #default="{ row }">
          {{ ['文本','音频','图片','文件'][row.bookSourceType] || '文本' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-popconfirm title="确认删除此书源？" @confirm="deleteSource(row)">
            <template #reference>
              <el-button text type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ajax from '@/api/axios'

const router = useRouter()
const sources = ref<any[]>([])
const searchKey = ref('')

const filteredSources = computed(() => {
  if (!searchKey.value) return sources.value
  const key = searchKey.value.toLowerCase()
  return sources.value.filter(s =>
    s.bookSourceName?.toLowerCase().includes(key) ||
    s.bookSourceGroup?.toLowerCase().includes(key) ||
    s.bookSourceUrl?.toLowerCase().includes(key)
  )
})

const refreshList = async () => {
  const { data } = await ajax.get('getBookSources')
  if (data.isSuccess) {
    sources.value = data.data || []
  }
}

const toggleEnabled = async (source: any, enabled: boolean) => {
  source.enabled = enabled
  await ajax.post('saveBookSource', source)
}

const deleteSource = async (source: any) => {
  await ajax.post('deleteBookSources', [source])
  sources.value = sources.value.filter(s => s.bookSourceUrl !== source.bookSourceUrl)
}

onMounted(refreshList)
</script>

<style scoped>
.source-manage {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header h2 {
  margin: 0;
}
.actions {
  display: flex;
  gap: 8px;
}
.search {
  margin-bottom: 12px;
}
</style>
