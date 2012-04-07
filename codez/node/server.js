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
	client.emit('message', {text:'Hello, world from node.js'});
	client.emit('message', {check:true});
	client.emit('message', {slider:33});
	////// GENERIC MESSAGE BROADCAST //////////
    client.on('', function(data){
      console.log("received an empty message");
      });
      
	client.on('message', function(data){
		// send to all other clients
        console.log('message received and broadcasted: ' + data);
		client.broadcast.emit('message',data);
		
	});
});