#NB modified from https://github.com/incompl/voxel-heightmap-terrain/

# voxel-heightmap-terrain

generate voxel terrain using a png heightmap

![example](https://github.com/incompl/voxel-heightmap-terrain/blob/master/example.png?raw=true)

this is designed to work out of the box with [voxel-engine](https://npmjs.org/package/voxel-engine)

## heightmaps

The generator uses heightmaps in JSON form

This repo includes png2json.js which lets you create these JSON files from PNG images

`node png2json dog.png`

## api

```javascript
var generator = require('voxel-heightmap-terrain');
var heightmap = require('./heightmap.json');

window.game = createGame({
  generate: function(x, y, z) {
    return generator(x, y, z, heightmap);
  },
  ...
});
```

## license

MIT
