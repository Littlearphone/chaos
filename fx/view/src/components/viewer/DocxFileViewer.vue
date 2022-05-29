<template>
  <el-dialog
      center
      :title="property.name"
      v-model="dialogVisible"
      v-loading="loadingText"
      custom-class="viewer-dialog"
      element-loading-text="玩命加载中"
  >
    <div
        v-html="doc"
        ref="container"
        v-if="dialogVisible"
        class="word-container viewer-container"
        :style="{height : contentHeight}"
    />
  </el-dialog>
</template>
<script>
// import mammoth from 'mammoth'
// import { bufferContent } from '../../api'

export default {
  components: {},
  emits: ['changeSelected'],
  props: {
    property: {
      type: Object,
      default: () => ({})
    }
  },
  watch: {
    property(value) {
      this.dialogVisible = true
      this.loadFile()
    }
  },
  computed: {
    docxFilePath() {
      return `/api/viewer/file?storageId=${this.property.storageId}&location=${btoa(encodeURIComponent(this.property.path))}`
    }
  },
  data() {
    return {
      doc: null,
      pages: [],
      options: {},
      loadingText: true,
      contentHeight: '',
      dialogVisible: true
    }
  },
  mounted() {
    window.require = function(){}
    const vm = this
    window.addEventListener('resize', function() {
      vm.computeDialogSize()
    }, false)
    this.$nextTick(() => {
      this.computeDialogSize()
      this.loadFile()
    })
  },
  methods: {
    computeDialogSize() {
      const headerHeight = 38
      const paddingHeight = 20
      const availableRate = 0.8
      const clientHeight = document.documentElement.clientHeight
      this.contentHeight = (clientHeight * availableRate - headerHeight - paddingHeight) + 'px'
    },
    loadFile() {
      // bufferContent(this.docxFilePath)
      //     .then(({data}) => {
      //       console.log(data)
      //       return mammoth.convertToHtml({ arrayBuffer: data })
      //     })
      //     .then((value) => {
      //       console.log(value)
      //       this.doc.value = value
      //     })
    }
  }
}
</script>
<style lang="less" scoped>
.viewer-dialog {

  &.el-dialog--center .el-dialog__body {
    padding-top: 8px !important;
    padding-bottom: 8px !important;
  }

  .viewer-container {
    width: 100%;
    overflow: auto;
  }
}
</style>
