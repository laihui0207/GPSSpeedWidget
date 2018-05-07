var express = require('express');
var qs = require('querystring');
var NodeCache = require("node-cache");
var moment = require('moment');
var bodyParser = require('body-parser');
var gpsCache = new NodeCache();
var sqlite3 = require('sqlite3').verbose();
var db = new sqlite3.Database('GPSHistory.db');
var urlencodedParser = bodyParser.urlencoded({ extended: false })

db.serialize(function () {
    db.run("CREATE TABLE IF NOT EXISTS  GPS (id integer primary key autoincrement,deviceId varchar(50), deviceId_short varchar(50),lng varchar(20), " +
        "lat varchar(20),speed varchar(10), speedValue REAL,bearingValue REAL,createTime integer,lineId integer)");
    db.run("create index deviceId on GPS(deviceId)");
    db.run("create index deviceId_short on GPS(deviceId_short)");
    db.run("create index deviceId_short_date on GPS(deviceId_short,createtime)");
    db.run("create index deviceId_short_date_lineId on GPS(deviceId_short,createtime,lineId)");
    db.run("CREATE TABLE IF NOT EXISTS feedback (id integer primary key autoincrement,name varchar(50),content varchar(500),createTime integer)");
});

var PORT = 8090;

var app = express();
app.use(express.static('.'));
app.listen(PORT, function () {
    console.log('server start at %d', PORT);
});
app.post('/gps', function (req, res) {
    var body = '';
    req.on('data', function (data) {
        body += data;
    });
    req.on('end', function () {
        var post = qs.parse(body);
        var deviceId = post['deviceId'];
        var data = post['data'];
        var json = JSON.parse(data);
        for (var i = 0; i < json.length; i++) {
            var lineId=1000;
            if(json[i].lineId!=undefined){
                lineId=json[i].lineId;
            }
            db.serialize(function () {
                db.run("insert into GPS(deviceId,lng,lat,speed,speedValue,bearingValue,createTime,lineId,deviceId_short) values(?,?,?,?,?,?,?,?,?)",
                    [deviceId, json[i].lng, json[i].lat, json[i].speed, json[i].speedValue, json[i].bearingValue, json[i].createTime,lineId,deviceId.substr(0,8)]);
            })
        }

    });
    res.end("Success");
});
app.get("/data", function (req, res) {
    //Search sql: select id,deviceId,lat,lng,strftime('%Y-%m-%d %H:%M:%S', createTime / 1000,'unixepoch', 'localtime') from GPS;
    var deviceId = req.query.deviceId;
    var startTime = req.query.startTime;
    var endTime = req.query.endTime;
    var queryStart = moment(startTime * 1);
    var queryEnd = moment(endTime * 1);
    var queryDate = queryStart.format("YYYYMMDD");
    var cacheKey = deviceId + "_" + queryDate;
    var cacheValue = gpsCache.get(cacheKey);
    if (cacheValue != undefined) {
        console.log("Get Data From Cache!");
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.write(cacheValue);
        res.end();
    }
    else {
        var sql = "select deviceId,lng,lat,speed,speedValue,bearingValue," +
            "strftime('%Y-%m-%d %H:%M:%S', createTime / 1000,'unixepoch', 'localtime') as createTime,lineId " +
            "from GPS where deviceId_short = ?";
        if (startTime != undefined) {
            sql += " and createTime > ? ";
        }
        if (endTime != undefined) {
            sql += " and createTime < ? ";
        }
        sql += " order by createTime ASC";

        db.serialize(function () {
            db.all(sql, [deviceId, startTime, endTime], function (err, rows) {
                if (err) {
                    console.log(err.message);
                    res.end("-1");
                }
                else {
                    res.writeHead(200, {'Content-Type': 'application/json'});
                    var result = JSON.stringify(rows.map(function (row) {
                        return {
                            deviceId: row.deviceId,
                            lng: row.lng,
                            lat: row.lat,
                            speed: row.speed,
                            speedValue: row.speedValue,
                            bearingValue: row.bearingValue,
                            createTime: row.createTime,
                            lineId: row.lineId
                        };
                    }));
                    var cacheTTL=0;
                    if(moment().isBetween(queryStart,queryEnd)){
                        cacheTTL=7200;
                    }
                    gpsCache.set(cacheKey, result,cacheTTL, function (err, success) {
                        if (!err && success) {
                            console.log(cacheKey + " Cache Success!")
                        }
                        else {
                            console.log(cacheKey + " Cache Failed!")
                        }
                    })
                    res.write(result);
                    res.end();
                }

            })
        })
    }
});
app.get("/lasted", function (req, res) {
    var deviceId = req.query.deviceId;
    var sql = "select deviceId,lng,lat,speed,speedValue,bearingValue," +
        "strftime('%Y-%m-%d %H:%M:%S', createTime / 1000,'unixepoch', 'localtime') as createTime,lineId from GPS where deviceId_short = ? ";
    sql += " order by createTime DESC limit 1";
    db.serialize(function () {
        db.all(sql, [deviceId], function (err, rows) {
            if (err) {
                console.log(err.message);
            } else {
                res.writeHead(200, {'Content-Type': 'application/json'});
                res.write(JSON.stringify(rows.map(function (row) {
                    return {
                        deviceId: row.deviceId,
                        lng: row.lng,
                        lat: row.lat,
                        speed: row.speed,
                        speedValue: row.speedValue,
                        bearingValue: row.bearingValue,
                        createTime: row.createTime,
                        lineId: row.lineId
                    };
                })));
                res.end();
            }
        })
    })
});

app.get("/dates", function (req, res) {
    var deviceId = req.query.deviceId;
    var sql = "select strftime('%Y-%m-%d', createTime / 1000,'unixepoch', 'localtime') as createTime" +
        " from GPS where deviceId_short = ? group by strftime('%Y-%m-%d', createTime / 1000,'unixepoch', 'localtime');";
    sql += " order by createTime";
    db.serialize(function () {
        db.all(sql, [deviceId], function (err, rows) {
            if (err) {
                console.log(err.message);
            } else {
                res.writeHead(200, {'Content-Type': 'application/json'});
                res.write(JSON.stringify(rows.map(function (row) {
                    return {createTime: row.createTime};
                })));
                res.end();
            }
        })
    })
});

app.get("/devices", function (req, res) {
    var sql = "select deviceId from GPS where deviceId is not null  group by deviceId ";
    sql += " order by deviceId";
    db.serialize(function () {
        db.all(sql, [], function (err, rows) {
            if (err) {
                console.log(err.message);
            } else {
                res.writeHead(200, {'Content-Type': 'application/json'});
                res.write(JSON.stringify(rows.map(function (row) {
                    return {deviceId: row.deviceId};
                })));
                res.end();
            }
        })
    })
});




