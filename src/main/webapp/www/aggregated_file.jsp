<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>


<t:main_template>
    <jsp:body>
        <div style="max-width: 100%; height: auto;">
            <div class="dropdown" style="margin: 8px;">
                <button class="btn btn-secondary dropdown-toggle" type="button" data-toggle="dropdown">Aggregated file
                    <span class="caret"></span></button>
                <ul class="dropdown-menu">
                    <li><a href="${pageContext.request.contextPath}/${fileID}/graph">Aggregated graph</a></li>
                    <li><a href="${pageContext.request.contextPath}/${fileID}/defaultFile">Default file</a></li>
                </ul>
            </div>
        </div>
    </jsp:body>
</t:main_template>