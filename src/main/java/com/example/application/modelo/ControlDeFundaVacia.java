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
@Table(name = "control_de_funda_vacia")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ControlDeFundaVacia.findAll", query = "SELECT c FROM ControlDeFundaVacia c")})
public class ControlDeFundaVacia implements Serializable {

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
    @Column(name = "inventario_inicial")
    private int inventarioInicial;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_final")
    private int inventarioFinal;
    @Basic(optional = false)
    @NotNull
    @Column(name = "empacada")
    private int empacada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "recibida")
    private int recibida;
    @Basic(optional = false)
    @NotNull
    @Column(name = "funda_rota")
    private int fundaRota;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_fisico")
    private int inventarioFisico;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
    @Basic(optional = false)
    @NotNull
    @Column(name = "supervisor")
    private int supervisor;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "nombre_supervisor")
    private String nombreSupervisor;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "controlFundaVacia")
    private Collection<DetalleControlDeFundaVacia> detalleControlDeFundaVaciaCollection;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Usuariop usuario;

    public ControlDeFundaVacia() {
    }

    public ControlDeFundaVacia(Integer codigo) {
        this.codigo = codigo;
    }

    public ControlDeFundaVacia(Integer codigo, Date fecha, int inventarioInicial, int inventarioFinal, int empacada, int recibida, int fundaRota, int inventarioFisico, Date fechaRegistro, int supervisor, String nombreSupervisor) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.inventarioInicial = inventarioInicial;
        this.inventarioFinal = inventarioFinal;
        this.empacada = empacada;
        this.recibida = recibida;
        this.fundaRota = fundaRota;
        this.inventarioFisico = inventarioFisico;
        this.fechaRegistro = fechaRegistro;
        this.supervisor = supervisor;
        this.nombreSupervisor = nombreSupervisor;
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

    public int getInventarioInicial() {
        return inventarioInicial;
    }

    public void setInventarioInicial(int inventarioInicial) {
        this.inventarioInicial = inventarioInicial;
    }

    public int getInventarioFinal() {
        return inventarioFinal;
    }

    public void setInventarioFinal(int inventarioFinal) {
        this.inventarioFinal = inventarioFinal;
    }

    public int getEmpacada() {
        return empacada;
    }

    public void setEmpacada(int empacada) {
        this.empacada = empacada;
    }

    public int getRecibida() {
        return recibida;
    }

    public void setRecibida(int recibida) {
        this.recibida = recibida;
    }

    public int getFundaRota() {
        return fundaRota;
    }

    public void setFundaRota(int fundaRota) {
        this.fundaRota = fundaRota;
    }

    public int getInventarioFisico() {
        return inventarioFisico;
    }

    public void setInventarioFisico(int inventarioFisico) {
        this.inventarioFisico = inventarioFisico;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(int supervisor) {
        this.supervisor = supervisor;
    }

    public String getNombreSupervisor() {
        return nombreSupervisor;
    }

    public void setNombreSupervisor(String nombreSupervisor) {
        this.nombreSupervisor = nombreSupervisor;
    }

    @XmlTransient
    public Collection<DetalleControlDeFundaVacia> getDetalleControlDeFundaVaciaCollection() {
        return detalleControlDeFundaVaciaCollection;
    }

    public void setDetalleControlDeFundaVaciaCollection(Collection<DetalleControlDeFundaVacia> detalleControlDeFundaVaciaCollection) {
        this.detalleControlDeFundaVaciaCollection = detalleControlDeFundaVaciaCollection;
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
        if (!(object instanceof ControlDeFundaVacia)) {
            return false;
        }
        ControlDeFundaVacia other = (ControlDeFundaVacia) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.ControlDeFundaVacia[ codigo=" + codigo + " ]";
    }
    
}
