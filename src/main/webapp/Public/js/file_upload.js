/**
 * Function for upload file to server
 */
function uploadTurtleFile() {
    if (document.getElementById("my-file-selector").files.length == 0) {
        console.log("ERROR: no selected file");
    }
    else {
        var form = $('#fileUploadForm')[0];
        var data = new FormData(form);

        //Enable loading animation
        $('#loader').css("display", "block");

        //Disable buttons
        $('#uploadButton').prop('disabled', true);
        $('#my-file-selector').prop('disabled', true);

        $.ajax({
            type: "POST",
            url: window.location.href + "/uploadTurtleFile",
            data: data,
            contentType: false,
            processData: false,
            success: function (response) {
               if(response.localeCompare("0") == 0){
                   var errorResult = '<div class="alert alert-danger">You did not send a file</div>';
                   $('#fileUploadResult').html(errorResult);
               }
               else if(response.localeCompare("1") == 0){
                    var errorResult = '<div class="alert alert-danger">Wrong file type</div>';
                    $('#fileUploadResult').html(errorResult);
                }
               else if(response.localeCompare("2") == 0){
                    var errorResult = '<div class="alert alert-danger">Disabled file extension</div>';
                    $('#fileUploadResult').html(errorResult);
                }
               else if(response.localeCompare("3") == 0){
                    console.log("IOException");
                }
               else{
                   window.location.replace(window.location.href+response+"/graph");
               }

               //Disable loading animation
               $('#loader').css("display", "none");

                //Enable buttons
                $('#uploadButton').prop('disabled', true);
                $('#my-file-selector').prop('disabled', true);

            },
        });
    }
}