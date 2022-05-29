<template>
  <el-dialog center :title="property.name" v-model="dialogVisible" custom-class="viewer-dialog text-viewer-dialog">
    <div
        ref="container"
        v-if="dialogVisible"
        class="viewer-container"
        v-loading="loadingText"
        element-loading-text="玩命加载中"
        :style="{height : contentHeight}"
    ></div>
  </el-dialog>
</template>
<script>
import { textContent } from '../../api'
import { hiddenXsOnly, monacoTypeConvert } from '../../lib/common'

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
      const vm = this
      setTimeout(() => {
        vm.$nextTick(() => {
          vm.computeDialogSize()
        })
      })
      vm.dialogVisible = true
      if (value && value.path) {
        vm.loadText(value.path)
      }
    }
  },
  data() {
    this.loadText(this.property.path)
    return {
      options: {},
      loadingText: true,
      contentHeight: '',
      dialogVisible: true
    }
  },
  mounted() {
    const vm = this
    window.addEventListener('resize', function() {
      vm.computeDialogSize()
    }, false)
    setTimeout(() => {
      vm.$nextTick(() => {
        vm.computeDialogSize()
      })
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
      this.contentHeight = ($dialog.clientHeight - headerHeight - paddingHeight) + 'px'
    },
    readTextContent(data) {
      if (typeof data === 'string') {
        return data
      }
      return JSON.stringify(data, null, 4)
    },
    loadText(path) {
      if (!path) {
        this.loadingText = false
        return
      }
      this.loadingText = true
      textContent(this.property.storageId, path).then(response => {
        import( '../../lib/monaco').then(({ createEditor }) => {
          const options = {
            theme: 'vs',
            fontSize: 20,
            wordWrap: true,
            // linkedEditing: true,
            value: this.readTextContent(response.data),
            automaticLayout: true,
            folding: hiddenXsOnly(),
            lineNumbers: hiddenXsOnly(),
            scrollBeyondLastLine: false,
            minimap: {
              enabled: hiddenXsOnly(),
              size: 'fit'
            },
            language: monacoTypeConvert(this.property.type)
          }
          const editor = createEditor(this.$refs.container, options)
          editor.revealLineNearTop(0)
          // const model = editor.getModel()
          // changeLanguage(model, 'javascript')
        })
        this.loadingText = false
      })
    }
  }
}
</script>
<style lang="less">
.text-viewer-dialog.el-dialog--center {
  .el-dialog__body {
    padding: 4px 0 !important;
  }

  .viewer-container {
    width: 100%;
    height: 100%;

    .margin-view-overlays {
      .line-numbers {
        width: auto !important;
      }
    }

    .overflowingContentWidgets {
      display: none;
    }
  }
}
</style>
