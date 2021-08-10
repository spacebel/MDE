/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.extensions.model.fluidgrid.FluidGridItem;

/**
 * A representation of data table model that is used by Primefaces to display
 * list of metadata records on a table
 *
 *
 * @author mng
 */
public class DataTableModel implements Serializable {

    private List<MetadataFile> items;
    private List<FluidGridItem> fluidItems;

    public DataTableModel() {
        this.items = new ArrayList<>();
    }

    public DataTableModel(List<MetadataFile> items) {
        this.items = items;
        this.fluidItems = new ArrayList<>();
        this.items.forEach((item) -> {
            this.fluidItems.add(new FluidGridItem(item));
        });
    }

    public List<MetadataFile> getItems() {
        return items;
    }

    public void setItems(List<MetadataFile> items) {
        this.items = items;
    }

    public List<FluidGridItem> getFluidItems() {
        return fluidItems;
    }

    public void setFluidItems(List<FluidGridItem> fluidItems) {
        this.fluidItems = fluidItems;
    }

    public int getItemCount() {
        if (this.items != null) {
            return this.items.size();
        } else {
            return 0;
        }
    }
}
