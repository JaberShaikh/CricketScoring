<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Match</title>
  
  <script type="text/javascript">
  
  	$(document).on("keypress", function(e){
  		processUserInput('LOG_VARIOUS',e.which);
	});
 
 	$(document).on('show.bs.dropdown', '#select_event_div .dropdown', function () {
  	  var $c = $('#select_event_div');
  	  // save originals (only first time)
  	  if (!$c.data('orig')) $c.data('orig', { o: $c.css('overflow'), m: $c.css('max-height'), z: $c.css('z-index') });
  	  $c.css({ overflow: 'visible', 'max-height': 'none', 'z-index': 2000 });
  	});
  	$(document).on('hide.bs.dropdown', '#select_event_div .dropdown', function () {
  	  var $c = $('#select_event_div'), orig = $c.data('orig') || {};
  	  $c.css({ overflow: orig.o || '', 'max-height': orig.m || '', 'z-index': orig.z || '' });
  	});  	

  	document.addEventListener('DOMContentLoaded', function () {
  	    const setupDiv = document.getElementById('load_setup_match');
  	    const toggleBtn = document.getElementById('toggle_setup_btn');
  	    const toggleIcon = document.getElementById('toggle_setup_icon');

  	    function hideSetup() {
  	        setupDiv.classList.add('d-none');
  	        toggleIcon.classList.remove('fa-eye');
  	        toggleIcon.classList.add('fa-eye-slash');
  	        toggleBtn.title = 'Show setup';
  	    }

  	    function showSetup() {
  	        setupDiv.classList.remove('d-none');
  	        toggleIcon.classList.remove('fa-eye-slash');
  	        toggleIcon.classList.add('fa-eye');
  	        toggleBtn.title = 'Hide setup';
  	    }

  	    // Toggle from button
  	    toggleBtn.addEventListener('click', function (e) {
  	        e.stopPropagation();
  	        if (setupDiv.classList.contains('d-none')) {
  	            showSetup();
  	        } else {
  	            hideSetup();
  	        }
  	    });

  	    // Auto-hide when clicking anywhere on the page (outside setup)
  	    document.addEventListener('click', function (e) {
  	        if (setupDiv.classList.contains('d-none')) return;
  	        if (setupDiv.contains(e.target) || toggleBtn.contains(e.target)) return;
  	        hideSetup();
  	    });
  	});
  	
  </script>

