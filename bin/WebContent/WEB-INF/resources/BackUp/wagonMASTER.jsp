<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Wagon Wheel</title>
</head>
<body onload="">
<form:form name="wagon_form" autocomplete="off">
<div class="content py-5" style="background-color:#EAE8FF;color:#2E008B;height:500px;width:500px;">
  <div class="container">
	<div class="row">
	 <div class="col-md-12 offset-md-12">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
          <div class="card-body">
			<div class="row">
			 <div class="col-lg-6">

			<div class= "row">
			    <label for="select_boundary_height" class="col-sm-4 col-form-label text-left">Select Boundary Height</label>
			      <select id="select_boundary_height" name="select_cricket_matches" 
		      		class="browser-default custom-select custom-select-sm boundary_single_check_only">
			          <option value="boundary_along_ground">Along ground</option>
			          <option value="boundary_below_head_height">Below Head Height</option>
			          <option value="boundary_just_over_head_height">Just over head height</option>
			          <option value="boundary_high_in_the_air">High in the air</option>
			          <option value="boundary_very_high_in_the_air">Very high in the air</option>
			      </select>	
		    </div>
			<div class= "row">
			    <label for="log_six_distance" class="col-sm-4 col-form-label text-left">Log Six Distance </label>
			    <div class="col-sm-2 col-md-2">
		             <input type="text" id="log_six_distance" name="log_six_distance" 
		             	class="form-control form-control-sm floatlabel" value="0"></input>
			    </div>
		    </div>
			<div class= "row">
			    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
			  		name="upload_wagon_btn" id="upload_wagon_btn" onclick="processUserSelection(this)">
			  		<i class="fas fa-tools"></i> Upload Wagon Data</button>
			    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
			  		name="cancel_wagon_btn" id="cancel_wagon_btn" onclick="processUserSelection(this)">
			  		<i class="fas fa-tools"></i> Cancel</button>
			  <h4 id="whichWagonData">Select Wagon X and Y Coordinates:</h4>
			  <canvas id="wagon_canvas" onclick="handleClick(event)"></canvas>
            </div>
            </div>
           </div>
          </div>
         </div>
       </div>
    </div>
  </div>
 </div>
 <input type="hidden" id="wagonData" name="wagonData">
<%--  <input type="hidden" id="current_batsman_style" name="current_batsman_style" value="${current_batsman_style}"> --%>
 <input type="hidden" id="wagonXcoOrd" name="wagonXcoOrd" value="${wagonXcoOrd}">
 <input type="hidden" id="wagonYcoOrd" name="wagonYcoOrd" value="${wagonYcoOrd}">
</form:form>
</body>
</html>