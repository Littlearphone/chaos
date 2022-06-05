import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import { svgBuilder } from './src/plugins/svg-builder'
import pluginRewriteAll from 'vite-plugin-rewrite-all'

const prefix = `monaco-editor/esm/vs`
/**
 * @type {import('vite').UserConfig}
 */
export default defineConfig({
  plugins: [vue(), pluginRewriteAll(), svgBuilder('./src/icons/svg/')],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          jsonWorker: [`${prefix}/language/json/json.worker`],
          cssWorker: [`${prefix}/language/css/css.worker`],
          htmlWorker: [`${prefix}/language/html/html.worker`],
          tsWorker: [`${prefix}/language/typescript/ts.worker`],
          editorWorker: [`${prefix}/editor/editor.worker`]
        }
      }
    },
    outDir: 'target/classes/static'
  },
  define: {
    'process.env': { 'VUE_ENV': 'server' }
  },
  server: {
    host: '0.0.0.0',
    port: 8888,
    proxy: {
      '/api': {
        target: 'http://localhost:6666',
        changeOrigin: true
      }
    }
  }
})
