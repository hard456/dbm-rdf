<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script src="${pageContext.request.contextPath}/js/file_upload.js"></script>

<t:main_template>
    <jsp:body>
        <%--FILE UPLOADER--%>
        <div class="container" style="max-width: 1000px; margin-top: 70px;">

            <div class="row">
                <form:form method="post" action="${pageContext.request.contextPath}/uploadTurtleFile" enctype="multipart/form-data" id="fileUploadForm">
                        <%-- INPUT TEXT FOR DISPLAY SELECTED FILE --%>
                    <div class="col-lg-8 col-sm-8 col-xs-12">
                        <input class="form-control" id="upload-file-info" readonly
                               style="background: #ffffff; margin-bottom: 8px; color: #848484"
                               value="Input for *.ttl,*.owl,*.rdf,*.nt files">
                    </div>
                        <%-- BROWSE BUTTON --%>
                    <div class="col-lg-2 col-sm-2 col-xs-6">
                        <label class="btn btn-primary" for="my-file-selector" style="width: 100%">
                            <input name="file" id="my-file-selector" type="file" style="display:none;"
                                   onchange="$('#upload-file-info').val(this.files[0].name);"
                                   accept=".ttl,.owl,.nt,.rdf">
                            Browse
                        </label>
                    </div>
                        <%-- UPLOAD BUTTON --%>
                    <div class="col-lg-2 col-sm-2 col-xs-6">
                        <button type="button" class="btn btn-success" onclick="uploadTurtleFile()"
                                style="background-color: #339966; width: 100%">Upload
                        </button>
                    </div>
                </form:form>
            </div>
            <div id="fileUploadResult"></div>
        </div>
    </jsp:body>
</t:main_template>