<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>

  <meta charset="utf-8" name="viewport" content="width=device-width, initial-scale=1">
  <title>Error</title>
	
  <script type="text/javascript" src="<c:url value="/webjars/jquery/1.9.1/jquery.min.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/webjars/bootstrap/3.3.6/js/bootstrap.min.js"/>"></script>
  <link rel="stylesheet" href="<c:url value="/webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>"/>  
	
</head>
<body>
	<div class="content py-5" style="background-color: #EAE8FF; color: #2E008B">
	  <div class="container">
		<div class="row">
		 <div class="col-md-8 offset-md-2">
	       <span class="anchor"></span>
	         <div class="card card-outline-secondary">
	           <div class="card-header">
	             <h2>${error_message}</h2>
	           </div>
	         </div>
	     </div>
	   </div>
	 </div>
	</div>
</body>
</html>