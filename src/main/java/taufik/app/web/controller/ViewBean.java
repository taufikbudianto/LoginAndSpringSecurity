/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.controller;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Randy
 */
@Controller
@Scope("session")
public class ViewBean implements InitializingBean {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Getter
    private String dateFormatPattern;
    @Getter
    private String dateTimeFormatPattern;
    @Getter
    private String decimalFormatPattern;
    @Getter
    private String integerFormatPattern;
    @Getter
    private String rowsPageDefault;
    @Getter
    private String rowsPerPageTemplate;
    
    @Getter
    private final String emailPattern = 
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
    @Override
    public void afterPropertiesSet() throws Exception {
        // load default values from settings
        try {
             dateFormatPattern = "dd-MM-yyyy";
        } catch (Exception e) {
//            log.warn("Can't find setting with prefix_name : {}" + Setting.X_DATE_FORMAT);
            dateFormatPattern = "dd-MM-yyyy";
        }
        
        try {
            dateTimeFormatPattern = "dd-MM-yyyy HH:mm:ss";
        } catch (Exception e) {
//            log.warn("Can't find setting with prefix_name : {}" + Setting.X_DATE_TIME_FORMAT);
            dateTimeFormatPattern = "dd-MM-yyyy HH:mm:ss";
        }
        
        try {
            decimalFormatPattern = "###.##";
        } catch (Exception e) {
//            log.warn("Can't find setting with prefix_name : {}" + Setting.X_DECIMAL_FORMAT);
            decimalFormatPattern = "###.##";
        }
        
        try {
            integerFormatPattern = "###";
        } catch (Exception e) {
//            log.warn("Can't find setting with prefix_name : {}" + Setting.X_INTEGER_FORMAT);
            integerFormatPattern = "###";
        }
        
        try {
            rowsPageDefault = "5";
        } catch (Exception e) {
//            log.warn("Can't find setting with prefix_name : {}" + Setting.X_ROWS_PER_PAGE_DEFAULT);
            rowsPageDefault = "5";
        }
        
        try {
            rowsPerPageTemplate ="5";
        } catch (Exception e) {
//            log.warn("Can't find setting with prefix_name : {}" + Setting.X_ROWS_PER_PAGE_TEMPLATE);
            rowsPerPageTemplate = "5";
        }
    }
    
    /**
     * Convert current string to first uppercase and replace '_' with space character. For example, 
     * to display java enum values.
     * @param string
     * @return 
     */
    public String toFirstUppercase(String string) {
        return string != null ? WordUtils.capitalize(string.replace("_", " ").toLowerCase()) : null;
    }
    
    /**
     * Add space between letters.
     * @param string
     * @return 
     */
    public String addWhiteSpaceBetweenCapitalLetters(String string) {
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                string.replaceAll("\\s\\d+", "\\s \\d")), " ");
    }
    
}
