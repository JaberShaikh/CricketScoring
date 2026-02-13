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
  <script src="https://cdn.anychart.com/releases/8.0.1/js/anychart-core.min.js"></script>
  <script src="https://cdn.anychart.com/releases/8.0.1/js/anychart-pie.min.js"></script>  
  
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
</head>
<body>
<form:form name="wagon_form" autocomplete="off">
<div id="container" style="width: 100%; height: 100%">
</div>
    <script>

    anychart.onDocumentReady(function() {

    	  // set the data
    	  var data = [
    	      {x: "White", value: 223553265},
    	      {x: "Black or African American", value: 38929319},
    	      {x: "American Indian and Alaska Native", value: 2932248},
    	      {x: "Asian", value: 14674252},
    	      {x: "Native Hawaiian and Other Pacific Islander", value: 540013},
    	      {x: "Some Other Race", value: 19107368},
    	      {x: "Two or More Races", value: 9009073}
    	  ];

    	  // create the chart
    	  var chart = anychart.pie();

    	  // set the chart title
    	  chart.title("Population by Race for the United States: 2010 Census");

    	  // add the data
    	  chart.data(data);
    	  
    	  // set legend position
    	  chart.legend().position("right");
    	  // set items layout
    	  chart.legend().itemsLayout("vertical");  

    	  // display the chart in the container
    	  chart.container('container');
    	  chart.draw();

    	});
    </script>
 <input type="hidden" id="current_wagon_data" name="current_wagon_data"></
 <input type="hidden" id="current_bat_style" name="current_bat_style" value="${current_bat_style}"></
</form:form>
</body>
</html>