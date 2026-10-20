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
@Table(name = "detalle_factura_de_compra")
@NamedQueries({
    @NamedQuery(name = "DetalleFacturaDeCompra.findAll", query = "SELECT d FROM DetalleFacturaDeCompra d")})
public class DetalleFacturaDeCompra implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Size(max = 80)
    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "factor_conversion")
    private BigDecimal factorConversion;
    @Column(name = "cantidad_fisica_base")
    private BigDecimal cantidadFisicaBase;
    @Column(name = "unidad")
    private Integer unidad;
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
    @Column(name = "sub_total")
    private BigDecimal subTotal;
    @Column(name = "porciento_itbis")
    private BigDecimal porcientoItbis;
    @Column(name = "porciento_isr")
    private BigDecimal porcientoIsr;
    @Column(name = "porciento_descuento")
    private BigDecimal porcientoDescuento;
    @Column(name = "total_descuento")
    private BigDecimal totalDescuento;
    @Column(name = "total_itbis")
    private BigDecimal totalItbis;
    @Column(name = "total_isr")
    private BigDecimal totalIsr;
    @Column(name = "otros_impuesto")
    private BigDecimal otrosImpuesto;
    @Column(name = "total_impuesto")
    private BigDecimal totalImpuesto;
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "total_a_pagar")
    private BigDecimal totalAPagar;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne
    private Articulo articulo;
    @JoinColumn(name = "articulo_empaque", referencedColumnName = "codigo")
    @ManyToOne
    private ArticuloEmpaque articuloEmpaque;
    @JoinColumn(name = "factura_de_compra", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private FacturaDeCompra facturaDeCompra;

    public DetalleFacturaDeCompra() {
    }

    public DetalleFacturaDeCompra(Integer codigo) {
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

    public Integer getUnidad() {
        return unidad;
    }

    public void setUnidad(Integer unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getPorcientoItbis() {
        return porcientoItbis;
    }

    public void setPorcientoItbis(BigDecimal porcientoItbis) {
        this.porcientoItbis = porcientoItbis;
    }

    public BigDecimal getPorcientoIsr() {
        return porcientoIsr;
    }

    public void setPorcientoIsr(BigDecimal porcientoIsr) {
        this.porcientoIsr = porcientoIsr;
    }

    public BigDecimal getPorcientoDescuento() {
        return porcientoDescuento;
    }

    public void setPorcientoDescuento(BigDecimal porcientoDescuento) {
        this.porcientoDescuento = porcientoDescuento;
    }

    public BigDecimal getTotalDescuento() {
        return totalDescuento;
    }

    public void setTotalDescuento(BigDecimal totalDescuento) {
        this.totalDescuento = totalDescuento;
    }

    public BigDecimal getTotalItbis() {
        return totalItbis;
    }

    public void setTotalItbis(BigDecimal totalItbis) {
        this.totalItbis = totalItbis;
    }

    public BigDecimal getTotalIsr() {
        return totalIsr;
    }

    public void setTotalIsr(BigDecimal totalIsr) {
        this.totalIsr = totalIsr;
    }

    public BigDecimal getOtrosImpuesto() {
        return otrosImpuesto;
    }

    public void setOtrosImpuesto(BigDecimal otrosImpuesto) {
        this.otrosImpuesto = otrosImpuesto;
    }

    public BigDecimal getTotalImpuesto() {
        return totalImpuesto;
    }

    public void setTotalImpuesto(BigDecimal totalImpuesto) {
        this.totalImpuesto = totalImpuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalAPagar() {
        return totalAPagar;
    }

    public void setTotalAPagar(BigDecimal totalAPagar) {
        this.totalAPagar = totalAPagar;
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

    public FacturaDeCompra getFacturaDeCompra() {
        return facturaDeCompra;
    }

    public void setFacturaDeCompra(FacturaDeCompra facturaDeCompra) {
        this.facturaDeCompra = facturaDeCompra;
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
        if (!(object instanceof DetalleFacturaDeCompra)) {
            return false;
        }
        DetalleFacturaDeCompra other = (DetalleFacturaDeCompra) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleFacturaDeCompra[ codigo=" + codigo + " ]";
    }
    
}
