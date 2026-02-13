<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
  
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Wagon Wheel</title>
  
  <script type="text/javascript" src="<c:url value="/webjars/jquery/1.9.1/jquery.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/webjars/bootstrap/3.3.6/js/bootstrap.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/javascript/index.js"/>"></script>
  <link rel="stylesheet" href="<c:url value="/webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>"/> 
  <script type="text/javascript">
  
  $(document).ready(function(){

	  const wagon_array = [
	      {label: "Deep Mid Wicket", total: 30, shade: "green", x: 580, y: 470, arc_x:400, arc_y:400, radius:300},
	      {label: "Long On", total: 30, shade: "green", x: 480, y: 650, arc_x:400, arc_y:400, radius:300},
	      {label: "Long Off", total:30, shade: "green", x: 330, y: 650, arc_x:400, arc_y:400, radius:300},
	      {label: "Deep Cover", total: 30, shade: "green", x: 200, y: 470, arc_x:400, arc_y:400, radius:300},
	      {label: "Deep Point", total: 30, shade: "green", x: 200, y: 350, arc_x:400, arc_y:400, radius:300},
	      {label: "Third Man", total: 30, shade: "green", x: 310, y: 200, arc_x:400, arc_y:400, radius:300},
	      {label: "Deep Fine Leg", total: 30, shade: "green", x: 490, y: 200, arc_x:400, arc_y:400, radius:300},
	      {label: "Deep Square Leg", total: 30, shade: "green", x: 580, y: 350, arc_x:400, arc_y:400, radius:300}
	  ];
	  var sum = 0, currentAngle = 0, portionAngle;
	  var totalNumberOfArcs = wagon_array.reduce((sum, {total}) => sum + total, 0);

	  var canvas = document.getElementById("wagon_canvas");
 	  canvas.height = window.innerHeight;
	  canvas.width = window.innerWidth; 

	  window.onresize = function () {
	   	canvas.width = window.innerWidth;
	   	canvas.height = window.innerHeight;
	  }; 
	  var ctx = canvas.getContext("2d");
	  var isPointInPath;
	  canvas.addEventListener('click', (event) => {clickIt(event)});

	  drawWagonArcs();
	  
	  function drawWagonArcs() {
		  for (var i = 0; i < wagon_array.length; i++) {
			  
		      //calculating the angle the slice (portion) will take in the chart
		      portionAngle = (wagon_array[i].total / totalNumberOfArcs) * 2 * Math.PI;
		      //drawing an arc and a line to the center to differentiate the slice from the rest
		      ctx.lineWidth = 1;
		      ctx.beginPath();
		      ctx.arc(wagon_array[i].arc_x, wagon_array[i].arc_y, wagon_array[i].radius, 
		    		  currentAngle, currentAngle + portionAngle);
		      currentAngle += portionAngle;
		      ctx.lineTo(400, 400);
		      //filling the slices with the corresponding mood's color
		      ctx.fillStyle = wagon_array[i].shade;
		      ctx.fill();
		      ctx.stroke();
		      ctx.font = '20pt Calibri';
		      ctx.textAlign = 'center';
		      ctx.fillStyle = 'white';
		      ctx.fillText(wagon_array[i].label, wagon_array[i].x, wagon_array[i].y);	
		  }
	  }

      var xPos, yPos, saveLabel;
	  function clickIt(evt) { 
		var i, xDiff, yDiff, dist, result, cX, cY; 
	    xPos=null; yPos=null; 
	    evt= evt || event;
	    xPos=evt.offsetX || evt.pageX;
	    yPos=evt.offsetY || evt.pageY;
	    // check posn against centres
	    for(i=0; i < wagon_array.length; i++) { 
	       cX=wagon_array[i].x; cY=wagon_array[i].y;
	       xDiff=Math.abs(cX-xPos);
	       yDiff=Math.abs(cY-yPos);
	       dist=Math.sqrt(Math.pow(xDiff,2) + Math.pow(yDiff,2)); 
	      // info on clicked arc       
	       if(dist <= 30) { 
	    	 saveLabel=wagon_array[i].label;  
	       }
	     }
	     result=(saveLabel.length > 0)? 'You have selected: ' + saveLabel : 'You have selected: N/A'; 
	     document.getElementById("whichWagonData").innerHTML = result;  
	     document.getElementById("mouseClickCoOrds").innerHTML = 'Mouse clicked co-ordinates: ' + xPos + ',' + yPos;  
	     document.getElementById("wagonData").value = saveLabel + ',' + xPos + ',' + yPos;
	  }	   
	  
	});
  
    document.addEventListener('keyup', function (e) {keyPressEvent('WAGON', e);}, false);
    
  </script>
</head>
<body>
<form:form name="wagon_form" autocomplete="off">
<div class="content py-5" style="background-color: #EAE8FF; color: #2E008B">
  <div class="container">
	<div class="row">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
          <div class="card-body">
			<div class="row">
			  <h5>Press ENTER key to submit or ESCAPE key to cancel</h5>
			  <h4 id="whichWagonData">You have selected: N/A</h4>
			  <h4 id="mouseClickCoOrds">Mouse clicked co-ordinates:</h4>
		 	  <canvas id="wagon_canvas"></canvas>
           </div>
          </div>
         </div>
    </div>
  </div>
 </div>
 <input type="hidden" id="wagonData" name="wagonData"></
</form:form>
</body>
</html>