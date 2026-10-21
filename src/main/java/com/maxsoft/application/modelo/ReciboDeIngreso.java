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
import jakarta.persistence.Lob;
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
@Table(name = "recibo_de_ingreso")
@NamedQueries({
    @NamedQuery(name = "ReciboDeIngreso.findAll", query = "SELECT r FROM ReciboDeIngreso r")})
public class ReciboDeIngreso implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Size(max = 85)
    @Column(name = "nombre_cliente")
    private String nombreCliente;
    @Lob
    @Size(max = 65535)
    @Column(name = "concepto")
    private String concepto;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "total_pagado")
    private BigDecimal totalPagado;
    @Column(name = "balance_pendiente_antes")
    private BigDecimal balancePendienteAntes;
    @Column(name = "balance_pendiente_despues")
    private BigDecimal balancePendienteDespues;
    @Column(name = "anulado")
    private Boolean anulado;
    @Column(name = "caja_chica")
    private Integer cajaChica;
    @Column(name = "numero")
    private Integer numero;
    @Basic(optional = false)
    @NotNull
    @Column(name = "imprimir")
    private boolean imprimir;
    @Size(max = 85)
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @JoinColumn(name = "cliente", referencedColumnName = "codigo")
    @ManyToOne
    private Cliente cliente;
    @JoinColumn(name = "secuencia_documento", referencedColumnName = "codigo")
    @ManyToOne
    private SecuenciaDocumento secuenciaDocumento;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne
    private Usuario usuario;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reciboIngreso" )
    private Collection<DetalleReciboDeIngreso> detalleReciboDeIngresoCollection;

    public ReciboDeIngreso() {
    }

    public ReciboDeIngreso(Integer codigo) {
        this.codigo = codigo;
    }

    public ReciboDeIngreso(Integer codigo, boolean imprimir) {
        this.codigo = codigo;
        this.imprimir = imprimir;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public BigDecimal getBalancePendienteAntes() {
        return balancePendienteAntes;
    }

    public void setBalancePendienteAntes(BigDecimal balancePendienteAntes) {
        this.balancePendienteAntes = balancePendienteAntes;
    }

    public BigDecimal getBalancePendienteDespues() {
        return balancePendienteDespues;
    }

    public void setBalancePendienteDespues(BigDecimal balancePendienteDespues) {
        this.balancePendienteDespues = balancePendienteDespues;
    }

    public Boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(Boolean anulado) {
        this.anulado = anulado;
    }

    public Integer getCajaChica() {
        return cajaChica;
    }

    public void setCajaChica(Integer cajaChica) {
        this.cajaChica = cajaChica;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public boolean getImprimir() {
        return imprimir;
    }

    public void setImprimir(boolean imprimir) {
        this.imprimir = imprimir;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public SecuenciaDocumento getSecuenciaDocumento() {
        return secuenciaDocumento;
    }

    public void setSecuenciaDocumento(SecuenciaDocumento secuenciaDocumento) {
        this.secuenciaDocumento = secuenciaDocumento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Collection<DetalleReciboDeIngreso> getDetalleReciboDeIngresoCollection() {
        return detalleReciboDeIngresoCollection;
    }

    public void setDetalleReciboDeIngresoCollection(Collection<DetalleReciboDeIngreso> detalleReciboDeIngresoCollection) {
        this.detalleReciboDeIngresoCollection = detalleReciboDeIngresoCollection;
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
        if (!(object instanceof ReciboDeIngreso)) {
            return false;
        }
        ReciboDeIngreso other = (ReciboDeIngreso) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.ReciboDeIngreso[ codigo=" + codigo + " ]";
    }
    
}
