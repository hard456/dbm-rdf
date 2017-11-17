package cz.jpalcut.dbm.controller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

@Controller
public class FileUploadController {

    @Autowired
    ServletContext servletContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView viewFileUploader() {
        ModelAndView model = new ModelAndView();
        model.setViewName("file_upload");
        return model;
    }

    @RequestMapping(value = "/uploadTurtleFile", method = RequestMethod.POST)
    public @ResponseBody
    String uploadTurtleFile(@RequestParam("file") MultipartFile file) {

        if(file == null || file.isEmpty()){
            return "0";
        }

        String url = RandomStringUtils.random(25,true,true);
        String filePathToGraphsDir = servletContext.getRealPath("/Public/ttl/");

        try {
            String filePath = filePathToGraphsDir + url+"-default.ttl";
            File dest = new File(filePath);
            file.transferTo(dest);
        } catch (IOException e) {
            return "0";
        }


        return url;
    }

}
