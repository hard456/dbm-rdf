package cz.jpalcut.dbm.controller;



import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

@Controller
public class FileUploadController {

    @Autowired
    ServletContext servletContext;

    /**
     * Display File upload VIEW
     * @return ModelAndView - file_upload (VIEW)
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView viewFileUploader() {
        ModelAndView model = new ModelAndView();
        model.setViewName("file_upload");
        return model;
    }

    /**
     * For upload turtle file and process him to aggregated file
     * @param file Turtle FILE(.ttl)
     * @return 0 = file not exits, 1 = wrong file type, -1 = file not closed
     */
    @RequestMapping(value = "/uploadTurtleFile", method = RequestMethod.POST)
    public @ResponseBody
    String uploadTurtleFile(@RequestParam("file") MultipartFile file) {



        if(file == null || file.isEmpty()){
            return "0";
        }
        if(file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".ttl")){
            return "1";
        }

//      TODO: RDF aggregation

        String fileID;
        String ttlPath = servletContext.getRealPath("/Public/ttl/");

        while(true){
            fileID = RandomStringUtils.random(25,true,true);
            if(!new File(ttlPath + fileID +"-default.ttl").exists()){
                break;
            }
        }

        try {
            String filePath = ttlPath + fileID + "-default.ttl";
            File dest = new File(filePath);
            file.transferTo(dest);
        } catch (IOException e) {
            return "-1";
        }

        return fileID;
    }

}
