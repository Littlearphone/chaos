<template>
  <div class="image-wrapper" v-if="hideOnClickModal">
    <div class="el-image-film__wrapper" v-show="thumbnails.length > 1">
      <div class="el-image-film">
        <div
            :class="['el-image'].concat(customThumbnailClass(item, index)).join(' ')"
            @click="handleSwitchPreview(index - selected)"
            v-for="(item, index) in thumbnails"
            :key="item.path"
        >
          <img :src="url(item.path)" alt="">
        </div>
      </div>
    </div>
    <div class="el-image-viewer__wrapper">
      <div class="el-image-viewer__mask" @click="handleClosePreview"></div>
      <span class="el-image-viewer__btn el-image-viewer__close" @click="handleClosePreview">
        <el-icon><close-bold /></el-icon>
      </span>
      <span
          class="el-image-viewer__btn el-image-viewer__prev"
          @click="handleSwitchPreview(-1)"
          v-if="thumbnails.length > 1"
      >
        <el-icon><arrow-left-bold /></el-icon>
      </span>
      <span
          class="el-image-viewer__btn el-image-viewer__next"
          @click="handleSwitchPreview(1)"
          v-if="thumbnails.length > 1"
      >
        <el-icon><arrow-right-bold /></el-icon>
      </span>
      <div class="el-image-viewer__btn el-image-viewer__actions">
        <div class="el-image-viewer__actions__inner">
          <el-icon @click="imageZoomOut">
            <zoom-out />
          </el-icon>
          <el-icon @click="imageZoomIn">
            <zoom-in />
          </el-icon>
          <i class="el-image-viewer__actions__divider"></i>
          <el-icon class="el-icon-full-screen" ref="toggleScale" @click="toggleScaleAndOriginal">
            <full-screen />
          </el-icon>
          <i class="el-image-viewer__actions__divider"></i>
          <el-icon @click="imageTurnLeft">
            <refresh-left />
          </el-icon>
          <el-icon @click="imageTurnRight">
            <refresh-right />
          </el-icon>
        </div>
      </div>
      <div class="el-image-viewer__canvas">
        <img
            alt=""
            :src="previewList[property.index]"
            class="el-image-viewer__img"
            :style="computedImageStyle"
            @mousedown="handleMouseDown"
            @touchstart="handleMouseDown"
        >
      </div>
    </div>
  </div>
</template>
<script>
import {
  ArrowLeftBold,
  ArrowRightBold,
  CloseBold,
  ZoomIn,
  ZoomOut,
  FullScreen,
  RefreshLeft,
  RefreshRight
} from '@element-plus/icons-vue'

