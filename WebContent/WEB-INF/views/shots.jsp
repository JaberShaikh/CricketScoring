<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>
  
  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Shot Selection</title>
  
  <script type="text/javascript">
  
    $(document).ready(function(){
      $('.aerial_ground_single_check_only').click(function() {
         $('.aerial_ground_single_check_only').not(this).prop('checked', false);
      });
      $('.boundary_single_check_only').click(function() {
          $('.boundary_single_check_only').not(this).prop('checked', false);
       });
    });
   
  </script>
</head>
<body>
<form:form name="shot_form" autocomplete="off">
<div class="content py-2" style="background-color: #EAE8FF; color: #2E008B">
  <div class="container">
	<div class="row">
	 <div class="col-md-12 offset-md-12">
       <span class="anchor"></span>
         <div class="card card-outline-secondary">
          <div class="card-body">
			<div class="row">
			 <div class="col-lg-4">
			    <h6>On Ground</h6>
 				<table class="table table-bordered table-responsive">
				  <thead>
				    <tr>
				      <th scope="col">Shots</th>
				      <th scope="col">Front</th>
				      <th scope="col">Back</th>
				      <th scope="col">Edge/Miss</th>
				    </tr>
				  </thead>
				  <tbody>
				    <tr>
				      <th scope="row">No Shot</th>
				      <td>
					    <input id="no_shot_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="no_shot_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="no_shot_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Defence</th>
				      <td>
					    <input id="defence_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="defence_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="defence_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Nudge</th>
				      <td>
					    <input id="nudge_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="nudge_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="nudge_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Cover Drive</th>
				      <td>
					    <input id="cover_drive_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="cover_drive_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="cover_drive_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Square Cut</th>
				      <td>
					    <input id="square_cut_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="square_cut_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="square_cut_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Off Drive</th>
				      <td>
					    <input id="off_drive_front" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="off_drive_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="off_drive_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Straight Drive</th>
				      <td>
					    <input id="straight_drive_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="straight_drive_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="straight_drive_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">On Drive</th>
				      <td>
					    <input id="on_drive_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="on_drive_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="on_drive_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Pull/Hook</th>
				      <td>
					    <input id="pull_hook_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="pull_hook_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="pull_hook_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Reverse Sweep</th>
				      <td>
					    <input id="reverse_sweep_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="reverse_sweep_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="reverse_sweep_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Sweep/Slog Sweep</th>
				      <td>
					    <input id="sweep_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="sweep_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="sweep_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Glance/Flick</th>
				      <td>
					    <input id="glance_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="glance_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="glance_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Steer 3rd Man</th>
				      <td>
					    <input id="steer_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="steer_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="steer_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Slog Shot</th>
				      <td>
					    <input id="slog_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="slog_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="slog_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <th scope="row">Unorthodox Shot</th>
				      <td>
					    <input id="unorthodox_front_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="unorthodox_back_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="unorthodox_edge_miss_ground" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				  </tbody>
				</table> 
			  </div>
			 <div class="col-lg-3">
			    <h6>Aerial</h6>
				<table class="table table-bordered table-responsive">
				  <thead>
				    <tr>
				      <th scope="col">Front</th>
				      <th scope="col">Back</th>
				      <th scope="col">Edge/Miss</th>
				    </tr>
				  </thead>
				  <tbody>
				    <tr>
				      <td>
					    <input id="no_shot_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="no_shot_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="no_shot_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="defence_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="defence_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="defence_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="nudge_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="nudge_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="nudge_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="cover_drive_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="cover_drive_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="cover_drive_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="square_cut_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="square_cut_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="square_cut_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="off_drive_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="off_drive_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="off_drive_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="straight_drive_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="straight_drive_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="straight_drive_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="on_drive_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="on_drive_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="on_drive_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="pull_hook_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="pull_hook_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="pull_hook_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="reverse_sweep_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="reverse_sweep_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="reverse_sweep_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="sweep_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="sweep_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="sweep_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="glance_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="glance_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="glance_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="steer_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="steer_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="steer_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="slog_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="slog_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="slog_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				    <tr>
				      <td>
					    <input id="unorthodox_front_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="unorthodox_back_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				      <td>
					    <input id="unorthodox_edge_miss_aerial" type="checkbox" class="aerial_ground_single_check_only" ></input>
					  </td>
				    </tr>
				  </tbody>
				</table>
				 <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
					name="upload_shots_btn" id="upload_shots_btn" onclick="processUserSelection(this)">
					<i class="fas fa-cloud-upload-alt"></i> Upload Shots Data</button>
				 <button style="background-color:#2E008B;color:#FEFEFE;" class="btn btn-sm" type="button"
					name="cancel_shots_btn" id="cancel_shots_btn" onclick="processUserSelection(this)">
					<i class="fas fa-times"></i> Cancel</button>

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