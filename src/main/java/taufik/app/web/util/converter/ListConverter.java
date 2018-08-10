/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taufik.app.web.util.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import org.apache.tomcat.util.codec.binary.Base64;

/**
 *
 * @author Randy
 */
@FacesConverter(value = "listConverter")
public class ListConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        try {
            byte[] data = Base64.decodeBase64(value);
            Object o;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
                o = ois.readObject();
            }
            
            return o;
            
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(value);
            }

            return Base64.encodeBase64String(baos.toByteArray());
            
        } catch (IOException | NullPointerException e) {
            return "";
        }
    }
    
}
