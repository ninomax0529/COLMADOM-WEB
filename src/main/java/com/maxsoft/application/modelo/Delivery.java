/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "delivery")
@NamedQueries({
    @NamedQuery(name = "Delivery.findAll", query = "SELECT d FROM Delivery d")})
public class Delivery implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 80)
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 15)
    @Column(name = "celular")
    private String celular;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 80)
    @Column(name = "direccion")
    private String direccion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha")
//    @Temporal(TemporalTypeTemporalType.DATE)
    private Date fecha;
    @Size(max = 15)
    @Column(name = "cedula")
    private String cedula;
    @JoinColumn(name = "vehiculo", referencedColumnName = "codigo")
    @ManyToOne
    private Vehiculo vehiculo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "delivery")
    private Collection<LiquidacionDelivery> liquidacionDeliveryCollection;
    @OneToMany(mappedBy = "delivery")
    private Collection<FacturaDeVenta> facturaDeVentaCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "habilitado")
    private Boolean habilitado;

    public Delivery() {
    }

    public Delivery(Integer codigo) {
        this.codigo = codigo;
    }

    public Delivery(Integer codigo, String nombre, String celular, String direccion, Date fecha) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.celular = celular;
        this.direccion = direccion;
        this.fecha = fecha;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }


    public Boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(Boolean habilitado) {
        this.habilitado = habilitado;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codigo != null ? codigo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Delivery)) {
            return false;
        }
        Delivery other = (Delivery) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.Delivery[ codigo=" + codigo + " ]";
    }


    public Collection<LiquidacionDelivery> getLiquidacionDeliveryCollection() {
        return liquidacionDeliveryCollection;
    }

    public void setLiquidacionDeliveryCollection(Collection<LiquidacionDelivery> liquidacionDeliveryCollection) {
        this.liquidacionDeliveryCollection = liquidacionDeliveryCollection;
    }

    public Collection<FacturaDeVenta> getFacturaDeVentaCollection() {
        return facturaDeVentaCollection;
    }

    public void setFacturaDeVentaCollection(Collection<FacturaDeVenta> facturaDeVentaCollection) {
        this.facturaDeVentaCollection = facturaDeVentaCollection;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
    
}
