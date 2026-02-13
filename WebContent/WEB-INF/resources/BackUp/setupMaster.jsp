<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>

  <sec:csrfMetaTags/>
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Setup</title>
	
  <script type="text/javascript" src="<c:url value="/webjars/jquery/3.7.1/jquery.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/webjars/bootstrap/5.3.2/js/bootstrap.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/webjars/select2/4.0.13/js/select2.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/javascript/index.js"/>"></script>

  <link rel="stylesheet" href="<c:url value="/webjars/bootstrap/5.3.2/css/bootstrap.min.css"/>"/>  
  <link rel="stylesheet" href="<c:url value="/webjars/font-awesome/6.5.1/css/all.css"/>">
  <link rel="stylesheet" href="<c:url value="/webjars/select2/4.0.13/css/select2.css"/>"/>  
  <link rel="stylesheet" href="<c:url value="/resources/css/index.css"/>"/> 
	
</head>
<body onload="afterPageLoad('SETUP');">
<form:form name="setup_form" method="POST" action="save_match" enctype="multipart/form-data">
<div class="content py-5" style="background-color: #EAE8FF; color: #2E008B">
  <div class="container">
	<div class="row">
	 <div class="col-md-9 offset-md-2">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
           <div class="card-header">
             <h3 class="mb-0">Setup</h3>
           </div>
          <div class="card-body">
	         <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
			  		name="cancel_match_setup_btn" id="cancel_match_setup_btn" onclick="processUserSelection(this)">
		  		<i class="fas fa-window-close"></i> Back</button>
	         </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="specialMatchRules" class="col-sm-4 col-form-label text-left">Special Match Rules</label>
			    <div class="col-sm-6 col-md-6">
			      <select id="specialMatchRules" name="specialMatchRules" class="browser-default custom-select custom-select-sm" >
			          <option value=""></option>
			          <option value="ISPL">ISPL (50-50 Over, 9-street-runs)</option>
			          <option value="MPL">MPL (Men Or Women)</option>
			      </select>
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="matchType" class="col-sm-4 col-form-label text-left">Select Match Type</label>
			    <div class="col-sm-6 col-md-6">
			      <select id="matchType" name="matchType" class="browser-default custom-select custom-select-sm" onchange="processUserSelection(this)">
			          <option value="ODI">One Day International</option>
			          <option value="IT20">T20 International</option>
			          <option value="DT20">Domestic T20</option>
			          <option value="D10">Domestic 10 Overs</option>
			          <option value="TEST">Test Match</option>
			          <option value="SUPER_OVER">Super Over</option>
			          <option value="OD">Domestic One Day</option>
			          <option value="FC">First Class</option>
			      </select>
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="select_existing_cricket_matches" class="col-sm-4 col-form-label text-left">Select Cricket Match 
			    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
			      <select id="select_existing_cricket_matches" name="select_existing_cricket_matches" class="browser-default custom-select custom-select-sm"
			      		onchange="processUserSelection(this)">
				        <option value="new_match">New Match</option>
						<c:forEach items = "${match_files}" var = "match">
				          <option value="${match.name}">${match.name}</option>
						</c:forEach>
			      </select>
			    </div>
			  </div>
			  <div id="matchFileName_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="matchFileName" class="col-sm-4 col-form-label text-left">Match Filename <i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
		             <input type="text" id="matchFileName" name="matchFileName" class="form-control form-control-sm floatlabel" onblur="processUserSelection(this);"></input>
		              <label id="matchFileName-validation" style="color:red;display: none;"></label> 
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="seasonId" class="col-sm-4 col-form-label text-left">Select Season 
			    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
			      <select id="seasonId" name="seasonId" class="browser-default custom-select custom-select-sm">
					<c:forEach items = "${seasons}" var = "season">
			          <option value="${season.seasonId}">${season.seasonDescription}</option>
					</c:forEach>
			      </select>
			    </div>
			  </div>
			  <div id="tournament_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="tournament" class="col-sm-4 col-form-label text-left">Tournament/Series Name <i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
		             <input type="text" id="tournament" name="tournament" class="form-control form-control-sm floatlabel" 
		             	onblur="processUserSelection(this);"></input>
			    </div>
			  </div>
			  <div id="matchIdent_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="matchIdent" class="col-sm-4 col-form-label text-left">Match Ident <i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
		             <input type="text" id="matchIdent" name="matchIdent" 
		             	class="form-control form-control-sm floatlabel" onblur="processUserSelection(this);"></input>
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="speedFilePath" class="col-sm-4 col-form-label text-left">Speed File Path</label>
			    <div class="col-sm-6 col-md-6">
		             <input type="text" id="speedFilePath" name="speedFilePath" class="form-control form-control-sm floatlabel" ></input>
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			 <label for="ballsPerOver" class="col-sm-1 col-form-label text-left">Balls Per Over</label>
			   <div class="col-sm-1 col-md-1">
			     <select id="ballsPerOver" name="ballsPerOver" class="browser-default custom-select custom-select-sm" >
 			       <option value="6">Six</option>
			       <option value="5">Five</option>
			     </select>
			   </div>
			  <label for="noBallsRuns" class="col-sm-1 col-form-label text-left">No Ball Runs</label>
			    <div class="col-sm-1 col-md-1">
			      <select id="noBallsRuns" name="noBallsRuns" class="browser-default custom-select custom-select-sm" >
 			        <option value="1">One</option>
			        <option value="2">Two</option>
			      </select>
			    </div>
			    <label for="generateInteractiveFile" class="col-sm-1 col-form-label text-left" style="display:none;">Hawk-eye</label>
			    <div class="col-sm-1 col-md-1">
			      <select id="generateInteractiveFile" name="generateInteractiveFile" class="browser-default custom-select custom-select-sm">
			          <option value="no">No</option>
			          <option value="yes">Yes</option>
			      </select>
			    </div>
