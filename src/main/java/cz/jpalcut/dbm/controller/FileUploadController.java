package cz.jpalcut.dbm.controller;


import cz.jpalcut.dbm.utils.Enum;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.ServletContext;
import java.io.*;

@Controller
public class FileUploadController {

    @Autowired
    ServletContext servletContext;

    /**
     * Display File upload VIEW
     *
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
     *
     * @param file Turtle FILE(.ttl)
     * @return 0 = file not exits, 1 = wrong file type, -1 = file not closed
     */
    @RequestMapping(value = "/uploadTurtleFile", method = RequestMethod.POST)
    public @ResponseBody
    String uploadTurtleFile(@RequestParam("file") MultipartFile file) {


        if (file == null || file.isEmpty()) {
            return "0";
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            return "1";
        }

        String fileExt = FileUtils.getFilenameExt(fileName);

        if (!EnumUtils.isValidEnum(Enum.RDFFileExt.class, fileExt)) {
            return "2";
        }


//      TODO: RDF aggregation

        String fileID;
        String ttlPath = servletContext.getRealPath("/Public/ttl/");

        while (true) {
            fileID = RandomStringUtils.random(25, true, true);
            if (!new File(ttlPath + fileID + "-default.ttl").exists()) {
                break;
            }
        }

        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(file.getBytes());
            ByteArrayOutputStream byteOutstream = new ByteArrayOutputStream();

            Model loadModel = ModelFactory.createDefaultModel();
            RDFDataMgr.read(loadModel, byteArrayInputStream, Enum.RDFFileExt.valueOf(fileExt).getLangType());
            loadModel.write(byteOutstream, "Turtle");

            FileOutputStream outStream = new FileOutputStream(ttlPath + fileID + "-default.ttl");
            String newString = new String(byteOutstream.toByteArray(),"UTF-8");
            outStream.write(newString.getBytes("UTF-8"));
            outStream.close();

        } catch (IOException e) {
            return "3";
        }

        return fileID;
    }

}
