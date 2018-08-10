/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.security;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * Based on code from LoginUrlAuthenticationEntryPoint
 *
 * @author mhewedy
 *
 */
// see http://forum.spring.io/forum/spring-projects/security/88829-is-it-possible-to-change-spring-security-3-redirects-from-full-urls-to-relative-urls
@SuppressWarnings("deprecation")
public class JsfLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private RedirectStrategy redirectStrategy;

    public JsfLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        String redirectUrl = null;

        if (isUseForward()) {

            if (isForceHttps() && "http".equals(request.getScheme())) {
                // First redirect the current request to HTTPS.
                // When that request is received, the forward to the login page
                // will be used.
                redirectUrl = buildHttpsRedirectUrlForRequest(request);
            }

            if (redirectUrl == null) {
                String loginForm = determineUrlToUseForThisRequest(request,
                        response, authException);

                log.debug("Server side forward to: {}", loginForm);

                RequestDispatcher dispatcher = request
                        .getRequestDispatcher(loginForm);

                dispatcher.forward(request, response);

                return;
            }
        } else {
            // redirect to login page. Use https if forceHttps true

            redirectUrl = buildRedirectUrlToLoginPage(request, response,
                    authException);
        }
        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }

}
