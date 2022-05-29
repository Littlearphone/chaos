<template>
  <el-dialog center :title="property.name" v-model="dialogVisible" custom-class="viewer-dialog">
    <div
        ref="container"
        v-if="dialogVisible"
        v-loading="loadingText"
        element-loading-text="玩命加载中"
        class="viewer-container"
        v-html="compiledMarkdown"
    ></div>
  </el-dialog>
</template>
<script>
import marked from 'marked'
import { textContent } from '../../api'

export default {
  components: {},
  props: {
    property: {
      type: Object,
      default: () => ({})
    }
  },
  watch: {
    property(value) {
      this.dialogVisible = true
      if (value && value.path) {
        this.loadText(value.path)
      }
    }
  },
  computed: {
    compiledMarkdown() {
      // TODO md-editor-v3
      return marked(this.markdownHtml, {
        sanitize: false,
        xhtml: true
      })
    }
  },
  data() {
    this.loadText(this.property.path)
    return {
      options: {},
      loadingText: true,
      contentHeight: '',
      markdownHtml: '',
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
      const headerHeight = 38
      const paddingHeight = 20
      const clientHeight = document.documentElement.clientHeight
      this.contentHeight = (clientHeight - headerHeight - paddingHeight) + 'px'
    },
    loadText(path) {
      if (!path) {
        this.loadingText = false
        return
      }
      this.loadingText = true
      textContent(this.property.storageId, path).then(response => {
        this.markdownHtml = String(response.data)
        this.loadingText = false
      })
    }
  }
}
</script>
<style lang="less" scoped>
.viewer-dialog {

  &.el-dialog--center .el-dialog__body {
    overflow: auto;
    height: calc(100% - 56px);
    padding-top: 8px !important;
    padding-bottom: 8px !important;

  }

  .viewer-container {
    width: 100%;

    .margin-view-overlays {
      .line-numbers {
        width: auto !important;
      }
    }
  }
}
</style>
