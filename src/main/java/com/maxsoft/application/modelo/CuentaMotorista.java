package com.maxsoft.application.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Movimiento de la cuenta de un motorista. Monto positivo = el motorista debe
 * más (faltante en una liquidación); negativo = debe menos (abono o reverso).
 * El saldo es la suma de los montos.
 */
@Entity
@Table(name = "cuenta_motorista")
public class CuentaMotorista implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Faltante de una liquidación cargado como deuda. */
    public static final String FALTANTE = "FALTANTE";
    /** Pago en efectivo del motorista a su deuda. */
    public static final String ABONO = "ABONO";
    /** Reverso del faltante al anular la liquidación. */
    public static final String ANULACION_FALTANTE = "ANULACION_FALTANTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @JoinColumn(name = "delivery", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Delivery delivery;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @JoinColumn(name = "liquidacion_delivery", referencedColumnName = "codigo")
    @ManyToOne
    private LiquidacionDelivery liquidacionDelivery;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "nombre_usuario", length = 100)
    private String nombreUsuario;

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LiquidacionDelivery getLiquidacionDelivery() {
        return liquidacionDelivery;
    }

    public void setLiquidacionDelivery(LiquidacionDelivery liquidacionDelivery) {
        this.liquidacionDelivery = liquidacionDelivery;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CuentaMotorista other)) {
            return false;
        }
        return codigo != null && codigo.equals(other.codigo);
    }
}
