module.exports = DataOverlay;

function DataOverlay(pathToData) {
    var self = this;
    this.CHUNK_X = 50;
    this.CHUNK_Y = 55;
    this.CHUNK_WIDTH = 14;
    this.CHUNK_HEIGHT = 20;
    this.path = pathToData;
    this.overlays = [];

    for(var i = 0; i < this.CHUNK_X; i++) {
        this.overlays[i] = [];
    }
}

DataOverlay.prototype.getOverlay = function(point) {

    filePoint = {
        x: (Math.floor((point.x / 14000)) * this.CHUNK_WIDTH),
        y: 1100 - (Math.floor((point.y / 20000)) * this.CHUNK_HEIGHT)
    }

    point = {
        x: (filePoint.x / this.CHUNK_WIDTH), 
        y: (filePoint.y / this.CHUNK_HEIGHT)
    }

    if(filePoint.y === 1100) filePoint.y -= 20;

    if(typeof this.overlays[point.x][point.y] !== 'undefined') {
        return this.overlays[point.x][point.y];
    } else {
        var url = this.path + "chunk_" + filePoint.x + "_" + filePoint.y + ".json";

        var overlay = new XMLHttpRequest();
        overlay.open("GET", url, false); // TODO make async work

        overlay.send(null);

        this.overlays[point.x][point.y] = JSON.parse(overlay.response);

        return this.overlays[point.x][point.y];
    }

}
