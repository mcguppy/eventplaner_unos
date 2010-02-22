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
import org.mcguppy.eventplaner.jpa.controllers.ShiftJpaController;
import org.mcguppy.eventplaner.jpa.controllers.exceptions.NonexistentEntityException;
import org.mcguppy.eventplaner.jpa.entities.Location;
import org.mcguppy.eventplaner.jpa.entities.Shift;
import org.mcguppy.eventplaner.jpa.entities.StaffMember.Title;
import org.mcguppy.eventplaner.jsf.util.JsfUtil;

/**
 *
 * @author mbohm
 */
public class ShiftController {

    public ShiftController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        jpaController = (ShiftJpaController) facesContext.getApplication().getELResolver().getValue(facesContext.getELContext(), null, "shiftJpa");
        converter = new ShiftConverter();
    }
    private Shift shift = null;
    private List<Shift> shiftItems = null;
    private ShiftJpaController jpaController = null;
    private ShiftConverter converter = null;

    public SelectItem[] getShiftItemsAvailableSelectMany() {
        List<Shift> itemsList = jpaController.findShiftEntities();
        Collections.sort(itemsList);
        return JsfUtil.getSelectItems(itemsList, false);
    }

    public SelectItem[] getShiftItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(jpaController.findShiftEntities(), true);
    }

    public Shift getShift() {
        if (shift == null) {
            shift = (Shift) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentShift", converter, null);
        }
        if (shift == null) {
            shift = new Shift();
        }
        return shift;
    }

    public int getItemCount() {
        return jpaController.getShiftCount();
    }

    public String listSetup() {
        reset();
        return "shiftList";
    }

    public String createSetup() {
        reset();
        shift = new Shift();
        shift.setLocation((Location) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentLocation", new LocationConverter(), null));
        return "shiftCreate";
    }

    public String create() {
        try {
            jpaController.create(shift);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ShiftCreated"));
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return "shiftDetail";
    }

    public String detailSetup() {
        return scalarSetup("shiftDetail");
    }

    public String editSetup() {
        return scalarSetup("shiftEdit");
    }

    public String destroySetup() {
        JsfUtil.addWarnMessage(ResourceBundle.getBundle("/Bundle").getString("DestroyShiftWarning"));
        return scalarSetup("shiftDestroy");
    }

    private String scalarSetup(String destination) {
        reset();
        shift = (Shift) JsfUtil.getObjectFromRequestParameter("jsfcrud.currentShift", converter, null);
        if (shift == null) {
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ShiftMissing"));
//            return relatedOrListOutcome();    // TODO: find a better solution
            return "locationList";
        }
        return destination;
    }

    public String edit() {
        String shiftString = converter.getAsString(FacesContext.getCurrentInstance(), null, shift);
        String currentShiftString = JsfUtil.getRequestParameter("jsfcrud.currentShift");
        if (shiftString == null || shiftString.length() == 0 || !shiftString.equals(currentShiftString)) {
            String outcome = editSetup();
            if ("shiftEdit".equals(outcome)) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ShiftEditError"));
            }
            return outcome;
        }
        try {
            jpaController.edit(shift);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ShiftUpdated"));
        } catch (NonexistentEntityException ne) {
            JsfUtil.addErrorMessage(ne.getLocalizedMessage());
            return listSetup();
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
        return detailSetup();
    }

    public String destroy() {
        String shiftString = converter.getAsString(FacesContext.getCurrentInstance(), null, shift);
        String currentShiftString = JsfUtil.getRequestParameter("jsfcrud.currentShift");
        if (shiftString == null || shiftString.length() == 0 || !shiftString.equals(currentShiftString)) {
            String outcome = destroySetup();
            if ("shiftDestroy".equals(outcome)) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle("/Bundle").getString("ShiftDestoryError"));
            }
            return outcome;
        }
        try {
            jpaController.destroy(shift);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ShiftDeleted"));
        } catch (NonexistentEntityException ne) {
            JsfUtil.addErrorMessage(ne.getLocalizedMessage());
//            return relatedOrListOutcome(); // TODO: find a better solution
            return "locationList";
        } catch (Exception e) {
            JsfUtil.ensureAddErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
//        return relatedOrListOutcome(); // TODO: find a better solution
        return "locationList";
    }

    private String relatedOrListOutcome() {
        String relatedControllerOutcome = relatedControllerOutcome();
        if (relatedControllerOutcome != null) {
            return relatedControllerOutcome;
        }
        return listSetup();
    }

    public List<Shift> getShiftItems() {
        if (shiftItems == null) {
            shiftItems = jpaController.findShiftEntities();
            Collections.sort(shiftItems);
        }
        return shiftItems;
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
        shift = null;
        shiftItems = null;
    }

    public void validateCreate(FacesContext facesContext, UIComponent component, Object value) {
        Shift newShift = new Shift();
        String newCustomerString = converter.getAsString(FacesContext.getCurrentInstance(), null, newShift);
        String customerString = converter.getAsString(FacesContext.getCurrentInstance(), null, shift);
        if (!newCustomerString.equals(customerString)) {
            createSetup();
        }
    }

    public Converter getConverter() {
        return converter;
    }

    public SelectItem[] getTitlesAvailableSelectOne() {
        List<Title> titleList = Arrays.asList(Title.values());
        return JsfUtil.getSelectItems(titleList, true);
    }
}