const fullScreenMode = {
  scale: 1,
  rotate: 0,
  offsetX: 0,
  offsetY: 0,
  maxWidth: '100%',
  maxHeight: '100%'
}
const originalMode = {
  scale: 1,
  rotate: 0,
  offsetX: 0,
  offsetY: 0,
  maxWidth: '',
  maxHeight: ''
}
export default {
  props: {
    property: {
      type: Object,
      default: () => ({})
    }
  },
  components: {
    ZoomIn,
    ZoomOut,
    CloseBold,
    FullScreen,
    RefreshLeft,
    RefreshRight,
    ArrowLeftBold,
    ArrowRightBold
  },
  watch: {
    property(value) {
      this.hideOnClickModal = true
      this.bindKeydownEvent()
    }
  },
  emits: ['changeSelected'],
  computed: {
    thumbnails: function() {
      const list = this.property.list
      if (!list) {
        return []
      }
      const index = this.property.index
      if (list.length <= 5) {
        this.selected = index
        return list
      }
      this.selected = 2
      const start = index - 2
      const end = index + 3
      if (start < 0) {
        return list.slice(start).concat(list.slice(0, end))
      }
      if (end >= list.length) {
        return list.slice(start, list.length).concat(list.slice(0, end - list.length))
      }
      return list.slice(start, end)
    },
    previewList: function() {
      return this.property.list.map(item => this.url(item.path))
    },
    computedImageStyle: function() {
      const style = this.previewImageStyle
      return {
        marginTop: `${style.offsetY}px`,
        marginLeft: `${style.offsetX}px`,
        maxWidth: style.maxWidth,
        maxHeight: style.maxHeight,
        transform: `scale(${style.scale}) rotate(${style.rotate}deg)`
      }
    }
  },
  data() {
    this.bindWheelEvent()
    this.bindKeydownEvent()
    const index = this.property.index
    return {
      selected: index,
      hideOnClickModal: true,
      previewImageStyle: {
        scale: 1,
        rotate: 0,
        offsetX: 0,
        offsetY: 0,
        maxWidth: '100%',
        maxHeight: '100%'
      }
    }
  },
  methods: {
    url: function(path) {
      return `/api/viewer/file?storageId=${this.property.storageId}&location=${btoa(encodeURIComponent(path))}`
    },
    toggleScaleAndOriginal() {
      const originalClass = 'el-icon-c-scale-to-original'
      const fullScreenClass = 'el-icon-full-screen'
      const toggleScale = this.$refs.toggleScale.$el
      if (toggleScale.classList.contains(originalClass)) {
        Object.assign(this.previewImageStyle, fullScreenMode)
        toggleScale.classList.replace(originalClass, fullScreenClass)
      } else {
        Object.assign(this.previewImageStyle, originalMode)
        toggleScale.classList.replace(fullScreenClass, originalClass)
      }
    },
    handleMouseDown(e) {
      const ee = e.touches ? e.touches[0] : e
      const {
        offsetX,
        offsetY
      } = this.previewImageStyle
      const vm = this
      const startX = ee.pageX
      const startY = ee.pageY
      const _dragHandler = function(ev) {
        const event = ev.touches ? ev.touches[0] : ev
        vm.previewImageStyle = Object.assign({}, vm.previewImageStyle, {
          offsetX: offsetX + event.pageX - startX,
          offsetY: offsetY + event.pageY - startY
        })
      }
      document.addEventListener('mousemove', _dragHandler)
      document.addEventListener('mouseup', () => {
        document.removeEventListener("mousemove", _dragHandler)
      })
      document.addEventListener('touchmove', _dragHandler)
      document.addEventListener('touchend', () => {
        document.removeEventListener("touchmove", _dragHandler)
      })
      e.preventDefault()
    },
    imageZoomOut() {
      this.imageTransform(-0.1)
    },
    imageZoomIn() {
      this.imageTransform(0.1)
    },
    imageTransform(zoomDiffer, rotateDiffer) {
      const style = this.previewImageStyle
      const scale = style.scale + (zoomDiffer || 0)
      style.scale = Math.min(Math.max(scale, 0.1), 10)
      style.rotate += (rotateDiffer || 0)
    },
    imageTurnLeft() {
      this.imageTransform(0, -90)
    },
    imageTurnRight() {
      this.imageTransform(0, 90)
    },
    bindWheelEvent() {
      const vm = this
      const timestamp = String(new Date().getTime())
      sessionStorage.setItem('image-mousewheel-timestamp', timestamp)
      document.addEventListener('mousewheel', function keydown(e) {
        if (timestamp !== sessionStorage.getItem('image-mousewheel-timestamp')) {
          document.removeEventListener('mousewheel', keydown)
          return
        }
        const delta = e.wheelDelta ? e.wheelDelta : -e.detail
        if (delta > 0) {
          vm.imageTransform(0.1)
        } else {
          vm.imageTransform(-0.1)
        }
      })
    },
    bindKeydownEvent() {
      const vm = this
      const timestamp = String(new Date().getTime())
      sessionStorage.setItem('image-viewer-timestamp', timestamp)
      document.addEventListener('keydown', function keydown(event) {
        if (timestamp !== sessionStorage.getItem('image-viewer-timestamp')) {
          document.removeEventListener('keydown', keydown)
          return
        }
        switch (event.code) {
          case 'ArrowLeft':
            vm.handleSwitchPreview(-1)
            return
          case 'ArrowRight':
            vm.handleSwitchPreview(1)
            return
          default:
        }
      })
    },
    handleSwitchPreview(differ) {
      const length = this.property.list.length
      this.$emit('changeSelected', (this.property.index + differ + length) % length)
    },
    customThumbnailClass(item, index) {
      if (this.selected === index) {
        return ['activated-thumbnail']
      }
      return []
    },
    handleClosePreview() {
      this.hideOnClickModal = false
    }
  }
}
</script>
<style lang="less">
.image-wrapper {
  position: fixed;
  z-index: 2020;

  .el-image-film__wrapper {
    left: 0;
    right: 0;
    z-index: 10;
    margin: auto;
    bottom: 10px;
    position: fixed;
    width: fit-content;
    justify-items: center;

    .el-image-film {
      height: 50px;
      padding: 4px;
      display: flex;
      align-items: center;
      border-radius: 0;
      background-color: var(--el-text-color-regular);

      .el-image {
        width: 50px;
        height: 50px;
        display: flex;
        text-align: center;
        align-items: center;
        justify-content: center;
        border: 1px solid transparent;

        &:hover, &.activated-thumbnail {
          cursor: pointer;
          border: 1px solid burlywood;
        }

        img {
          max-width: 100%;
          max-height: 100%;
        }
      }
    }
  }

  .el-image-viewer__wrapper {
    .el-image-viewer__btn, [class*='el-icon-'] {
      cursor: pointer;
    }

    .el-image-viewer__actions {
      bottom: 80px;
    }

    .el-image-viewer__img {
      transition: transform 0.3s ease 0s;
    }
  }

  //left: 0;
  //right: 0;
  //top: 10px;
  //height: 60px;
  //margin: auto;
  //z-index: 2020;
  //position: fixed;
  //width: fit-content;
  //background-color: var(--el-text-color-regular);
  //
  //.el-image {
  //  width: 50px;
  //  height: 50px;
  //  margin: 5px;
  //}
}
</style>
