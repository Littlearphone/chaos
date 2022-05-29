import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/theme-chalk/index.css'
import loadMore from './lib/windowing'
import { router } from './router'
import bus from './lib/event-bus'
import SvgIconFont from './components/SvgIconFont.vue'

const app = createApp(App)
app.config.globalProperties.$bus = bus
app.directive(loadMore.name, loadMore.command)
  .component('svg-icon', SvgIconFont)
  .use(ElementPlus)
  .use(router)
  .mount('#app')
