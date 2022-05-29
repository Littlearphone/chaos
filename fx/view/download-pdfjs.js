const Fs = require('fs')
const Https = require('https')
const Axios = require('axios').default
const ProgressBar = require('progress')
const { JSDOM } = require("jsdom")
const httpsAgent = new Https.Agent({ rejectUnauthorized: false })
const vgmUrl = 'https://mozilla.github.io/pdf.js/getting_started/#download'
Axios.get(vgmUrl, { httpsAgent }).then(response => {
  const dom = new JSDOM(response.data)
  const button = dom.window.document.querySelector('a[href*="-dist.zip"]')
  const url = button.href
  console.log('Connecting file server...')
  Axios({
    url,
    httpsAgent,
    method: 'GET',
    responseType: 'stream'
  }).then((response) => {
    const name = 'public/pdfjs.zip'
    console.log(`Starting download -> ${name}`)
    const file = Fs.createWriteStream(name)
    const progressBar = new ProgressBar('-> downloading [:bar] :percent :rate/bps :etas', {
      width: 40,
      complete: '=',
      incomplete: ' ',
      renderThrottle: 1,
      total: parseInt(response.headers['content-length'])
    })
    const data = response.data
    data.on('data', (chunk) => progressBar.tick(chunk.length))
    data.pipe(file)
    file.on('finish', () => {
      file.close()
      console.log(`File downloaded!`)
      // Fs.createReadStream(name).pipe(require('unzip').Extract({ path: 'path' }))
    })
  })
}).catch(err => {
  console.log(err)
})
