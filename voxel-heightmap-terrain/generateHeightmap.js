var fs = require('fs');
var lonLat = require('../lonLat');

var options = require('dreamopt')([
  "Usage: png2json <source> [<destination>]",
  "  <source>           Source file to convert to json #required",
  "  <destination>      Destination file (defaults to source file with .json extension)",
  function(value, options) {
    if (!value) {
      return options.source.replace(/\/data\/heightData\//, '') + '.json';
    }
  }
]);

var contents = fs.readdirSync(options.source),
    jsonFiles = [];
//console.log(contents);

for(var i = 0; i < contents.length; i++) {
    if(contents[i].match(/\.asc$/g)) { // Grab all the ones which are ascii grid files
        jsonFiles.push({
            name: contents[i].slice(0, 4),
            data: fs.readFileSync(options.source + '/' + contents[i], 'utf8').split("\n").slice(5).join("\n") // Funky stuff removes first five lines
        });
    }
}


var heightMaps = {},
    width = 200,
    height = 200;


for(var i = 0; i < jsonFiles.length; i++) {
    var name = jsonFiles[i].name,
        data = jsonFiles[i].data,
        numbers = data.match(/\-?\d+\.?\d+/g);
    
    // Make 2D array to store los numbers
    heightMaps[name] = [];
    for (var x = 0; x < height; x++) {
        heightMaps[name][x] = [];
    }

    for(var j = 0; j < numbers.length; j++) {
        heightMaps[name][j % width][Math.floor(j / width)] = parseFloat(numbers[j]); // This does maths. Makes the 1D array a 2D one.
    }
}

fs.writeFile(options.destination, JSON.stringify(heightMaps), function(err) {
    if(err) {
        console.log(err);
        process.exit(1);
    } else {
        console.log("Saved " + options.destination);
    }
});

/*fs.readFile('dasd', 'utf8', function(err, data) {
  var xllcorner = parseInt(data.match(/xllcorner.+/g)[0].match(/[0-9]+/g)[0]), // Gets the values for the coords of this grid box
      yllcorner = parseInt(data.match(/yllcorner.+/g)[0].match(/[0-9]+/g)[0]),
      width = 200, // dimensions should always be 200x200
      height = 200;

  var heightMap = {
      xllcorner: xllcorner,
      yllcorner: yllcorner,
      map: []
  };
 
  // Remove first five lines
  data = data.split("\n").slice(5).join("\n");

  // Match all numbers into an array

  // Makes the 2D object for JSONization
  for (var x = 0; x < height; x++) {
      heightMap.map[x] = [];
  }
        
  for(var i = 0; i < heightsArray.length; i++) {
      heightMap.map[i % width][Math.floor(i / width)] = parseFloat(heightsArray[i]); //This does maths. Makes the 1D array a 2D one.
  }

  fs.writeFile(options.destination, JSON.stringify(heightMap), function(err) {
    if (err) {
      console.log(err);
      process.exit(1);
    }
    else {
      console.log("Saved " + options.destination);
    }
  }); 

});*/
