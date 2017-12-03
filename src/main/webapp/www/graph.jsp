<%@ page contentType="text/html; charset=UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script src="http://d3js.org/d3.v3.min.js"></script>
<script src="${pageContext.request.contextPath}/js/graph_definition.js"></script>

<t:main_template>
    <jsp:body>

        <div id="graph_menu" style="max-width: 100%; min-height: 60px;">
            <div class="dropdown" style="margin: 8px; float: left;">
                <button class="btn btn-secondary dropdown-toggle" type="button" data-toggle="dropdown">Aggregated graph
                    <span class="caret"></span></button>
                <ul class="dropdown-menu">
                    <li><a href="${pageContext.request.contextPath}/${fileID}/aggregatedFile/Turtle">Aggregated file</a></li>
                    <li><a href="${pageContext.request.contextPath}/${fileID}/defaultFile/Turtle">Default file</a></li>
                </ul>

            </div>
            <input type="button" class="btn btn-secondary" style="margin: 8px;" id="objectMerge" value="Split objects" onclick="splitObjects()">
        </div>

        <div id="svg-body" class="panel-body"></div>

        <script>
            $(document).ready(function(){
                createGraph('${fileID}');
            });
        </script>

    </jsp:body>
</t:main_template>