var createGame = require('voxel-engine');
var highlight = require('voxel-highlight');
var player = require('voxel-player');
var voxel = require('voxel');
var extend = require('extend');
var fly = require('voxel-fly');
var walk = require('voxel-walk');
var generator = require('voxel-heightmap-terrain');
var heightmap = require('./uk_original.json');
var game;
var yellow = "#0b7a25";
var green = "#2f8e28";
var blue = "#4251b2";
var brown = "#552f0b";


module.exports = function(opts, setup) {
    var defaults = {
      texturePath: './textures/',
      //generate: voxel.generator['Valley'],
      generate: function(x, y, z) {
        return generator(x, y, z, heightmap);
      }, 
      materials: [[green,brown,brown]], //[top,bottom,sides]
      materialFlatColor: true,
      chunkSize: 32,
      chunkDistance:5,
      worldOrigin: [0, 0, 0],
      controls: { discreteFire: false },
      lightsDisabled: false,
      fogDisabled: false,
      generateChunks: true,
      mesher: voxel.meshers.greedy,
      playerHeight: 0.1
    };
    
    setup = setup || defaultSetup;
    opts = extend({}, defaults, opts || {});

    // setup the game
    var game = createGame(opts);
    var container = opts.container || document.body;
    window.game = game; // for debugging
    game.appendTo(container);
    if (game.notCapable()) return game;
    
    var createPlayer = player(game);

    // create the player from a minecraft skin file and tell the
    // game to use it as the main player
    var avatar = createPlayer(opts.playerSkin || 'player.png');
    avatar.possess();
    avatar.yaw.position.set(2, 14, 4);

    setup(game, avatar);
    
    return game;
};

function defaultSetup(game, avatar) {
  
  var makeFly = fly(game);
  var target = game.controls.target();
  game.flyer = makeFly(target);
  
  // highlight blocks when you look at them, hold <Ctrl> for block placement
  var blockPosPlace, blockPosErase;
  var hl = game.highlighter = highlight(game, { color: 0xff0000 });
  hl.on('highlight', function (voxelPos) { blockPosErase = voxelPos; });
  hl.on('remove', function (voxelPos) { blockPosErase = null; });
  hl.on('highlight-adjacent', function (voxelPos) { blockPosPlace = voxelPos; });
  hl.on('remove-adjacent', function (voxelPos) { blockPosPlace = null; });

  // toggle between first and third person modes
  window.addEventListener('keydown', function (ev) {
    if (ev.keyCode === 'R'.charCodeAt(0)) avatar.toggle();
  });

  // block interaction stuff, uses highlight data
  var currentMaterial = 2;

  game.on('fire', function (target, state) {
    var position = blockPosPlace;
    if (position) {
      game.createBlock(position, currentMaterial);
    }
    else {
      position = blockPosErase;
      if (position) game.setBlock(position, 0);
    }
  });

  game.on('tick', function() {
    walk.render(target.playerSkin);
    var vx = Math.abs(target.velocity.x);
    var vz = Math.abs(target.velocity.z);
    if (vx > 0.001 || vz > 0.001) walk.stopWalking();
    else walk.startWalking();
  });

}
