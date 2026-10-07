/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "plazo")
@NamedQueries({
    @NamedQuery(name = "Plazo.findAll", query = "SELECT p FROM Plazo p")})
public class Plazo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Size(max = 100)
    @Column(name = "descripcion")
    private String descripcion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "dias")
    private int dias;
    @OneToMany(mappedBy = "plazo")
    private Collection<OrdenDeCompra> ordenDeCompraCollection;
    @OneToMany(mappedBy = "plazo")
    private Collection<Proveedor> proveedorCollection;

    public Plazo() {
    }

    public Plazo(Integer codigo) {
        this.codigo = codigo;
    }

    public Plazo(Integer codigo, int dias) {
        this.codigo = codigo;
        this.dias = dias;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDias() {
        return dias;
    }

    public void setDias(int dias) {
        this.dias = dias;
    }

    public Collection<OrdenDeCompra> getOrdenDeCompraCollection() {
        return ordenDeCompraCollection;
    }

    public void setOrdenDeCompraCollection(Collection<OrdenDeCompra> ordenDeCompraCollection) {
        this.ordenDeCompraCollection = ordenDeCompraCollection;
    }

    public Collection<Proveedor> getProveedorCollection() {
        return proveedorCollection;
    }

    public void setProveedorCollection(Collection<Proveedor> proveedorCollection) {
        this.proveedorCollection = proveedorCollection;
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
        if (!(object instanceof Plazo)) {
            return false;
        }
        Plazo other = (Plazo) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.Plazo[ codigo=" + codigo + " ]";
    }
    
}
