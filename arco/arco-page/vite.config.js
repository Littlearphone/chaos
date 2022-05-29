import vue from '@vitejs/plugin-vue'

/**
 * @type {import('vite').UserConfig}
 */
export default {
    build: {outDir: 'target/classes/META-INF/static'},
    plugins: [vue()],
    server: {
        proxy: {
            '/rs': 'http://localhost:8080/',
        }
    }
}
