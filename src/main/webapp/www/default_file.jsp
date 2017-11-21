<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:main_template>
    <jsp:body>
        <div style="max-width: 100%; height: auto;">
            <div class="dropdown" style="margin: 8px;">
                <button class="btn btn-secondary dropdown-toggle" type="button" data-toggle="dropdown">Default file
                    <span class="caret"></span></button>
                <ul class="dropdown-menu">
                    <li><a href="${pageContext.request.contextPath}/${fileID}/graph">Aggregated graph</a></li>
                    <li><a href="${pageContext.request.contextPath}/${fileID}/aggregatedFile">Aggregated file</a></li>
                </ul>
                <button class="btn btn-secondary">TTL</button>
            </div>
        </div>

        <div class="wrap_text"
             style="padding: 12px; margin: 0 8px 8px 8px; background: #eeeeee; max-width: 100%; border-radius: 5px;">
            ${fileContent}
        </div>

    </jsp:body>
</t:main_template>