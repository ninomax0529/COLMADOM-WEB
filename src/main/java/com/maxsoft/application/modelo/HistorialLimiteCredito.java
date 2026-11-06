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
 * Registro de cada cambio del límite de crédito de un cliente.
 * Un límite NULL significa "sin límite definido".
 */
@Entity
@Table(name = "historial_limite_credito")
public class HistorialLimiteCredito implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente", referencedColumnName = "codigo")
    private Cliente cliente;

    @Column(name = "limite_anterior")
    private BigDecimal limiteAnterior;

    @Column(name = "limite_nuevo")
    private BigDecimal limiteNuevo;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    public HistorialLimiteCredito() {
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

    public BigDecimal getLimiteAnterior() {
        return limiteAnterior;
    }

    public void setLimiteAnterior(BigDecimal limiteAnterior) {
        this.limiteAnterior = limiteAnterior;
    }

    public BigDecimal getLimiteNuevo() {
        return limiteNuevo;
    }

    public void setLimiteNuevo(BigDecimal limiteNuevo) {
        this.limiteNuevo = limiteNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
