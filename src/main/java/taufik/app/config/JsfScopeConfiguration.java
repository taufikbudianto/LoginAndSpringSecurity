package taufik.app.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import taufik.app.web.scope.ViewScope;

/**
 *
 * @author Tommy
 */
@Configuration
public class JsfScopeConfiguration {

    @Bean
    public static CustomScopeConfigurer customScope() {

        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        Map<String, Object> customScopes = new HashMap<>();
        customScopes.put("view", new ViewScope());
        configurer.setScopes(customScopes);

        return configurer;
    }
}
