package be.spacebel.metadataeditor.models.catalogue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 * This class implements Comparator interface that is used to sort options in
 * drop-down list
 *
 * @author mng
 */
public class SelectItemComparator implements Comparator<SelectItem> {

    public static SelectItemComparator instance;

    private SelectItemComparator() {

    }

    public static void sort(List<SelectItem> list) {
        Collections.sort(list, getInstance());
    }

    @Override
    public int compare(SelectItem o1, SelectItem o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.getLabel().compareTo(o2.getLabel());
    }

    public static SelectItemComparator getInstance() {
        if (instance == null) {
            instance = new SelectItemComparator();
        }
        return instance;
    }
}
