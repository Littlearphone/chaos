<template>
  <svg :class="svgClass" aria-hidden="true">
    <use :xlink:href="iconName"></use>
  </svg>
</template>
<script>
export default {
  name: 'svg-icon',
  props: {
    iconClass: {
      type: String,
      required: true
    },
    className: {
      type: String
    }
  },
  computed: {
    iconName() {
      return `#icon-${this.iconClass}`
    },
    svgClass() {
      return `svg-icon ${this.className || ''}`
    }
  }
}
</script>
<style scoped>
.svg-icon {
  width: 1.5em;
  height: 1.5em;
  padding: 0 4px;
  overflow: hidden;
  line-height: 1em;
  fill: currentColor;
  vertical-align: middle;
}
</style>
