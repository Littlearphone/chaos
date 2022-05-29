<template>
  <el-dialog
      center
      :title="property.name"
      v-model="dialogVisible"
      v-loading="loadingText"
      element-loading-text="玩命加载中"
      custom-class="viewer-dialog pdf-viewer-dialog"
  >
    <div
        ref="container"
        v-if="dialogVisible"
        :style="contentStyle"
        class="viewer-container"
    >
      <iframe :src="viewerPath" class="pdf-viewer-frame"></iframe>
    </div>
  </el-dialog>
</template>
<script>
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
    }
  },
  computed: {
    viewerPath() {
      return `/libs/pdfjs/web/viewer.html?file=${encodeURIComponent(this.pdfFilePath)}`
    },
    pdfFilePath() {
      return `/api/viewer/file?storageId=${this.property.storageId}&location=${btoa(encodeURIComponent(this.property.path))}`
    }
  },
  data() {
    return {
      pdf: null,
      pages: [],
      options: {},
      contentStyle: {},
      loadingText: true,
      dialogVisible: true
    }
  },
  mounted() {
    const vm = this
    window.addEventListener('resize', function() {
      vm.computeDialogSize()
    }, false)
    this.$nextTick(() => {
      this.computeDialogSize()
    })
  },
  methods: {
    computeDialogSize() {
      const $dialog = document.querySelector('.viewer-dialog')
      const $header = $dialog.querySelector('.el-dialog__header')
      const $content = $dialog.querySelector('.el-dialog__body')
      const style = window.getComputedStyle($content)
      const headerHeight = $header.scrollHeight
      const contentPaddingTop = style.getPropertyValue('padding-top')
      const contentPaddingBottom = style.getPropertyValue('padding-bottom')
      const paddingHeight = parseInt(contentPaddingTop) + parseInt(contentPaddingBottom)
      const contentHeight = ($dialog.clientHeight - headerHeight - paddingHeight)
      if (this.contentStyle.height === contentHeight + 'px' || contentHeight <= 0) {
        requestAnimationFrame(this.computeDialogSize.bind(this))
        return
      }
      this.contentStyle = { height: contentHeight + 'px' }
    }
  }
}
</script>
<style lang="less">
.pdf-viewer-dialog.el-dialog--center {

  .el-dialog__body {
    padding: 0 !important;
  }

  .viewer-container {
    width: 100%;
    overflow: hidden;

    .pdf-viewer-frame {
      width: 100%;
      height: 100%;
      border: none;
    }
  }
}
</style>
