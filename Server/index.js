const express = require('express');
const socket = require('socket.io');

const app = express();
const io = socket(require('http').createServer(app));

let passengers = {};
let info = {
    updateInfo: () => {
        console.log(passengers);
        info.degree += 0.1;
        info.speedKnot -= 3;
        info.speedKph -= 5;
        if (info.degree >= 10)
            for (let p in passengers)
                passengers[p].emit('emergency');
    },
    departure: "목포항",
    destination: "제주항",
    direction: "SE",
    remainingTime: 100,
    speedKnot: 53.5,
    speedKph: 99.2,
    degree: 0.5
};

app.get('/map', (req, res) => res.status(200).json(require('./map.json')));
app.get('/info', (req, res) => {
    info.updateInfo();
    console.log(info.degree);
    res.status(200).json(info);
});

io.on('connection', (socket) => {
    passengers[socket.id] = socket;
    socket.on('crew', () => socket.join('crew'));
    socket.on('location', (location) => {
        passengers[socket.id] = {
            socket: socket,
            location: location
        };
        console.log(socket.id, location);
        socket.to('crew').emit('location', location);
    });
    socket.on('disconnect', () => delete passengers[socket.id]);
});

app.listen(8080, () => console.log('listening'));