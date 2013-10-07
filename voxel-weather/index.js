function Weather(opts) {
  var self = this;

  
  if (!(this instanceof Weather)) return new Weather(opts || {});
  if (opts.THREE) opts = {game:opts};
  this.game = opts.game;
  this.speed = opts.speed || 0.1;
  this.drift = opts.drift || 1;
  this.type = opts.type || 'rain';
  this.particles = [];
  if (opts.count != null || opts.size != null || opts.material != null) {
    this.game.scene.add(this.add(
      opts.count || null, opts.size || null, opts.material || null
    ));
  }
}
module.exports = Weather;

Weather.prototype.add = function(count, size, material) {
  var game = this.game,
      color,
      map = null,
      transparent = false,
      partSize = 1;

  count = count || 1000;
  size  = size  || 20;
  
  switch(this.type) {
      case 'rain':
          color = 0x0069C4;
          break;
      case 'snow':
          color = 0xffffff;
          break;
      case 'soot':
          color = 0x000000;
          break;
      case 'engageCage':
          color = 0xffffff;
          map = game.THREE.ImageUtils.loadTexture(
                "nic.png"
          );
          transparent = true;
          partSize = 20;
          break;
      default:
          console.log('Weather type not supported. Please contact your system administrator.'); 
          break;
  }

  material = material || new game.THREE.ParticleBasicMaterial({
    color: color,
    size: partSize,
    map: map,
    transparent: transparent
  });

  var half = size / 2;

  var geom = new game.THREE.Geometry();
  geom.boundingBox = new game.THREE.Box3(
    new game.THREE.Vector3(-half, -half, -half),
    new game.THREE.Vector3(half, half, half)
  );

  for (var i = 0; i < count; i++) {
    geom.vertices.push(new game.THREE.Vector3(
      rand(-half, half), rand(-half, half), rand(-half, half)
    ));
  }

  var particles = new game.THREE.ParticleSystem(geom, material);
  this.particles.push(particles);

  return particles;
};

Weather.prototype.tick = function(dt) {
  var self = this;
  var target = self.game.controls.target();
  self.particles.forEach(function(particle) {
    if (target == null) return;

    particle.position.copy(target.position);

    var bounds = particle.geometry.boundingBox;
    var count = particle.geometry.vertices.length;
    var a = target.yaw.rotation.y;
    var x = Math.floor(target.velocity.x) / 50; //The velocities used to be timesed by 1000. I have no idea why. This seems to work. The 50 seems necessary. 
    var y = Math.floor(target.velocity.y) / 50;
    var z = Math.floor(target.velocity.z) / 50;
    // todo: fix this, should handle 2 directions at the same time
    var r = x !== 0 ? x * 0.5 : z !== 0 ? z : 0;
    if (x !== 0) a += Math.PI / 2;
    while (count--) {
      var p = particle.geometry.vertices[count];
      if (p.y < bounds.min.y) p.y = bounds.max.y;
      p.y -= Math.random() * (self.speed + y / 2);
      ['x', 'z'].forEach(function(x) {
        if (p[x] > bounds.max[x] || p[x] < bounds.min[x]) {
         p[x] = rand(bounds.min[x], bounds.max[x]);
        }
      });
      p.x += Math.sin(a) * -r * self.drift;
      p.z += Math.cos(a) * -r * self.drift;
    }
    particle.geometry.verticesNeedUpdate = true;
  });
};

function rand(min, max) { return Math.random() * (max - min) + min; }
