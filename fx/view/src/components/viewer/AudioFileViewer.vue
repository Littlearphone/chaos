<template>
  <div
      ref="container"
      v-if="playerVisible"
      class="aplayer-viewer-container"
  >
    <i ref="playerMinimumButton" class="player-minimum-button collapse" @click="togglePlayer"></i>
    <div
        id="audioPlayer"
        ref="audioPlayer"
        class="audio-player"
    >
    </div>
  </div>
</template>
<script>
import 'aplayer/dist/APlayer.min.css'
import APlayer from 'aplayer'
import { audioMetadata } from '../../api'

export default {
  data() {
    return {
      player: {},
      options: {
        lrcType: 1,
        fixed: true,
        autoplay: true,
        listFolded: false
      },
      loadingAudio: true,
      playerVisible: false
    }
  },
  mounted() {
    this.$nextTick(() => this.initialPlayer())
    this.$bus.$on('playAudio', data => {
      if (data) {
        this.playerVisible = true
        this.loadAudio(data)
      }
    })
  },
  methods: {
    initialPlayer() {
      const container = this.$refs.audioPlayer
      if (!container) {
        requestAnimationFrame(() => this.initialPlayer())
        return
      }
      this.player = new APlayer({
        ...this.options,
        audio: [],
        container
      })
    },
    audioFilePath(path) {
      return `/api/viewer/stream?storageId=${this.property.storageId}&location=${btoa(encodeURIComponent(path))}`
    },
    loadAudio(audio) {
      const path = audio.path
      const url = this.audioFilePath(path)
      const playerList = this.player.list || { audios: [] }
      const index = playerList.audios.findIndex(a => a.url === url)
      if (index >= 0) {
        playerList.switch && playerList.switch(index)
        return
      }
      audioMetadata(btoa(encodeURIComponent(path))).then(response => response.data).then(data => {
        // TODO delay switch item
        const list = this.player.list
        list.add([{
          url,
          lrc: data.lrc || '',
          artist: data.artist,
          cover: data.cover || '',
          name: data.name || this.property.name
        }])
        list.switch(list.audios.length - 1)
        this.showPlayer()
        if (this.player.audio.paused) {
          this.player.play()
        }
      })
    },
    togglePlayer() {
      if (this.$refs.audioPlayer.classList.contains('aplayer-narrow')) {
        this.showPlayer()
      } else {
        this.hidePlayer()
      }
    },
    showPlayer() {
      this.$refs.playerMinimumButton.classList.remove('collapse')
      this.$refs.audioPlayer.classList.remove('aplayer-narrow')
    },
    hidePlayer() {
      this.$refs.playerMinimumButton.classList.add('collapse')
      this.$refs.audioPlayer.classList.add('aplayer-narrow')
    }
  }
}
</script>
<style lang="less">
.aplayer-viewer-container {
  bottom: 0;
  width: 100%;
  z-index: 10;
  margin: 0 -8px;
  position: fixed;
  max-width: 100%;

  .player-minimum-button {
    left: 0;
    right: 0;
    top: -16px;
    z-index: 0;
    width: 48px;
    height: 16px;
    display: flex;
    margin: 0 auto;
    cursor: pointer;
    background: white;
    border-style: none;
    position: absolute;
    align-items: center;
    border-width: initial;
    border-color: initial;
    border-image: initial;
    transition: all 300ms;
    justify-content: center;
    box-shadow: grey 0 0 10px;
    border-top-left-radius: 5px;
    border-top-right-radius: 5px;

    &.collapse {
      bottom: 0;
      top: calc(100% - 16px);
      //backdrop-filter: blur(10px);
      //background-color: transparent;
    }

    &:before {
      content: " ";
      border-width: 8px;
      align-self: center;
      border-style: solid;
      border-bottom-width: 0;
      display: inline-flex;
      background: transparent;
      border-color: transparent;
      border-top-color: grey;
    }

    &.collapse:before {
      border-top-width: 0;
      border-top-color: transparent;
      border-bottom-width: 8px;
      border-bottom-color: grey;
    }
  }

  #audioPlayer {
    width: 100%;
    z-index: 2020;
    max-width: 100%;
    position: unset;
    transform: translateY(0);
    transition: transform 300ms;
    box-shadow: 0 2px 10px 0 rgb(0 0 0 / 50%);

    &.aplayer.aplayer-narrow {
      overflow: hidden;
      transform: translateY(100%);
    }

    div.aplayer-miniswitcher {
      display: none;
    }

    div.aplayer-list {
      margin-bottom: 0;
    }

    div.aplayer-body {
      position: unset;
      max-width: 100%;
      width: calc(100% - 18px) !important;

      div.aplayer-info {
        transform: scale(1);

        div.aplayer-music {
          width: calc(100% - 160px);
        }
      }

      div.aplayer-controller {
        span[class*='aplayer-icon-'],
        button.aplayer-icon-menu {
          width: 32px;
          height: 32px;
        }

        span.aplayer-icon-back {
          right: 112px;
        }

        span.aplayer-icon-play {
          right: 76px;
        }

        span.aplayer-icon-forward {
          right: 40px;
        }
      }
    }

    div.aplayer-lrc {
      p.aplayer-lrc-current {
        font-size: 16px;
        font-weight: bold;
      }
    }
  }
}
</style>
