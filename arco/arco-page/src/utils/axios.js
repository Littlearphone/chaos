import axios from "axios";

// 这边由于后端没有区分测试和正式，姑且都写成一个接口。
axios.defaults.baseURL = '/rs'
// 携带 cookie，对目前的项目没有什么作用，因为我们是 token 鉴权
axios.defaults.withCredentials = true
// 请求头，headers 信息
axios.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'
// 默认 post 请求，使用 application/json 形式
axios.defaults.headers.post['Content-Type'] = 'application/json'

// 请求拦截器，内部根据返回值，重新组装，统一管理。
axios.interceptors.response.use(response => {
    if (typeof response.data !== 'object') {
        this.$message.error('错误的数据类型！')
        return Promise.reject(response)
    }
    if (response.status !== 200) {
        this.$message.error(`[${response.status}]${response.statusText}！`)
        return Promise.reject(response.data)
    }
    return response
})

export default axios