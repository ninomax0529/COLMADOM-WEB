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
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "traslado_inventario")
@NamedQueries({
    @NamedQuery(name = "TrasladoInventario.findAll", query = "SELECT t FROM TrasladoInventario t")})
public class TrasladoInventario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "numero_documento")
    private String numeroDocumento;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_emision")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEmision;
    @Column(name = "fecha_recepcion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRecepcion;
    @Size(max = 255)
    @Column(name = "observacion")
    private String observacion;
    @JoinColumn(name = "almacen_destino", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacenDestino;
    @JoinColumn(name = "almacen_origen", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacenOrigen;
    @JoinColumn(name = "estado", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private EstadoDocumento estado;
    @JoinColumn(name = "usuario_envia", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Usuario usuarioEnvia;
    @JoinColumn(name = "usuario_recibe", referencedColumnName = "codigo")
    @ManyToOne
    private Usuario usuarioRecibe;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "traslado")
    private Collection<DetalleTrasladoInventario> detalleTrasladoInventarioCollection;

    public TrasladoInventario() {
    }

    public TrasladoInventario(Integer codigo) {
        this.codigo = codigo;
    }

    public TrasladoInventario(Integer codigo, String numeroDocumento, Date fechaEmision) {
        this.codigo = codigo;
        this.numeroDocumento = numeroDocumento;
        this.fechaEmision = fechaEmision;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Date getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(Date fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Almacen getAlmacenDestino() {
        return almacenDestino;
    }

    public void setAlmacenDestino(Almacen almacenDestino) {
        this.almacenDestino = almacenDestino;
    }

    public Almacen getAlmacenOrigen() {
        return almacenOrigen;
    }

    public void setAlmacenOrigen(Almacen almacenOrigen) {
        this.almacenOrigen = almacenOrigen;
    }

    public EstadoDocumento getEstado() {
        return estado;
    }

    public void setEstado(EstadoDocumento estado) {
        this.estado = estado;
    }

    public Usuario getUsuarioEnvia() {
        return usuarioEnvia;
    }

    public void setUsuarioEnvia(Usuario usuarioEnvia) {
        this.usuarioEnvia = usuarioEnvia;
    }

    public Usuario getUsuarioRecibe() {
        return usuarioRecibe;
    }

    public void setUsuarioRecibe(Usuario usuarioRecibe) {
        this.usuarioRecibe = usuarioRecibe;
    }

    public Collection<DetalleTrasladoInventario> getDetalleTrasladoInventarioCollection() {
        return detalleTrasladoInventarioCollection;
    }

    public void setDetalleTrasladoInventarioCollection(Collection<DetalleTrasladoInventario> detalleTrasladoInventarioCollection) {
        this.detalleTrasladoInventarioCollection = detalleTrasladoInventarioCollection;
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
        if (!(object instanceof TrasladoInventario)) {
            return false;
        }
        TrasladoInventario other = (TrasladoInventario) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.TrasladoInventario[ codigo=" + codigo + " ]";
    }
    
}
