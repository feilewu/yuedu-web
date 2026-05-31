<template>
  <div class="editor">
    <source-tab-form class="left" :config="config" />
    <tool-bar />
    <source-tab-tools class="right" />
  </div>
</template>
<script setup lang="ts">
import { onMounted } from 'vue'
import bookSourceConfig from '@/config/bookSourceEditConfig'
import rssSourceConfig from '@/config/rssSourceEditConfig'
import '@/assets/sourceeditor.css'
import { useDark } from '@vueuse/core'
import type { SourceConfig } from '@/config/sourceConfig'
import ajax from '@/api/axios'
import { useSourceStore } from '@/store'

const sourceStore = useSourceStore()
useDark()

let config: SourceConfig
const isBookSource = /bookSource/i.test(location.href)

if (isBookSource) {
  config = bookSourceConfig as SourceConfig
  document.title = '书源管理'
  onMounted(() => {
    ajax.get('getBookSources').then(({ data }) => {
      if (data.isSuccess && data.data) {
        sourceStore.saveSources(data.data)
      }
    })
  })
} else {
  config = rssSourceConfig as SourceConfig
  document.title = '订阅源管理'
  onMounted(() => {
    ajax.get('getRssSources').then(({ data }) => {
      if (data.isSuccess && data.data) {
        sourceStore.saveSources(data.data)
      }
    })
  })
}
</script>
<style lang="scss" scoped>
.editor {
  display: flex;
  height: 100vh;
  overflow: hidden;
  .left {
    flex: 1;
    margin-left: 20px;
  }
  .right {
    flex: 1;
    width: 360px;
    margin-right: 20px;
  }
}
</style>
