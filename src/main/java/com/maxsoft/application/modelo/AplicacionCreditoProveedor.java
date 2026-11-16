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

/**
 * Aplicación de (parte de) una nota de crédito de proveedor a una factura de
 * compra. Cada aplicación genera un recibo de egreso sin efectivo, que es lo
 * que rebaja el pendiente de la factura.
 */
@Entity
@Table(name = "aplicacion_credito_proveedor")
public class AplicacionCreditoProveedor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @JoinColumn(name = "devolucion", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private DevolucionProveedor devolucion;

    @JoinColumn(name = "factura_compra", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private FacturaDeCompra facturaCompra;

    @JoinColumn(name = "recibo_egreso", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private ReciboDeEgreso reciboEgreso;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "usuario", length = 60)
    private String usuario;

    @Column(name = "anulada", nullable = false)
    private boolean anulada;

    public AplicacionCreditoProveedor() {
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

    public FacturaDeCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaDeCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }

    public ReciboDeEgreso getReciboEgreso() {
        return reciboEgreso;
    }

    public void setReciboEgreso(ReciboDeEgreso reciboEgreso) {
        this.reciboEgreso = reciboEgreso;
    }

    public BigDecimal getMonto() {
        return monto != null ? monto : BigDecimal.ZERO;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public boolean isAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }
}
