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
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "detalle_ordende_de_compra")
@NamedQueries({
    @NamedQuery(name = "DetalleOrdendeDeCompra.findAll", query = "SELECT d FROM DetalleOrdendeDeCompra d")})
public class DetalleOrdendeDeCompra implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Size(max = 80)
    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;
    @Column(name = "unidad")
    private Integer unidad;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Column(name = "cantidad_recibida")
    private BigDecimal cantidadRecibida;
    @Column(name = "impuesto")
    private BigDecimal impuesto;
    @Column(name = "descuento")
    private BigDecimal descuento;
    @Column(name = "sub_total")
    private BigDecimal subTotal;
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "pendiente")
    private BigDecimal pendiente;
    @Column(name = "otros_impuesto")
    private BigDecimal otrosImpuesto;
    @Column(name = "itbis")
    private BigDecimal itbis;
    @Column(name = "isr")
    private BigDecimal isr;
    @Column(name = "factor_conversion")
    private BigDecimal factorConversion;
    @Column(name = "cantidad_fisica_base")
    private BigDecimal cantidadFisicaBase;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne
    private Articulo articulo;
    @JoinColumn(name = "articulo_empaque", referencedColumnName = "codigo")
    @ManyToOne
    private ArticuloEmpaque articuloEmpaque;
    @JoinColumn(name = "orden_de_compra", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private OrdenDeCompra ordenDeCompra;

    public DetalleOrdendeDeCompra() {
    }

    public DetalleOrdendeDeCompra(Integer codigo) {
        this.codigo = codigo;
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

    public Integer getUnidad() {
        return unidad;
    }

    public void setUnidad(Integer unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(BigDecimal cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPendiente() {
        return pendiente;
    }

    public void setPendiente(BigDecimal pendiente) {
        this.pendiente = pendiente;
    }

    public BigDecimal getOtrosImpuesto() {
        return otrosImpuesto;
    }

    public void setOtrosImpuesto(BigDecimal otrosImpuesto) {
        this.otrosImpuesto = otrosImpuesto;
    }

    public BigDecimal getItbis() {
        return itbis;
    }

    public void setItbis(BigDecimal itbis) {
        this.itbis = itbis;
    }

    public BigDecimal getIsr() {
        return isr;
    }

    public void setIsr(BigDecimal isr) {
        this.isr = isr;
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

    public OrdenDeCompra getOrdenDeCompra() {
        return ordenDeCompra;
    }

    public void setOrdenDeCompra(OrdenDeCompra ordenDeCompra) {
        this.ordenDeCompra = ordenDeCompra;
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
        if (!(object instanceof DetalleOrdendeDeCompra)) {
            return false;
        }
        DetalleOrdendeDeCompra other = (DetalleOrdendeDeCompra) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleOrdendeDeCompra[ codigo=" + codigo + " ]";
    }
    
}
