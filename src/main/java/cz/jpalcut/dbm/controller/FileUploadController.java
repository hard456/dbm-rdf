package cz.jpalcut.dbm.controller;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class FileUploadController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView viewFileUploader() {
        ModelAndView model = new ModelAndView();
        model.setViewName("file_upload");
        return model;
    }

    @RequestMapping(value = "/uploadTurtleFile", method = RequestMethod.POST)
    public @ResponseBody
    int uploadTurtleFile(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        ModelAndView model = new ModelAndView();
        model.setViewName("file_upload");
        RandomStringUtils.random(20,true,true);
        return 1;
    }

}
