<template>
  <el-dialog
    v-model="detail.visible"
    :custom-class="'video-player-dialog'"
    :close-on-click-modal="closeOnClickModal"
    :destroy-on-close="destroyOnClose"
    :show-close="showClose"
    v-if="detail.visible"
    center
  >
    <!--<div slot="title" class="el-dialog__header">-->
    <!--  <span>-->
    <!--    <i class="el-icon-video-play"></i>-->
    <!--    <span>{{ detail.title }}</span>-->
    <!--  </span>-->
    <!--</div>-->
    <div class="video-player-wrapper">
      <video ref="player" :src="videoSrc" autoplay="autoplay" loop="loop" controls="controls"></video>
    </div>
    <div class="video-descriptions">
      <div class="video-poster-thumbnail">
        <el-image :src="detail.poster"></el-image>
      </div>
      <div>
        <div class="video-descriptions-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>
              <span class="video-descriptions-title">{{ detail.title }}</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item><span>{{ detail.year }}</span></el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="video-descriptions-countries">
          <el-tag v-for="item in detail.country" :key="item">{{ item }}</el-tag>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer video-dialog-footer">
        <el-button icon="el-icon-circle-close video-dialog-close-button" circle @click="closeDialog"></el-button>
      </span>
    </template>
  </el-dialog>
</template>
<script>
export default {
  props: {
    detail: {
      type: Object,
      default: () => ({
        title: '视频详情',
        visible: false,
        hash: []
      })
    }
  },
  computed: {
    directSrc() {
      const detail = this.detail
      const hash = detail.hash
      if (!hash || !hash.length) {
        return ''
      }
      return hash.slice(0, 1)
    }
  },
  watch: {
    directSrc(newSrc, oldSrc) {
      if (newSrc === oldSrc) {
        return
      }
      this.videoSrc = `/rs/movie/${newSrc}`
      this.$nextTick(() => {
        const video = this.$refs.player
        video.addEventListener('progress', function () {
          // 视频总长度
          let duration = video.duration
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
      })
    }
  },
  data() {
    return {
      videoSrc: '',
      showClose: false,
      destroyOnClose: true,
      closeOnClickModal: false
    }
  },
  methods: {
    closeDialog() {
      this.detail.visible = false
    }
  }
}
</script>
<style>
div.el-overlay {
  backdrop-filter: blur(10px);
}

.video-player-dialog.el-dialog {
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  margin: auto !important;
  position: fixed;
  min-width: 300px;
  box-shadow: unset;
  height: fit-content;
  background-color: transparent;
}

.video-player-dialog.el-dialog--center .el-dialog__body {
  padding: 0;
  margin-top: -30px;
  font-size: 18px;
  background-color: #fff;
  box-shadow: 0 0 10px rgb(255 255 255);
}

.video-player-wrapper {
  display: flex;
}

.video-player-wrapper video {
  width: 100%;
  min-height: 300px;
  background-color: darkgray;
}

.video-descriptions {
  display: flex;
}

.video-descriptions > div {
  margin: 8px;
}

.video-descriptions > div.video-poster-thumbnail {
  width: 100px;
}

.video-descriptions-header span {
  margin: 4px 2px;
}

span.video-descriptions-title {
  font-size: 16px;
  font-weight: bold;
}

.video-descriptions-countries {
  margin: 10px 0;
}

.video-descriptions-countries .el-tag {
  margin: 0 4px;
}

.video-dialog-footer > .el-button.is-circle {
  font-size: 42px;
  padding: 0;
  background-color: transparent;
  border: transparent;
  color: #ccc;
}
</style>