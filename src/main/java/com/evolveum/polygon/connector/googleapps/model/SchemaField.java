/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evolveum.polygon.connector.googleapps.model;

/**
 *
 * @author oskar.butovic
 */
public class SchemaField {
    
    private String schemaName;
    private String fieldName;
    private String fieldType;
    private boolean multivalued;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isMultivalued() {
        return multivalued;
    }

    public void setMultivalued(boolean multivalued) {
        this.multivalued = multivalued;
    }
    
    public void setMultivalued(Boolean multivalued) {
        if(multivalued == null){
            this.multivalued = false;
        }else{
            this.multivalued = multivalued;
        }
    }
    
}
