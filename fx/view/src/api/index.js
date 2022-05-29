import axios from 'axios'

export const esStatus = function() {
  return axios.get('/api/everything/status')
}
export const storages = function() {
  return axios.get('/api/storages')
}
export const storageFiles = function(id, paths) {
  const relativePath = btoa(encodeURIComponent(paths.join('/')))
  return axios.get(`/api/storage/${id}/files`, { params: { relativePath } })
}
export const searchFiles = function(id, path, source) {
  const relativePath = btoa(encodeURIComponent(path))
  return axios.get(`/api/storage/${id}/es`, {
    params: { relativePath },
    cancelToken: source.token
  })
}
export const textContent = function(storageId, path) {
  const location = btoa(encodeURIComponent(path))
  return axios.get('/api/viewer/text', {
    params: {
      storageId,
      location
    }
  })
}
export const saveStorage = function(storage) {
  return axios.post('/api/storage', storage)
}
export const deleteStorage = function(storageId) {
  return axios.delete(`/api/storage/${storageId}`)
}
export const audioMetadata = function(location) {
  return axios.get('/api/audio/metadata', { params: { location } })
}
