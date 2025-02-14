/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.modelo;

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
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "resumen_despacho")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ResumenDespacho.findAll", query = "SELECT r FROM ResumenDespacho r")})
public class ResumenDespacho implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resumenDespacho")
    private Collection<DetalleResumenDespacho> detalleResumenDespachoCollection;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Usuariop usuario;

    public ResumenDespacho() {
    }

    public ResumenDespacho(Integer codigo) {
        this.codigo = codigo;
    }

    public ResumenDespacho(Integer codigo, Date fecha, Date fechaRegistro) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.fechaRegistro = fechaRegistro;
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

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @XmlTransient
    public Collection<DetalleResumenDespacho> getDetalleResumenDespachoCollection() {
        return detalleResumenDespachoCollection;
    }

    public void setDetalleResumenDespachoCollection(Collection<DetalleResumenDespacho> detalleResumenDespachoCollection) {
        this.detalleResumenDespachoCollection = detalleResumenDespachoCollection;
    }

    public Usuariop getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuariop usuario) {
        this.usuario = usuario;
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
        if (!(object instanceof ResumenDespacho)) {
            return false;
        }
        ResumenDespacho other = (ResumenDespacho) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.ResumenDespacho[ codigo=" + codigo + " ]";
    }
    
}
