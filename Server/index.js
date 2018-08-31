const express = require('express');
const socket = require('socket.io');

const app = express();
const io = socket(require('http').createServer(app));

let passengers = {};
let
let info = {
    updateInfo: () => {
        this.degree += 5;
        if (this.degree >= 10)
            for (let p of passengers)
                p.emit('emergency');
    },
    destination: "Jeju",
    direction: "SE",
    remainingTime: 100,
    speedKnot: 53.5,
    speedKph: 99.2,
    degree: 0.5
};

app.get('/map', (req, res) => res.status(200).json(require('./map.json')));
app.get('/info', (req, res) => {
    info.updateInfo();
    res.status(200).json(info);
});

io.on('connection', (socket) => {
    socket.on('crew', () => socket.join('crew'));
    socket.on('location', (location) => {
        passengers[socket.id] = {
            socket: socket,
            location: location
        };
        socket.to('crew').emit('location', location);
    });
    socket.on('disconnect', () => delete passengers[socket.id]);
});

app.listen(8080, () => console.log('listening'));