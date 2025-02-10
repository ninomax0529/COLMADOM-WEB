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
@Table(name = "cemento_empacado_por_silo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CementoEmpacadoPorSilo.findAll", query = "SELECT c FROM CementoEmpacadoPorSilo c")})
public class CementoEmpacadoPorSilo implements Serializable {

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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "empaqueSilo")
    private Collection<DetalleCementoEmpacadoPorSilo> detalleCementoEmpacadoPorSiloCollection;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne
    private Usuariop usuario;

    public CementoEmpacadoPorSilo() {
    }

    public CementoEmpacadoPorSilo(Integer codigo) {
        this.codigo = codigo;
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

    @XmlTransient
    public Collection<DetalleCementoEmpacadoPorSilo> getDetalleCementoEmpacadoPorSiloCollection() {
        return detalleCementoEmpacadoPorSiloCollection;
    }

    public void setDetalleCementoEmpacadoPorSiloCollection(Collection<DetalleCementoEmpacadoPorSilo> detalleCementoEmpacadoPorSiloCollection) {
        this.detalleCementoEmpacadoPorSiloCollection = detalleCementoEmpacadoPorSiloCollection;
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
        if (!(object instanceof CementoEmpacadoPorSilo)) {
            return false;
        }
        CementoEmpacadoPorSilo other = (CementoEmpacadoPorSilo) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.CementoEmpacadoPorSilo[ codigo=" + codigo + " ]";
    }
    
}
