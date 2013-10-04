var $ = require('jquery-browserify');
var lonLat = require('../../lonLat');
var createClient = require('../')
var highlight = require('voxel-highlight')
var extend = require('extend')
var voxelPlayer = require('voxel-player')
var fly = require('voxel-fly')
var toolbar = require('toolbar')
var bartab = toolbar('.bar-tab')
var Weather = require('../../voxel-weather')
var game

module.exports = function(opts, setup) {
  setup = setup || defaultSetup
  opts = extend({}, opts || {})

  var client = createClient(opts.server || "ws://localhost:8080/")
  
  client.emitter.on('noMoreChunks', function(id) {
    console.log("Attaching to the container and creating player")
    var container = opts.container || document.body
    game = client.game
    game.appendTo(container)
    game.terminalVelocity[1] = 1; // Up the terminal velocity of falling to what speed demon Milo considers a sensible value
    if (game.notCapable()) return game
    var createPlayer = voxelPlayer(game)

    // create the player from a minecraft skin file and tell the
    // game to use it as the main player
    var avatar = createPlayer('player.png')
    window.avatar = avatar
    avatar.possess()
    var settings = game.settings.avatarInitialPosition
    avatar.position.set(settings[0],settings[1],settings[2])
    setup(game, avatar, client)
  })


  return game
}

function defaultSetup(game, avatar, client) {
  // highlight blocks when you look at them, hold <Ctrl> for block placement
  var blockPosPlace, blockPosErase
  var hl = game.highlighter = highlight(game, { color: 0xff0000 })
  hl.on('highlight', function (voxelPos) { blockPosErase = voxelPos })
  hl.on('remove', function (voxelPos) { blockPosErase = null })
  hl.on('highlight-adjacent', function (voxelPos) { blockPosPlace = voxelPos })
  hl.on('remove-adjacent', function (voxelPos) { blockPosPlace = null })

  // toggle between first and third person modes
  window.addEventListener('keydown', function (ev) {
    if (ev.keyCode === 'R'.charCodeAt(0)) avatar.toggle()
  })

  // block interaction stuff, uses highlight data
  var currentMaterial = 1

  bartab.on('select', function(item) {
      currentMaterial = parseInt(item)
  })

  game.on('fire', function (target, state) {
    var position = blockPosPlace
    if (position) {
      game.createBlock(position, currentMaterial)
      client.emitter.emit('set', position, currentMaterial)
    } else {
      position = blockPosErase
      if (position) {
        game.setBlock(position, 0)
        console.log("Erasing point at " + JSON.stringify(position))
        client.emitter.emit('set', position, 0)
      }
    }
  })

  // Flying!
  var makeFly = fly(game)
  makeFly(game.controls.target())

  // Weather!
  var weathering = false,
      weather;

  function makeWeather(yesno, type) {
      if(yesno) {
          weather = Weather({
              game: game,
              count: 2000,
              size: 20,
              speed: 0.1,
              type: type
          });
          weathering = yesno;
      }
  }
  
  function getWeather() {
      console.log('getting weather..');
      var loc = lonLat({
          x: window.avatar.position.x * 50,
          y: window.avatar.position.z * 50
      }, 'lonLat');

      var url = "https://api.forecast.io/forecast/687b1e3ca3b67e95e0308640c0edbe5a/" + loc.x + "," + loc.y + "?callback=?";

      $.getJSON(url, function(data) {
          if(typeof data.currently.precipType !== 'undefined') {
              makeWeather(true, data.currently.precipType)
              console.log(data);
          } else {
              console.log("No precipitation here right now :'(");
          }
      });
  }

  //makeWeather(true, 'rain');

  var fiveMins = 0; // Five minute timer
  game.on('tick', function(dt) {
      // Check weather every five minutes
      fiveMins -= dt;
      if(fiveMins <= 0) {
          fiveMins = 1000 * 60 * 5;
          getWeather();
      }
      if(weathering) weather.tick(dt);
  });
}

