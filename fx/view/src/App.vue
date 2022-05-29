<template>
  <div class="top-container" @contextmenu.prevent.native="openMenu($event)">
    <div class="top-bar">
      <el-button
          circle
          size="mini"
          :icon="searchIcon"
          v-if="supportEverything"
          @click="searchCascadeDialog"
      ></el-button>
      <el-breadcrumb separator-class="el-icon-arrow-right" ref="breadcrumb">
        <el-breadcrumb-item>NUC</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: activated.path }">{{ activated.title }}</el-breadcrumb-item>
        <el-breadcrumb-item
            :to="{ path: computeBreadcrumbPath(index) }"
            v-for="(path, index) in breadcrumbs"
            :key="index"
        >
          {{ decodeURIComponent(path) }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs
        editable
        type="border-card"
        class="main-wrapper"
        @edit="handleTabsEdit"
        @tab-click="handleTabsClick"
        v-model="activated.tab"
    >
      <el-tab-pane :key="storage.uuid" v-for="storage in storages" :name="storage.uuid" lazy>
        <template #label>
          <el-icon>
            <List v-if="storage.type === 'fs'" />
            <Cloudy v-if="storage.type === 'smb'" />
          </el-icon>
          <span>{{ storage.title }}</span>
        </template>
        <storage-pane :storage="storage" @refreshBreadcrumb="refreshBreadcrumb"></storage-pane>
      </el-tab-pane>
    </el-tabs>
    <audio-file-viewer></audio-file-viewer>
    <storage-form :storage-form="storageForm" :form-visible="showStorageConfig"></storage-form>
    <cascade-search :property="cascadeSearchProperty"></cascade-search>
  </div>
</template>
<script>
import { shallowRef } from 'vue'
import AudioFileViewer from './components/viewer/AudioFileViewer.vue'
import StoragePane from './components/StoragePane.vue'
import StorageForm from './components/StorageForm.vue'
import CascadeSearch from './components/CascadeSearch.vue'
import { deleteStorage, esStatus, storages } from './api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Cloudy, List, Search } from '@element-plus/icons-vue'

export default {
  components: {
    List,
    Cloudy,
    StoragePane,
    StorageForm,
    CascadeSearch,
    AudioFileViewer
  },
  watch: {
    '$router.currentRoute.value': {
      handler: function(newRoute, oldRoute) {
        console.log(newRoute, oldRoute)
        const newPath = newRoute && newRoute.path
        const oldPath = oldRoute && oldRoute.path
        if (newPath === oldPath) {
          return
        }
        const pattern = '/storage/'
        const start = newPath.indexOf(pattern)
        const path = newPath.substring(start + pattern.length)
        if (!path) {
          return
        }
        const paths = path.split('/')
        const id = parseInt(paths.shift())
        this.refreshBreadcrumb(paths.filter(one => one))
        this.switchTab(this.storages.findIndex(storage => storage.id === id))
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
    cascadeSearchProperty() {
      const storage = this.storages[this.current]
      return {
        count: this.cascadeSearchCount,
        storageId: storage && storage.id
      }
    }
  },
  data() {
    return {
      activated: {
        path: '/',
        tab: '',
        title: ''
      },
      searchIcon: shallowRef(Search),
      supportEverything: false,
      cascadeSearchCount: 0,
      showStorageConfig: 0,
      breadcrumbs: [],
      storageForm: {},
      storages: [],
      current: 0
    }
  },
  mounted() {
    esStatus().then(response => response.data).then(data => this.supportEverything = data)
    storages().then(response => response.data).then(data => {
      this.storages = data || []
      if (!this.storages.length) {
        return
      }
      const keyPath = '/storage/'
      const pathname = location.pathname
      const start = pathname.indexOf(keyPath)
      if (start < 0) {
        this.switchTab(0)
        return
      }
      const subPath = pathname.substring(start + keyPath.length)
      const id = parseInt(subPath.substring(0, subPath.indexOf('/')))
      this.switchTab(this.storages.findIndex(storage => storage.id === id))
    })
    this.$bus.$on('saveStorage', data => this.storages.push(data))
  },
  methods: {
    openMenu(event) {
      console.log(event)
    },
    switchTab(index, manual) {
      if (index < 0) {
        return
      }
      this.current = index
      const storage = this.storages[index || 0]
      if (!storage) {
        return
      }
      this.activated.path = `/storage/${storage.id}/`
      this.activated.title = storage.title
      this.activated.tab = storage.uuid
      const path = this.$router.currentRoute.value.path
      if (!path.startsWith(this.activated.path) || (manual && path !== this.activated.path)) {
        console.log(this.activated.path)
        this.$router.push(this.activated.path)
      }
      this.$nextTick(() => {
        const breadcrumb = this.$refs.breadcrumb
        if (!breadcrumb) {
          return
        }
        const children = breadcrumb.$el.children
        const segments = Array.from(children).map(s => s.querySelector('.el-breadcrumb__inner').textContent)
        document.title = [segments[segments.length - 1], 'File Explorer'].filter(e => e).join(' | ')
      })
    },
    searchCascadeDialog() {
      this.cascadeSearchCount++
    },
    handleTabsClick(tab) {
      this.refreshBreadcrumb([])
      this.switchTab(tab.index, true)
    },
    handleTabsEdit(targetName, action) {
      if (action === 'add') {
        this.storageForm = {}
        this.showStorageConfig++
        return
      }
      if (action === 'remove') {
        const index = this.storages.findIndex(storage => storage.uuid === targetName)
        const storage = this.storages[index]
        ElMessageBox.confirm(`是否确定要删除存储"${storage.title}"?`, '删除提示', {
          distinguishCancelAndClose: true,
          confirmButtonText: '确认删除',
          cancelButtonText: '我点错了'
        }).then(() => {
          this.storageForm = {}
          if (this.current === index) {
            this.switchTab(0, true)
          }
          deleteStorage(storage.id).then(response => response.data).then(data => {
            this.storages.splice(index, 1)
            ElMessage({
              type: 'success',
              showClose: true,
              message: `删除存储"${data.title}"成功`
            })
          }).catch(error => ElMessage({
            type: 'error',
            showClose: true,
            message: `删除存储失败: ${error}`
          }))
        }).catch(data => console.log(`Perform action: ${data}`))
        return
      }
      console.log(targetName, action)
    },
    refreshBreadcrumb(paths) {
      this.breadcrumbs = paths
    },
    computeBreadcrumbPath(index) {
      const paths = this.breadcrumbs.slice(0, index + 1)
      return this.activated.path + paths.filter(path => path).map(decodeURIComponent).join('/')
    }
  }
}
</script>
<style lang="less">
html, body, #app, .main-wrapper {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

body {
  margin: 0;

  #app {
    background-color: #f5f5f5;
    font-family: Lato, PingFang SC, Microsoft YaHei, sans-serif !important;
  }

  div.main-wrapper {
    height: calc(100% - 46px);
  }

  div.el-breadcrumb {
    padding: 0 16px;

    span.el-breadcrumb__item {
      padding: 16px 0;
    }
  }

  div.top-container {
    margin: 8px;
    width: calc(100% - 16px);
    height: calc(100% - 16px);

    .top-bar {
      display: flex;
      align-items: center;
    }
  }

  div.el-overlay {
    backdrop-filter: blur(10px);
    background-color: rgba(0, 0, 0, .1);
  }

  span.el-dialog__title {
    padding: 0 10px;
    overflow: hidden;
    white-space: nowrap;
    display: inline-block;
    text-overflow: ellipsis;
    width: calc(100% - 60px);
  }

  .el-tabs__header {
    display: flex;
    user-select: none;
  }

  span.el-tabs__new-tab {
    float: none;
    margin: 9px;
  }

  .el-tabs--border-card {
    & > .el-tabs__header .el-tabs__item {
      cursor: pointer;

      .el-icon {
        padding: 0 2px;
        vertical-align: -2px;
      }

      &.is-closable {
        &:hover {
          padding-left: 12px;
          padding-right: 12px;

          .is-icon-close {
            padding: 0;
            width: 16px;
            height: 16px;
            margin: 0 2px;
          }
        }

        .is-icon-close {
          width: 0;
          margin: 0;
          overflow: hidden;
        }
      }
    }

    & > div.el-tabs__content {
      padding-top: 0;
      overflow: auto;
      height: calc(100% - 45px);

      //.el-tab-pane {
      //  & > .el-table {
      //    font-size: 18px;
      //
      //    .el-table__cell {
      //      padding: 4px 0;
      //    }
      //  }
      //}
    }
  }

  .viewer-dialog {
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    height: 80%;
    width: 80% !important;
    margin: auto !important;
    position: fixed !important;
    --el-dialog-padding-primary: 6px !important;

    .el-dialog__close {
      font-size: 24px;
    }
  }

  .el-scrollbar {
    --el-scrollbar-opacity: 0.3;
    --el-scrollbar-background-color: var(--el-text-color-secondary);
    --el-scrollbar-hover-opacity: 0.5;
    --el-scrollbar-hover-background-color: var(--el-text-color-secondary);
    overflow: hidden;
    position: relative;
    height: 100%;
  }

  .el-select {
    width: 100%;
  }

  .cell i {
    margin-right: 8px;
  }

  .el-message {
    padding: 0;
    border: none;
    background-color: white;
    box-shadow: 0 2px 10px 0 rgb(0 0 0 / 20%);

    .el-message__icon {
      color: white;
      padding: 10px;
    }

    p.el-message__content {
      font-weight: bold;
    }

    &-icon--info {
      background-color: #409EFF;
    }

    &-icon--error {
      background-color: #F56C6C;
    }

    &-icon--warning {
      background-color: #E6A23C;
    }

    &-icon--success {
      background-color: #67C23A;
    }
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 8px;
  background: rgba(144, 147, 153, .3);
}

::-webkit-scrollbar-corner, ::-webkit-scrollbar-track {
  background-color: #e2e2e2;
}

::-webkit-scrollbar-thumb {
  border-radius: 8px;
  background-color: #a6a6a6;
}

::-webkit-scrollbar-corner, ::-webkit-scrollbar-track {
  background-color: #e2e2e2;
}
</style>
