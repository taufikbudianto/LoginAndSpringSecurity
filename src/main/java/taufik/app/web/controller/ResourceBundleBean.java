/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.controller;

import java.util.HashMap;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Randy
 */
@Controller(value = "msg")
public class ResourceBundleBean extends HashMap {

    @Autowired
    private MessageSource messageSource;

    @Override
    public String get(Object key) {
        ServletRequest request = (ServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String message;
        try {
            message = messageSource.getMessage((String) key, null, request.getLocale());
        } catch (NoSuchMessageException e) {
            message = "???" + key + "???";
        }
        return message;
    }
    
}
