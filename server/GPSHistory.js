var express=require('express');
var qs = require('querystring');
var sqlite3 = require('sqlite3').verbose();
var db = new sqlite3.Database('GPSHistory.db');
db.serialize(function () {
 db.run("CREATE TABLE IF NOT EXISTS  GPS (id integer primary key autoincrement,deviceId varchar(50),lng varchar(20), lat varchar(20),createTime integer)");
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
		var deviceId=post['deviceid'];
		var data=post['data'];
		var json=JSON.parse(data);
		for(var i=0;i<json.length;i++){
			db.serialize(function(){
				db.run("insert into GPS(deviceId,lng,lat,createTime) values(?,?,?,?)",[deviceId,json[i].lng,json[i].lat,json[i].createTime]);
			})
		}

	});
	res.end("Success");
});
app.get("/data",function(req,res){
	res.end("Build....");
})


