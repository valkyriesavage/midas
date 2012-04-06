// SIMPLE NODE.JS SERVER FOR MIDAS
// bjoern@eecs, 04/2012
var app = require('express').createServer(),
	io = require('socket.io').listen(app);

////////// HTML SERVER URL MAPPING /////////
app.listen(8080);
app.get('/test', function(req,res) {
	res.sendfile(__dirname + "/test.html");
});

////////// SOCKET.IO CALLBACKS /////////
io.sockets.on('connection', function(client) {
	// http://stackoverflow.com/questions/6458083/socket-io-get-clients-ip-address
	var address = client.handshake.address;
	console.log("New client connection from " + address.address + ":" + address.port);	
	client.emit('message', 'Hello, world from node.js');
	////// GENERIC MESSAGE BROADCAST //////////
	client.on('message', function(data){
		// send to all other clients

		client.broadcast.emit('message',data);
		console.log('message received and broadcasted: ' + data);
	});
});