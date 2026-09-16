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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "detalle_ajuste_inventario")
public class DetalleAjusteInventario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 120)
    @Column(name = "decripcion_articulo")
    private String decripcionArticulo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "existencia")
    private Double existencia;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad")
    private Double cantidad;
    @Basic(optional = false)
    @NotNull
    @Column(name = "nueva_existencia")
    private Double nuevaExistencia;
    @Size(max = 20)
    @Column(name = "nombre_unidad")
    private String nombreUnidad;
    @Size(max = 80)
    @Column(name = "nombre_almacen")
    private String nombreAlmacen;
    @JoinColumn(name = "ajuste_inventario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private AjusteInventario ajusteInventario;
    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacen;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
    @JoinColumn(name = "unidad", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Unidad unidad;

    public DetalleAjusteInventario() {
    }

    public DetalleAjusteInventario(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleAjusteInventario(Integer codigo, String decripcionArticulo, double existencia, double cantidad, double nuevaExistencia) {
        this.codigo = codigo;
        this.decripcionArticulo = decripcionArticulo;
        this.existencia = existencia;
        this.cantidad = cantidad;
        this.nuevaExistencia = nuevaExistencia;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDecripcionArticulo() {
        return decripcionArticulo;
    }

    public void setDecripcionArticulo(String decripcionArticulo) {
        this.decripcionArticulo = decripcionArticulo;
    }

    public Double getExistencia() {
        return existencia;
    }

    public void setExistencia(Double existencia) {
        this.existencia = existencia;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Double getNuevaExistencia() {
        return nuevaExistencia;
    }

    public void setNuevaExistencia(Double nuevaExistencia) {
        this.nuevaExistencia = nuevaExistencia;
    }

    public String getNombreUnidad() {
        return nombreUnidad;
    }

    public void setNombreUnidad(String nombreUnidad) {
        this.nombreUnidad = nombreUnidad;
    }

    public String getNombreAlmacen() {
        return nombreAlmacen;
    }

    public void setNombreAlmacen(String nombreAlmacen) {
        this.nombreAlmacen = nombreAlmacen;
    }

    public AjusteInventario getAjusteInventario() {
        return ajusteInventario;
    }

    public void setAjusteInventario(AjusteInventario ajusteInventario) {
        this.ajusteInventario = ajusteInventario;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }


    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
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
        if (!(object instanceof DetalleAjusteInventario)) {
            return false;
        }
        DetalleAjusteInventario other = (DetalleAjusteInventario) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleAjusteInventario[ codigo=" + codigo + " ]";
    }
    
}
