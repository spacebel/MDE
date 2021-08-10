/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers.layout;

/**
 * A model class provides data and functionalities to control the view of a page
 *
 * @author mng
 */
public class Layout {

    private final View view;
    private int selectedCount;
    private boolean selectedAll;
    private boolean toggle;

    public Layout() {
        view = new View();
        view.toList();
        selectedCount = 0;
    }

    public void increaseSelectedCount() {
        selectedCount++;
    }

    public void decreaseSelectedCount() {
        if (selectedCount > 0) {
            selectedCount--;
        }
    }

////////////////////////////////////////////////////////////////////////////////
    public View getView() {
        return view;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(int selectedCount) {
        this.selectedCount = selectedCount;
    }

    public boolean isSelectedAll() {
        return selectedAll;
    }

    public void setSelectedAll(boolean selectedAll) {
        this.selectedAll = selectedAll;
    }

    public boolean isToggle() {
        return toggle;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }
}
