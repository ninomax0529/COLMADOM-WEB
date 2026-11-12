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
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Cuánto de una línea de venta salió de cada lote (unidad base). Una línea
 * puede consumir varios lotes. La anulación y la devolución reponen en estos
 * mismos lotes.
 */
@Entity
@Table(name = "detalle_factura_venta_lote")
public class DetalleFacturaVentaLote implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @JoinColumn(name = "detalle_factura_de_venta", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private DetalleFacturaDeVenta detalleFacturaDeVenta;

    @JoinColumn(name = "lote", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Lote lote;

    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacen;

    /** Cantidad que salió de este lote, en unidad base. */
    @Column(name = "cantidad", nullable = false)
    private BigDecimal cantidad;

    /** Cantidad ya repuesta al lote por devolución o anulación (unidad base). */
    @Column(name = "cantidad_devuelta", nullable = false)
    private BigDecimal cantidadDevuelta = BigDecimal.ZERO;

    /** Se vendió con el lote vencido (requiere autorización). */
    @Column(name = "lote_vencido", nullable = false)
    private boolean loteVencido;

    /** Supervisor que autorizó vender el lote vencido. */
    @Column(name = "autorizado_por", length = 50)
    private String autorizadoPor;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** Lo que todavía se puede reponer a este lote. */
    public BigDecimal getPendientePorDevolver() {
        return getCantidad().subtract(getCantidadDevuelta());
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleFacturaDeVenta getDetalleFacturaDeVenta() {
        return detalleFacturaDeVenta;
    }

    public void setDetalleFacturaDeVenta(DetalleFacturaDeVenta detalleFacturaDeVenta) {
        this.detalleFacturaDeVenta = detalleFacturaDeVenta;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public BigDecimal getCantidad() {
        return cantidad != null ? cantidad : BigDecimal.ZERO;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCantidadDevuelta() {
        return cantidadDevuelta != null ? cantidadDevuelta : BigDecimal.ZERO;
    }

    public void setCantidadDevuelta(BigDecimal cantidadDevuelta) {
        this.cantidadDevuelta = cantidadDevuelta;
    }

    public boolean isLoteVencido() {
        return loteVencido;
    }

    public void setLoteVencido(boolean loteVencido) {
        this.loteVencido = loteVencido;
    }

    public String getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(String autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DetalleFacturaVentaLote other)) {
            return false;
        }
        return codigo != null && codigo.equals(other.codigo);
    }
}
