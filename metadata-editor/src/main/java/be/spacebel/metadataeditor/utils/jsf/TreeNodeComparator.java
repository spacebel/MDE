/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.jsf;

import be.spacebel.metadataeditor.models.configuration.Concept;
import java.util.Comparator;
import org.primefaces.model.TreeNode;

/**
 * This class implements {@link java.util.Comparator} interface to be used to
 * compare {@link org.primefaces.model.TreeNode}. The comparator can be passed
 * to a sort method of {@link java.util.Comparator}
 *
 * @author mng
 */
public class TreeNodeComparator implements Comparator<TreeNode> {

    @Override
    public int compare(TreeNode o1, TreeNode o2) {
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }

        if (o1.getData() == null) {
            return -1;
        }

        if (o2.getData() == null) {
            return 1;
        }
        return ((Concept) o1.getData()).getLabel().compareTo(((Concept) o2.getData()).getLabel());
    }

}
