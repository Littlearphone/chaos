// 根据这里的方案改造https://segmentfault.com/q/1010000017202682
// 不可见的渲染行数，存在于可见区域的前后，这样上下滚动都有缓冲
const windowSize = 50
let timeout = false
const setRowDisableNone = function(startRow, endRow, binding) {
  if (timeout) {
    // 避免滚动过快出现反复横跳
    cancelAnimationFrame(timeout)
  }
  timeout = requestAnimationFrame(() => binding.value.call(null, startRow, endRow))
}
const queryOrCreate = function(selector, type) {
  return document.querySelector(selector) || document.createElement(type)
}

function computeTableScrollSize(node, binding) {
  const currentSize = parseInt(sessionStorage.getItem('windowing-currentSize'))
  // 直接取element plus的dom元素，用于计算填充量
  const wrapper = node.querySelector('.el-table__body-wrapper')
  const selectRow = wrapper.querySelector('table tr.el-table__row')
  // 算可见区域的行数，目前来看主要是不太平滑，索引值切换的时候行会跳动
  const definedHeight = (selectRow && selectRow.clientHeight) || 50
  const visibleRows = Math.round(wrapper.clientHeight / definedHeight)
  sessionStorage.setItem('windowing-visibleRows', String(visibleRows))
  if (currentSize <= 2 * windowSize + visibleRows) {
    sessionStorage.setItem('windowing-fillHeight', '0')
    sessionStorage.setItem('windowing-fillRows', '0')
    queryOrCreate('tr.head-fill-row', 'tr').style.height = '0'
    queryOrCreate('tr.tail-fill-row', 'tr').style.height = '0'
    return
  }
  // 使用前后填充行，上下滚动都做缓冲
  const virtualHead = queryOrCreate('tr.head-fill-row', 'tr')
  virtualHead.style.height = '0'
  virtualHead.className = 'head-fill-row'
  const virtualTail = queryOrCreate('tr.tail-fill-row', 'tr')
  const fillRows = currentSize - visibleRows - 2 * windowSize
  const fillHeight = fillRows * definedHeight
  virtualTail.style.height = `${fillHeight}px`
  virtualTail.className = 'tail-fill-row'
  const body = wrapper.querySelector('table tbody')
  body.prepend(virtualHead)
  body.append(virtualTail)
  const timestamp = String(new Date().getTime())
  sessionStorage.setItem('windowing-timestamp', timestamp)
  sessionStorage.setItem('windowing-fillHeight', String(fillHeight))
  sessionStorage.setItem('windowing-fillRows', String(fillRows))
  wrapper.addEventListener('scroll', function onScroll() {
    if (timestamp !== sessionStorage.getItem('windowing-timestamp')) {
      wrapper.removeEventListener('scroll', onScroll)
      return
    }
    const fillHeight = parseInt(sessionStorage.getItem('windowing-fillHeight'))
    const fillRows = parseInt(sessionStorage.getItem('windowing-fillRows'))
    const visibleRows = parseInt(sessionStorage.getItem('windowing-visibleRows'))
    const currentSize = parseInt(sessionStorage.getItem('windowing-currentSize'))
    // 目前的滚动不够平滑，有缘再琢磨了
    const scrollTop = this.scrollTop - windowSize * definedHeight
    const headHeight = Math.min(Math.max(scrollTop, 0), fillHeight)
    queryOrCreate('tr.head-fill-row', 'tr').style.height = `${headHeight}px`
    queryOrCreate('tr.tail-fill-row', 'tr').style.height = `${fillHeight - headHeight}px`
    // body.style.transform = `translateY(${scrollTop}px)`
    const startRow = Math.min(Math.max(Math.round(headHeight / definedHeight), 0), fillRows)
    const endRow = Math.min(2 * windowSize + visibleRows + startRow, currentSize)
    setRowDisableNone(startRow, endRow, binding)
  })
}

export default {
  name: 'windowing',
  command: function(node, binding, currentNode, previewNode) {
    requestAnimationFrame(() => {
      const currentSize = (currentNode && currentNode.props['data-size']) || 0
      const previewSize = (previewNode && previewNode.props['data-size']) || 0
      // 前后的总记录数一致者没必要变更填充
      if (currentSize === previewSize) {
        return
      }
      sessionStorage.setItem('windowing-currentSize', String(currentSize))
      computeTableScrollSize(node, binding)
    })
  }
}
