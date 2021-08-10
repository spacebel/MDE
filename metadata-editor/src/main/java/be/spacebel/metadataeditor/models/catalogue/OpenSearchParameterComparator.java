/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.catalogue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements Comparator interface that is used to sort OpenSearch
 * parameters before displaying on search input panel
 *
 * @author mng
 */
public class OpenSearchParameterComparator implements Comparator<OpenSearchParameter> {

    public static OpenSearchParameterComparator instance;

    private OpenSearchParameterComparator() {

    }

    public static void sort(List<OpenSearchParameter> list) {
        Collections.sort(list, getInstance());

        OpenSearchParameter start = null;
        int startIndex = -1;

        OpenSearchParameter end = null;
        int endIndex = -1;

        OpenSearchParameter place = null;
        int placeIndex = -1;

        int count = 1;
        for (OpenSearchParameter osParam : list) {

            if ("time_start".equals(osParam.getName())) {
                start = osParam;
                startIndex = count;
            }

            if ("time_end".equals(osParam.getName())) {
                end = osParam;
                endIndex = count;
            }

            if ("geonames".equals(osParam.getType())) {
                place = osParam;
                placeIndex = count;
            }

            if (osParam.isShow()) {
                count++;
            }
        }        

        if (start != null) {
            start.setSpacePosition("above");
            if ((startIndex == 1) || place != null && placeIndex == (startIndex - 1)) {
                start.setSpacePosition("none");
            }
        }

        if (end != null) {
            end.setSpacePosition("below");
            if (place != null && placeIndex == (endIndex + 1)) {
                end.setSpacePosition("none");
            }
        }

        if (place != null && placeIndex == 1) {
            place.setSpacePosition("noneAbove");
        }

    }

    @Override
    public int compare(OpenSearchParameter o1, OpenSearchParameter o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.getOrder() - o2.getOrder();
    }

    public static OpenSearchParameterComparator getInstance() {
        if (instance == null) {
            instance = new OpenSearchParameterComparator();
        }
        return instance;
    }

}
