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
 * Devolución de mercancía a un proveedor (vencida, dañada, etc.). Saca la
 * mercancía del lote exacto y se liquida de una de tres formas:
 * <ul>
 * <li>{@link #NOTA_CREDITO}: rebaja lo que se le debe al proveedor; se aplica
 * a facturas de compra pendientes y lo que sobre queda como crédito
 * disponible.</li>
 * <li>{@link #CAMBIO}: el proveedor repone la misma cantidad en un lote
 * nuevo; no hay dinero.</li>
 * <li>{@link #EFECTIVO}: el proveedor devuelve el dinero y entra a la caja
 * abierta.</li>
 * </ul>
 */
@Entity
@Table(name = "devolucion_proveedor")
public class DevolucionProveedor implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String NOTA_CREDITO = "NOTA_CREDITO";
    public static final String CAMBIO = "CAMBIO";
    public static final String EFECTIVO = "EFECTIVO";

    public static final String MOTIVO_VENCIDO = "VENCIDO";
    public static final String MOTIVO_DANADO = "DANADO";
    public static final String MOTIVO_OTRO = "OTRO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @JoinColumn(name = "proveedor", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Proveedor proveedor;

    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacen;

    @Column(name = "tipo_liquidacion", nullable = false, length = 20)
    private String tipoLiquidacion;

    @Column(name = "motivo", nullable = false, length = 20)
    private String motivo;

    @Column(name = "observacion", length = 250)
    private String observacion;

    /** Factura de compra de referencia; en nota de crédito, la primera a la que se aplica. */
    @JoinColumn(name = "factura_compra", referencedColumnName = "codigo")
    @ManyToOne
    private FacturaDeCompra facturaCompra;

    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(name = "itbis", nullable = false)
    private BigDecimal itbis = BigDecimal.ZERO;

    @Column(name = "total", nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "credito_aplicado", nullable = false)
    private BigDecimal creditoAplicado = BigDecimal.ZERO;

    @Column(name = "credito_disponible", nullable = false)
    private BigDecimal creditoDisponible = BigDecimal.ZERO;

    @Column(name = "caja_turno")
    private Integer cajaTurno;

    @Column(name = "salida_inventario")
    private Integer salidaInventario;

    @Column(name = "entrada_inventario")
    private Integer entradaInventario;

    @Column(name = "anulada", nullable = false)
    private boolean anulada;

    @Column(name = "fecha_anulada")
    private LocalDateTime fechaAnulada;

    @Column(name = "motivo_anulacion", length = 250)
    private String motivoAnulacion;

    @Column(name = "usuario", length = 60)
    private String usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    public DevolucionProveedor() {
    }

    /** Número para mostrar: DP-15. */
    public String getNumero() {
        return codigo != null ? "DP-" + codigo : "DP-(nueva)";
    }

    public static String nombreLiquidacion(String tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case NOTA_CREDITO -> "Nota de crédito";
            case CAMBIO -> "Cambio por mercancía";
            case EFECTIVO -> "Reembolso en efectivo";
            default -> tipo;
        };
    }

    public static String nombreMotivo(String motivo) {
        if (motivo == null) {
            return "";
        }
        return switch (motivo) {
            case MOTIVO_VENCIDO -> "Vencido";
            case MOTIVO_DANADO -> "Dañado";
            case MOTIVO_OTRO -> "Otro";
            default -> motivo;
        };
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public String getTipoLiquidacion() {
        return tipoLiquidacion;
    }

    public void setTipoLiquidacion(String tipoLiquidacion) {
        this.tipoLiquidacion = tipoLiquidacion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public FacturaDeCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaDeCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
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

    public BigDecimal getCreditoAplicado() {
        return creditoAplicado != null ? creditoAplicado : BigDecimal.ZERO;
    }

    public void setCreditoAplicado(BigDecimal creditoAplicado) {
        this.creditoAplicado = creditoAplicado;
    }

    public BigDecimal getCreditoDisponible() {
        return creditoDisponible != null ? creditoDisponible : BigDecimal.ZERO;
    }

    public void setCreditoDisponible(BigDecimal creditoDisponible) {
        this.creditoDisponible = creditoDisponible;
    }

    public Integer getCajaTurno() {
        return cajaTurno;
    }

    public void setCajaTurno(Integer cajaTurno) {
        this.cajaTurno = cajaTurno;
    }

    public Integer getSalidaInventario() {
        return salidaInventario;
    }

    public void setSalidaInventario(Integer salidaInventario) {
        this.salidaInventario = salidaInventario;
    }

    public Integer getEntradaInventario() {
        return entradaInventario;
    }

    public void setEntradaInventario(Integer entradaInventario) {
        this.entradaInventario = entradaInventario;
    }

    public boolean isAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    public LocalDateTime getFechaAnulada() {
        return fechaAnulada;
    }

    public void setFechaAnulada(LocalDateTime fechaAnulada) {
        this.fechaAnulada = fechaAnulada;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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
        if (!(obj instanceof DevolucionProveedor other)) {
            return false;
        }
        return codigo != null && codigo.equals(other.codigo);
    }
}
