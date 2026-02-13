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
  
    <style type="text/css">
             .container {
          width: 100%;
          height: 100vh;
          display: flex;
          justify-content: center;
          align-items: center;
        }
    </style>  
  <script type="text/javascript">
  
  $(document).ready(function(){

	  var canvas = document.getElementById("wagon_canvas");
 	  canvas.height = window.innerHeight;
	  canvas.width = window.innerWidth; 
	  
	  window.onresize = function () {
	   	canvas.width = window.innerWidth;
	   	canvas.height = window.innerHeight;
	  }; 
	  
	  var ctx = canvas.getContext("2d");
  
	  const results = [
	      {mood: "deep_mid_wicket", total: 30, shade: "green"},
	      {mood: "long_on", total: 30, shade: "green"},
	      {mood: "long_off", total:30, shade: "green"},
	      {mood: "deep_cover", total: 30, shade: "green"},
	      {mood: "deep_point", total: 30, shade: "green"},
	      {mood: "third_man", total: 30, shade: "green"},
	      {mood: "deep_fine_leg", total: 30, shade: "green"},
	      {mood: "deep_square_leg", total: 30, shade: "green"}
	  ];
	  var sum = 0, currentAngle = 0, portionAngle;
	  var totalNumberOfPeople = results.reduce((sum, {total}) => sum + total, 0);
	  
	  for (var i = 0; i < results.length; i++) {
		  
	      //calculating the angle the slice (portion) will take in the chart
	      portionAngle = (results[i].total / totalNumberOfPeople) * 2 * Math.PI;
	      //drawing an arc and a line to the center to differentiate the slice from the rest
	      ctx.lineWidth = 1;
	      ctx.beginPath();
	      ctx.arc(400, 400, 300, currentAngle, currentAngle + portionAngle);
	      currentAngle += portionAngle;
	      ctx.lineTo(400, 400);
	      //filling the slices with the corresponding mood's color
	      ctx.fillStyle = results[i].shade;
	      ctx.fill();
	      ctx.stroke();
		  
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
			 <div>
			 	<canvas id="wagon_canvas">
			 	</canvas>
            </div>
		    <h5>Press ENTER key to submit or ESCAPE key to cancel</h5>
           </div>
          </div>
         </div>
    </div>
  </div>
 </div>
 <input type="hidden" id="current_wagon_data" name="current_wagon_data"></
 <input type="hidden" id="current_bat_style" name="current_bat_style" value="${current_bat_style}"></
</form:form>
</body>
</html>