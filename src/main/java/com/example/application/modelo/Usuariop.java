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
import jakarta.validation.constraints.Size;
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
@Table(name = "usuariop")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Usuariop.findAll", query = "SELECT u FROM Usuariop u")})
public class Usuariop implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 55)
    @Column(name = "nombre")
    private String nombre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 55)
    @Column(name = "apellidos")
    private String apellidos;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 15)
    @Column(name = "usuario")
    private String usuario;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "password")
    private String password;
    @Basic(optional = false)
    @NotNull
    @Column(name = "habilitado")
    private boolean habilitado;
    @Basic(optional = false)
    @NotNull
    @Column(name = "primer_login")
    private boolean primerLogin;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ultima_session")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ultimaSession;
    @JoinColumn(name = "rol", referencedColumnName = "codigo")
    @ManyToOne
    private Rolp rol;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuario")
    private Collection<OperacionEmpacadora> operacionEmpacadoraCollection;
    @OneToMany(mappedBy = "usuario")
    private Collection<CementoEmpacadoPorSilo> cementoEmpacadoPorSiloCollection;

    public Usuariop() {
    }

    public Usuariop(Integer codigo) {
        this.codigo = codigo;
    }

    public Usuariop(Integer codigo, String nombre, String apellidos, String usuario, String password, boolean habilitado, boolean primerLogin, Date ultimaSession) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.usuario = usuario;
        this.password = password;
        this.habilitado = habilitado;
        this.primerLogin = primerLogin;
        this.ultimaSession = ultimaSession;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public boolean getPrimerLogin() {
        return primerLogin;
    }

    public void setPrimerLogin(boolean primerLogin) {
        this.primerLogin = primerLogin;
    }

    public Date getUltimaSession() {
        return ultimaSession;
    }

    public void setUltimaSession(Date ultimaSession) {
        this.ultimaSession = ultimaSession;
    }

    public Rolp getRol() {
        return rol;
    }

    public void setRol(Rolp rol) {
        this.rol = rol;
    }

    @XmlTransient
    public Collection<OperacionEmpacadora> getOperacionEmpacadoraCollection() {
        return operacionEmpacadoraCollection;
    }

    public void setOperacionEmpacadoraCollection(Collection<OperacionEmpacadora> operacionEmpacadoraCollection) {
        this.operacionEmpacadoraCollection = operacionEmpacadoraCollection;
    }

    @XmlTransient
    public Collection<CementoEmpacadoPorSilo> getCementoEmpacadoPorSiloCollection() {
        return cementoEmpacadoPorSiloCollection;
    }

    public void setCementoEmpacadoPorSiloCollection(Collection<CementoEmpacadoPorSilo> cementoEmpacadoPorSiloCollection) {
        this.cementoEmpacadoPorSiloCollection = cementoEmpacadoPorSiloCollection;
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
        if (!(object instanceof Usuariop)) {
            return false;
        }
        Usuariop other = (Usuariop) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.Usuariop[ codigo=" + codigo + " ]";
    }
    
}
