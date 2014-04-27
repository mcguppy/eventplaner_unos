package org.mcguppy.eventplaner.jsf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import org.mcguppy.eventplaner.jpa.controllers.LocationJpaController;
import org.mcguppy.eventplaner.jpa.controllers.exceptions.NonexistentEntityException;
import org.mcguppy.eventplaner.jpa.entities.Location;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember.Title;
import org.mcguppy.eventplaner.jsf.util.JsfUtil;

/**
 *
 * @author mbohm
 */
public class LocationController {

    public LocationController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (LocationJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "locationJpa");
        converter = new LocationConverter();
    }
    private Location location = null;
    private List<Location> locationItems = null;
    private LocationJpaController jpaController = null;
    private LocationConverter converter = null;

    public SelectItem[] getLocationItemsAvailableSelectMany() {
        List<Location> itemsList = jpaController.findLocationEntities();
        Collections.sort(itemsList);
        return JsfUtil.getSelectItems(itemsList, false);
    }

    public SelectItem[] getLocationItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(jpaController.findLocationEntities(), true);
    }

    public Location getLocation() {
        if (location == null) {
            location = (Location) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentLocation", converter, null);
        }
        if (location == null) {
            location = new Location();
        }
        if (null != location.getShifts() && location.getShifts().size() > 0) {
            Collections.sort((List<Shift>)location.getShifts());
        }
        return location;
    }

    public int getItemCount() {
        return jpaController.getLocationCount();
    }

    public String listSetup() {
        reset();
        return "locationList";
    }

    public String createSetup() {
        reset();
        location = new Location();
        return "locationCreate";
    }

    public String create() {
        try {
            jpaController.create(location);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LocationCreated"));
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return listSetup();
    }

    public String detailSetup() {
        return scalarSetup("locationDetail");
    }

    public String editSetup() {
        return scalarSetup("locationEdit");
    }

    public String destroySetup() {
        JsfUtil.addWarnMessage(ResourceBundle.getBundle("/Bundle").getString("DestroyLocationWarning"));
        return scalarSetup("locationDestroy");
    }

    private String scalarSetup(String destination) {
        reset();
        location = (Location) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentLocation", converter, null);
        if (location == null) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("LocationMissing"));
            return relatedOrListOutcome();
        }
        return destination;
    }

    public String edit() {
        String locationString = converter.getAsString(FacesContext.getCurrentInstance(), null, location);
        String currentLocationString = JsfUtil.getRequestParameter("jsfcrud.currentLocation");
        if (locationString == null || locationString.length() == 0 || !locationString.equals(currentLocationString)) {
            String outcome = editSetup();
            if ("locationEdit".equals(outcome)) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("LocationEditError"));
            }
            return outcome;
        }
        try {
            jpaController.edit(location);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LocationUpdated"));
        }  catch (NonexistentEntityException ne) {
            JsfUtil.addErrorMessage(ne.getLocalizedMessage());
            return listSetup();
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return detailSetup();
    }

    public String destroy() {
        String locationString = converter.getAsString(FacesContext.getCurrentInstance(), null, location);
        String currentLocationString = JsfUtil.getRequestParameter("jsfcrud.currentLocation");
        if (locationString == null || locationString.length() == 0 || !locationString.equals(currentLocationString)) {
            String outcome = destroySetup();
            if ("locationDestroy".equals(outcome)) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("LocationDestoryError"));
            }
            return outcome;
        }
        try {
            jpaController.destroy(location);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LocationDeleted"));
        }  catch (NonexistentEntityException ne) {
            JsfUtil.addErrorMessage(ne.getLocalizedMessage());
            return relatedOrListOutcome();
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return relatedOrListOutcome();
    }
    
    private String relatedOrListOutcome() {
        String relatedControllerOutcome = relatedControllerOutcome();
        if (relatedControllerOutcome != null) {
            return relatedControllerOutcome;
        }
        return listSetup();
    }

    public List<Location> getLocationItems() {
        if (locationItems == null) {
            locationItems = jpaController.findLocationEntities();
            Collections.sort(locationItems);
        }
        return locationItems;
    }

    private String relatedControllerOutcome() {
        String relatedControllerString = JsfUtil.getRequestParameter("jsfcrud.relatedController");
        String relatedControllerTypeString = JsfUtil.getRequestParameter("jsfcrud.relatedControllerType");
        if (relatedControllerString != null && relatedControllerTypeString != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            Object relatedController = context.getApplication().getELResolver().getValue(context.getELContext(), null, relatedControllerString);
            try {
                Class<?> relatedControllerType = Class.forName(relatedControllerTypeString);
                Method detailSetupMethod = relatedControllerType.getMethod("detailSetup");
                return (String) detailSetupMethod.invoke(relatedController);
            } catch (ClassNotFoundException e) {
                throw new FacesException(e);
            } catch (NoSuchMethodException e) {
                throw new FacesException(e);
            } catch (IllegalAccessException e) {
                throw new FacesException(e);
            } catch (InvocationTargetException e) {
                throw new FacesException(e);
            }
        }
        return null;
    }

    private void reset() {
        location = null;
        locationItems = null;
    }

    public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
        Location newLocation = new Location();
        String newCustomerString = converter.getAsString(FacesContext.getCurrentInstance(), null, newLocation);
        String customerString = converter.getAsString(FacesContext.getCurrentInstance(), null, location);
        if (!newCustomerString.equals(customerString)) {
            createSetup();
        }
    }

    public Converter getConverter() {
        return converter;
    }
}
