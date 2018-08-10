/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Controller;
import taufik.app.model.Role;
import taufik.app.web.util.AbstractManagedBean;
import taufik.app.web.controller.ApplicationData;
import taufik.app.model.Task;
import taufik.app.model.Tenant;
import taufik.app.repo.TaskRepo;
import taufik.app.repo.TenantRepo;
import taufik.app.service.TaskService;
import taufik.app.service.UserService;

/**
 *
 * @author Randy
 */
@Controller
public class ApplicationListener implements org.springframework.context.ApplicationListener<ContextRefreshedEvent> {

    @Value("${basepackage.abstractmanagedbean}")
    private String abstractManagedBeanBasePackage;

    @Autowired
    private ApplicationData applicationData;
    
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;
    
    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private TenantRepo tenantRepo;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        
        createTopLevelMenuRootTask();
        
        checkingAbstractManagedBeanPackage();        
        
        applicationData.setSharedTenant(createSharedTenant());
    }
    
    /**
     * Create and insert SharedTenant if not exists in database, and do nothing if already exists.
     * @return the SharedTenant.
     */
    private Tenant createSharedTenant() {
        Tenant newDefaultTenant = tenantRepo.findTop1ByTenantName(Tenant.DEFAULT_TENANT_NAME);
        
        if (newDefaultTenant == null) {
            log.info("Insert a new shared tenant and admin user.");

            try {
                // create a new shared tenant
                newDefaultTenant = new Tenant(Tenant.DEFAULT_TENANT_NAME, (short) 1, 
                        Tenant.UserLimitType.CONCURRENT_USER);
                newDefaultTenant.setEnabled(true);

                tenantRepo.saveAndFlush(newDefaultTenant);

                // create default admin and roles
                userService.createDefaultAdminUserAndRole(newDefaultTenant, Role.X_ROLE_SUPERADMIN_NAME, 
                        "Administrator", "superadmin", "Superadmin", "Pr0s14!", true);
            } catch (Exception e) {
                log.error("Error create shared tenant : ", e);
            }
        }
        
        return newDefaultTenant;
    }

    /**
     * Create and insert TopLevelMenu task if not exists in database, and do nothing if already exists.
     */
    private void createTopLevelMenuRootTask() {
        if (!taskRepo.exists(Task.TOP_LEVEL_MENU_TASK_ID)) {
            log.info("Insert a new task for Top Level Menu.");

            try {
                taskService.insertDefaultMenuTask();
                
            } catch (Exception e) {
                log.error("Error create TopLevelMenu : ", e);
            }
        }
    }

    /**
     * Checking class in AbstractManagedBean base package.
     */
    private void checkingAbstractManagedBeanPackage() {
        if (abstractManagedBeanBasePackage != null) {

            log.info("Checking AbstractManagedBean instances.");

            for (String basePackage : abstractManagedBeanBasePackage.split(",")) {
                
                log.info("Reading base package : {}", basePackage);

                Reflections reflections = new Reflections(basePackage);

                for (Class classObj : reflections.getSubTypesOf(AbstractManagedBean.class)) {
                    
                    // insert as menu_task if not in database
                    boolean exists = false;
                    for (Task task : applicationData.getListTaskMenu()) {
                        if (task.getTaskId().equals(classObj.getName())) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        insertNewMenuTask(classObj.getName(), classObj.getSimpleName());
                    }
                }
            }
        }
    }

    /**
     * Insert a new menu_task (child of TopLevelMenu Task) into database with default no role_task
     *
     * @param taskId
     * @param taskName
     */
    private void insertNewMenuTask(String taskId, String taskName) {
        try {
            Task parentTask = taskRepo.findOne(Task.SUB_MENU_ADMIN_TASK_ID);
            
            Task menuTask = new Task(taskId, taskName, Task.Type.MENU);
            menuTask.setStatus(Task.Status.ACTIVE);
            
            taskService.insertTask(menuTask, parentTask);

            log.info("A new menu_task : {} child of {}", 
                    taskId, parentTask != null ? parentTask.getTaskId() : "-");

        } catch (Exception e) {
            log.error("Error insert a new menu_task {} : ", taskId, e);
        }
    }

}
