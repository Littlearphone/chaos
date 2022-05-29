<template>
  <el-card class="main-panel" v-loading="loadingVideos">
    <template #header>
      <div class="card-header">
        <span>卡片名称</span>
        <div>
          <el-button type="primary" icon="el-icon-video-play" circle></el-button>
          <el-button type="primary" icon="el-icon-view" circle></el-button>
        </div>
      </div>
    </template>
    <el-scrollbar>
      <video-list :videos="videos"></video-list>
    </el-scrollbar>
  </el-card>
</template>

<script>
import { loadMovies } from './apis'
import VideoList from './components/VideoList.vue'

export default {
  components: { VideoList },
  mounted() {
    loadMovies().then(response => {
      const data = response.data
      data.forEach(item => {
        const poster = item.posters.map(poster => {
          if (!poster || poster.startsWith('http')) {
            return poster
          }
          return `/rs/poster?uri=${encodeURI(poster)}`
        }).shift()
        this.videos.push({
          poster,
          country: item.country,
          title: item.title,
          hash: item.hash,
          year: item.year,
          loading: true
        })
      })
      this.loadingVideos = false
    }).catch(error => console.log(error))
  },
  data() {
    return {
      loadingVideos: true,
      videos: []
    }
  },
  methods: {}
}
</script>
<style>
html, body, .main-panel {
  margin: 0;
  padding: 0;
  height: 100%;
}

.display-flex {
  display: flex;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  padding: 8px;
  height: calc(100% - 16px);
}

.main-panel .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.main-panel > .el-card__body {
  height: calc(100% - 120px);
}
</style>