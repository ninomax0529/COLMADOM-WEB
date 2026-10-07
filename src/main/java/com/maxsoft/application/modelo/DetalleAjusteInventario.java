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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "detalle_ajuste_inventario")
@NamedQueries({
    @NamedQuery(name = "DetalleAjusteInventario.findAll", query = "SELECT d FROM DetalleAjusteInventario d")})
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
    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "existencia")
    private BigDecimal existencia;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Basic(optional = false)
    @NotNull
    @Column(name = "nueva_existencia")
    private BigDecimal nuevaExistencia;
    @Size(max = 20)
    @Column(name = "nombre_unidad")
    private String nombreUnidad;
    @Size(max = 80)
    @Column(name = "nombre_almacen")
    private String nombreAlmacen;
    @Column(name = "factor_conversion")
    private BigDecimal factorConversion;
    @Column(name = "cantidad_fisica_base")
    private BigDecimal cantidadFisicaBase;
    @JoinColumn(name = "ajuste_inventario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private AjusteInventario ajusteInventario;
    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacen;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
    @JoinColumn(name = "articulo_empaque", referencedColumnName = "codigo")
    @ManyToOne
    private ArticuloEmpaque articuloEmpaque;
    @JoinColumn(name = "unidad", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Unidad unidad;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sub_total")
    private BigDecimal subTotal;
    @Basic(optional = false)
    @NotNull
    @Column(name = "itbis")
    private BigDecimal itbis;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total")
    private BigDecimal total;

    public DetalleAjusteInventario() {
    }

    public DetalleAjusteInventario(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleAjusteInventario(Integer codigo, String descripcionArticulo, BigDecimal existencia, BigDecimal cantidad, BigDecimal nuevaExistencia) {
        this.codigo = codigo;
        this.descripcionArticulo = descripcionArticulo;
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

    public String getDescripcionArticulo() {
        return descripcionArticulo;
    }

    public void setDescripcionArticulo(String descripcionArticulo) {
        this.descripcionArticulo = descripcionArticulo;
    }

    public BigDecimal getExistencia() {
        return existencia;
    }

    public void setExistencia(BigDecimal existencia) {
        this.existencia = existencia;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getNuevaExistencia() {
        return nuevaExistencia;
    }

    public void setNuevaExistencia(BigDecimal nuevaExistencia) {
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

    public BigDecimal getFactorConversion() {
        return factorConversion;
    }

    public void setFactorConversion(BigDecimal factorConversion) {
        this.factorConversion = factorConversion;
    }

    public BigDecimal getCantidadFisicaBase() {
        return cantidadFisicaBase;
    }

    public void setCantidadFisicaBase(BigDecimal cantidadFisicaBase) {
        this.cantidadFisicaBase = cantidadFisicaBase;
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

    public ArticuloEmpaque getArticuloEmpaque() {
        return articuloEmpaque;
    }

    public void setArticuloEmpaque(ArticuloEmpaque articuloEmpaque) {
        this.articuloEmpaque = articuloEmpaque;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getItbis() {
        return itbis;
    }

    public void setItbis(BigDecimal itbis) {
        this.itbis = itbis;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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
