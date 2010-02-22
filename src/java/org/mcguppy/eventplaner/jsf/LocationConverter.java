
package org.mcguppy.eventplaner.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.mcguppy.eventplaner.jpa.controllers.LocationJpaController;
import org.mcguppy.eventplaner.jpa.entities.Location;

/**
 *
 * @author mbohm
 */
public class LocationConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent component, String string) {
        if (string == null || string.length() == 0) {
            return null;
        }
        Long id = new Long(string);
        LocationJpaController controller = (LocationJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "locationJpa");
        return controller.findLocation(id);
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Location) {
            Location o = (Location) object;
            return o.getId() == null ? "" : o.getId().toString();
        } else {
            throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: org.mcguppy.eventplaner.jpa.entities.Location");
        }
    }

}
