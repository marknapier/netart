
window.FlagShapes = (function () {
	// shape drawing parameters are in absolute pixels
	function drawRect(ctx, x, y, w, h, color) {
	    ctx.fillStyle = color;
	    ctx.fillRect(x, y, w, h);
	}

	function drawImage(ctx, image, x, y, w, h) {
		ctx.drawImage(image, x, y, w, h);
	}

	// given a rectangle position and widht/height, draw a triangle with
	// tip facing right
	function drawTriangleRight(ctx, x=0, y=0, w=250, h=300, color = 'red') {
		ctx.fillStyle = color;
		ctx.beginPath();
		ctx.moveTo(x, y);
		ctx.lineTo(x + w, y + Math.round(h / 2));
		ctx.lineTo(x, y + h);
		ctx.fill();
	}

	function drawCircle(ctx, x = 0, y = 0, w = 100, color = 'blue') {
		var r = w / 2;
		ctx.fillStyle = color;
		ctx.beginPath();
		ctx.arc(x + r, y + r, r, 0, Math.PI * 2, true);
		ctx.fill();
	}

	function drawPolygon(ctx, points, color = 'yellow') {
		ctx.fillStyle = color;
		if (points.length > 0) {
			ctx.beginPath();
			ctx.moveTo(points[0].x, points[0].y);
			for (let i=1; i < points.length; i++) {
				ctx.lineTo(points[i].x, points[i].y);
			}
			ctx.fill();
		}
	}

	function drawStar(ctx, x, y, d, numPoints, spikyness, rotation, color = 'yellow') {
		var points = makeStarPoints(x, y, d, spikyness, numPoints, rotation);
		drawPolygon(ctx, points, color);
	}

	function makeStarPoints(x, y, d, spikyness = 0.5, numPoints = 5, rotation = 0) {
		var radius = (d / 2);
		var x = Math.round(x + radius); // x y params are upper left, convert to center
		var y = Math.round(y + radius);
		var rotation = (rotation/360) * (2 * Math.PI);

		// spikyness is the amount of point.  it goes
		// from 0 to 1. At 0,
		// the interior point is at the origin
		// at 1, it is on the star's bounding circle.

		var points = [];
		var px;
		var py;

		// this is the amount we rotate.  It's double the amount
		// of points on the star,
		// because we rotate halfway to put inthe interior points
		// the code works by defining an exterior point
		// then an interior point
		var angleDifference = ((2*Math.PI) / (numPoints*2));

		// 'cause we want the top of the star to be a point:
		var angle = ((6*Math.PI/4) + rotation);

		for (let i=0; i < (numPoints*2); i++){
			if (i % 2 == 0){
				px = Math.round(x + radius * Math.cos(angle));
				py = Math.round(y + radius * Math.sin(angle));
			} else {
				px = Math.round(x + spikyness * radius * Math.cos(angle));
				py = Math.round(y + spikyness * radius * Math.sin(angle));
		 	}
		 	points.push({x: px, y: py});
		 	angle = angle + angleDifference;
		}

		return points;
  }

	function convertDataPointsToPixels(datapointsStr = '', flagW, flagH) {
		var datapoints = datapointsStr.split(',');
		var pixelPoints = [];
		for (let i=0; i < datapoints.length; i += 2) {
    	pixelPoints.push({
    		x: Math.round((datapoints[i] * .001) * flagW),
    		y: Math.round((datapoints[i+1] * .001) * flagH),
    	});
    }
    return pixelPoints;
	}

	function convertPixelsToDataPoints(pixelPoints, flagW, flagH) {
		var dataPoints = [];
		for (let i=0; i < pixelPoints.length; i++) {
    	dataPoints.push(Math.round(pixelPoints[i].x / flagW * 1000));
			dataPoints.push(Math.round(pixelPoints[i].y / flagH * 1000));
    }
    return dataPoints;
	}

	// given "rgb(13, 176, 43)" return string "13, 176, 43"
	function stripRGB(rgbString = '') {
		return rgbString.replace('rgb(', '').replace(')', '');
	}

	return {
		drawRect,
		drawCircle,
		drawTriangleRight,
		drawStar,
		drawPolygon,
		drawImage,
		makeStarPoints,
		convertDataPointsToPixels,
		convertPixelsToDataPoints,
	};
}());
