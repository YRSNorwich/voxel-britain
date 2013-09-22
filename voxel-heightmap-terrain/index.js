module.exports = function(x, y, z, heightmap) {
  var rgba;
  z = Math.round((z + 0) * 1);
  x = Math.round((x + 0) * 1);

  // Value here tweaked from 4 to 8 to make things flatter
  // I also changed the y + 64 to just y to make coords better. Probably broke some wonderful mathematics with my simpleton's tramplings.
  y = Math.round(y * 8);
  if (!heightmap[z] || !heightmap[z][x]) {
    return 0;
  }
  rgba = heightmap[z][x];

  if(y < 0) {
      //lowest value happens to be 63 with this heightmap. What was that about ugly hack?
      return 0;
  }
  
  if(y === 0) {
      return 2;
  }

  return y < rgba.r ? 1 : 0;
};
