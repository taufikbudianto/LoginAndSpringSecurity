/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Randy
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Mail implements Serializable {
    
    private String from;
    private String to;
    private String subject;
    private String body;
    
}
