/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.data.sap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.Generated;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Maximiliano
 */
@Generated("jsonschema2pojo")
public class Data implements Serializable{

@JsonProperty("cantidad")
private String quantity;
@JsonIgnore
private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("cantidad")
public String getQuantity() {
return quantity;
}

@JsonProperty("cantidad")
public void setQuantity(String quantity) {
this.quantity = quantity;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
