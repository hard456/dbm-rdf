function uploadTurtleFile() {
    if (document.getElementById("my-file-selector").files.length == 0) {
        console.log("no files selected");
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
                   console.log("error");
               }
               else{
                   window.location.replace("/"+response+"/graph");
               }
            }
        });

    }
}