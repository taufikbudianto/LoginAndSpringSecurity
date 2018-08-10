/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import taufik.app.model.Role;
import taufik.app.model.Task;
import taufik.app.model.User;
import taufik.app.repo.TaskRepo;

/**
 *
 * @author Randy
 */
@Controller
@Scope("session")
public class MenuNavMBean implements InitializingBean {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SessionData sessionData;

    @Autowired
    private TaskRepo taskRepo;

    @Getter
    private User userSession;

    @Getter
    private LinkedHashMap<Task, MenuModel> menuMap;
    @Getter
    private MenuModel menuModel = new DefaultMenuModel();

    @Override
    public void afterPropertiesSet() throws Exception {
        // get user_login
        try {
            this.userSession = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        } catch (Exception e) {
            this.userSession = null;
        }

        this.menuMap = new LinkedHashMap<>();
        if (this.userSession != null) {
            // save data to session
            for (Role role : userSession.getRoles()) {
                // user_role_task
                this.sessionData.getUserRoleTasks().addAll(role.getRoleTasks());

                // user_task_menu
                for (Task task : role.getTasks()) {
                    if (task.getTaskType().equals(Task.Type.MENU) && !sessionData.getUserTaskMenus().contains(task)) {
                        sessionData.getUserTaskMenus().add(task);
                    }
                }
            }

            // generate menu model
            this.menuModel = generateMenuModel();
        }
    }

    /**
     * generate menu from database
     *
     * @return hash map contains menu model based on database model
     */
    private MenuModel generateMenuModel() {
        DefaultMenuModel menu = new DefaultMenuModel();

        // push data to user's task_menu
        for (Role role : userSession.getRoles()) {
            for (Task task : role.getTasks()) {
                System.out.println("masuk if 1 "+sessionData.getUserTaskMenus());
                if (task.getTaskType().equals(Task.Type.MENU) && !sessionData.getUserTaskMenus().contains(task)) {
                    sessionData.getUserTaskMenus().add(task);
                }
            }
        }

        // get menu_task (child of TopLevelMenu
        Task topLevelMenu = taskRepo.findOne(Task.TOP_LEVEL_MENU_TASK_ID);
        System.out.println(topLevelMenu.getChildrenByStatus(Task.Status.ACTIVE));
        for (Task child : topLevelMenu.getChildrenByStatus(Task.Status.ACTIVE)) {
            // rendered menu if it's in user's task_menu
            if (sessionData.getUserTaskMenus().contains(child)) {
                System.out.println("Msuk if");
                menu.addElement(generateMenuChild(child));
            }
        }

        return menu;
    }

    private DefaultSubMenu createSubMenu(final String label, final String icon) {
        final DefaultSubMenu result = new DefaultSubMenu(label, icon);
        return result;
    }

    private DefaultMenuItem createMenuItem(final String label, final String icon, final String outcome) {
        final DefaultMenuItem result = new DefaultMenuItem(label, icon);
        result.setIconPos("left");
        result.setOutcome(outcome);
        return result;
    }

    /**
     * Get list of child task from its parent_menu and remove the child if it's
     * not define in the role_task for the current user login.
     *
     * @param parentMenu
     * @return
     */
    private List<Task> getListChildTask(Task parentMenu) {
        List<Task> listChildTask = new ArrayList<>();
        for (Task child : parentMenu.getChildrenByStatus(Task.Status.ACTIVE)) {
            if (sessionData.getUserTaskMenus().contains(child)) {
                listChildTask.add(child);
            }
        }

        return listChildTask;
    }

    /**
     * Generate child_menu for the specific parent_menu.
     *
     * @param parentMenu
     * @return parentMenu as a MenuItem if it isn't a nested menu, and
     * parentMenu as a SubMenu if it have another nested child_menu.
     */
    private MenuElement generateMenuChild(Task parentMenu) {
        List<Task> listChildMenu = getListChildTask(parentMenu);

        if (listChildMenu != null && listChildMenu.size() > 0) {
            DefaultSubMenu subMenu = new DefaultSubMenu();
            subMenu.setLabel(parentMenu.getTaskName());
            subMenu.setIcon(parentMenu.getIconName());
            for (Task child : listChildMenu) {
                subMenu.addElement(generateMenuChild(child));
            }
            return subMenu;
        } else {
            DefaultMenuItem menuItem = new DefaultMenuItem();
            menuItem.setValue(parentMenu.getTaskName());
            menuItem.setIcon(parentMenu.getIconName());
            menuItem.setOutcome(parentMenu.getOutcome());
            return menuItem;
        }
    }

    /**
     * @return list tab menu.
     */
    public Set<Task> getListMenu() {
        return this.menuMap != null ? this.menuMap.keySet() : new HashSet<>();
    }

}
