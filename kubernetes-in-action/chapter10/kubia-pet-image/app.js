const http = require('http')
const os = require('os')
const fs = require('fs')

const dataFile = '/var/data/kubia.txt'

function fileExists(file) {
    try {
        fs.statSync(file);
        return true;
    } catch (e) {
        return false;
    }
}

let www = http.createServer((req, res) => {
    if (req.method == 'POST') {
        var file = fs.createWriteStream(dataFile)
        file.on('open', fd => {
            req.pipe(file)
            console.log(`New data has been received and stored.`)
            res.writeHead(200)
            res.end(`Data stored on pod ${os.hostname()}\n`)
        })
    } else {
        var data = fileExists(dataFile) ? fs.readFileSync(dataFile, 'utf8') : `No data posted yet`
        res.writeHead(200)
        res.write(`You've hit ${os.hostname()}\n`)
        res.end(`Data stored on this pod: ${data}\n`)
    }
})
www.listen(8080)
