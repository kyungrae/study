const http = require("http")
const os = require('os')

var www = http.createServer((req, res) => {
    console.log(`Received request from ${req.socket.remoteAddress}`)
    res.writeHead(200)
    res.end(`This is v4 running in pod ${os.hostname()}\n`)
})
www.listen(8080)
