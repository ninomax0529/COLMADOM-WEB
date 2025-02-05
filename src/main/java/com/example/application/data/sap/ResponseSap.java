/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.data.sap;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 *
 * @author Maximiliano
 */
public class ResponseSap {

//    @SerializedName("Quantity")
//    @Expose
     @JsonProperty("quantity")  
     private String quantity;

    /**
     * @return the quantity
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

}
