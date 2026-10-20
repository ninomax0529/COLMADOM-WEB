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
@Table(name = "condicion_pago")
@NamedQueries({
    @NamedQuery(name = "CondicionPago.findAll", query = "SELECT c FROM CondicionPago c")})
public class CondicionPago implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "nombre")
    private String nombre;
    @OneToMany(mappedBy = "condicionPago")
    private Collection<FacturaDeCompra> facturaDeCompraCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;

    public CondicionPago() {
    }

    public CondicionPago(Integer codigo) {
        this.codigo = codigo;
    }

    public CondicionPago(Integer codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
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
        if (!(object instanceof CondicionPago)) {
            return false;
        }
        CondicionPago other = (CondicionPago) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.CondicionPago[ codigo=" + codigo + " ]";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Collection<FacturaDeCompra> getFacturaDeCompraCollection() {
        return facturaDeCompraCollection;
    }

    public void setFacturaDeCompraCollection(Collection<FacturaDeCompra> facturaDeCompraCollection) {
        this.facturaDeCompraCollection = facturaDeCompraCollection;
    }
    
}
