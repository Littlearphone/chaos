import { readFileSync, readdirSync } from 'fs'

const prefix = 'icon'
const svgTitle = /<svg([^>+].*?)>/
const clearHeightWidth = /(width|height)="([^>+].*?)"/g
const hasViewBox = /(viewBox="[^>+].*?")/g
const clearReturn = /(\r)|(\n)/g

function findSvgFile(dir) {
  const resources = []
  const paths = readdirSync(dir, { withFileTypes: true })
  for (const path of paths) {
    if (path.isDirectory()) {
      resources.push(...findSvgFile(dir + path.name + '/'))
    } else {
      const svg = readFileSync(dir + path.name)
        .toString()
        .replace(clearReturn, '')
        .replace(svgTitle, ($1, $2) => {
          let width = 0
          let height = 0
          let content = $2.replace(clearHeightWidth, (s1, s2, s3) => {
            if (s2 === 'width') {
              width = s3
            } else if (s2 === 'height') {
              height = s3
            }
            return ''
          })
          if (!hasViewBox.test($2)) {
            content += `viewBox="0 0 ${width} ${height}"`
          }
          return `<symbol id="${prefix}-${path.name.replace('.svg', '')}" ${content}>`
        })
        .replace('</svg>', '</symbol>')
      resources.push(svg)
    }
  }
  return resources
}

export const svgBuilder = (path) => {
  if (path === '') {
    return
  }
  const res = findSvgFile(path)
  return {
    name: 'svg-transform',
    transformIndexHtml(html) {
      return html.replace('<body>', `<body><svg xmlns="http://www.w3.org/2000/svg"  style="position: absolute; width: 0; height: 0">${res.join('')}</svg>`)
    }
  }
}
