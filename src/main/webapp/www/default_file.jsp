<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<t:main_template>
    <jsp:body>
        <div style="max-width: 100%; height: auto;">
            <div class="dropdown" style="margin: 8px 5px 8px 8px; float: left;">
                <button class="btn btn-secondary dropdown-toggle" type="button" data-toggle="dropdown">Default file
                    <span class="caret"></span></button>
                <ul class="dropdown-menu">
                    <li><a href="${pageContext.request.contextPath}/${fileID}/graph">Aggregated graph</a></li>
                    <li><a href="${pageContext.request.contextPath}/${fileID}/aggregatedFile">Aggregated file</a></li>
                </ul>
            </div>
            <div class="dropdown" style="margin: 8px 8px 8px 0; float: left;">
                <button class="btn btn-secondary dropdown-toggle" type="button" data-toggle="dropdown">${RDFModelType}
                    <span class="caret"></span></button>

                <ul class="dropdown-menu">
                    <c:forEach var="model" items="${RDFModelEnum}">
                        <c:if test="${model ne RDFModelType}">
                            <li><a href="${pageContext.request.contextPath}/${fileID}/defaultFile/${model}">${model}</a></li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
        <br>
        <div class="wrap_text"
             style="padding: 12px; margin: 28px 8px 8px 8px; background: #eeeeee; max-width: 100%; border-radius: 5px;">
            ${fileContent}
        </div>

    </jsp:body>
</t:main_template>