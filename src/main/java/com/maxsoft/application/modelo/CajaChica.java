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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author maximilianoalmonte
 */
@Entity
@Table(name = "caja_chica")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CajaChica.findAll", query = "SELECT c FROM CajaChica c")})
public class CajaChica implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "punto_de_venta")
    private String puntoDeVenta;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_apertura")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaApertura;
    @Basic(optional = false)
    @NotNull
    @Column(name = "monto_apertura")
    private double montoApertura;
    @Column(name = "fecha_cierre")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCierre;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "monto_cierre")
    private Double montoCierre;
    @Column(name = "monto_cierre_esperado")
    private Double montoCierreEsperado;
    @Column(name = "monto_cierre_real")
    private Double montoCierreReal;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "usuario_apertura")
    private String usuarioApertura;
    @Size(max = 200)
    @Column(name = "usuario_cierre")
    private String usuarioCierre;
    @Basic(optional = false)
    @NotNull
    @Column(name = "abierta")
    private boolean abierta;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cajaChica")
    private Collection<MovimientoCajaChica> movimientoCajaChicaCollection;

    public CajaChica() {
    }

    public CajaChica(Integer codigo) {
        this.codigo = codigo;
    }

    public CajaChica(Integer codigo, String puntoDeVenta, Date fechaApertura, double montoApertura, String usuarioApertura, boolean abierta) {
        this.codigo = codigo;
        this.puntoDeVenta = puntoDeVenta;
        this.fechaApertura = fechaApertura;
        this.montoApertura = montoApertura;
        this.usuarioApertura = usuarioApertura;
        this.abierta = abierta;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getPuntoDeVenta() {
        return puntoDeVenta;
    }

    public void setPuntoDeVenta(String puntoDeVenta) {
        this.puntoDeVenta = puntoDeVenta;
    }

    public Date getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Date fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public double getMontoApertura() {
        return montoApertura;
    }

    public void setMontoApertura(double montoApertura) {
        this.montoApertura = montoApertura;
    }

    public Date getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Date fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Double getMontoCierre() {
        return montoCierre;
    }

    public void setMontoCierre(Double montoCierre) {
        this.montoCierre = montoCierre;
    }

    public Double getMontoCierreEsperado() {
        return montoCierreEsperado;
    }

    public void setMontoCierreEsperado(Double montoCierreEsperado) {
        this.montoCierreEsperado = montoCierreEsperado;
    }

    public Double getMontoCierreReal() {
        return montoCierreReal;
    }

    public void setMontoCierreReal(Double montoCierreReal) {
        this.montoCierreReal = montoCierreReal;
    }

    public String getUsuarioApertura() {
        return usuarioApertura;
    }

    public void setUsuarioApertura(String usuarioApertura) {
        this.usuarioApertura = usuarioApertura;
    }

    public String getUsuarioCierre() {
        return usuarioCierre;
    }

    public void setUsuarioCierre(String usuarioCierre) {
        this.usuarioCierre = usuarioCierre;
    }

    public boolean getAbierta() {
        return abierta;
    }

    public void setAbierta(boolean abierta) {
        this.abierta = abierta;
    }

    @XmlTransient
    public Collection<MovimientoCajaChica> getMovimientoCajaChicaCollection() {
        return movimientoCajaChicaCollection;
    }

    public void setMovimientoCajaChicaCollection(Collection<MovimientoCajaChica> movimientoCajaChicaCollection) {
        this.movimientoCajaChicaCollection = movimientoCajaChicaCollection;
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
        if (!(object instanceof CajaChica)) {
            return false;
        }
        CajaChica other = (CajaChica) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.CajaChica[ codigo=" + codigo + " ]";
    }
    
}
