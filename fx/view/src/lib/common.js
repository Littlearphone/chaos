export const directoryFirst = (a, b) => {
  if (a.type === b.type && a.type === 'folder') {
    return a.name.localeCompare(b.name)
  }
  if (a.type === 'folder') {
    return -1
  }
  if (b.type === 'folder') {
    return 1
  }
  return a.name.localeCompare(b.name)
}
const SIZE_UNIT = ['B', 'KB', 'MB', 'GB', 'TB', 'EB', 'PB']
export const unitFormatter = (size) => {
  if (typeof size === 'undefined') {
    return ''
  }
  let output = size
  const length = SIZE_UNIT.length
  for (let i = 0; i < length; i++) {
    if (output < 1024) {
      return output.toFixed(2) + SIZE_UNIT[i]
    }
    output /= 1024
  }
  return output.toFixed(2) + SIZE_UNIT[SIZE_UNIT.length - 1]
}
export const monacoTypeConvert = contentType => {
  if (contentType.toUpperCase() === 'TEXT/HTML') {
    return 'html'
  }
  if (contentType.toUpperCase() === 'TEXT/CSS') {
    return 'css'
  }
  if (contentType.toUpperCase() === 'APPLICATION/JAVASCRIPT') {
    return 'javascript'
  }
  if (contentType.toUpperCase() === 'APPLICATION/JSON') {
    return 'json'
  }
  return 'plaintext'
}
export const hiddenXsOnly = () => {
  return document.documentElement.clientWidth > 767
}
export const isAudio = (type) => /^(mp3|m4a|flac|ape|wma|aac|wav)$/i.test(type)
export const isImage = (type) => /^(jpg|jpeg|gif|webp|png|bmp|ico|svg)$/i.test(type)
export const VIEWER_MAPPING = [
  {
    viewer: 'MarkdownViewer',
    pattern: /^(md)$/i
  },
  {
    viewer: 'PdfFileViewer',
    pattern: /^(pdf)$/i
  },
  // {
  //   viewer: 'AudioFileViewer',
  //   pattern: /mp3|m4a|flac|ape|wma|aac/i
  // },
  {
    viewer: 'VideoFileViewer',
    pattern: /^(mp4|avi|mkv|ts|wmv)$/i
  },
  // {
  //   viewer: 'DocxFileViewer',
  //   pattern: /doc|docx/i
  // },
  {
    viewer: 'TextPlainViewer',
    pattern: /^(sql|url|ass|ssa|srt|sh|txt|bat|js|json|css|xml|less|vbs|^log|ps1|cmd|java|properties|ini|yml|kt|c|h|cpp|conf)$/i
  }
]
export const ICON_CLASS = {
  'folder': 'folder',
  'tar.xy': 'archive',
  'tar.gz': 'archive',
  'tar': 'archive',
  'rar': 'archive',
  'zip': 'archive',
  '7z': 'archive',
  'xz': 'archive',
  'doc': 'word',
  'docx': 'word',
  'csv': 'excel',
  'xls': 'excel',
  'xlsx': 'excel',
  'xlsm': 'excel',
  'pptx': 'ppt',
  'reg': 'reg',
  'jar': 'java',
  'exe': 'microsoft',
  'msi': 'microsoft',
  'deb': 'deb',
  'url': 'url',
  'md': 'md',
  'php': 'php',
  'py': 'py',
  'ts': 'file',
  'torrent': 'torrent',
  'apk': 'android',
  'bat': 'script',
  'vbs': 'script',
  'sh': 'shell',
  'txt': 'text',
  'log': 'log',
  'pdf': 'pdf',
  'umd': 'book',
  'iso': 'disc',
  'img': 'disc',
  'mp3': 'mp3',
  'm4a': 'm4a',
  'wav': 'audio',
  'aac': 'audio',
  'ape': 'audio',
  'flac': 'flac',
  'png': 'image',
  'jpg': 'image',
  'jpeg': 'image',
  'ico': 'image',
  'gif': 'image',
  'webp': 'image',
  'mp4': 'video',
  'avi': 'video',
  'rm': 'video',
  'rmvb': 'video',
  'wmv': 'video',
  'mkv': 'video',
  'ass': 'subtitle',
  'ssa': 'subtitle',
  'srt': 'subtitle',
  'js': 'js',
  'properties': 'text',
  'json': 'json',
  'css': 'css',
  'html': 'html',
  'xml': 'xml',
  'less': 'less',
  'ps1': 'text',
  'cmd': 'text',
  'java': 'java',
  'config': 'config',
  'conf': 'config',
  'cfg': 'config',
  'cpp': 'text',
  'yml': 'text',
  'ini': 'config',
  'kt': 'text',
  'c': 'text',
  'h': 'text',
  'vue': 'vue',
  'db': 'db',
  'sql': 'sql',
  'epub': 'application'
}
