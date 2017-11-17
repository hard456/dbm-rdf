package cz.jpalcut.dbm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import java.io.*;

@Controller
public class GraphController {

    @Autowired
    ServletContext servletContext;

    @RequestMapping(value = "/{id}/graph", method = RequestMethod.GET)
    public ModelAndView viewGraph(@PathVariable("id") String fileID) {

        ModelAndView model = new ModelAndView();
        FileReader reader;

//        try {
//            reader = new FileReader( File.separator + "ttl" + File.separator + fileID + "-agg.ttl");
//        } catch (Exception e) {
//            model.addObject("error",404);
//            model.setViewName("error");
//            return model;
//        }
//
//        try {
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        model.addObject("fileID",fileID);

        model.setViewName("graph");
        return model;
    }

    @RequestMapping(value = "/{id}/defaultFile", method = RequestMethod.GET)
    public ModelAndView viewDefaultFile(@PathVariable("id") String fileID) {

        ModelAndView model = new ModelAndView();
        BufferedReader reader;
        StringBuffer stringBuffer = new StringBuffer();

        try {
            String filePathToGraphsDir = servletContext.getRealPath("/Public/ttl/");
            reader = new BufferedReader(new FileReader(filePathToGraphsDir + fileID + "-default.ttl"));


            String line = null;
            while((line =reader.readLine())!=null){

                stringBuffer.append(line).append("<br>");
            }


        } catch (Exception e) {
            model.addObject("error",404);
            model.setViewName("error");
            return model;
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addObject("fileContent",stringBuffer);
        model.addObject("fileID",fileID);

        model.setViewName("default_file");
        return model;
    }

    @RequestMapping(value = "/{id}/aggregatedFile", method = RequestMethod.GET)
    public ModelAndView viewAggregatedFile(@PathVariable("id") String fileID) {

        ModelAndView model = new ModelAndView();
        FileReader reader;

//        try {
//            reader = new FileReader( File.separator + "ttl" + File.separator + fileID + "-agg.ttl");
//        } catch (Exception e) {
//            model.addObject("error",404);
//            model.setViewName("error");
//            return model;
//        }
//
//        try {
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        model.addObject("fileID",fileID);

        model.setViewName("aggregated_file");
        return model;
    }

}
