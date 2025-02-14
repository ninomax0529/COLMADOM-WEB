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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "tipo_de_funda")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TipoDeFunda.findAll", query = "SELECT t FROM TipoDeFunda t")})
public class TipoDeFunda implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "descripcion")
    private String descripcion;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tipoDeFunda")
    private Collection<DetalleCementoDejadoEnPiso> detalleCementoDejadoEnPisoCollection;

    public TipoDeFunda() {
    }

    public TipoDeFunda(Integer codigo) {
        this.codigo = codigo;
    }

    public TipoDeFunda(Integer codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @XmlTransient
    public Collection<DetalleCementoDejadoEnPiso> getDetalleCementoDejadoEnPisoCollection() {
        return detalleCementoDejadoEnPisoCollection;
    }

    public void setDetalleCementoDejadoEnPisoCollection(Collection<DetalleCementoDejadoEnPiso> detalleCementoDejadoEnPisoCollection) {
        this.detalleCementoDejadoEnPisoCollection = detalleCementoDejadoEnPisoCollection;
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
        if (!(object instanceof TipoDeFunda)) {
            return false;
        }
        TipoDeFunda other = (TipoDeFunda) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.TipoDeFunda[ codigo=" + codigo + " ]";
    }
    
}
