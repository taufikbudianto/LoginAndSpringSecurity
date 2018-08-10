/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taufik.app.model.Role;
import taufik.app.model.RoleTask;
import taufik.app.model.Task;
import taufik.app.repo.RoleRepo;

/**
 *
 * @author Randy
 */
@Service
@Transactional(readOnly = false, rollbackFor = { Exception.class })
public class RoleService {
    
    @Autowired
    private RoleRepo roleRepo;
    
    /**
     * Update status of the list of roles.
     * @param roles
     * @param enabled
     */
    public void updateRolesStatus(List<Role> roles, boolean enabled) {
        for (Role role : roles) {
            role.setEnabled(enabled);
            roleRepo.save(role);
        }
    }
    
    /**
     * Update roleTask for specific user.
     * @param role
     * @param newRoleTasks
     * @throws java.lang.Exception
     */
    public void updateRoleTasks(Role role, List<RoleTask> newRoleTasks) throws Exception {
        List<RoleTask> oldRoleTasks = new ArrayList<>(role.getRoleTasks());
        
        // add default top_level_menu_task if not exists
        if (role.getTasks().isEmpty()) {
            role.addTask(Task.TOP_LEVEL_MENU_TASK_ID);
            role = roleRepo.save(role);
        }
                
        // update role_tasks if exists and insert if not exists
        for (RoleTask roleTask : newRoleTasks) {
            if (oldRoleTasks.contains(roleTask)) {
                role.updateTask(roleTask.getTask().getTaskId(), roleTask.getAccessRight());
            } else if (!roleTask.getTask().getTaskId().equals(Task.TOP_LEVEL_MENU_TASK_ID)) {
                role.addTask(roleTask.getTask(), roleTask.getAccessRight());
            }
        }

        // remove unused role_tasks
        for (RoleTask oldRoleTask : oldRoleTasks) {
            if (!newRoleTasks.contains(oldRoleTask)) {
                role.removeTask(oldRoleTask.getTask().getTaskId());
            }
        }

        roleRepo.save(role);
    }
    
    /**
     * Create new role with default enabled status is false.
     * @param role
     * @return  
     * @throws java.lang.Exception  
     */
    public Role createNewRole(Role role) throws Exception {
        role.setEnabled(false);
        return roleRepo.save(role);
    }
    
    /**
     * Copy role tasks from source to targetRole.
     * @param targetRole
     * @param sourceRoleTasks
     * @return 
     * @throws java.lang.Exception 
     */
    public Role copyRoleTasksTo(Role targetRole, List<RoleTask> sourceRoleTasks) throws Exception {
        for (RoleTask roleTask : sourceRoleTasks) {
            targetRole.addTask(roleTask.getTask(), roleTask.getAccessRight());
        }
        
        return roleRepo.save(targetRole);
    }
    
}
