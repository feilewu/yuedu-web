<template>
  <div class="books-wrapper">
    <div class="wrapper">
      <div
        class="book"
        v-for="book in books"
        :key="book.bookUrl"
        @click="handleClick(book)"
      >
        <div class="delete-btn" v-if="!isSearch" @click.stop="handleDelete(book)">
          <el-icon><Delete /></el-icon>
        </div>
        <div class="change-source-btn" v-if="!isSearch" @click.stop="handleChangeSource(book)">
          <el-icon><Refresh /></el-icon>
        </div>
        <div class="cover-img">
          <img
            class="cover"
            :src="getCover(book)"
            :key="book.coverUrl"
            @error.once="proxyImage"
            @error="handleImageError"
            alt=""
            loading="lazy"
          />
        </div>
        <div class="info">
          <div class="name">{{ book.name }}</div>
          <div class="sub">
            <div class="author">
              {{ book.author }}
            </div>
            <div class="tags" v-if="isSearch">
              <el-tag
                v-for="tag in book.kind?.split(',').slice(0, 2)"
                :key="tag"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="update-info" v-if="!isSearch">
              <div class="dot">•</div>
              <div class="size">共{{ (book as Book).totalChapterNum }}章</div>
              <div class="dot">•</div>
              <div class="date">
                {{ dateFormat((book as Book).lastCheckTime) }}
              </div>
            </div>
          </div>
          <div class="intro" v-if="isSearch">{{ book.intro }}</div>

          <div class="source-name" v-if="!isSearch" style="font-size:12px;color:#999;margin-top:2px">
            来源：{{ (book as Book).originName }}
          </div>
          <div class="dur-chapter" v-if="!isSearch">
            已读：{{ (book as Book).durChapterTitle }}
          </div>
          <div class="last-chapter">最新：{{ book.latestChapterTitle }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import type { Book, SeachBook } from '@/book'
import { dateFormat, isLegadoUrl } from '../utils/utils'
import { Delete, Refresh } from '@element-plus/icons-vue'
import API from '@api'
const props = defineProps<{
  books: Array<Book | SeachBook>
  isSearch: boolean
}>()

const emit = defineEmits(['bookClick', 'deleteBook', 'changeSource'])
const handleClick = (book: Book | SeachBook) => emit('bookClick', book)
const handleDelete = (book: Book | SeachBook) => emit('deleteBook', book)
const handleChangeSource = (book: Book | SeachBook) => emit('changeSource', book)
const getCover = ({ bookUrl, coverUrl }: Book | SeachBook) => {
  if (coverUrl === undefined) return API.getProxyCoverUrl(bookUrl)
  return isLegadoUrl(coverUrl) ? API.getProxyCoverUrl(coverUrl) : coverUrl
}
const defaultCover = "data:image/svg+xml," + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 84 112" fill="#eee">' +
  '<rect width="84" height="112"/><text x="42" y="56" text-anchor="middle" fill="#aaa" font-size="14">暂无封面</text></svg>'
)
const proxyImage = (evt: Event) => {
  const target = evt.target as HTMLImageElement
  if (!target.src.startsWith('data:')) {
    target.src = API.getProxyCoverUrl(target.src)
  }
}
const handleImageError = (evt: Event) => {
  const target = evt.target as HTMLImageElement
  target.src = defaultCover
}

const subJustify = computed(() =>
  props.isSearch ? 'space-between' : 'flex-start',
)
</script>

<style lang="scss" scoped>
.books-wrapper {
  overflow: auto;

  .wrapper {
    display: grid;
    grid-template-columns: repeat(auto-fill, 380px);
    justify-content: space-around;
    grid-gap: 10px;

    .book {
      position: relative;
      user-select: none;
      display: flex;
      cursor: pointer;
      margin-bottom: 18px;
      padding: 24px 24px;
      width: 360px;
      flex-direction: row;
      justify-content: space-around;

      .cover-img {
        width: 84px;
        height: 112px;

        .cover {
          width: 84px;
          height: 112px;
        }
      }

      .info {
        display: flex;
        flex-direction: column;
        justify-content: space-around;
        align-items: left;
        height: 112px;
        margin-left: 20px;
        flex: 1;
        overflow: hidden;

        .name {
          width: fit-content;
          font-size: 16px;
          font-weight: 700;
          color: #33373d;
        }

        .sub {
          display: flex;
          flex-direction: row;
          align-items: baseline;
          justify-content: v-bind('subJustify');
          font-size: 12px;
          font-weight: 600;
          color: #6b6b6b;
          .tags {
            :deep(.el-tag) {
              margin-right: 0.5em;
            }
          }
          .update-info {
            display: flex;
            .dot {
              margin: 0 7px;
            }
          }
        }

        .intro,
        .dur-chapter,
        .last-chapter {
          color: #969ba3;
          font-size: 13px;
          margin-top: 3px;
          font-weight: 500;
          word-wrap: break-word;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 1;
          line-clamp: 1;
          text-align: left;
        }
      }
    }

    .book:hover {
      background: rgba(0, 0, 0, 0.1);
      transition-duration: 0.5s;
    }

    .delete-btn {
      position: absolute;
      top: 4px;
      right: 4px;
      cursor: pointer;
      font-size: 16px;
      color: #999;
      z-index: 1;
      opacity: 0;
      transition: opacity 0.2s;
    }

    .change-source-btn {
      position: absolute;
      top: 4px;
      right: 30px;
      cursor: pointer;
      font-size: 14px;
      color: #999;
      z-index: 1;
      opacity: 0;
      transition: opacity 0.2s;
    }

    .book:hover .delete-btn,
    .book:hover .change-source-btn {
      opacity: 0.7;
    }

    .delete-btn:hover {
      color: #e74c3c;
      opacity: 1;
    }

    .change-source-btn:hover {
      color: #409eff;
      opacity: 1;
    }
  }

  .wrapper:last-child {
    margin-right: auto;
  }
}

.books-wrapper::-webkit-scrollbar {
  width: 0 !important;
}

@media screen and (max-width: 750px) {
  .books-wrapper {
    .wrapper {
      display: flex;
      flex-direction: column;

      .book {
        box-sizing: border-box;
        width: 100%;
        margin-bottom: 0;
        padding: 10px 20px;
      }
    }
  }
}
</style>
