/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taufik.app.model.Role;
import taufik.app.model.Task;
import taufik.app.repo.RoleRepo;
import taufik.app.repo.TaskRepo;
import taufik.app.web.controller.ApplicationData;

/**
 *
 * @author Randy
 */
@Service
@Transactional(readOnly = false, rollbackFor = { Exception.class })
public class TaskService {
    
    @Autowired
    private ApplicationData applicationData;
    
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private TaskRepo taskRepo;
    
    /**
     * Push Task to applicationData.
     * @param task
     * @param taskType 
     */
    private void pushToAppTaskList(Task task, Task.Type taskType) {
        switch (taskType) {
            case ACTION:
                applicationData.getListTaskAction().add(task);
                break;
            case FIELD:
                applicationData.getListTaskField().add(task);
                break;
            case MENU:
                applicationData.getListTaskMenu().add(task);
                break;
            default:
                break;
        }
    }
    
    /**
     * Insert a new Task and also as the child for specific parent. Leave the parentTask null 
     * if task doesn't have a parent.
     * @param task
     * @param parentTask
     * @throws Exception 
     */
    public void insertTask(Task task, Task parentTask) throws Exception {
        
        // insert table task
        taskRepo.save(task);
        
        // insert as child_task
        if (parentTask != null) {
            parentTask.addChild(task, (short) (parentTask.getChildTaskList().size() + 1));
            taskRepo.save(parentTask);
        }

        // push to task list
        pushToAppTaskList(task, task.getTaskType());
    }
    
    /**
     * Insert a new Task for assigned roles and also as the child for specific parent. 
     * Leave the parentTask null if task doesn't have a parent.
     * @param task
     * @param roles
     * @param parentTask
     * @param childSequence
     * @throws Exception 
     */
    public void insertTask(Task task, List<Role> roles, Task parentTask, short childSequence) 
            throws Exception {
        
        // insert table task
        taskRepo.saveAndFlush(task);
        
        // loop for each roles
        for (Role role : roles) {
            // insert new role_task
            role.addTask(task);
            roleRepo.save(role);
        }
        
        // insert as child_task
        if (parentTask != null) {
            parentTask.addChild(task, childSequence);
            taskRepo.save(parentTask);
        }

        // push to task list
        pushToAppTaskList(task, task.getTaskType());
    }
    
    /**
     * Create default root menu and sub menu.
     * @throws Exception 
     */
    public void insertDefaultMenuTask() throws Exception {
        
        // create a new task as TopLevelMenu root task
        Task rootMenu = new Task(Task.TOP_LEVEL_MENU_TASK_ID, Task.TOP_LEVEL_MENU_TASK_ID, Task.Type.MENU);
        rootMenu.setStatus(Task.Status.ACTIVE);
        insertTask(rootMenu, null);
        
        // create a new task as TopLevelMenu root task
        Task menuAdmin = new Task(Task.SUB_MENU_ADMIN_TASK_ID, Task.SUB_MENU_ADMIN_TASK_ID, Task.Type.MENU);
        menuAdmin.setStatus(Task.Status.ACTIVE);
        menuAdmin.setIconName("star");
        insertTask(menuAdmin, rootMenu);
    }
    
}
