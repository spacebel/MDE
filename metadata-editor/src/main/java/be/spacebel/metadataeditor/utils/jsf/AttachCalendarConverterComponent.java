package be.spacebel.metadataeditor.utils.jsf;

import java.io.IOException;
import java.io.Serializable;
import java.util.TimeZone;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import org.primefaces.component.calendar.Calendar;

/**
 * Automatically attach our date converter to wrapped date pickers
 * (primefaces:calendar)
 *
 * @author jpr
 *
 */
public class AttachCalendarConverterComponent extends UIComponentBase implements Serializable{

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        super.encodeBegin(context);
        handleComponent(this);
    }

    private void handleComponent(UIComponent component) {
        for (UIComponent child : component.getChildren().toArray(
                new UIComponent[0])) {
            if (child instanceof Calendar) {
                Calendar cal = (Calendar) child;
                cal.setConverter(new DateCalendarConverter(cal.getPattern(),
                        ((TimeZone) cal.getTimeZone())));
            } else {
                handleComponent(child);
            }
        }
    }

    @Override
    public String getFamily() {

        return getClass().getName();
    }

}