<!-- 			    <label for="readPhotoColumn" class="col-sm-1 col-form-label text-right">Photo </label>
			    <div class="col-sm-1 col-md-1">
			      <select id="readPhotoColumn" name="readPhotoColumn" class="browser-default custom-select custom-select-sm">
			          <option value="yes">Yes</option>
			          <option value="no">No</option>
			      </select>
			    </div> -->
			    <label for="playerGender" class="col-sm-1 col-form-label text-right">Gender </label>
			    <div class="col-sm-1 col-md-1">
			      <select id="playerGender" name="playerGender" class="browser-default custom-select custom-select-sm">
			          <option value="men">Men</option>
			          <option value="women">Women</option>
			      </select>
			    </div>
			    <label for="reviewsPerTeam" class="col-sm-1 col-form-label text-center">Reviews</label>
			    <div class="col-sm-1 col-md-1">
			      <select id="reviewsPerTeam" name="reviewsPerTeam" class="browser-default custom-select custom-select-sm">
			      		<c:forEach begin="1" end="3" varStatus="loop">
				          <option value="${loop.index}">${loop.index}</option>
						</c:forEach>
			      </select>
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="tossResult" class="col-sm-4 col-form-label text-left">Select Toss Result <i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
			      <select id="tossResult" name="tossResult" class="browser-default custom-select custom-select-sm">
			          <option value="home_bat">Home Team Won Toss And Bat First</option>
			          <option value="home_field">Home Team Won Toss And Field First</option>
			          <option value="away_bat">Away Team Won Toss And Bat First</option>
			          <option value="away_field">Away Team Won Toss And Field First</option>
			      </select>
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="groundId" class="col-sm-4 col-form-label text-left">Select Ground 
			    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
			      <select id="groundId" name="groundId" class="browser-default custom-select custom-select-sm"
			      		onchange="processUserSelection(this)">
						<c:forEach items = "${grounds}" var = "ground">
				          <option value="${ground.groundId}">${ground.fullname}</option>
						</c:forEach>
			      </select>
			    </div>
			  </div>
			  <div id="subs_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="homeSubstitutesNumber" class="col-sm-2 col-form-label text-left">Home Substitutes </label>
			    <div class="col-sm-2 col-md-2">
			      <select id="homeSubstitutesNumber" name="homeSubstitutesNumber" class="browser-default custom-select custom-select-sm">
		      		<c:forEach begin="0" end="7" varStatus="loop">
			          <option value="${loop.index}">${loop.index}</option>
					</c:forEach>
			      </select>
			    </div>
			    <label for="awaySubstitutesNumber" class="col-sm-2 col-form-label text-left">Away Substitutes </label>
			    <div class="col-sm-2 col-md-2">
			      <select id="awaySubstitutesNumber" name="awaySubstitutesNumber" class="browser-default custom-select custom-select-sm">
		      		<c:forEach begin="0" end="7" varStatus="loop">
			          <option value="${loop.index}">${loop.index}</option>
					</c:forEach>
			      </select>
			    </div>
			    <label for="reducedOvers" class="col-sm-2 col-form-label text-left">Overs At Start</label>
			    <div class="col-sm-2 col-md-2">
		             <input type="text" id="reducedOvers" name="reducedOvers" class="form-control form-control-sm floatlabel"></input>
			    </div>
			  </div>
			  <div id="wagon_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
			    <label for="wagonXOffSet" class="col-sm-2 col-form-label text-left">Wagon X-Offset </label>
			    <div class="col-sm-2 col-md-2">
	               <input type="text" id="wagonXOffSet" name="wagonXOffSet" class="form-control form-control-sm floatlabel" value="84"></input>
			    </div>
			    <label for="wagonYOffSet" class="col-sm-2 col-form-label text-left">Wagon Y-Offset </label>
			    <div class="col-sm-2 col-md-2">
	               <input type="text" id="wagonYOffSet" name="wagonYOffSet" class="form-control form-control-sm floatlabel" value="66"></input>
			    </div>
			  </div>
			  <div id="target_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="targetRuns" class="col-sm-2 col-form-label text-left">Target Runs</label>
			    <div class="col-sm-2 col-md-2">
		             <input type="text" id="targetRuns" name="targetRuns" class="form-control form-control-sm floatlabel"></input>
			    </div>
			    <label for="targetType" class="col-sm-2 col-form-label text-left">Target Type</label>
			    <div class="col-sm-2 col-md-2">
			      <select id="targetType" name="targetType" class="browser-default custom-select custom-select-sm">
			          <option value=""></option>
			          <option value="dls">DLS</option>
			          <option value="vjd">VJD</option>
			      </select>
			    </div>
			    <label for="targetOvers" class="col-sm-2 col-form-label text-left">Target Overs </label>
			    <div class="col-sm-2 col-md-2">
		             <input type="text" id="targetOvers" name="targetOvers" class="form-control form-control-sm floatlabel"></input>
			    </div>
			  </div>
			  <div id="secondary_target_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
			    <label for="secondaryTargetRuns" class="col-sm-2 col-form-label text-left">Secondary Target Runs</label>
			    <div class="col-sm-2 col-md-2">
		             <input type="text" id="secondaryTargetRuns" name="secondaryTargetRuns" class="form-control form-control-sm floatlabel"></input>
			    </div>
			    <label for="secondaryTargetOvers" class="col-sm-2 col-form-label text-left">Secondary Target Overs </label>
			    <div class="col-sm-2 col-md-2">
		             <input type="text" id="secondaryTargetOvers" name="secondaryTargetOvers" class="form-control form-control-sm floatlabel"></input>
			    </div>
			  </div>
			  <div id="overs_remaining_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
			    <div class="col-sm-2 col-md-2">
				  <label for="overs_remaining_select_day" class="col-form-label text-left">Select Day</label>
			      <select id="overs_remaining_select_day" name="overs_remaining_select_day" 
			      		class="browser-default custom-select custom-select-sm" onclick="processUserSelection(this)">
		      		<c:forEach begin="1" end="5" varStatus="loop">
			          <option value="${loop.index}">${loop.index}</option>
					</c:forEach>
			      </select>
			   </div>
