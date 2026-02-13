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
       
	  <link rel="icon" href="data:,">
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
                           <h3 class="mb-0 text-center">Setup</h3>
                        </div>
                        <div class="card-body">
                           <div class="form-group d-flex flex-wrap align-items-center gap-2 mb-2" style="margin-bottom:5px;"> 
                           	<button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button" 
                           		name="cancel_match_setup_btn" id="cancel_match_setup_btn" onclick="processUserSelection(this)"> 
                           		<i class="fas fa-arrow-left"></i> Back</button> </div>
                           		
								<div class="d-flex flex-wrap align-items-center mb-2 match-settings-row"
								     style="column-gap:24px; row-gap:8px;">
							        <label for="specialMatchRules" class="form-label mb-0" style="white-space:nowrap;">
							            Special Match Rules
							        </label>
						            <select id="specialMatchRules" name="specialMatchRules" class="form-select form-select-sm" style="width:90px;">
						                <option value=""></option>
						                <option value="ISPL">ISPL (50-50 Over, 9-street-runs)</option>
						                <option value="MPL">MPL (Men Or Women)</option>
						            </select>
							        <label for="matchType" class="form-label mb-0" style="white-space:nowrap;">Select Match Type</label>
						            <select id="matchType" name="matchType" class="form-select form-select-sm" style="width:120px;" onchange="processUserSelection(this)">
						                <option value="ODI">One Day International</option>
						                <option value="IT20">T20 International</option>
						                <option value="DT20">Domestic T20</option>
						                <option value="D10">Domestic 10 Overs</option>
						                <option value="TEST">Test Match</option>
						                <option value="SUPER_OVER">Super Over</option>
						                <option value="OD">Domestic One Day</option>
						                <option value="FC">First Class</option>
						            </select>
							        <label for="reducedOvers" class="form-label mb-0" style="white-space:nowrap;">Overs At Start</label>
							        <input type="text" id="reducedOvers" name="reducedOvers" style="width:70px;" class="form-control form-control-sm floatlabel custom-small-border">
								</div>
								<div class="d-flex flex-wrap align-items-start gap-3 mb-2 match-settings-row">
								    <div class="form-group d-flex flex-wrap align-items-center gap-2 mb-2">
								        <label for="select_existing_cricket_matches" class="form-label me-2 mb-0" style="white-space:nowrap;">
								            Select Cricket Match
								        </label>
								        <div style="min-width:220px; margin-right:12px;">
								            <select id="select_existing_cricket_matches" name="select_existing_cricket_matches" class="form-select form-select-sm"
								                    onchange="processUserSelection(this)">
								                <option value="new_match">New Match</option>
								                <c:forEach items="${match_files}" var="match">
								                    <option value="${match.name}">${match.name}</option>
								                </c:forEach>
								            </select>
								        </div>
								    </div>
								    <div id="matchFileName_div"
								         class="form-group d-flex flex-wrap align-items-center gap-2 mb-2">
								        <label for="matchFileName" class="form-label me-2 mb-0" style="white-space:nowrap;">Match Filename</label>
								        <div style="min-width:220px; margin-right:12px;">
								            <input type="text" id="matchFileName" name="matchFileName" class="form-control form-control-sm floatlabel"
								                   onblur="processUserSelection(this)" />
											<!--<label id="matchFileName-validation" style="color:red; display:none;"></label> -->
								        </div>
								    </div>
								</div>
								<div class="d-flex flex-wrap align-items-start gap-3 mb-2 match-settings-row">
								    <div class="form-group d-flex flex-wrap align-items-center gap-2 mb-2">
								        <label for="seasonId" class="form-label me-2 mb-0" style="white-space:nowrap;">Select Season</label>
								        <div style="min-width:220px; margin-right:12px;">
								            <select id="seasonId"
								                    name="seasonId"
								                    class="form-select form-select-sm">
								                <c:forEach items="${seasons}" var="season">
								                    <option value="${season.seasonId}">
								                        ${season.seasonDescription}
								                    </option>
								                </c:forEach>
								            </select>
								        </div>
								    </div>
								    <div id="tournament_div"
								         class="form-group d-flex flex-wrap align-items-center gap-2 mb-2">
								        <label for="tournament"
								               class="form-label me-2 mb-0"
								               style="white-space:nowrap;">
								            Tournament/Series Name
								        </label>
								        <div style="min-width:220px; margin-right:12px;">
								            <input type="text"
								                   id="tournament"
								                   name="tournament"
								                   class="form-control form-control-sm floatlabel"
								                   onblur="processUserSelection(this)" />
								        </div>
								    </div>
								</div>
								<div class="d-flex flex-wrap align-items-start gap-3 mb-2 match-settings-row">
								    <div id="matchIdent_div"
								         class="form-group d-flex flex-wrap align-items-center gap-2 mb-2">
								        <label for="matchIdent"
								               class="form-label me-2 mb-0"
								               style="white-space:nowrap;">
								            Match Ident
								        </label>
								        <div style="min-width:220px; margin-right:12px;">
								            <input type="text"
								                   id="matchIdent"
								                   name="matchIdent"
								                   class="form-control form-control-sm floatlabel"
								                   onblur="processUserSelection(this)" />
								        </div>
								    </div>
								    <div class="form-group d-flex flex-wrap align-items-center gap-2 mb-2">
								        <label for="speedFilePath"
								               class="form-label me-2 mb-0"
								               style="white-space:nowrap;">
								            Speed File Path
								        </label>
								        <div style="min-width:220px; margin-right:12px;">
								            <input type="text"
								                   id="speedFilePath"
								                   name="speedFilePath"
								                   class="form-control form-control-sm floatlabel" />
								        </div>
								    </div>
								</div>
							<div class="d-flex flex-wrap align-items-start gap-3 mb-2 match-settings-row">
 							    <div class="form-group d-flex flex-wrap align-items-center gap-2 mb-2"> 
							        <label for="tossResult" class="form-label me-2 mb-0">Select Toss Result</label>
							        <div style="min-width:220px; margin-right:12px;">
							            <select id="tossResult" name="tossResult" class="form-select form-select-sm" 
							            	style="min-width:220px; margin-right:12px; border:1px solid #0d6efd; border-radius:6px; padding:3px; background:#f8f9ff;">
							                <option value="home_bat">Home Team Won Toss And Bat First</option>
							                <option value="home_field">Home Team Won Toss And Field First</option>
							                <option value="away_bat">Away Team Won Toss And Bat First</option>
							                <option value="away_field">Away Team Won Toss And Field First</option>
							            </select>
							        </div>
							    </div>
							    <div class="form-group d-flex flex-wrap align-items-center gap-2 mb-2">
							        <label for="groundId"
							               class="form-label me-2 mb-0"
							               style="white-space:nowrap;">
							            Select Ground
							        </label>
							        <div style="min-width:220px; margin-right:12px;">
							            <select id="groundId"
							                    name="groundId"
							                    class="form-select form-select-sm"
							                    onchange="processUserSelection(this)">
							                <c:forEach items="${grounds}" var="ground">
							                    <option value="${ground.groundId}">${ground.fullname}</option>
							                </c:forEach>
							            </select>
							        </div>
							    </div>
							</div>
                           <div class="form-group d-flex flex-wrap align-items-center gap-2 mb-2" style="margin-bottom:5px;">
                              <label for="ballsPerOver" class="form-label me-2 mb-0" style="white-space:nowrap;">Balls/Over</label> 
                              <div class="col-sm-1 col-md-1">
                                 <select id="ballsPerOver" name="ballsPerOver" class="form-select form-select-sm" style="width:70px;">
                                    <option value="6">Six</option>
                                    <option value="5">Five</option>
                                 </select>
                              </div>
                              <label for="noBallsRuns" class="form-label me-2 mb-0" style="white-space:nowrap;">No Ball Runs</label> 
                              <div class="col-sm-1 col-md-1">
                                 <select id="noBallsRuns" name="noBallsRuns" class="form-select form-select-sm" style="width:70px;">
                                    <option value="1">One</option>
                                    <option value="2">Two</option>
                                 </select>
                              </div>
                              <label for="generateInteractiveFile" class="form-label me-2 mb-0" style="white-space:nowrap;" style="display:none;">Hawk-eye</label> 
                              <div class="col-sm-1 col-md-1">
                                 <select id="generateInteractiveFile" name="generateInteractiveFile" class="form-select form-select-sm" style="width:70px;">
                                    <option value="no">No</option>
                                    <option value="yes">Yes</option>
                                 </select>
                              </div>
                              <!-- <label for="readPhotoColumn" class="form-label me-2 mb-0" style="white-space:nowrap;">Photo </label> <div class="col-sm-1 col-md-1"> <select id="readPhotoColumn" name="readPhotoColumn" class="form-select form-select-sm"> <option value="yes">Yes</option> <option value="no">No</option> </select> </div> --> <label for="playerGender" class="form-label me-2 mb-0" style="white-space:nowrap;">Gender </label> 
                              <div class="col-sm-1 col-md-1">
                                 <select id="playerGender" name="playerGender" class="form-select form-select-sm" style="width:70px;">
                                    <option value="men">Men</option>
                                    <option value="women">Women</option>
                                 </select>
                              </div>
                              <label for="reviewsPerTeam" class="form-label me-2 mb-0" style="white-space:nowrap;">Reviews</label> 
                              <div class="col-sm-1 col-md-1">
                                 <select id="reviewsPerTeam" name="reviewsPerTeam" class="form-select form-select-sm" style="width:70px;">
                                    <c:forEach begin="1" end="3" varStatus="loop">
                                       <option value="${loop.index}">${loop.index}</option>
                                    </c:forEach>
                                 </select>
                              </div>
                           </div>
							<div id="target_div"
							     class="form-group d-flex align-items-center mb-2"
							     style="margin-bottom:5px; gap:8px; flex-wrap: nowrap;">
							  <label for="targetRuns" class="form-label mb-0" style="white-space:nowrap;">Target Runs</label>
							  <input type="text" id="targetRuns" name="targetRuns" class="form-control form-control-sm floatlabel custom-small-border" style="width:50px;">
							  <label for="targetType" class="form-label mb-0" style="white-space:nowrap;">Target Type</label>
							  <select id="targetType" name="targetType" class="form-select form-select-sm" style="width:90px;">
							      <option value=""></option>
							      <option value="dls">DLS</option>
							      <option value="vjd">VJD</option>
							  </select>
							  <label for="targetOvers" class="form-label mb-0" style="white-space:nowrap;">Target Overs</label>
							  <input type="text" id="targetOvers" name="targetOvers" class="form-control form-control-sm floatlabel custom-small-border" style="width:50px;">
						    <label for="secondaryTargetRuns" class="form-label mb-0" style="white-space:nowrap;">2nd Target Runs</label>
						    <input type="text" id="secondaryTargetRuns" name="secondaryTargetRuns" class="form-control form-control-sm floatlabel custom-small-border" style="width:50px;">
						    <label for="secondaryTargetOvers" class="form-label mb-0" style="white-space:nowrap;">2nd Target Overs</label>
						    <input type="text" id="secondaryTargetOvers" name="secondaryTargetOvers" class="form-control form-control-sm floatlabel custom-small-border" style="width:50px;">
																																																																																	</div>
						<div id="overs_remaining_div" class="form-group d-flex align-items-center gap-2 mb-2" style="margin-bottom:5px; display:none; flex-wrap: nowrap;">
						  <label for="overs_remaining_select_day" class="form-label mb-0">Select Day</label>
						  <select id="overs_remaining_select_day" name="overs_remaining_select_day" class="form-select form-select-sm" 
						  		onclick="processUserSelection(this)" style="width:auto; min-width:max-content;">
						    <c:forEach begin="1" end="5" varStatus="loop">
						      <option value="${loop.index}">${loop.index}</option>
						    </c:forEach>
						  </select>
						  <label for="followOn" class="form-label mb-0">Follow On</label>
						  <select id="followOn" name="followOn" class="form-select form-select-sm" style="width:auto; min-width:max-content;">
						    <option value="no">No</option>
						    <option value="yes">Yes</option>
						  </select>
						  <label for="followOnThreshold" class="form-label mb-0">F/O Threshold</label>
						  <input type="text" id="followOnThreshold" name="followOnThreshold" class="form-control form-control-sm floatlabel custom-small-border" style="width:6ch;">
						</div>

						<div id="subs_div" class="form-group row mb-2" style="margin-bottom:5px;">
						   <div class="col-4 d-flex align-items-center dap-1">
						      <label for="homeSubstitutesNumber" class="form-label mb-0" style="white-space:nowrap;">Home Substitutes</label>
						      <select id="homeSubstitutesNumber" 
						              name="homeSubstitutesNumber" 
						              class="form-select form-select-sm"
						              style="width:70px;">
						         <c:forEach begin="0" end="7" varStatus="loop">
						            <option value="${loop.index}">${loop.index}</option>
						         </c:forEach>
						      </select>
						   </div>
						   <div class="col-2"></div>
						   <div class="col-4 d-flex align-items-center gap-1">
						      <label for="awaySubstitutesNumber" class="form-label mb-0" style="white-space:nowrap;">
						         Away Substitutes
						      </label>
						      <select id="awaySubstitutesNumber" 
						              name="awaySubstitutesNumber" 
						              class="form-select form-select-sm"
						              style="width:70px;">
						         <c:forEach begin="0" end="7" varStatus="loop">
						            <option value="${loop.index}">${loop.index}</option>
						         </c:forEach>
						      </select>
						   </div>
						</div>
                           <table class="table table-striped table-bordered">
                              <thead>
                                 <tr>
                                    <th>
                                       Select HOME Team: 
                                       <select id="homeTeamId" name="homeTeamId" class="form-select form-select-sm">
                                          <c:forEach items = "${teams}" var = "team">
                                             <option value="${team.teamId}">${team.teamName1}</option>
                                          </c:forEach>
                                       </select>
                                    </th>
                                    <th>
                                       Select AWAY Team: 
                                       <select id="awayTeamId" name="awayTeamId" class="form-select form-select-sm">
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
                                    	<i class="fas fa-users"></i> Load Teams</button> 
                                    </th>
                                 </tr>
                              </thead>
                           </table>
                           <div id="team_selection_div" class="text-center" style="display:none;"> </div>
                           <div id="save_match_div" class="form-group d-flex flex-wrap align-items-center gap-2 mb-2" style="margin-bottom:5px;display:none;"> 
                           		<button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button" name="save_match_btn" 
                           		id="save_match_btn" onclick="processUserSelection(this)"> <i class="fas fa-save"></i> Save Match</button> 
                           		<button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button" name="reset_match_btn" 
                           		id="reset_match_btn" onclick="processUserSelection(this)"> <i class="fas fa-trash-restore"></i> Reset Match</button> 
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