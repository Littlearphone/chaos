<template>
  <div>
    <el-table
        stripe
        align="center"
        ref="fileTable"
        style="width: 100%"
        :data="filteredData"
        :height="tableHeight"
        v-loading="loadingFiles"
        :data-size="files.length"
        @row-click="handleRowClick"
        v-windowing="handleLoadMore"
        :row-class-name="rowClassName"
        cell-class-name="file-list-cell"
        element-loading-text="玩命加载中"
        @row-contextmenu="handleRowContextMenu"
    >
      <el-table-column type="index" width="30">
        <template #default="scope">
          <file-type-icon :row="scope.row" :index="resolveIndex(scope)"></file-type-icon>
        </template>
      </el-table-column>
      <el-table-column prop="name" show-overflow-tooltip>
        <template #header>
          <el-icon>
            <Document />
          </el-icon>
          <span>文件名</span>
        </template>
      </el-table-column>
      <!--<el-table-column prop="type" align="center" v-if="hiddenXsOnly">-->
      <!--  <template #header>-->
      <!--    <i class="el-icon-files"></i>-->
      <!--    <span>文件类型</span>-->
      <!--  </template>-->
      <!--</el-table-column>-->
      <el-table-column prop="lastModified" width="220" align="center" v-if="hiddenXsOnly">
        <template #header>
          <el-icon>
            <Calendar />
          </el-icon>
          <span>修改时间</span>
        </template>
      </el-table-column>
      <el-table-column prop="length" width="180" align="center" v-if="hiddenXsOnly">
        <template #header>
          <el-icon>
            <Coin />
          </el-icon>
          <span>文件大小</span>
        </template>
        <template #default="scope">
          <span v-if="scope.row.type === 'folder'">-</span>
          <span v-else>{{ formatFileSize(scope.row.length) }}</span>
        </template>
      </el-table-column>
      <el-table-column width="110" align="center">
        <template #header>
          <el-icon>
            <Operation />
          </el-icon>
          <span>操作</span>
        </template>
        <template #default="scope">
          <el-link
              :icon="downloadIcon"
              :underline="false"
              v-if="scope.row.path && scope.row.type !== 'folder'"
              @click="event => download(event, scope)"
          ></el-link>
          <!--<el-link icon="el-icon-view" :underline="false" v-if="scope.row.path"></el-link>-->
        </template>
      </el-table-column>
    </el-table>
  </div>
  <component :is="fileViewer" v-if="fileViewer" :property="viewerData" @changeSelected="changeSelected"></component>
</template>
<script>
import { shallowRef } from 'vue'
import { Calendar, Coin, Document, Download, Operation } from '@element-plus/icons-vue'
import { storageFiles } from '../api'
import FileTypeIcon from './FileTypeIcon.vue'
import { directoryFirst, hiddenXsOnly, isAudio, isImage, unitFormatter, VIEWER_MAPPING } from '../lib/common'
import { ElMessage } from 'element-plus'

