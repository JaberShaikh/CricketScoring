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
<div class="content py-5 wagon-card" style="background-color:#EAE8FF;color:#2E008B; min-height:500px; width:100%;">
  <div class="container">
	<div class="row">
	  <div class="col-12">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
          <div class="card-body">
			<div class="row">
			 <div class="col-lg-6">
				<div class="wagon-controls-row d-flex flex-wrap align-items-center gap-2 mb-3">
				  <div class="control-item d-flex align-items-center" style="gap:.5rem;">
				    <label for="selectBoundaryHeight" class="form-label mb-0" style="white-space:nowrap;">
				      Boundary Height
				    </label>
				    <select id="selectBoundaryHeight" name="selectBoundaryHeight" class="form-select form-select-sm"
				          style="min-width:0; max-width:360px;">
			          <option value="boundary_along_ground">Along ground</option>
			          <option value="boundary_below_head_height">Below Head Height</option>
			          <option value="boundary_just_over_head_height">Just over head height</option>
			          <option value="boundary_high_in_the_air">High in the air</option>
			          <option value="boundary_very_high_in_the_air">Very high in the air</option>
				    </select>
				  </div>
				  <div class="control-item d-flex align-items-center" style="gap:.5rem;">
				    <label for="log_six_distance" class="form-label mb-0" style="white-space:nowrap;">
				      Six Distance
				    </label>
				    <input type="text" id="log_six_distance" name="log_six_distance"
				           class="form-control form-control-sm"
				           style="min-width:0; max-width:180px;" />
				  </div>
				</div>
			<div class= "row">
			  <div class="wagon-controls-row wagon-canvas-wrap d-flex align-items-center" style="gap:6px;">
			    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
			  		name="upload_wagon_btn" id="upload_wagon_btn" onclick="processUserSelection(this)">
			  		<i class="fas fa-cloud-upload-alt"></i> Upload Wagon Data</button>
			    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
			  		name="cancel_wagon_btn" id="cancel_wagon_btn" onclick="processUserSelection(this)">
			  		<i class="fas fa-times"></i> Cancel</button>
			    <h6 id="whichWagonData">Select Wagon X and Y Coordinates:</h6>
			  </div>
			  <canvas id="wagon_canvas" width="300" height="300" onclick="handleClick(event)"></canvas>
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
 <input type="hidden" id="wagonXcoOrd" name="wagonXcoOrd" value="${wagonXcoOrd}">
 <input type="hidden" id="wagonYcoOrd" name="wagonYcoOrd" value="${wagonYcoOrd}">
</form:form>
</body>
</html>