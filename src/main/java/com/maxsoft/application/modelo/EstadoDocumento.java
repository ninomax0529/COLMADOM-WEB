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
@Table(name = "estado_documento")
@NamedQueries({
    @NamedQuery(name = "EstadoDocumento.findAll", query = "SELECT e FROM EstadoDocumento e")})
public class EstadoDocumento implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "nombre_modulo")
    private String nombreModulo;
    @Size(max = 20)
    @Column(name = "color_badge")
    private String colorBadge;
    @Column(name = "modulo")
    private Integer modulo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "estado")
    private Collection<TrasladoInventario> trasladoInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "estadoDocumento")
    private Collection<RecepcionMercancia> recepcionMercanciaCollection;

    public EstadoDocumento() {
    }

    public EstadoDocumento(Integer codigo) {
        this.codigo = codigo;
    }

    public EstadoDocumento(Integer codigo, String nombre, String nombreModulo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.nombreModulo = nombreModulo;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreModulo() {
        return nombreModulo;
    }

    public void setNombreModulo(String nombreModulo) {
        this.nombreModulo = nombreModulo;
    }

    public String getColorBadge() {
        return colorBadge;
    }

    public void setColorBadge(String colorBadge) {
        this.colorBadge = colorBadge;
    }

    public Integer getModulo() {
        return modulo;
    }

    public void setModulo(Integer modulo) {
        this.modulo = modulo;
    }

    public Collection<TrasladoInventario> getTrasladoInventarioCollection() {
        return trasladoInventarioCollection;
    }

    public void setTrasladoInventarioCollection(Collection<TrasladoInventario> trasladoInventarioCollection) {
        this.trasladoInventarioCollection = trasladoInventarioCollection;
    }

    public Collection<RecepcionMercancia> getRecepcionMercanciaCollection() {
        return recepcionMercanciaCollection;
    }

    public void setRecepcionMercanciaCollection(Collection<RecepcionMercancia> recepcionMercanciaCollection) {
        this.recepcionMercanciaCollection = recepcionMercanciaCollection;
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
        if (!(object instanceof EstadoDocumento)) {
            return false;
        }
        EstadoDocumento other = (EstadoDocumento) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.EstadoDocumento[ codigo=" + codigo + " ]";
    }
    
}
