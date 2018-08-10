/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.controller;

import java.io.IOException;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Controller;
import taufik.app.service.UserService;

/**
 *
 * @author Randy
 */
@Controller
@Scope("session")
public class LoginMBean implements InitializingBean {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Getter
    private String errMessage;
    @Getter
    @Setter
    private String userLogin;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private Boolean rememberMe;
    
    @Autowired
    private ResourceBundleBean messages;
    
    @Autowired
    private UserService userService;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        userLogin="admin";
        password="superadmin";
    }
    
    public void login() {        
        try {
            // do any job with the associated values that you've got from the user, 
            // like persisting attempted login, etc.
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            
            RequestDispatcher dispatcher = ((ServletRequest) externalContext.getRequest())
                    .getRequestDispatcher("/j_spring_security_check");
            dispatcher.forward((ServletRequest) externalContext.getRequest(),
                    (ServletResponse) externalContext.getResponse());
            facesContext.responseComplete();

            // check for AuthenticationException in the session
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            Exception e = (Exception) sessionMap.get(WebAttributes.AUTHENTICATION_EXCEPTION);

            errMessage = null;
            
            if (e instanceof BadCredentialsException || e instanceof UsernameNotFoundException) {
                log.error("Password BadCredentialsException or UsernameNotFoundException");
                errMessage = messages.get("error_login_invalid_username");
                return;
            }
            
            if (e instanceof SessionAuthenticationException) {
                log.error("Username or Password SessionAuthenticationException");
                errMessage = messages.get("error_login_session_exceeded");
                return;
            }
            
            if (e instanceof LockedException) {
                log.error("Username or Password LockedException");
                errMessage = messages.get("error_login_inactive_user");
                return;
            }

            // update user last login
            userService.updateLastLogin(userLogin);
            
            
        } catch (ServletException | IOException ex) {
            log.error("Error login : ", ex);
        }
    }
    
}