<%-- 			    <div class="col-sm-2 col-md-2">
				  <label for="overs_remaining_select_overs" class="col-form-label text-left">Daily Overs</label>
			      <select id="overs_remaining_select_overs" name="overs_remaining_select_overs" 
			      	class="browser-default custom-select custom-select-sm">
		      		<c:forEach begin="1" end="120" varStatus="loop">
			          <option value="${loop.index}" selected="selected">${loop.index}</option>
					</c:forEach>
			      </select>
				  <label for="new_ball_select_overs" class="col-form-label text-left">New Ball</label>
		            <input type="text" id="new_ball_select_overs" name="new_ball_select_overs" 
		            	class="col-sm-4 form-control form-control-sm floatlabel"></input>
				    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
				  		name="save_overs_remaining_btn" id="save_overs_remaining_btn" onclick="processUserSelection(this)">
			  			Save Overs</button>
                </div> --%>
			    <div class="col-sm-2 col-md-2">
				    <label for="followOn" class="col-form-label text-left">Follow On</label>
				      <select id="followOn" name="followOn" class="browser-default custom-select custom-select-sm">
				          <option value="no">No</option>
				          <option value="yes">Yes</option>
				      </select>
                </div>
			    <div class="col-sm-2 col-md-2">
				    <label for="followOnThreshold" class="col-form-label text-left">F/O Threshold</label>
		            <input type="text" id="followOnThreshold" name="followOnThreshold" 
		            	class="col-sm-4 form-control form-control-sm floatlabel"></input>
			    </div>
			  </div>
			  <div class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display: none;">
			    <label for="numberOfPowerplays" class="col-sm-4 col-form-label text-left">Number Of Powerplays 
			    	<i class="fas fa-asterisk fa-sm text-danger" style="font-size: 7px;"></i></label>
			    <div class="col-sm-6 col-md-6">
			      <select id="numberOfPowerplays" name="numberOfPowerplays" class="browser-default custom-select custom-select-sm"
			      		onchange="processUserSelection(this)">
			      		<c:forEach begin="0" end="3" varStatus="loop">
				          <option value="${loop.index}">${loop.index}</option>
						</c:forEach>
			      </select>
			    </div>
			  </div>
			  <div id="first_inn_pp_1" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
				  <div id="firstInningFirstPowerplayStartOverDiv" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
				    <label for="firstInningFirstPowerplayStartOver" class="col-sm-2 col-form-label text-left">1st Inn PP 1</label>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="firstInningFirstPowerplayStartOver" name="firstInningFirstPowerplayStartOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="firstInningFirstPowerplayEndOver" name="firstInningFirstPowerplayEndOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				  </div>
			  </div>
			  <div id="first_inn_pp_2" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
				  <div id="firstInningSecondPowerplayStartOverDiv" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
				    <label for="firstInningSecondPowerplayStartOver" class="col-sm-2 col-form-label text-left">1st Inn PP 2</label>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="firstInningSecondPowerplayStartOver" name="firstInningSecondPowerplayStartOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="firstInningSecondPowerplayEndOver" name="firstInningSecondPowerplayEndOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				  </div>
			  </div>
			  <div id="first_inn_pp_3" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
				  <div id="firstInningThirdPowerplayStartOverDiv" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
				    <label for="firstInningThirdPowerplayStartOver" class="col-sm-2 col-form-label text-left">1st Inn PP 3</label>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="firstInningThirdPowerplayStartOver" name="firstInningThirdPowerplayStartOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="firstInningThirdPowerplayEndOver" name="firstInningThirdPowerplayEndOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				  </div>
			  </div>
			  <div id="second_inn_pp_1" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
				  <div id="secondInningFirstPowerplayStartOverDiv" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
				    <label for="secondInningFirstPowerplayStartOver" class="col-sm-2 col-form-label text-left">2nd Inn PP 1</label>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="secondInningFirstPowerplayStartOver" name="secondInningFirstPowerplayStartOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="secondInningFirstPowerplayEndOver" name="secondInningFirstPowerplayEndOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				  </div>
			  </div>
			  <div id="second_inn_pp_2" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
				  <div id="secondInningSecondPowerplayStartOverDiv" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
				    <label for="secondInningSecondPowerplayStartOver" class="col-sm-2 col-form-label text-left">2nd Inn PP 2</label>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="secondInningSecondPowerplayStartOver" name="secondInningSecondPowerplayStartOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="secondInningSecondPowerplayEndOver" name="secondInningSecondPowerplayEndOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				  </div>
			  </div>
			  <div id="second_inn_pp_3" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
				  <div id="secondInningThirdPowerplayStartOverDiv" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;">
				    <label for="secondInningThirdPowerplayStartOver" class="col-sm-2 col-form-label text-left">2nd Inn PP 3</label>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="secondInningThirdPowerplayStartOver" name="secondInningThirdPowerplayStartOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				    <div class="col-sm-2 col-md-2">
			             <input type="text" id="secondInningThirdPowerplayEndOver" name="secondInningThirdPowerplayEndOver" 
			             	class="form-control form-control-sm floatlabel"></input>
				    </div>
				  </div>
			  </div>
	        	<table class="table table-striped table-bordered"> 
				  <thead>
			        <tr>
			        	<th>Select HOME Team: 
					      <select id="homeTeamId" name="homeTeamId" class="browser-default custom-select custom-select-sm">
							<c:forEach items = "${teams}" var = "team">
					          <option value="${team.teamId}">${team.teamName1}</option>
							</c:forEach>
					      </select>
			        	</th>
			        	<th>Select AWAY Team: 
					      <select id="awayTeamId" name="awayTeamId" class="browser-default custom-select custom-select-sm">
							<c:forEach items = "${teams}" var = "team" varStatus="status">
								<c:choose>
									<c:when test="${status.last}">
							          <option value="${team.teamId}" selected="selected">${team.teamName1}</option>
									</c:when>
									<c:otherwise>
							          <option value="${team.teamId}">${team.teamName1}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
					      </select>
			        	</th>
			        	<th>
						    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
						  		name="load_default_team_btn" id="load_default_team_btn" onclick="processUserSelection(this)">
					  		<i class="fas fa-download"></i> Load Teams</button>
			        	</th>
				    </tr>
				  </thead>
				</table>
			  <div id="team_selection_div" class="text-center" style="display:none;">
	         </div>
	         <div id="save_match_div" class="form-group row row-bottom-margin ml-2" style="margin-bottom:5px;display:none;">
			    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
			  		name="save_match_btn" id="save_match_btn" onclick="processUserSelection(this)">
		  		<i class="fas fa-download"></i> Save Match</button>
			    <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
			  		name="reset_match_btn" id="reset_match_btn" onclick="processUserSelection(this)">
		  		<i class="fas fa-window-close"></i> Reset Match</button>
	         </div>
          </div>
         </div>
       </div>
    </div>
  </div>
</div>
</form:form>
</body>
</html>