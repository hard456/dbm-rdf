<%@ tag description="Page template" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<!DOCTYPE html>

<html>
<head>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">

    <!-- Mobile setting-->
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <%-- OWN CSS --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">

    <%-- jQuery --%>
    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>

    <!-- Bootstrap JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>


</head>

<body>

<%-- HEADER --%>
<div id="header" style="background-color: #339966; height: auto; padding: 10px 0 5px 0;">
    <div style="margin: 0 auto; padding: 7px; max-width: 1000px; height: auto;">
        <b><h4 style="color: #ffffff;"><a class="link" href="${pageContext.request.contextPath}/">RDF Model Aggregation</a></h4></b>
    </div>
</div>

<div id="bodyContent">
    <jsp:doBody/>
</div>

</body>

</html>
