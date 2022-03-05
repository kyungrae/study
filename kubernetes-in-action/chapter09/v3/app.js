const http = require("http")
const os = require('os')

var requestCount = 0;

var www = http.createServer((req, res) => {
    console.log(`Received request from ${req.socket.remoteAddress}`)
    if (++requestCount >= 5) {
        res.writeHead(500)
        res.end(`Some internal error has occured! This is pod ${os.hostname()}\n`)
        return;
    }
    res.writeHead(200)
    res.end(`This is v3 running in pod ${os.hostname()}\n`)
})
www.listen(8080)
