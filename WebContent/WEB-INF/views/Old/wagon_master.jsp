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
    
	  var canvas = document.getElementById("wagon_canvas");
	  canvas.height = window.innerHeight;
	  canvas.width = window.innerWidth; 
	  
	  window.onresize = function () {
		   	canvas.width = window.innerWidth;
		   	canvas.height = window.innerHeight;
		  };
	  
	  var ctx = canvas.getContext("2d");

	  var background = new Image();
	  background.src = '<c:url value="/resources/images/wagon_rhb.jpeg"/>';
	  if(document.getElementById("current_bat_style")) {
	 	  if(document.getElementById("current_bat_style").value.toLowerCase() == 'lhb') {
			  background.src = '<c:url value="/resources/images/wagon_lhb.jpeg"/>';
	 	  }
	  }
 	  
	  background.onload = function(){
		ctx.drawImage(background,0,0);  
	  }; 	
	  
	  function draw(e) {
	  	 ctx.fillStyle = "white";
	  	 ctx.clearRect(0,0,canvas.width,canvas.height);
  		 ctx.drawImage(background,0,0);  
	  	 ctx.beginPath();
	  	 ctx.arc(e.clientX - canvas.getBoundingClientRect().left, 
	  		e.clientY - canvas.getBoundingClientRect().top, 10, 0, 2 * Math.PI);
	  	 ctx.fill();
	  	 alert(e.clientX + ',' + e.clientY);
	  };
	  window.draw = draw;	
	  
    });
    document.addEventListener('keyup', function (e) {keyPressEvent('WAGON', e);}, false);
  </script>
</head>
<body>
<form:form name="wagon_form" autocomplete="off">
<div class="content py-5" style="background-color: #EAE8FF; color: #2E008B">
  <div class="container">
	<div class="row">
	 <div class="col-md-12 offset-md-12">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
          <div class="card-body">
			<div class="row">
			 <div class="col-lg-4">
			 	<canvas id="wagon_canvas" onclick="draw(event)">
			 	</canvas>
			  <h5>Press ENTER key to submit or ESCAPE key to cancel</h5>
            </div>
           </div>
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