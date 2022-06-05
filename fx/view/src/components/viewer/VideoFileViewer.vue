<template>
  <el-dialog center :title="property.name" v-model="dialogVisible" custom-class="viewer-dialog video-viewer-dialog">
    <div
        ref="container"
        v-if="dialogVisible"
        :style="contentStyle"
        class="viewer-container"
        v-loading="loadingVideo || convertVideo"
        element-loading-text="玩命加载中"
    >
      <video ref="player" :src="videoFilePath" autoplay="autoplay" loop="loop" controls></video>
    </div>
  </el-dialog>
</template>
<script>
import { detectVideo } from '../../api'

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
        vm.detectVideo(value.path)
        vm.loadVideo(value.path)
      }
    }
  },
  computed: {
    videoFilePath() {
      if (this.convertVideo) {
        return ''
      }
      return `/api/viewer/stream?storageId=${this.property.storageId}&location=${btoa(encodeURIComponent(this.property.path))}`
    }
  },
  data() {
    this.detectVideo(this.property.path)
    this.loadVideo(this.property.path)
    return {
      timer: null,
      options: {},
      contentStyle: {},
      loadingVideo: true,
      convertVideo: true,
      dialogVisible: true
    }
  },
  mounted() {
    const vm = this
    window.addEventListener('resize', function() {
      vm.computeDialogSize()
    }, false)
    requestAnimationFrame(vm.computeDialogSize.bind(vm))
  },
  destroyed() {
    this.timer && clearTimeout(this.timer)
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
    },
    detectVideo(path) {
      const vm = this
      if (!path) {
        return
      }
      detectVideo(vm.property.storageId, path).then(response => {
        vm.convertVideo = response.data !== 'CONVERTED'
        if (vm.convertVideo) {
          vm.timer = setTimeout(() => vm.detectVideo(path), 5000)
        }
      }).catch(console.error)
    },
    loadVideo(path) {
      const vm = this
      if (!path) {
        vm.loadingVideo = false
        return
      }
      const video = vm.$refs.player
      if (!video || vm.convertVideo) {
        setTimeout(() => vm.loadVideo(path), 100)
        return
      }
      vm.loadingVideo = false
      video.addEventListener('play', function() {
        vm.loadingVideo = false
      })
      video.addEventListener('progress', function() {
        vm.loadingVideo = false
        // 视频总长度
        const duration = video.duration
        if (duration <= 0) {
          return
        }
        const length = video.buffered.length
        for (let i = 0; i < length; i++) {
          // 寻找当前时间之后最近的点
          if (video.buffered.start(length - 1 - i) < video.currentTime) {
            // console.trace((video.buffered.end(length - 1 - i) / duration) * 100 + "%")
            break
          }
        }
      })
    }
  }
}
</script>
<style lang="less">
.video-viewer-dialog.el-dialog--center {
  .el-dialog__body {
    padding: 0 !important;
  }

  .viewer-container {
    width: 100%;
    overflow: auto;

    video {
      width: 100%;
      height: 100%;
      vertical-align: middle;
      background-color: black;
    }
  }
}
</style>
