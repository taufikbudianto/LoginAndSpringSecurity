/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import taufik.app.model.Task;

/**
 *
 * @author Randy
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SecureItem {
    
    private String itemName;
    private Task.Type itemType;
    
}
