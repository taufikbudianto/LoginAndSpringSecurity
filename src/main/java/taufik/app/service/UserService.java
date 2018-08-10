/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taufik.app.model.Role;
import taufik.app.model.Task;
import taufik.app.model.Tenant;
import taufik.app.model.User;
import taufik.app.model.UserRole;
import taufik.app.repo.RoleRepo;
import taufik.app.repo.TaskRepo;
import taufik.app.repo.UserRepo;

/**
 *
 * @author Randy
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Transactional(readOnly = false, rollbackFor = { Exception.class })
public class UserService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Create default user as the Administrator and the default role.
     * @param tenant
     * @param roleIdentifier
     * @param roleName
     * @param userLogin
     * @param fullName
     * @param password
     * @throws Exception 
     */
    public void createDefaultAdminUserAndRole(Tenant tenant, String roleIdentifier, String roleName, String userLogin, 
            String fullName, String password) throws Exception {
        createDefaultAdminUserAndRole(tenant, roleIdentifier, roleName, userLogin, fullName, password, false);
    }
    
    /**
     * Create default user as the Administrator and the default role.
     * @param tenant
     * @param roleIdentifier
     * @param roleName
     * @param userLogin
     * @param fullName
     * @param password
     * @param system
     * @throws java.lang.Exception 
     */
    public void createDefaultAdminUserAndRole(Tenant tenant, String roleIdentifier, String roleName, String userLogin, 
            String fullName, String password, Boolean system) throws Exception {
        // create admin role
        Role role = new Role(roleIdentifier, roleName, Role.AccessRight.READ_WRITE, tenant);
        role.setEnabled(true);
        role.setSystem(system);
        role = roleService.createNewRole(role);
        
        // create role_task
        Task rootMenu = taskRepo.findOne(Task.TOP_LEVEL_MENU_TASK_ID);
        Task adminMenu = taskRepo.findOne(Task.SUB_MENU_ADMIN_TASK_ID);
        Task roleMenuTask = taskRepo.findOne(Task.DEFAULT_ROLE_TASK_PACKAGE);
        role.addTask(rootMenu);
        role.addTask(adminMenu);
        if (roleMenuTask != null) {
            role.addTask(roleMenuTask);
        }
        roleRepo.save(role);
        
        // create user
        User user = registerUser(userLogin, fullName, password, role);
        
        // create tenant_user
        user.addTenant(tenant, true);
        userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByLogin(username);

        if (user != null) {
            List<UserRole> listUserRole = user.getUserRoles();
            List authorities = new ArrayList<>();

            // check if role is empty
            if (listUserRole == null || listUserRole.isEmpty()) {
                log.warn("user_type null, insert into table user_role first");
                
                return null;
            }
            
            for (UserRole userRole : listUserRole) {
                authorities.add(new SimpleGrantedAuthority(userRole.getRole().getRoleIdentifier()));
            }
            user.setAuthorities(authorities);
            
            // set default tenant
            if (user.getDefaultTenant() == null) {
                user.setDefaultTenant(listUserRole.get(0).getRole().getTenant());
                userRepo.save(user);
                
                log.info("set default_tenant : {} for user : {}", user.getDefaultTenant(), username);
            }
        } else {
            log.warn("User {} attempt to login but not found.", username);
            throw new UsernameNotFoundException("User " + username + " was not found in the database");
        }

        return user;
    }
    
    /**
     * Update user last_login with current datetime.
     * @param userLogin
     */
    public void updateLastLogin(String userLogin) {
        User user = userRepo.findByLogin(userLogin);
        user.setLastLogin(new Date());
        userRepo.save(user);
    }

    /**
     * Update status of the list of users.
     * @param users
     * @param isActive
     */
    public void updateUsersStatus(List<User> users, boolean isActive) {
        for (User user : users) {
            user.setEnabled(isActive);
            userRepo.save(user);
        }
    }

    /**
     * Update user's role. As a note, the order data in the list is important, 
     * the first record will be updated the primary role.
     * @param user
     * @param newRoles
     * @throws java.lang.Exception when user's role is not exists.
     */
    public void updateRoles(User user, List<Role> newRoles) throws Exception {
        // update roles if exists and insert if not exists
        List<Role> oldRoles = user.getRoles();
        for (Role role : newRoles) {
            if (oldRoles.contains(role)) {
                user.updateRole(role.getRoleId(), newRoles.indexOf(role) == 0);
            } else {
                user.addRole(role, newRoles.indexOf(role) == 0);
            }
        }

        // remove unused roles
        for (Role oldRole : oldRoles) {
            if (!newRoles.contains(oldRole)) {
                user.removeRole(oldRole.getRoleId());
            }
        }

        userRepo.save(user);
    }

    /**
     * Check duplication of user. if it's a new user, leave the userId empty.
     * @param user 
     * @throws DuplicateKeyException if userLogin already exists.
     */
    public void checkDuplicateUser(User user) throws DuplicateKeyException {
        User userCheck = userRepo.findByLogin(user.getLogin());
        
        if (userCheck != null) {
            // check duplicate for new user and if updated
            if (user.getUserId() == null || 
                    !user.getUserId().equals(userCheck.getUserId())) {
                throw new DuplicateKeyException("Email already in used.");
            }
        }
    }
    
    /**
     * Save user's data.
     * @param user
     * @param fullName
     * @param isGeneratedPassword
     * @param roles
     * @throws DuplicateKeyException
     * @throws Exception 
     */
    public void saveUser(User user, String fullName, boolean isGeneratedPassword, List<Role> roles) 
            throws DuplicateKeyException, Exception {
        // check user duplication
        checkDuplicateUser(user);
        // generate new password
        if (isGeneratedPassword) {
            String newPassword = passwordEncoder.encode(RandomStringUtils.randomAlphanumeric(6));
            user.setHashedPassword(newPassword);
        }
        userRepo.save(user);
        updateRoles(user, roles);
    }
    /**
     * Register a new user for a specific role.
     * @param userLogin
     * @param fullName
     * @param password
     * @param role
     * @return 
     * @throws DuplicateKeyException
     * @throws Exception 
     */
    public User registerUser(String userLogin, String fullName, String password, Role role) 
            throws DuplicateKeyException, Exception {
        if (role == null) {
            throw new Exception("Role cannot be null.");
        }
        
        // encrypt password
        String encryptPassword = passwordEncoder.encode(password);
        
        
        // create user
        User user = new User(userLogin, encryptPassword);
        user.setEnabled(true);
        user.setActivated(true);
        
        // check duplication
        checkDuplicateUser(user);
        
        userRepo.saveAndFlush(user);
        
        // mapping user_role
        user.addRole(role, true);
        
        // save user changes
        return userRepo.save(user);
    }
    
}
