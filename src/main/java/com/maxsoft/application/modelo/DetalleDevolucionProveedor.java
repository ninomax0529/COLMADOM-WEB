package com.maxsoft.application.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Línea de una devolución a proveedor: qué artículo, de qué lote y cuánto.
 * En un CAMBIO también dice en qué lote nuevo entró la reposición.
 */
@Entity
@Table(name = "detalle_devolucion_proveedor")
public class DetalleDevolucionProveedor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @JoinColumn(name = "devolucion", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private DevolucionProveedor devolucion;

    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;

    @JoinColumn(name = "articulo_empaque", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private ArticuloEmpaque articuloEmpaque;

    @Column(name = "descripcion_articulo", length = 250)
    private String descripcionArticulo;

    /** Cantidad en el empaque elegido. */
    @Column(name = "cantidad", nullable = false)
    private BigDecimal cantidad = BigDecimal.ZERO;

    @Column(name = "factor_conversion", nullable = false)
    private BigDecimal factorConversion = BigDecimal.ONE;

    /** cantidad x factor (unidad base). */
    @Column(name = "cantidad_base", nullable = false)
    private BigDecimal cantidadBase = BigDecimal.ZERO;

    /** Costo por empaque, sin ITBIS. */
    @Column(name = "costo_unitario", nullable = false)
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    @Column(name = "porcentaje_itbis", nullable = false)
    private BigDecimal porcentajeItbis = BigDecimal.ZERO;

    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(name = "itbis", nullable = false)
    private BigDecimal itbis = BigDecimal.ZERO;

    @Column(name = "total", nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    /** Lote del que sale (null = artículo sin control de lote, o FEFO). */
    @JoinColumn(name = "lote", referencedColumnName = "codigo")
    @ManyToOne
    private Lote lote;

    @Column(name = "numero_lote_nuevo", length = 50)
    private String numeroLoteNuevo;

    @Column(name = "fecha_vencimiento_nueva")
    private LocalDate fechaVencimientoNueva;

    @JoinColumn(name = "lote_nuevo", referencedColumnName = "codigo")
    @ManyToOne
    private Lote loteNuevo;

    public DetalleDevolucionProveedor() {
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public DevolucionProveedor getDevolucion() {
        return devolucion;
    }

    public void setDevolucion(DevolucionProveedor devolucion) {
        this.devolucion = devolucion;
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

    public String getDescripcionArticulo() {
        return descripcionArticulo;
    }

    public void setDescripcionArticulo(String descripcionArticulo) {
        this.descripcionArticulo = descripcionArticulo;
    }

    public BigDecimal getCantidad() {
        return cantidad != null ? cantidad : BigDecimal.ZERO;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getFactorConversion() {
        return factorConversion != null && factorConversion.signum() > 0 ? factorConversion : BigDecimal.ONE;
    }

    public void setFactorConversion(BigDecimal factorConversion) {
        this.factorConversion = factorConversion;
    }

    public BigDecimal getCantidadBase() {
        return cantidadBase != null ? cantidadBase : BigDecimal.ZERO;
    }

    public void setCantidadBase(BigDecimal cantidadBase) {
        this.cantidadBase = cantidadBase;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario != null ? costoUnitario : BigDecimal.ZERO;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getPorcentajeItbis() {
        return porcentajeItbis != null ? porcentajeItbis : BigDecimal.ZERO;
    }

    public void setPorcentajeItbis(BigDecimal porcentajeItbis) {
        this.porcentajeItbis = porcentajeItbis;
    }

    public BigDecimal getSubTotal() {
        return subTotal != null ? subTotal : BigDecimal.ZERO;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getItbis() {
        return itbis != null ? itbis : BigDecimal.ZERO;
    }

    public void setItbis(BigDecimal itbis) {
        this.itbis = itbis;
    }

    public BigDecimal getTotal() {
        return total != null ? total : BigDecimal.ZERO;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public String getNumeroLoteNuevo() {
        return numeroLoteNuevo;
    }

    public void setNumeroLoteNuevo(String numeroLoteNuevo) {
        this.numeroLoteNuevo = numeroLoteNuevo;
    }

    public LocalDate getFechaVencimientoNueva() {
        return fechaVencimientoNueva;
    }

    public void setFechaVencimientoNueva(LocalDate fechaVencimientoNueva) {
        this.fechaVencimientoNueva = fechaVencimientoNueva;
    }

    public Lote getLoteNuevo() {
        return loteNuevo;
    }

    public void setLoteNuevo(Lote loteNuevo) {
        this.loteNuevo = loteNuevo;
    }
}
