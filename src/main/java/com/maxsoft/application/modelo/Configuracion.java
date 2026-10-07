/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "configuracion")
@NamedQueries({
    @NamedQuery(name = "Configuracion.findAll", query = "SELECT c FROM Configuracion c")})
public class Configuracion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Size(max = 150)
    @Column(name = "nombre_empresa")
    private String nombreEmpresa;
    @Size(max = 20)
    @Column(name = "rnc")
    private String rnc;
    @Column(name = "permitir_cambio_almacen")
    private Boolean permitirCambioAlmacen;
    @JoinColumn(name = "almacen_entrada_id", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacenEntradaId;
    @JoinColumn(name = "almacen_venta_id", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacenVentaId;

    public Configuracion() {
    }

    public Configuracion(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getRnc() {
        return rnc;
    }

    public void setRnc(String rnc) {
        this.rnc = rnc;
    }

    public Boolean getPermitirCambioAlmacen() {
        return permitirCambioAlmacen;
    }

    public void setPermitirCambioAlmacen(Boolean permitirCambioAlmacen) {
        this.permitirCambioAlmacen = permitirCambioAlmacen;
    }

    public Almacen getAlmacenEntradaId() {
        return almacenEntradaId;
    }

    public void setAlmacenEntradaId(Almacen almacenEntradaId) {
        this.almacenEntradaId = almacenEntradaId;
    }

    public Almacen getAlmacenVentaId() {
        return almacenVentaId;
    }

    public void setAlmacenVentaId(Almacen almacenVentaId) {
        this.almacenVentaId = almacenVentaId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Configuracion)) {
            return false;
        }
        Configuracion other = (Configuracion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.Configuracion[ id=" + id + " ]";
    }
    
}
