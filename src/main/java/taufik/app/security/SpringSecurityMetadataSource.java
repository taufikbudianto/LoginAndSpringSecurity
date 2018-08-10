/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import taufik.app.model.Role;
import taufik.app.model.Task;
import taufik.app.model.User;
import taufik.app.repo.RoleRepo;
import taufik.app.repo.TaskRepo;
import taufik.app.web.controller.ApplicationData;

/**
 *
 * @author Randy
 */
public class SpringSecurityMetadataSource extends DefaultFilterInvocationSecurityMetadataSource {
    
    private ApplicationData applicationData;
    private RoleRepo roleRepo;
    private TaskRepo taskRepo;
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    private final String[] urlPathIgnores = {
        "/favicon.ico", 
        "/javax.faces.resource/**", 
        "/resources/**", 
        "/login.xhtml"};
    
    private final Map<String,String> defaultMetadataSource = new HashMap<String, String>() {{
        put("/","authenticated");
        put("/index.xhtml", "authenticated");
    }};
    
    public SpringSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap) {
        super(requestMap);
    }
    
    public SpringSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap, 
            ApplicationData applicationData, RoleRepo roleRepo, TaskRepo taskRepo) {
        super(requestMap);
        
        this.applicationData = applicationData;
        this.roleRepo = roleRepo;
        this.taskRepo = taskRepo;
    }
    
    /**
     * @param url
     * @return true if the given URL is match with the value from urlPathIgnores.
     */
    private boolean isUrlIgnore(String url) {
        for (String pattern : this.urlPathIgnores) {
            if (isPatternMatch(url, pattern)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @param path
     * @param pattern
     * @return true if the path match with the pattern.
     */
    private boolean isPatternMatch(String path, String pattern) {
        if (this.pathMatcher.isPattern(pattern)) {
            if (this.pathMatcher.match(pattern, path)) {
                return true;
            }
        } else { //exact match
            if (pattern.equals(path)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        FilterInvocation fi = (FilterInvocation) object;
        String url = fi.getRequestUrl();
        
        if (url != null) {
            
            // check if url was ignored
            if (isUrlIgnore(url)) {
                return null;
            }
            
            // check if contains in default_metadata_source
            for (String key : this.defaultMetadataSource.keySet()) {
                if (isPatternMatch(url, key)) {
//                    Collection<ConfigAttribute> defaultConfigAttributes = new ArrayList<>();
//                    defaultConfigAttributes.add(null);
//                    defaultConfigAttributes.add(new SecurityConfig(this.defaultMetadataSource.get(key)));
//                    log.debug("defaultMetadataSource for {} : {}", key, defaultConfigAttributes);
                    try{
                        User user = (User) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal();
                        log.debug("user : {}", user);
                    }catch(Exception e){
                        log.debug("user is null");
                    }
//                    return defaultConfigAttributes;
                    return super.getAttributes(object);
                }
            }
            
            String urlKey = url.endsWith(".xhtml") ? url.substring(0, url.indexOf(".xhtml")) : url;
            
            // check if already saved
            if (this.applicationData.getUrlConfigAttributes().containsKey(urlKey)) {
                return this.applicationData.getUrlConfigAttributes().get(urlKey);
            }
            
            Task menu = taskRepo.findOneByOutcome(urlKey);
            if (menu == null && urlKey != null && !url.equals(urlKey)) { 
                // try to search with extension if not found
                menu = taskRepo.findOneByOutcome(url);
            }
            
            // generate config_attribute
            Collection<ConfigAttribute> configAttributes = new ArrayList<>();
            if (menu != null) {
                // get role_tasks
                List<Role> roles = roleRepo.findAllByTaskIdAndAccessRightNot(
                        menu.getTaskId(), Role.AccessRight.NO_ACCESS);
                for (Role role : roles) {
                    configAttributes.add(new SecurityConfig(role.getRoleIdentifier()));
                }
                
//                if (configAttributes.isEmpty()) {
//                    log.debug("insert denyAll");
//                    configAttributes.add(new SecurityConfig("denyAll"));
//                }
            }
            this.applicationData.getUrlConfigAttributes().put(urlKey, configAttributes);
            log.debug("Save attribute metadata source for {} are : {}", urlKey, configAttributes);
            
            return configAttributes;
        } else {
            return super.getAttributes(object);
        }
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
    
}
