<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Match</title>
  
  <script type="text/javascript">
  	$(document).on("keypress", function(e){
  		processUserInput('LOG_VARIOUS',e.which);
	});
  </script>  

</head>
<body onload="afterPageLoad('MATCH');">
<form:form name="cricket_form" autocomplete="off" enctype="multipart/form-data">
<div class="content py-5" style="background-color: #EAE8FF; color: #2E008B">
  <div class="container">
	 <div class="col-md-8 offset-md-2">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
           <div class="card-header">
           </div>
          <div class="card-body">
          	<h6 id="match_error_lbl"></h6>
		      <div id="load_setup_match">
				<div class="row">
				    <div class="col-sm-2 col-md-2">
					    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
					  		name="setup_match_btn" id="setup_match_btn" onclick="processUserSelection(this)">
					  		<i class="fas fa-tools"></i> Setup</button>
					</div>
				    <div class="col-sm-8 col-md-8">
					    <label for="select_cricket_matches" class="col-sm-4 col-form-label text-left">Select Match</label>
					      <select id="select_cricket_matches" name="select_cricket_matches" 
					      		class="browser-default custom-select custom-select-sm">
								<c:forEach items = "${match_files}" var = "match">
						          <option value="${match.name}">${match.name}</option>
								</c:forEach>
					      </select>
					    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
					  		name="load_match_btn" id="load_match_btn" onclick="processUserSelection(this)">
					  		<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true" style="display:none"></span>
					  		<i class="fas fa-download"></i> Load Match</button>
				    </div>
				  <div id="start_pause_match_time_div" style="margin-bottom:5px;display:none;">
					<div class="row">
					  <div id="match_data_update_div" style="display:none;" class="col-6 col-sm-3">
					    <label for="matchDataUpdate" class="col-form-label text-left">Match Update</label>
					      <select id="matchDataUpdate" name="matchDataUpdate" 
					      		class="browser-default custom-select custom-select-sm" onchange="processUserSelection(this)">
					          <option value="start">Start</option>
					          <option value="pause">Pause</option>
					      </select>
					  </div>
					  <div class="w-100"></div>
					  <div id="select_match_innings_div" style="display:none;" class="col-6 col-sm-3">
					    <label for="select_match_innings" class="col-form-label text-left">Inning</label>
					      <select id="select_match_innings" name="select_match_innings" 
					      		class="browser-default custom-select custom-select-sm">
					      </select>
					  </div>
					  <div class="w-100"></div>
					  <div id="select_match_status_div" style="display:none;" class="col-6 col-sm-3">
					    <label for="select_match_status" class="col-form-label text-left">Status</label>
					      <select id="select_match_status" name="select_match_status" 
					      		class="browser-default custom-select custom-select-sm" onchange="processUserSelection(this)">
					          <option value="pause">Pause</option>
					          <option value="start">Start</option>
					      </select>
					  </div>
					  <div id="select_wagon_shot_div" class="col-6 col-sm-3">
					    <label for="select_wagon_shot" class="col-form-label text-left">Wagon Shots</label>
					      <select id="select_wagon_shot" name="select_wagon_shot"
					      		class="browser-default custom-select custom-select-sm">
					          <option value=""></option>
					          <option value="wagon">Wagon</option>
 					          <option value="wagon_shots">Wagon and Shots</option>
					      </select>
					  </div>
					  <div id="isDeclared_div" style="display:none;" class="col-6 col-sm-3">
						  <label for="isDeclared" class="col-form-label text-left">Declared </label>
					      <select id="isDeclared" name="isDeclared" class="browser-default custom-select custom-select-sm"
					      		onchange="processUserSelection(this)">
					          <option value="no">No</option>
					          <option value="yes">Yes</option>
					      </select>
					  </div>
					</div>
					  <div id="select_day_session_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
						<div class="row">
						  <div class="col-6 col-sm-3">
							    <label for="select_day" class="col-form-label text-left">Day </label>
						      <select id="select_day" name="select_day" class="browser-default custom-select custom-select-sm">
						          <option value="0"></option>
						      	  <c:forEach var="i" begin="1" end="5">
							          <option value="${i}">Day ${i}</option>
						      	  </c:forEach>
						      </select>
						  </div>
						  <div class="col-6 col-sm-3">
							  <label for="select_session" class="col-form-label text-left">Session </label>
						      <select id="select_session" name="select_session" class="browser-default custom-select custom-select-sm">
						          <option value="0"></option>
						      	  <c:forEach var="i" begin="1" end="3">
							          <option value="${i}">Session ${i}</option>
						      	  </c:forEach>
						      </select>
						  </div>
						  <div class="col-6 col-sm-3">
						    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
						  		name="log_day_session_btn" id="log_day_session_btn" onclick="processUserSelection(this)">
						  		<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true" style="display:none"></span>
						  		<i class="fas fa-download"></i> Log Day/Session</button>
						  </div>
						</div>
						<label id="selected_day_session" class="col-sm-8 col-form-label text-left"></label> 
					 </div>
				  </div>
			    </div>
		      </div>
		    <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			  <div id="inning_div" style="display:none;"></div>
			  <div id="select_event_div" style="display:none;"></div>
              <h6>${licence_expiry_message}</h6>
           </div>
          </div>
         </div>
       </div>
    </div>
  </div>
</form:form>
</body>
</html>