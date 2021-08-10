package be.spacebel.metadataeditor.models.catalogue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements Comparator interface that is used to sort options in
 * auto complete list
 *
 * @author mng
 */
public class ParameterOptionComparator implements Comparator<ParameterOption> {

    public static ParameterOptionComparator instance;

    private ParameterOptionComparator() {

    }

    public static void sort(List<ParameterOption> list) {
        Collections.sort(list, getInstance());
    }

    @Override
    public int compare(ParameterOption o1, ParameterOption o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.getLabel().compareTo(o2.getLabel());
    }

    public static ParameterOptionComparator getInstance() {
        if (instance == null) {
            instance = new ParameterOptionComparator();
        }
        return instance;
    }
}
