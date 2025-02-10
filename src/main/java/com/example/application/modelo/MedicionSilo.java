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
@Table(name = "medicion_silo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MedicionSilo.findAll", query = "SELECT m FROM MedicionSilo m")})
public class MedicionSilo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "hora_medicion")
    @Temporal(TemporalType.TIME)
    private Date horaMedicion;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Column(name = "usuario")
    private Integer usuario;
    @Basic(optional = false)
    @NotNull
    @Column(name = "turno")
    private int turno;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "medicionSilo")
    private Collection<DetalleMedicionSilo> detalleMedicionSiloCollection;

    public MedicionSilo() {
    }

    public MedicionSilo(Integer codigo) {
        this.codigo = codigo;
    }

    public MedicionSilo(Integer codigo, int turno) {
        this.codigo = codigo;
        this.turno = turno;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Date getHoraMedicion() {
        return horaMedicion;
    }

    public void setHoraMedicion(Date horaMedicion) {
        this.horaMedicion = horaMedicion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getUsuario() {
        return usuario;
    }

    public void setUsuario(Integer usuario) {
        this.usuario = usuario;
    }

    public int getTurno() {
        return turno;
    }

    public void setTurno(int turno) {
        this.turno = turno;
    }

    @XmlTransient
    public Collection<DetalleMedicionSilo> getDetalleMedicionSiloCollection() {
        return detalleMedicionSiloCollection;
    }

    public void setDetalleMedicionSiloCollection(Collection<DetalleMedicionSilo> detalleMedicionSiloCollection) {
        this.detalleMedicionSiloCollection = detalleMedicionSiloCollection;
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
        if (!(object instanceof MedicionSilo)) {
            return false;
        }
        MedicionSilo other = (MedicionSilo) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.MedicionSilo[ codigo=" + codigo + " ]";
    }
    
}
