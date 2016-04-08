package com.mrlu.bigData.mysql2excel;

/**
 * Created by stefan on 16-4-8.
 */
public class FieldDefinition {
    //excel cell name
    private String name;
    //table column name
    private String propertyName;
    //excel cell order
    private Integer order;

    public FieldDefinition(String name, String propertyName, int order) {
        this.name = name;
        this.propertyName = propertyName;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
