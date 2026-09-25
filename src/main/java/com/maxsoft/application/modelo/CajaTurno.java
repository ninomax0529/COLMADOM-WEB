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
import jakarta.persistence.Lob;
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
import java.time.LocalDateTime;
import java.util.Collection;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "caja_turno")
@NamedQueries({
    @NamedQuery(name = "CajaTurno.findAll", query = "SELECT c FROM CajaTurno c")})
public class CajaTurno implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_apertura")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaApertura;
    @Column(name = "fecha_cierre")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaCierre;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "monto_apertura")
    private BigDecimal montoApertura;
    @Column(name = "monto_cierre_real")
    private BigDecimal montoCierreReal;
    @Column(name = "ventas_efectivo_esperadas")
    private BigDecimal ventasEfectivoEsperadas;
    @Column(name = "ventas_tarjeta")
    private BigDecimal ventasTarjeta;
    @Column(name = "ventas_fiado")
    private BigDecimal ventasFiado;
    @Column(name = "diferencia")
    private BigDecimal diferencia;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "cajero_apertura")
    private String cajeroApertura;
    @Size(max = 100)
    @Column(name = "cajero_cierre")
    private String cajeroCierre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "estado")
    private String estado;
    @Lob
    @Size(max = 65535)
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cajaTurno")
    private Collection<DesgloseCaja> desgloseCajaCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cajaTurno")
    private Collection<MovimientosCaja> movimientosCajaCollection;

    public CajaTurno() {
    }

    public CajaTurno(Long id) {
        this.id = id;
    }

    public CajaTurno(Long id, LocalDateTime fechaApertura, BigDecimal montoApertura, String cajeroApertura, String estado) {
        this.id = id;
        this.fechaApertura = fechaApertura;
        this.montoApertura = montoApertura;
        this.cajeroApertura = cajeroApertura;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoApertura() {
        return montoApertura;
    }

    public void setMontoApertura(BigDecimal montoApertura) {
        this.montoApertura = montoApertura;
    }

    public BigDecimal getMontoCierreReal() {
        return montoCierreReal;
    }

    public void setMontoCierreReal(BigDecimal montoCierreReal) {
        this.montoCierreReal = montoCierreReal;
    }

    public BigDecimal getVentasEfectivoEsperadas() {
        return ventasEfectivoEsperadas;
    }

    public void setVentasEfectivoEsperadas(BigDecimal ventasEfectivoEsperadas) {
        this.ventasEfectivoEsperadas = ventasEfectivoEsperadas;
    }

    public BigDecimal getVentasTarjeta() {
        return ventasTarjeta;
    }

    public void setVentasTarjeta(BigDecimal ventasTarjeta) {
        this.ventasTarjeta = ventasTarjeta;
    }

    public BigDecimal getVentasFiado() {
        return ventasFiado;
    }

    public void setVentasFiado(BigDecimal ventasFiado) {
        this.ventasFiado = ventasFiado;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getCajeroApertura() {
        return cajeroApertura;
    }

    public void setCajeroApertura(String cajeroApertura) {
        this.cajeroApertura = cajeroApertura;
    }

    public String getCajeroCierre() {
        return cajeroCierre;
    }

    public void setCajeroCierre(String cajeroCierre) {
        this.cajeroCierre = cajeroCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Collection<DesgloseCaja> getDesgloseCajaCollection() {
        return desgloseCajaCollection;
    }

    public void setDesgloseCajaCollection(Collection<DesgloseCaja> desgloseCajaCollection) {
        this.desgloseCajaCollection = desgloseCajaCollection;
    }

    public Collection<MovimientosCaja> getMovimientosCajaCollection() {
        return movimientosCajaCollection;
    }

    public void setMovimientosCajaCollection(Collection<MovimientosCaja> movimientosCajaCollection) {
        this.movimientosCajaCollection = movimientosCajaCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CajaTurno)) {
            return false;
        }
        CajaTurno other = (CajaTurno) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.CajaTurno[ id=" + id + " ]";
    }
    
}