const files = import.meta.glob("./viewer/*.vue")
const components = {}
Object.keys(files).forEach(current => {
  const start = current.lastIndexOf('/') + 1
  const end = current.lastIndexOf('.')
  const name = current.substring(start, end)
  files[current]().then(response => {
    components[name] = response.default
  })
})
export default {
  components: {
    Coin,
    Calendar,
    Document,
    Operation,
    FileTypeIcon
  },
  emits: ['refreshBreadcrumb'],
  props: {
    storage: Object
  },
  watch: {
    'storage.id': {
      handler: function(value) {
        this.relativePath = []
        this.loadFiles(value)
      },
      deep: true,
      immediate: true
    },
    '$router.currentRoute.value': {
      handler: function(newRoute, oldRoute) {
        const newPath = newRoute && newRoute.path
        const oldPath = oldRoute && oldRoute.path
        if (newPath === oldPath) {
          return
        }
        const pattern = `/storage/${this.storage.id}/`
        const start = newPath.indexOf(pattern)
        if (start < 0) {
          return
        }
        const path = newPath.substring(start + pattern.length)
        this.relativePath = (path && path.split('/')) || []
        this.loadFiles(this.storage.id)
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
    filteredData() {
      return this.files.slice(this.startRowIndex, this.endRowIndex)
    },
    hiddenXsOnly() {
      return this.resizeTime && hiddenXsOnly()
    }
  },
  data() {
    return {
      visibleWidth: document.documentElement.clientWidth,
      downloadIcon: shallowRef(Download),
      resizeTime: new Date().getTime(),
      loadingFiles: true,
      startRowIndex: 0,
      endRowIndex: 100,
      othersHeight: 111,
      tableHeight: 100,
      relativePath: [],
      loadingPath: [],
      viewerData: {},
      fileViewer: '',
      files: []
    }
  },
  mounted() {
    const vm = this
    setTimeout(() => {
      vm.$nextTick(() => vm.computeClientSize())
    }, 100)
    window.addEventListener('resize', () => vm.computeClientSize(), false)
    vm.loadFiles(vm.storage.id)
    this.$bus.$on('cascadeSearch', data => this.handleRowClick(data))
  },
  methods: {
    download(event, scope) {
      event.preventDefault()
      event.stopPropagation()
      window.open(`/api/viewer/stream?download=true&storageId=${this.storage.id}&location=${btoa(encodeURIComponent(scope.row.path))}`)
    },
    loadFiles(id) {
      const loadingPath = this.relativePath.join(',')
      if (this.loadingPath === loadingPath) {
        return
      }
      this.loadingFiles = true
      this.loadingPath = loadingPath
      storageFiles(id, this.relativePath).then(response => response.data).then(data => {
        if (this.loadingPath !== loadingPath) {
          return
        }
        const files = data || []
        files.sort(directoryFirst)
        this.files = files
        if (this.relativePath.length) {
          let path = '/'
          if (this.relativePath.length > 1) {
            path = decodeURIComponent(this.relativePath[this.relativePath.length - 2])
          }
          this.files.unshift({ name: path || '/' })
        }
        const to = `/storage/${this.storage.id}/${this.relativePath.join('/')}`
        if (this.$router.currentRoute.value.path !== to && this.storage.id === id) {
          console.log(to)
          this.$router.push(to)
          this.$emit('refreshBreadcrumb', this.relativePath)
        }
        this.loadingFiles = false
      })
    },
    computeClientSize() {
      this.tableHeight = document.documentElement.clientHeight - this.othersHeight
      this.visibleWidth = document.documentElement.clientWidth
      this.resizeTime = new Date().getTime()
    },
    resolveIndex(scope) {
      return (scope.row.index = scope.$index)
    },
    rowClassName({ row }) {
      const classNames = []
      if (row.type === 'occupied') {
        classNames.push('file-occupied')
      }
      if (row.hidden) {
        classNames.push('file-hidden')
      }
      return classNames.join(' ')
    },
    handleRowClick(row, column, event) {
      event && event.preventDefault()
      event && event.stopPropagation()
      // console.log('click', row.name, row.path)
      if (row.type && row.type !== 'folder') {
        this.handleFileType(row)
        return
      }
      if (row.path) {
        row.name.split('\\').forEach(segment => this.relativePath.push(segment))
      } else {
        this.relativePath.pop()
      }
      this.$refs.fileTable.$refs.bodyWrapper.scrollTop = 0
      this.loadFiles(this.storage.id)
    },
    handleFileType(row) {
      const name = row.name.toLowerCase()
      const type = name.substring(name.lastIndexOf('.') + 1)
      if (isAudio(type)) {
        this.$bus.$emit('playAudio', row)
        // this.switchViewerComponent('AudioFileViewer', row)
        return
      }
      if (isImage(type)) {
        const list = this.filteredData.filter(item => {
          if (item.type && item.type !== 'file') {
            return isImage(item.type)
          }
          const name = item.name.toLowerCase()
          return isImage(name.substring(name.lastIndexOf('.') + 1))
        })
        if (!list.length) {
          list.push(row)
        }
        this.switchViewerComponent('ImageListViewer', {
          index: list.findIndex(item => item.path === row.path),
          list
        })
        return
      }
      const viewer = VIEWER_MAPPING.find(mapping => mapping.pattern.test(type))
      if (viewer) {
        this.switchViewerComponent(viewer.viewer, row)
        return
      }
      ElMessage({
        showClose: true,
        message: '不是可预览的类型'
      })
      this.fileViewer = ''
      this.viewerData = {}
    },
    switchViewerComponent(name, data) {
      if (!this.$.components[name]) {
        this.$.components[name] = components[name]
      }
      this.fileViewer = name
      this.viewerData = Object.assign({ storageId: this.storage.id }, data)
    },
    handleRowContextMenu(row, column, event) {
      event.preventDefault()
      event.stopPropagation()
      console.log('contextmenu', row.name, row.path)
    },
    formatFileSize(size) {
      return unitFormatter(size)
    },
    handleLoadMore(start, end) {
      this.startRowIndex = start
      this.endRowIndex = end
    },
    changeSelected(index) {
      this.viewerData.index = index
    }
  }
}
</script>
<style lang="less">
span.el-tabs__new-tab {
  &:hover {
    border-color: #409EFF;
  }

  .el-icon.is-icon-plus {
    transform: unset;
    vertical-align: text-top;
  }
}

span.el-tabs__nav-next,
span.el-tabs__nav-prev {
  width: 20px;
  line-height: 40px;
  text-align: center;

  i[class*=" el-icon-arrow-"],
  i[class^=el-icon-arrow-] {
    font-weight: bold;
  }

  &.is-disabled i[class*=" el-icon-arrow-"],
  &.is-disabled i[class^=el-icon-arrow-] {
    font-weight: unset;
  }
}

.el-table__row {
  cursor: pointer;

  .el-link .el-icon-download {
    font-size: 20px;
  }
}

.el-table--striped table.el-table__body tr.file-occupied td.el-table__cell {
  background-color: rgba(245, 108, 108, .3);
}

.el-table tr.file-hidden {
  color: #c0c4cc;
}

.el-table th.el-table__cell > .cell {
  display: flex;
  align-items: center;
}

.file-list-cell:first-child > div.cell {
  padding-left: 0;
  padding-right: 0;
  text-overflow: unset;
}

.el-table td.el-table__cell:nth-child(2) div.cell {
  white-space: nowrap;
}
</style>
