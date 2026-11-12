/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "liquidacion_delivery")
@NamedQueries({
    @NamedQuery(name = "LiquidacionDelivery.findAll", query = "SELECT l FROM LiquidacionDelivery l")})
public class LiquidacionDelivery implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    @Size(max = 100)
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "efectivo_esperado")
    private BigDecimal efectivoEsperado;
    @Basic(optional = false)
    @NotNull()
    @Column(name = "efectivo_recibido")
    private BigDecimal efectivoRecibido;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "diferencia")
    private BigDecimal diferencia;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_fiado")
    private BigDecimal totalFiado;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_liquidado")
    private BigDecimal totalLiquidado;
    @Size(max = 500)
    @Column(name = "observacion")
    private String observacion;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "liquidacionDelivery")
    private Collection<DetalleLiquidacionDelivery> detalleLiquidacionDeliveryCollection;
    @JoinColumn(name = "delivery", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Delivery delivery;

    public LiquidacionDelivery() {
    }

    public LiquidacionDelivery(Integer codigo) {
        this.codigo = codigo;
    }

    public LiquidacionDelivery(Integer codigo, Date fecha, BigDecimal efectivoEsperado, BigDecimal efectivoRecibido, BigDecimal diferencia, BigDecimal totalFiado, BigDecimal totalLiquidado) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.efectivoEsperado = efectivoEsperado;
        this.efectivoRecibido = efectivoRecibido;
        this.diferencia = diferencia;
        this.totalFiado = totalFiado;
        this.totalLiquidado = totalLiquidado;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }


    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public BigDecimal getEfectivoEsperado() {
        return efectivoEsperado;
    }

    public void setEfectivoEsperado(BigDecimal efectivoEsperado) {
        this.efectivoEsperado = efectivoEsperado;
    }

    public BigDecimal getEfectivoRecibido() {
        return efectivoRecibido;
    }

    public void setEfectivoRecibido(BigDecimal efectivoRecibido) {
        this.efectivoRecibido = efectivoRecibido;
    }


    public BigDecimal getTotalFiado() {
        return totalFiado;
    }

    public void setTotalFiado(BigDecimal totalFiado) {
        this.totalFiado = totalFiado;
    }

    public BigDecimal getTotalLiquidado() {
        return totalLiquidado;
    }

    public void setTotalLiquidado(BigDecimal totalLiquidado) {
        this.totalLiquidado = totalLiquidado;
    }


    public Collection<DetalleLiquidacionDelivery> getDetalleLiquidacionDeliveryCollection() {
        return detalleLiquidacionDeliveryCollection;
    }

    public void setDetalleLiquidacionDeliveryCollection(Collection<DetalleLiquidacionDelivery> detalleLiquidacionDeliveryCollection) {
        this.detalleLiquidacionDeliveryCollection = detalleLiquidacionDeliveryCollection;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
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
        if (!(object instanceof LiquidacionDelivery)) {
            return false;
        }
        LiquidacionDelivery other = (LiquidacionDelivery) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.LiquidacionDelivery[ codigo=" + codigo + " ]";
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    // ---- Pagos mixtos, cuenta del motorista y anulación ----
    /** Parte que el motorista entregó por transferencia (no entra a la gaveta). */
    @Column(name = "transferencia_recibida")
    private BigDecimal transferenciaRecibida = BigDecimal.ZERO;
    @Size(max = 100)
    @Column(name = "referencia_transferencia")
    private String referenciaTransferencia;
    /** Parte del faltante que se le cargó como deuda al motorista. */
    @Column(name = "faltante_cargado")
    private BigDecimal faltanteCargado = BigDecimal.ZERO;
    @Column(name = "anulada")
    private Boolean anulada = false;
    @Column(name = "fecha_anulacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAnulacion;
    @Size(max = 300)
    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;
    @Size(max = 100)
    @Column(name = "usuario_anulacion")
    private String usuarioAnulacion;

    public BigDecimal getTransferenciaRecibida() {
        return transferenciaRecibida != null ? transferenciaRecibida : BigDecimal.ZERO;
    }

    public void setTransferenciaRecibida(BigDecimal transferenciaRecibida) {
        this.transferenciaRecibida = transferenciaRecibida;
    }

    public String getReferenciaTransferencia() {
        return referenciaTransferencia;
    }

    public void setReferenciaTransferencia(String referenciaTransferencia) {
        this.referenciaTransferencia = referenciaTransferencia;
    }

    public BigDecimal getFaltanteCargado() {
        return faltanteCargado != null ? faltanteCargado : BigDecimal.ZERO;
    }

    public void setFaltanteCargado(BigDecimal faltanteCargado) {
        this.faltanteCargado = faltanteCargado;
    }

    public boolean isAnulada() {
        return Boolean.TRUE.equals(anulada);
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

    public Date getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(Date fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public String getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(String usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
    }

}
