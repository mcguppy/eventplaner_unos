
package org.mcguppy.eventplaner.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.mcguppy.eventplaner.jpa.controllers.ShiftJpaController;
import org.mcguppy.eventplaner.jpa.entities.Shift;

/**
 *
 * @author mbohm
 */
public class ShiftConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        Long id = new Long(string);
        ShiftJpaController controller = (ShiftJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "shiftJpa");
        return controller.findShift(id);
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Shift) {
            Shift o = (Shift) object;
            return o.getId() == null ? "" : o.getId().toString();
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: org.mcguppy.eventplaner.jpa.entities.Shift");
        }
    }

}
