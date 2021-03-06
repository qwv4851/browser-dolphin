var g_game = 'DOKAPON_KINGDOM';

var app = require('express')(),
	server = require('http').createServer(app),
  io = require('socket.io').listen(server),
  addon = require('../build/Release/addon');

io.set('log level', 1);
server.listen(80);

app.get('/', function(req, res) {
	res.sendfile(__dirname + "/index.html");
});

app.get(/^(.+)$/, function(req, res) {
	res.sendfile(__dirname + req.params[0]);
});

io.sockets.on('connection', function(socket) {

	socket.on('message', function(message) {
		console.log(message);
    addon.SendMessage(message);
    if (message[2] == 'g') {
      var s = message.split(' ');
      console.log('Changing game to: ' + s[2]);
      g_game = s[2];
      socket.broadcast.emit('game', g_game);
    }
	});
  socket.emit('game', g_game);
});