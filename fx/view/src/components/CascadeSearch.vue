<template>
  <div class="cascade-search-wrapper">
    <el-dialog center v-model="dialogVisible" :show-close="false">
      <div class="cascade-search-box">
        <el-select
            remote
            filterable
            reserve-keyword
            ref="searchInput"
            v-model="current"
            :loading="loading"
            placeholder="搜索文件"
            @change="cascadeSearchChange"
            :remote-method="performSearch"
        >
          <el-option
              v-for="item in options"
              :key="item.path"
              :title="item.path"
              :value="item.path"
              class="cascade-search-option"
          >
            <div class="cascade-search-item">
              <div class="cascade-search-left">
                <div class="cascade-search-name">
                  <file-type-icon :row="item"></file-type-icon>
                  <span>{{ item.name }}</span>
                </div>
                <div class="cascade-search-path">{{ item.path }}</div>
              </div>
              <!--<div class="cascade-search-right">{{ item.lastModified }}</div>-->
            </div>
          </el-option>
        </el-select>
        <!--<el-input-->
        <!--    ref="searchInput"-->
        <!--    v-model="keyword"-->
        <!--    placeholder="搜索文件"-->
        <!--    class="input-with-select"-->
        <!--    @keypress.enter="performSearch"-->
        <!--&gt;-->
        <!--  &lt;!&ndash;<template #prepend>&ndash;&gt;-->
        <!--  &lt;!&ndash;  <el-select v-model="storage" placeholder="Select" style="width: 110px">&ndash;&gt;-->
        <!--  &lt;!&ndash;    <el-option label="Restaurant" value="1"></el-option>&ndash;&gt;-->
        <!--  &lt;!&ndash;    <el-option label="Order No." value="2"></el-option>&ndash;&gt;-->
        <!--  &lt;!&ndash;    <el-option label="Tel" value="3"></el-option>&ndash;&gt;-->
        <!--  &lt;!&ndash;  </el-select>&ndash;&gt;-->
        <!--  &lt;!&ndash;</template>&ndash;&gt;-->
        <!--  <template #append>-->
        <!--    <el-button type="primary" :icon="Search" @click="performSearch"></el-button>-->
        <!--  </template>-->
        <!--</el-input>-->
      </div>
    </el-dialog>
  </div>
</template>
<script>
import FileTypeIcon from './FileTypeIcon.vue'
import { Search } from '@element-plus/icons-vue'
import { searchFiles } from '../api'
import axios from 'axios'

const cancelToken = axios.CancelToken
export default {
  props: {
    property: {
      type: Object,
      default: () => ({ count: 1 })
    }
  },
  components: { FileTypeIcon },
  watch: {
    'property.count': {
      handler(value, oldValue) {
        if (value === oldValue) {
          return
        }
        this.dialogVisible = value > 0
        this.current = ''
        this.options = []
        this.focusInput()
      },
      deep: true,
      immediate: true
    }
  },
  data() {
    return {
      Search,
      current: '',
      storage: '',
      options: [],
      source: null,
      loading: false,
      searchDelay: null,
      dialogVisible: false
    }
  },
  mounted() {
    this.focusInput()
  },
  methods: {
    focusInput() {
      this.$nextTick(() => {
        const searchInput = this.$refs.searchInput
        searchInput && searchInput.focus()
      })
    },
    performSearch(keyword) {
      if (!keyword || !this.property.storageId) {
        return
      }
      this.loading = true
      clearTimeout(this.searchDelay)
      this.source && this.source.cancel('Cancel last search request')
      this.searchDelay = setTimeout(() => {
        this.source = cancelToken.source()
        searchFiles(this.property.storageId, keyword, this.source)
            .then(response => response.data)
            .then(data => {
              console.log(data)
              this.loading = false
              this.options = (data.results || []).slice(0, 100)
            }).catch(error => {
          this.loading = false
          console.error(error)
        })
      }, 1000)
    },
    cascadeSearchChange(value) {
      const option = this.options.find(option => option.path === value)
      this.$bus.$emit('cascadeSearch', {
        ...option,
        name: option.path
      })
      this.dialogVisible = false
    }
  }
}
</script>
<style lang="less">
.cascade-search-wrapper {
  div.el-dialog__header {
    padding: 0;
  }

  div.el-dialog__body {
    padding: 0;
  }
}

.el-select__popper {
  max-width: 50%;

  .cascade-search-option {
    height: 50px;

    .cascade-search-item {
      display: flex;
      justify-content: space-between;

      .cascade-search-left {
        display: flex;
        flex-direction: column;

        .cascade-search-name {
          font-size: 18px;

          .svg-icon {
            width: 1em;
            height: 1em;
          }
        }

        .cascade-search-path {
          padding: 0;
          color: #aaa;
          font-size: 12px;
          line-height: 12px;
        }
      }

      .cascade-search-right {
        color: #aaa;
        line-height: 50px;
      }
    }
  }
}
</style>
