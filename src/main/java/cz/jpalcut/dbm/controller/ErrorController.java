package cz.jpalcut.dbm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {

    /**
     * Handle HTTP error codes that are defined in web.xml
     * @param error_code HTTP error code
     * @return ModelAndView - error (VIEW), error (error code)
     */
    @RequestMapping(value = {"/error/{error_code}"}, method = RequestMethod.GET)
    public ModelAndView loginPage(@PathVariable int error_code) {
        ModelAndView model = new ModelAndView();
        model.setViewName("error");
        model.addObject("error",error_code);
        return model;
    }

}
