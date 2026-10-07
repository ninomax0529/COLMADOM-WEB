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
@Table(name = "detalle_factura_de_venta")
@NamedQueries({
    @NamedQuery(name = "DetalleFacturaDeVenta.findAll", query = "SELECT d FROM DetalleFacturaDeVenta d")})
public class DetalleFacturaDeVenta implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;
    @Size(max = 10)
    @Column(name = "nombre_unidad")
    private String nombreUnidad;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "factor_conversion")
    private BigDecimal factorConversion;
    @Column(name = "cantidad_fisica_base")
    private BigDecimal cantidadFisicaBase;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad")
    private BigDecimal cantidad;
    @Column(name = "precio_compra")
    private BigDecimal precioCompra;
    @Column(name = "existencia_actual")
    private BigDecimal existenciaActual;
    @Column(name = "nueva_existencia")
    private BigDecimal nuevaExistencia;
    @Size(max = 20)
    @Column(name = "nombre_almacen")
    private String nombreAlmacen;
    @Column(name = "precio_venta")
    private BigDecimal precioVenta;
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
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_descuento")
    private BigDecimal totalDescuento;
    @Basic(optional = false)
    @NotNull
    @Column(name = "porciento_descuento")
    private BigDecimal porcientoDescuento;
    @Basic(optional = false)
    @NotNull
    @Column(name = "porciento_itbis")
    private BigDecimal porcientoItbis;
    @Basic(optional = false)
    @NotNull
    @Column(name = "numero_de_linea")
    private int numeroDeLinea;
    @Column(name = "cantidad_devuelta")
    private BigDecimal cantidadDevuelta;
    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacen;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
    @JoinColumn(name = "articulo_empaque", referencedColumnName = "codigo")
    @ManyToOne
    private ArticuloEmpaque articuloEmpaque;
    @JoinColumn(name = "factura", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private FacturaDeVenta factura;
    @JoinColumn(name = "unidad", referencedColumnName = "codigo")
    @ManyToOne
    private Unidad unidad;

    public DetalleFacturaDeVenta() {
    }

    public DetalleFacturaDeVenta(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleFacturaDeVenta(Integer codigo, String descripcionArticulo, BigDecimal cantidad, BigDecimal subTotal, BigDecimal itbis, BigDecimal total, BigDecimal totalDescuento, BigDecimal porcientoDescuento, BigDecimal porcientoItbis, int numeroDeLinea) {
        this.codigo = codigo;
        this.descripcionArticulo = descripcionArticulo;
        this.cantidad = cantidad;
        this.subTotal = subTotal;
        this.itbis = itbis;
        this.total = total;
        this.totalDescuento = totalDescuento;
        this.porcientoDescuento = porcientoDescuento;
        this.porcientoItbis = porcientoItbis;
        this.numeroDeLinea = numeroDeLinea;
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

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getExistenciaActual() {
        return existenciaActual;
    }

    public void setExistenciaActual(BigDecimal existenciaActual) {
        this.existenciaActual = existenciaActual;
    }

    public BigDecimal getNuevaExistencia() {
        return nuevaExistencia;
    }

    public void setNuevaExistencia(BigDecimal nuevaExistencia) {
        this.nuevaExistencia = nuevaExistencia;
    }

    public String getNombreAlmacen() {
        return nombreAlmacen;
    }

    public void setNombreAlmacen(String nombreAlmacen) {
        this.nombreAlmacen = nombreAlmacen;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
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

    public BigDecimal getTotalDescuento() {
        return totalDescuento;
    }

    public void setTotalDescuento(BigDecimal totalDescuento) {
        this.totalDescuento = totalDescuento;
    }

    public BigDecimal getPorcientoDescuento() {
        return porcientoDescuento;
    }

    public void setPorcientoDescuento(BigDecimal porcientoDescuento) {
        this.porcientoDescuento = porcientoDescuento;
    }

    public BigDecimal getPorcientoItbis() {
        return porcientoItbis;
    }

    public void setPorcientoItbis(BigDecimal porcientoItbis) {
        this.porcientoItbis = porcientoItbis;
    }

    public int getNumeroDeLinea() {
        return numeroDeLinea;
    }

    public void setNumeroDeLinea(int numeroDeLinea) {
        this.numeroDeLinea = numeroDeLinea;
    }

    public BigDecimal getCantidadDevuelta() {
        return cantidadDevuelta;
    }

    public void setCantidadDevuelta(BigDecimal cantidadDevuelta) {
        this.cantidadDevuelta = cantidadDevuelta;
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

    public FacturaDeVenta getFactura() {
        return factura;
    }

    public void setFactura(FacturaDeVenta factura) {
        this.factura = factura;
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
        if (!(object instanceof DetalleFacturaDeVenta)) {
            return false;
        }
        DetalleFacturaDeVenta other = (DetalleFacturaDeVenta) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleFacturaDeVenta[ codigo=" + codigo + " ]";
    }
    
}
