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
@Table(name = "detalle_traslado_inventario")
@NamedQueries({
    @NamedQuery(name = "DetalleTrasladoInventario.findAll", query = "SELECT d FROM DetalleTrasladoInventario d")})
public class DetalleTrasladoInventario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Size(max = 100)
    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;
    @Size(max = 20)
    @Column(name = "nombre_unidad")
    private String nombreUnidad;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad_enviada")
    private BigDecimal cantidadEnviada;
    @Column(name = "cantidad_recibida")
    private BigDecimal cantidadRecibida;
    @Basic(optional = false)
    @NotNull
    @Column(name = "costo_unitario")
    private BigDecimal costoUnitario;
    @Basic(optional = false)
    @NotNull
    @Column(name = "precio_compra")
    private BigDecimal precioCompra;
    @Column(name = "factor_conversion")
    private BigDecimal factorConversion;
    @Column(name = "cantidad_fisica_base")
    private BigDecimal cantidadFisicaBase;
    @Column(name = "sub_total")
    private BigDecimal subTotal;
    @Column(name = "itbis")
    private BigDecimal itbis;
    @Column(name = "total")
    private BigDecimal total;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
    @JoinColumn(name = "articulo_empaque", referencedColumnName = "codigo")
    @ManyToOne
    private ArticuloEmpaque articuloEmpaque;
    @JoinColumn(name = "traslado", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private TrasladoInventario traslado;
    @JoinColumn(name = "unidad", referencedColumnName = "codigo")
    @ManyToOne
    private Unidad unidad;

    public DetalleTrasladoInventario() {
    }

    public DetalleTrasladoInventario(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleTrasladoInventario(Integer codigo, BigDecimal cantidadEnviada, BigDecimal costoUnitario, BigDecimal precioCompra) {
        this.codigo = codigo;
        this.cantidadEnviada = cantidadEnviada;
        this.costoUnitario = costoUnitario;
        this.precioCompra = precioCompra;
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

    public String getNombreUnidad() {
        return nombreUnidad;
    }

    public void setNombreUnidad(String nombreUnidad) {
        this.nombreUnidad = nombreUnidad;
    }

    public BigDecimal getCantidadEnviada() {
        return cantidadEnviada;
    }

    public void setCantidadEnviada(BigDecimal cantidadEnviada) {
        this.cantidadEnviada = cantidadEnviada;
    }

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(BigDecimal cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
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

    public TrasladoInventario getTraslado() {
        return traslado;
    }

    public void setTraslado(TrasladoInventario traslado) {
        this.traslado = traslado;
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
        if (!(object instanceof DetalleTrasladoInventario)) {
            return false;
        }
        DetalleTrasladoInventario other = (DetalleTrasladoInventario) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleTrasladoInventario[ codigo=" + codigo + " ]";
    }
    
}
