<template>
  <div class="display-flex video-list-wrapper">
    <el-card v-for="video in videos" :key="video.title" shadow="hover">
      <div class="video-cover-outer">
        <el-image :src="video.poster" lazy v-loading="video.loading" @load="imageLoad(video)" @error="imageError(video)">
          <template #placeholder>
            <div class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </template>
          <template #error>
            <div class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </template>
        </el-image>
      </div>
      <div class="video-general-bottom">
        <div class="video-title" :title="video.title">{{ video.title }}</div>
        <div class="display-flex">
          <time class="time">{{ video.year }}</time>
          <div class="button-group">
            <el-button type="primary" icon="el-icon-video-play" circle size="mini" @click="playVideo(video)"></el-button>
            <el-button type="primary" icon="el-icon-view" circle size="mini" @click="playVideo(video)"></el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
  <video-box :detail="videoDetail"></video-box>
</template>
<script>
import VideoBox from './VideoBox.vue'

export default {
  components: { VideoBox },
  props: {
    videos: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      videoDetail: { visible: false }
    }
  },
  methods: {
    imageLoad(video) {
      video.loading = false
    },
    imageError(video) {
      video.loading = false
    },
    playVideo(video) {
      this.videoDetail = Object.assign({}, {
        visible: true,
        title: video.title,
        hash: video.hash,
        poster: video.poster,
        country: video.country,
        year: video.year
      })
    }
  }
}
</script>
<style>
.video-list-wrapper {
  flex-wrap: wrap;
  justify-content: space-evenly;
}

.video-list-wrapper .el-card {
  max-width: 150px;
  margin: 8px auto;
}

.video-list-wrapper .el-card:hover {
  transform: scale(1.1);
}

.video-list-wrapper .video-cover-outer {
  max-height: 192px;
  text-align: center;
  overflow: hidden;
}

.video-list-wrapper .video-cover-outer img {
  width: 100%;
}

.video-list-wrapper .video-cover-outer .image-slot {
  line-height: 150px;
  width: 46px;
}

.video-list-wrapper .el-card__body {
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.video-list-wrapper .video-general-bottom {
  padding: 8px;
}

.video-list-wrapper .video-general-bottom .video-title {
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.video-list-wrapper .video-general-bottom .display-flex {
  justify-content: space-between;
}

.video-list-wrapper .video-general-bottom time,
.video-list-wrapper .video-general-bottom div.button-group {
  line-height: 40px;
}
</style>