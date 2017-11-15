<%@ tag description="Page template" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<!DOCTYPE html>

<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <!-- Mobile setting-->
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <%-- OWN CSS --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

    <%-- BOOTSTRAP 4 CSS --%>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css"
          integrity="sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb" crossorigin="anonymous">

    <%-- jQuery --%>
    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>

    <%-- Bootstrap JavaScript --%>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.3/umd/popper.min.js"
            integrity="sha384-vFJXuSJphROIrBnz7yo7oB41mKfc8JzQZiCq4NCceLEaO4IHwicKwpJf9c9IpFgh"
            crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/js/bootstrap.min.js"
            integrity="sha384-alpBpkh1PFOepccYVYDB4do5UnbKysX5WZXm3XxPqe5iKTfUKjNkCk9SaVuEZflJ"
            crossorigin="anonymous"></script>

    <%-- FILE INPUT CSS & JS --%>
    <%--<link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-fileinput/4.4.5/css/fileinput.min.css" media="all" rel="stylesheet" type="text/css" />--%>
    <%--<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-fileinput/4.4.5/js/fileinput.min.js"></script>--%>

</head>

<body>

<div id="header" style="background-color: #339966; height: auto; padding: 10px 0 5px 0;">
    <div class="container" style="max-width: 1000px;">
        <h4 style="color: #ffffff;"><a class="link" href="${pageContext.request.contextPath}/">RDF Graph Aggregation</a></h4>
    </div>
</div>

<div id="bodyContent">
    <jsp:doBody/>
</div>

</body>

</html>
