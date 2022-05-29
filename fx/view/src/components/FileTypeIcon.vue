<template>
  <svg-icon :icon-class="icon"></svg-icon>
</template>
<script>
import { ICON_CLASS } from '../lib/common'

export default {
  props: {
    row: Object,
    index: Number
  },
  computed: {
    icon() {
      const type = this.row.type
      if (!type) {
        return 'back'
      }
      if (this.row.link && type === 'folder') {
        return 'link'
      }
      if (ICON_CLASS.hasOwnProperty(type)) {
        return ICON_CLASS[type]
      }
      return 'file'
    },
    suffix() {
      const path = this.row.path
      const index = path && path.lastIndexOf('.')
      if (index >= 0) {
        return path.substring(index + 1)
      }
      return ''
    }
  },
  data() {
    return {}
  },
  methods: {}
}
</script>
<style lang="less">
i[class^="el-icon"][file-icon], i[class*=" el-icon"][file-icon] {
  display: flex;
  width: fit-content;
  text-align: center;
  flex-direction: column;
}
</style>