</head>
<body>

  <form:form name="cricket_form" autocomplete="off" enctype="multipart/form-data">
	<div class="content py-2" style="background-color: #EAE8FF; color: #2E008B">
	  <div class="container">
		 <div class="col-12">
	       <span class="anchor"></span>
	         <div class="card card-outline-secondary">
	         
				<div class="card-body" style="position: relative;">
				
				  <h6 id="match_error_lbl"></h6>
				  
				  <div class="row match-split-layout">
				    <div class="col-12 col-md-3 match-right">
				      <div id="select_event_div"
				           class="p-2 custom-small-border"
				           style="display:none; max-width:100%;">
				      </div>
				    </div>
				    <div class="col-12 col-md-9 match-left">
				    
					  <!-- Toggle button always visible -->
					  <div class="d-flex justify-content-end mb-1">
						<button type="button"
						        id="toggle_setup_btn"
						        class="btn btn-sm btn-link p-0 ms-2"
						        title="Show setup"
						        style="color:#2E008B; text-decoration:none;">
						  <i id="toggle_setup_icon" class="fas fa-eye"></i>
						</button>
					  </div>				    
				    
				      <div id="load_setup_match">

						<div class="d-flex align-items-end gap-1">
						  <div class="d-flex gap-2 align-items-end">
						
						    <!-- Setup Button -->
						    <button type="button" id="setup_match_btn" name="setup_match_btn"
						            class="btn btn-sm d-flex align-items-center"
						            style="background-color:#2E008B;color:#FEFEFE;"
						            onclick="processUserSelection(this)">
						      <span>Setup</span>
						      <i class="fas fa-wrench ms-1"></i>
						    </button>
						
						    <!-- Match Select + Load Button -->
						    <div class="d-flex flex-column">
						      <div class="input-group input-group-sm">
							    <label for="select_cricket_matches" class="form-label mb-0 small">Select Match</label>
						        <select id="select_cricket_matches"
						                name="select_cricket_matches"
						                class="form-select form-select-sm w-auto"
						                style="max-width:220px;">
						          <c:forEach items="${match_files}" var="match">
						            <option value="${match.name}">${match.name}</option>
						          </c:forEach>
						        </select>
						
						        <button type="button" id="load_match_btn" name="load_match_btn"
						                class="btn btn-sm d-flex align-items-center"
						                style="background-color:#2E008B;color:#FEFEFE;"
						                onclick="processUserSelection(this)">
						          <span>Load</span>
						          <i class="fas fa-file-import ms-1"></i>
						        </button>
						      </div>
						    </div>
						
						  </div>
						</div>
						
				        <div id="start_pause_match_time_div" class="mt-2" style="display:none;">
				          <div class="row g-2 align-items-center flex-nowrap">
				            <div id="match_data_update_div" class="col-auto" style="display:none;">
				              <div class="d-flex align-items-center gap-2">
				                <label for="matchDataUpdate" class="form-label mb-0">Match Update</label>
				                <select id="matchDataUpdate" name="matchDataUpdate"
				                        class="form-select form-select-sm w-auto"
				                        onchange="processUserSelection(this)">
				                  <option value="start">Start</option>
				                  <option value="pause">Pause</option>
				                </select>
				              </div>
				            </div>
				            <div id="select_match_innings_div" class="col-auto" style="display:none;">
				              <div class="d-flex align-items-center gap-2">
				                <label for="select_match_innings" class="form-label mb-0">Inning</label>
				                <select id="select_match_innings" name="select_match_innings"
				                        class="form-select form-select-sm w-auto">
				                </select>
				              </div>
				            </div>
				            <div id="select_match_status_div" class="col-auto" style="display:none;">
				              <div class="d-flex align-items-center gap-2">
				                <label for="select_match_status" class="form-label mb-0">Status</label>
				                <select id="select_match_status" name="select_match_status"
				                        class="form-select form-select-sm w-auto"
				                        onchange="processUserSelection(this)">
				                  <option value="pause">Pause</option>
				                  <option value="start">Start</option>
				                </select>
				              </div>
				            </div>
				            <div id="select_wagon_shot_div" class="col-auto">
				              <div class="d-flex align-items-center gap-2">
				                <label for="select_wagon_shot" class="form-label mb-0">Wagon Shots</label>
				                <select id="select_wagon_shot" name="select_wagon_shot"
				                        class="form-select form-select-sm w-auto">
				                  <option value=""></option>
				                  <option value="wagon">Wagon</option>
				                  <option value="wagon_shots">Wagon and Shots</option>
				                </select>
				              </div>
				            </div>
				            <div id="isDeclared_div" class="col-auto" style="display:none;">
				              <div class="d-flex align-items-center gap-2">
				                <label for="isDeclared" class="form-label mb-0">Declared</label>
				                <select id="isDeclared" name="isDeclared"
				                        class="form-select form-select-sm w-auto"
				                        onchange="processUserSelection(this)">
				                  <option value="no">No</option>
				                  <option value="yes">Yes</option>
				                </select>
				              </div>
				            </div>
				          </div>
				        </div>
				        <div id="select_day_session_div" class="row mt-2" style="display:none;">
				          <div class="col-12">
				            <div class="d-flex align-items-center flex-wrap gap-2">
				              <div class="d-flex align-items-center gap-1">
				                <label for="select_day" class="form-label mb-0">Day</label>
				                <select id="select_day" name="select_day"
				                        class="form-select form-select-sm w-auto"
				                        onchange="processUserSelection(this)">
				                  <option value="0"></option>
				                  <c:forEach var="i" begin="1" end="5">
				                    <option value="${i}">Day ${i}</option>
				                  </c:forEach>
				                </select>
				              </div>
				              <div class="d-flex align-items-center gap-1">
				                <label for="select_session" class="form-label mb-0">Session</label>
				                <select id="select_session" name="select_session"
				                        class="form-select form-select-sm w-auto">
				                  <option value="0"></option>
				                  <c:forEach var="i" begin="1" end="3">
				                    <option value="${i}">Session ${i}</option>
				                  </c:forEach>
				                </select>
				              </div>
				              <button type="button" id="log_day_session_btn" name="log_day_session_btn"
				                      class="btn btn-sm px-3 py-1"
				                      style="background-color:#2E008B;color:#FEFEFE; white-space:nowrap;"
				                      onclick="processUserSelection(this)">
				                <span class="spinner-border spinner-border-sm" role="status"
				                      aria-hidden="true" style="display:none"></span>
				                <i class="fas fa-clipboard-check"></i> Log Session
				              </button>
				              <label id="selected_day_session" class="form-label mb-0"
				                     style="white-space:nowrap;"></label>
				            </div>
				          </div>
				        </div>
				      </div> 
					<div id="inning_div"
					     class="d-flex flex-wrap p-2 mt-3 w-100"
					     style="display:none !important; width:100%; box-sizing:border-box; border:1px solid #2E008B; border-radius:6px;">
					</div>
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
