var express=require('express');
var qs = require('querystring');
var sqlite3 = require('sqlite3').verbose();
var db = new sqlite3.Database('GPSHistory.db');
db.serialize(function () {
 db.run("CREATE TABLE IF NOT EXISTS  GPS (id integer primary key autoincrement,deviceId varchar(50),lng varchar(20), lat varchar(20),speed varchar(10),createTime integer)");
});

var PORT=8090;

var app=express();
app.use(express.static('.'));
app.listen(PORT,function(){
	console.log('server start at %d',PORT);
});
app.post('/gps',function(req,res){
	var body='';
	req.on('data',function(data){
		body+=data;	
	});
	req.on('end',function(){
		var post=qs.parse(body);
		var deviceId=post['deviceId'];
		var data=post['data'];
		var json=JSON.parse(data);
		for(var i=0;i<json.length;i++){
			db.serialize(function(){
				db.run("insert into GPS(deviceId,lng,lat,speed,createTime) values(?,?,?,?)",[deviceId,json[i].lng,json[i].lat,json[i].speed,json[i].createTime]);
			})
		}

	});
	res.end("Success");
});
app.get("/data",function(req,res){
	//Search sql: select id,deviceId,lat,lng,strftime('%Y-%m-%d %H:%M:%S', createTime / 1000,'unixepoch', 'localtime') from GPS;
    var deviceId=req.query.deviceId;
    var startTime=req.query.startTime;
    var endTime=req.query.endTime;

    var sql="select deviceId,lng,lat,strftime('%Y-%m-%d %H:%M:%S', createTime / 1000,'unixepoch', 'localtime') as createTime from GPS where deviceId=? ";
    if(startTime!=undefined){
        sql+=" and createTime > ? ";
    }
    if(endTime!=undefined){
        sql+=" and createTime < ? ";
    }
    sql+=" order by createTime";

    db.serialize(function () {
        db.all(sql, [deviceId, startTime, endTime], function (err, rows) {
            if (err) {
                console.log(err.message);
            }
            else {
                res.writeHead(200, {'Content-Type': 'application/json'});
                res.write(JSON.stringify(rows.map(function (row) {
                    return {deviceId: row.deviceId, lng: row.lng, lat: row.lat, createTime: row.createTime};
                })));
                res.end();
            }

        })
    })

});
app.get("/lasted",function(req,res){
    var deviceId=req.query.deviceId;
    var sql="select deviceId,lng,lat,strftime('%Y-%m-%d %H:%M:%S', createTime / 1000,'unixepoch', 'localtime') as createTime from GPS where deviceId=? ";
    sql+=" order by createTime DESC limit 1";
    db.serialize(function () {
        db.all(sql, [deviceId], function (err, rows) {
            if (err) {
                console.log(err.message);
            } else {
                res.writeHead(200, {'Content-Type': 'application/json'});
                res.write(JSON.stringify(rows.map(function (row){ return {deviceId: row.deviceId,lng:row.lng,lat: row.lat,createTime:row.createTime}; })));
                res.end();
            }
        })
    })
});

app.get("/dates",function(req,res){
    var deviceId=req.query.deviceId;
    var sql="select strftime('%Y-%m-%d', createTime / 1000,'unixepoch', 'localtime') as createTime from GPS where deviceId=? group by strftime('%Y-%m-%d', createTime / 1000,'unixepoch', 'localtime');";
    sql+=" order by createTime";
    db.serialize(function () {
        db.all(sql, [deviceId], function (err, rows) {
            if (err) {
                console.log(err.message);
            } else {
                res.writeHead(200, {'Content-Type': 'application/json'});
                res.write(JSON.stringify(rows.map(function (row){ return {createTime:row.createTime}; })));
                res.end();
            }
        })
    })
});

app.get("/devices",function(req,res){
    var sql="select deviceId from GPS where deviceId is not null  group by deviceId ";
    sql+=" order by deviceId";
    db.serialize(function () {
        db.all(sql, [], function (err, rows) {
            if (err) {
                console.log(err.message);
            } else {
                res.writeHead(200, {'Content-Type': 'application/json'});
                res.write(JSON.stringify(rows.map(function (row){ return {deviceId:row.deviceId}; })));
                res.end();
            }
        })
    })
});



