var lonLat = require('../lonLat');
module.exports = function(name, emitter, game) {
  // Handle entering a command
  window.addEventListener('keyup', function(e) {
    if (e.keyCode !== 13) return;
    var el = document.getElementById('cmd');
    if (document.activeElement === el) {
        if(el.value.match(/\/tp\s/)) { //Yo dawg player wants to teleport using an in game coord.
            var coords = el.value.match(/[-+]?[0-9]*\.?[0-9]/g);
            if(coords.length === 3) {
                coords[0] = parseFloat(coords[0]);
                coords[1] = parseFloat(coords[1]);
                coords[2] = parseFloat(coords[2]);
                window.avatar.position.set(coords[0], coords[1], coords[2]);
            } else {
                console.log("Those don't seem like valid coordinates...");
            }
        } else if (el.value.match(/\/tpl/)) { // Oh my oh me, they've given us a longitude and latitude
            var coords = el.value.match(/[-+]?[0-9]+\.?[0-9]+/g);
            console.log(coords);
            var gridRef = lonLat({
                x: parseFloat(coords[0]),
                y: parseFloat(coords[1])
            }, 'lonLatToGridRef');

            var E = gridRef.easting,
                N = gridRef.northing;

            coords[0] = Math.round(E / 50);
            //coords[1] = Math.round(game.generate('getHeight', gridRef)) + 10;
            coords[1] = window.avatar.position.y;
            coords[2] = Math.round(N / 50);

                
            window.avatar.position.set(coords[0], coords[1], coords[2]);
        } else {
            emitter.emit('message', {user: name, text: el.value});
            el.value = '';
            el.blur();
        }
    } else {
        el.focus();
    }
  });

  emitter.on('message', showMessage)

  function showMessage(message) {
    var li = document.createElement('li')
    li.innerHTML = message.user + ': ' + message.text
    messages.appendChild(li)
    messages.scrollTop = messages.scrollHeight
  }
}
