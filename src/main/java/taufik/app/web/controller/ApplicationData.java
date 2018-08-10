/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.stereotype.Controller;
import taufik.app.model.Task;
import taufik.app.model.Tenant;
import taufik.app.repo.TaskRepo;

/**
 * Use this class to store values in the application cache.
 * @author Randy
 */
@Controller
public class ApplicationData implements InitializingBean {
    
    @Autowired
    private TaskRepo taskRepo;
    
    @Setter
    @Getter
    private Tenant sharedTenant;
    
    @Getter
    private List<Task> listTaskMenu;
    @Getter
    private List<Task> listTaskAction;
    @Getter
    private List<Task> listTaskField;
    
    @Getter
    private Map<String, Collection<ConfigAttribute>> urlConfigAttributes;

    public ApplicationData() {
        resetUrlConfigAttributes();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // load all task
        listTaskMenu = taskRepo.findAllByTaskType(Task.Type.MENU);
        listTaskAction = taskRepo.findAllByTaskType(Task.Type.ACTION);
        listTaskField = taskRepo.findAllByTaskType(Task.Type.FIELD);
        
        // create new if null
        if (listTaskMenu == null) {
            listTaskMenu = new ArrayList<>();
        }
        if (listTaskAction == null) {
            listTaskAction = new ArrayList<>();
        }
        if (listTaskField == null) {
            listTaskField = new ArrayList<>();
        }
    }
    
    /**
     * function to reset URL roles that has been saved in URL-intercept list.
     */
    public void resetUrlConfigAttributes(){
        urlConfigAttributes = new HashMap<>();
    }
    
}
