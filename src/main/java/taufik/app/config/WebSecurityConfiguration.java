/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.query.spi.EvaluationContextExtension;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import taufik.app.repo.RoleRepo;
import taufik.app.repo.TaskRepo;
import taufik.app.security.JsfLoginUrlAuthenticationEntryPoint;
import taufik.app.security.JsfRedirectStrategy;
import taufik.app.security.SpringSecurityMetadataSource;
import taufik.app.service.UserService;
import taufik.app.web.controller.ApplicationData;

/**
 *
 * @author Randy
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private ApplicationData applicationData;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/favicon.ico")
                .antMatchers("/javax.faces.resource/**")
                .antMatchers("/resources/**")
                .antMatchers("/login.xhtml");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable();

        http.sessionManagement()
                .maximumSessions(1).expiredUrl("/login.xhtml?expired").and()
                .sessionFixation().newSession();

        http.authorizeRequests()
                .antMatchers("/").authenticated()
                .antMatchers("/index.xhtml").authenticated()
                .antMatchers("/admin/**").authenticated()
                .antMatchers("/upload/**").authenticated();
        
//        http.addFilterBefore(filterSecurityInterceptor(), FilterSecurityInterceptor.class);

        http.formLogin()
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .loginProcessingUrl("/j_spring_security_check")
                .defaultSuccessUrl("/index.xhtml")
                .failureUrl("/login.xhtml?error");
        
        http.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/login.xhtml")
                .deleteCookies("JSESSIONID");

        http.exceptionHandling()
                .authenticationEntryPoint(jsfLoginUrlAuthenticationEntryPoint())
                .accessDeniedPage("/403.xhtml");
    }
    
    @Bean
    public FilterSecurityInterceptor filterSecurityInterceptor() throws Exception {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<>();
        List<ConfigAttribute> configs = new ArrayList<>();
        configs.add(new SecurityConfig("isAuthenticated()"));
        requestMap.put(new AntPathRequestMatcher("/"), configs);
        requestMap.put(new AntPathRequestMatcher("/index.xhtml"), configs);
        
        FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
        filterSecurityInterceptor.setAuthenticationManager(authenticationManager());
        filterSecurityInterceptor.setAccessDecisionManager(accessDecisionManager());
        filterSecurityInterceptor.setSecurityMetadataSource(new SpringSecurityMetadataSource(
                requestMap, this.applicationData, this.roleRepo, this.taskRepo));
        filterSecurityInterceptor.afterPropertiesSet();

        return filterSecurityInterceptor;
    }
    
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }

    @Bean
    public EvaluationContextExtension securityExtension() {
        return new SecurityEvaluationContextExtension();
    }

    private AuthenticationEntryPoint jsfLoginUrlAuthenticationEntryPoint() {
        JsfLoginUrlAuthenticationEntryPoint entryPoint
                = new JsfLoginUrlAuthenticationEntryPoint("/login.xhtml");

        entryPoint.setRedirectStrategy(new JsfRedirectStrategy());

        return entryPoint;
    }

    @Bean
    public AffirmativeBased accessDecisionManager() {
        List<AccessDecisionVoter<? extends Object>> decisionVoterList = new ArrayList<>();
        decisionVoterList.add(new RoleVoter());
        decisionVoterList.add(new AuthenticatedVoter());
        return new AffirmativeBased(decisionVoterList);
    }
    
}
