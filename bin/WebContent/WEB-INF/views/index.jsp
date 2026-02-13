<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html>
<html>
<head>

  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">

  <title>Cricket</title>

  <script type="text/javascript" src="<c:url value="/webjars/jquery/3.7.1/jquery.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/webjars/bootstrap/5.3.2/js/bootstrap.bundle.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/webjars/select2/4.0.13/js/select2.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/resources/javascript/index.js"/>"></script>

  <link rel="stylesheet" href="<c:url value="/webjars/bootstrap/5.3.2/css/bootstrap.min.css"/>"/>  
  <link rel="stylesheet" href="<c:url value="/webjars/font-awesome/6.5.1/css/all.css"/>">
  <link rel="stylesheet" href="<c:url value="/webjars/select2/4.0.13/css/select2.css"/>"/>  
  <link rel="stylesheet" href="<c:url value="/resources/css/index.css"/>"/> 
  
</head>
<body onload="afterPageLoad('MATCH')">
	<div class="content py-1" style="background-color:#EAE8FF;color:#2E008B;">
		<div class="container">
            <div class="accordion" id="match_wagon_shots_menu">
              <div class="accordion-item" id="match-panel">
                <h2 class="accordion-header" id="headingMatch">
                  <button class="accordion-button collapsed" type="button"
                          data-bs-toggle="collapse"
                          data-bs-target="#match_sub_menu"
                          aria-expanded="false"
                          aria-controls="match_sub_menu"
                          style="border-style:solid;border-width:medium;border-radius:25px; background-color:transparent; color:#2E008B;">
                    Match
                  </button>
                </h2>
                <div id="match_sub_menu" class="accordion-collapse collapse" aria-labelledby="headingMatch" data-bs-parent="#match_wagon_shots_menu">
                  <div class="accordion-body p-2">
                    <%@ include file="match.jsp" %>
                  </div>
                </div>
              </div>
            </div>

            <div class="panel panel-default" id="wagon-panel"
     style="margin-top:5px;display:none;">
			    <div class="panel-heading" role="tab" id="wagon_heading" style="border-style:solid;border-width:medium;border-radius:25px;">
			      <h5 class="panel-title" style="position:relative;left:10px;">
			        <a class="collapsed" data-toggle="collapse" data-parent="#match_wagon_shots_menu" href="#wagon_sub_menu" aria-expanded="false" 
			        	aria-controls="wagon_sub_menu">Wagon 
			        </a>
			      </h5>
			    </div>
			    <div id="wagon_sub_menu" class="panel-collapse collapse" role="tabpanel" aria-labelledby="wagon_heading">
			      <div class="panel-body">
					<%@ include file="wagon.jsp" %>
			      </div>
			    </div>
			  </div>
			  <div class="panel panel-default" id="shots-panel" style="margin-top:5px;display:none;">
			    <div class="panel-heading" role="tab" id="shots_heading" style="border-style:solid;border-width:medium;border-radius:25px;">
			      <h5 class="panel-title" style="position:relative;left:10px;">
			        <a class="collapsed" data-toggle="collapse" data-parent="#match_wagon_shots_menu" href="#shots_sub_menu" aria-expanded="false" 
			        	aria-controls="shots_sub_menu" >Shots 
			        </a>
			      </h5>
			    </div>
			    <div id="shots_sub_menu" class="panel-collapse collapse" role="tabpanel" aria-labelledby="shots_heading">
			      <div class="panel-body">
					<%@ include file="shots.jsp" %>
			      </div>
			    </div>
			  </div>
			</div>
		</div>
	</div>
</body>
</html>