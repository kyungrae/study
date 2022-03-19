const http = require('http')
const os = require('os')
const fs = require('fs')
const dns = require('dns')
const dataFile = "/var/data/kubia.txt"
const serviceName = "kubia.default.svc.cluster.local"
const port = 8080

function fileExists(file) {
    try {
        fs.statSync(file);
        return true;
    } catch (e) {
        return false;
    }
}

function httpGet(reqOptions, callback) {
    return http.get(reqOptions, function (res) {
        let body = ''
        res.on('data', d => body += d)
        res.on('end', () => callback(body))
    }).on('error', e => callback(`Error: ${e.message}`))
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
        res.writeHead(200)
        if (req.url == '/data') {
            var data = fileExists(dataFile) ? fs.readFileSync(dataFile, 'utf8') : `No data posted yet`
            res.end(data)
        } else {
            res.write(`You've hit ${os.hostname()}\n`)
            res.write(`Data stored in the cluster:\n`)
            dns.resolveSrv(serviceName, (err, addresses) => {
                if (err) {
                    res.end(`Could not look up DNS SRV records: ${err}`)
                    return
                }
                let numResponses = 0
                if (addresses.length == 0) {
                    res.end("No peers discovered.")
                } else {
                    addresses.forEach(item => {
                        let requestOptions = {
                            host: item.name,
                            port: port,
                            path: '/data'
                        }
                        httpGet(requestOptions, data => {
                            numResponses++
                            res.write(`- ${item.name}: ${data}\n`)
                            if (numResponses == addresses.length) {
                                res.end()
                            }
                        })
                    })
                }
            })
        }
    }
})
www.listen(8080)
