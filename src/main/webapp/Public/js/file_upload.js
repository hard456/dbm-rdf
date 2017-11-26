/**
 * Function for upload Turtle file
 */
function uploadTurtleFile() {
    if (document.getElementById("my-file-selector").files.length == 0) {
        console.log("ERROR: no selected file");
    }
    else {
        var formData = new FormData();
        formData.append('file', $('input[type=file]')[0].files[0]);

        $.ajax({
            type: "POST",
            url: "/uploadTurtleFile",
            data: formData,
            contentType: false,
            processData: false,
            success: function (response) {
               if(response.localeCompare("0") == 0){
                   var errorResult = '<div class="alert alert-danger">You did not send a file</div>';
                   $('#fileUploadResult').html(errorResult);
               }
                if(response.localeCompare("1") == 0){
                    var errorResult = '<div class="alert alert-danger">Wrong file type</div>';
                    $('#fileUploadResult').html(errorResult);
                }
                if(response.localeCompare("2") == 0){
                    var errorResult = '<div class="alert alert-danger">Disabled file extension</div>';
                    $('#fileUploadResult').html(errorResult);
                }
                if(response.localeCompare("3") == 0){
                    console.log("File not closed");
                }
               else{
                   window.location.replace("/"+response+"/graph");
               }
            },
        });
    }
}