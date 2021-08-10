/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.catalogue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.extensions.model.fluidgrid.FluidGridItem;

/**
 * This class represents a data model that is used by Primefaces to display the search results table
 * 
 * @author mng
 */
public class SearchResultModel implements Serializable {

    private List<SearchResultItem> items;
    private List<FluidGridItem> fluidItems;

    public SearchResultModel() {
        this.items = new ArrayList<>();
    }

    public SearchResultModel(List<SearchResultItem> items) {
        this.items = items;
        this.fluidItems = new ArrayList<>();
        for (SearchResultItem i : this.items) {
            this.fluidItems.add(new FluidGridItem(i));
        }
    }

    public List<SearchResultItem> getItems() {
        return items;
    }

    public void setItems(List<SearchResultItem> items) {
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
