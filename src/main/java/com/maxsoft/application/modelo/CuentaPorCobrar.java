package com.maxsoft.application.modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Movimiento del estado de cuenta de un cliente (libreta / fiado).
 * <p>
 * Débito = aumenta lo que debe el cliente (factura a crédito).<br>
 * Crédito = disminuye lo que debe (recibo, anulación, devolución).
 */
@Entity
@Table(name = "cuenta_por_cobrar")
public class CuentaPorCobrar implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FACTURA = "FACTURA";
    public static final String RECIBO = "RECIBO";
    public static final String ANULACION_FACTURA = "ANULACION_FACTURA";
    public static final String DEVOLUCION = "DEVOLUCION";
    public static final String AJUSTE_DEBITO = "AJUSTE_DEBITO";
    public static final String AJUSTE_CREDITO = "AJUSTE_CREDITO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente", referencedColumnName = "codigo")
    private Cliente cliente;

    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Column(name = "tipo_movimiento")
    private String tipoMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura", referencedColumnName = "codigo")
    private FacturaDeVenta factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_ingreso", referencedColumnName = "codigo")
    private ReciboDeIngreso reciboIngreso;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "concepto")
    private String concepto;

    @Column(name = "debito")
    private BigDecimal debito = BigDecimal.ZERO;

    @Column(name = "credito")
    private BigDecimal credito = BigDecimal.ZERO;

    @Column(name = "balance_anterior")
    private BigDecimal balanceAnterior;

    @Column(name = "balance_nuevo")
    private BigDecimal balanceNuevo;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    public CuentaPorCobrar() {
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public FacturaDeVenta getFactura() {
        return factura;
    }

    public void setFactura(FacturaDeVenta factura) {
        this.factura = factura;
    }

    public ReciboDeIngreso getReciboIngreso() {
        return reciboIngreso;
    }

    public void setReciboIngreso(ReciboDeIngreso reciboIngreso) {
        this.reciboIngreso = reciboIngreso;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getDebito() {
        return debito;
    }

    public void setDebito(BigDecimal debito) {
        this.debito = debito;
    }

    public BigDecimal getCredito() {
        return credito;
    }

    public void setCredito(BigDecimal credito) {
        this.credito = credito;
    }

    public BigDecimal getBalanceAnterior() {
        return balanceAnterior;
    }

    public void setBalanceAnterior(BigDecimal balanceAnterior) {
        this.balanceAnterior = balanceAnterior;
    }

    public BigDecimal getBalanceNuevo() {
        return balanceNuevo;
    }

    public void setBalanceNuevo(BigDecimal balanceNuevo) {
        this.balanceNuevo = balanceNuevo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
