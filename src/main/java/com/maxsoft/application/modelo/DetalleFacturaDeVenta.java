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
    @Column(name = "precio_compra")
    private Double precioCompra;
    @Column(name = "existencia_actual")
    private Double existenciaActual;
    @Column(name = "nueva_existencia")
    private Double nuevaExistencia;
    @Size(max = 20)
    @Column(name = "nombre_almacen")
    private String nombreAlmacen;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad")
    private double cantidad;
    @Column(name = "precio_venta")
    private Double precioVenta;
    @Basic(optional = false)
    @NotNull
    @Column(name = "sub_total")
    private double subTotal;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_descuento")
    private double totalDescuento;
    @Basic(optional = false)
    @NotNull
    @Column(name = "porciento_descuento")
    private double porcientoDescuento;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_itbis")
    private double totalItbis;
    @Basic(optional = false)
    @NotNull
    @Column(name = "porciento_itbis")
    private double porcientoItbis;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total")
    private double total;
    @Basic(optional = false)
    @NotNull
    @Column(name = "numero_de_linea")
    private int numeroDeLinea;
    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacen;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
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

    public DetalleFacturaDeVenta(Integer codigo, String descripcionArticulo, double cantidad, double subTotal, double totalDescuento, double porcientoDescuento, double totalItbis, double porcientoItbis, double total, int numeroDeLinea) {
        this.codigo = codigo;
        this.descripcionArticulo = descripcionArticulo;
        this.cantidad = cantidad;
        this.subTotal = subTotal;
        this.totalDescuento = totalDescuento;
        this.porcientoDescuento = porcientoDescuento;
        this.totalItbis = totalItbis;
        this.porcientoItbis = porcientoItbis;
        this.total = total;
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

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Double getExistenciaActual() {
        return existenciaActual;
    }

    public void setExistenciaActual(Double existenciaActual) {
        this.existenciaActual = existenciaActual;
    }

    public Double getNuevaExistencia() {
        return nuevaExistencia;
    }

    public void setNuevaExistencia(Double nuevaExistencia) {
        this.nuevaExistencia = nuevaExistencia;
    }

    public String getNombreAlmacen() {
        return nombreAlmacen;
    }

    public void setNombreAlmacen(String nombreAlmacen) {
        this.nombreAlmacen = nombreAlmacen;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getTotalDescuento() {
        return totalDescuento;
    }

    public void setTotalDescuento(double totalDescuento) {
        this.totalDescuento = totalDescuento;
    }

    public double getPorcientoDescuento() {
        return porcientoDescuento;
    }

    public void setPorcientoDescuento(double porcientoDescuento) {
        this.porcientoDescuento = porcientoDescuento;
    }

    public double getTotalItbis() {
        return totalItbis;
    }

    public void setTotalItbis(double totalItbis) {
        this.totalItbis = totalItbis;
    }

    public double getPorcientoItbis() {
        return porcientoItbis;
    }

    public void setPorcientoItbis(double porcientoItbis) {
        this.porcientoItbis = porcientoItbis;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getNumeroDeLinea() {
        return numeroDeLinea;
    }

    public void setNumeroDeLinea(int numeroDeLinea) {
        this.numeroDeLinea = numeroDeLinea;
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
