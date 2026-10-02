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
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "ajuste_inventario")
@NamedQueries({
    @NamedQuery(name = "AjusteInventario.findAll", query = "SELECT a FROM AjusteInventario a")})
public class AjusteInventario implements Serializable {

    /**
     * @return the almacen
     */
    public Almacen getAlmacen() {
        return almacen;
    }

    /**
     * @param almacen the almacen to set
     */
    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    /**
     * @return the nombreAlmacen
     */
    public String getNombreAlmacen() {
        return nombreAlmacen;
    }

    /**
     * @param nombreAlmacen the nombreAlmacen to set
     */
    public void setNombreAlmacen(String nombreAlmacen) {
        this.nombreAlmacen = nombreAlmacen;
    }

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
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 65535)
    @Column(name = "observacion")
    private String observacion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "anulado")
    private boolean anulado;
    @Size(max = 80)
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Column(name = "fecha_anulado")
    @Temporal(TemporalType.DATE)
    private Date fechaAnulado;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ajusteInventario")
    private Collection<DetalleAjusteInventario> detalleAjusteInventarioCollection;
    @JoinColumn(name = "tipo_ajuste", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private TipoAjuste tipoAjuste;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Usuario usuario;
    @Size(max = 255)
    @Column(name = "nombre_almacen")
    private String nombreAlmacen;
    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacen;

    public AjusteInventario() {
    }

    public AjusteInventario(Integer codigo) {
        this.codigo = codigo;
    }

    public AjusteInventario(Integer codigo, Date fecha, Date fechaRegistro, String observacion, boolean anulado) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.fechaRegistro = fechaRegistro;
        this.observacion = observacion;
        this.anulado = anulado;
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(boolean anulado) {
        this.anulado = anulado;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Date getFechaAnulado() {
        return fechaAnulado;
    }

    public void setFechaAnulado(Date fechaAnulado) {
        this.fechaAnulado = fechaAnulado;
    }

    public Collection<DetalleAjusteInventario> getDetalleAjusteInventarioCollection() {
        return detalleAjusteInventarioCollection;
    }

    public void setDetalleAjusteInventarioCollection(Collection<DetalleAjusteInventario> detalleAjusteInventarioCollection) {
        this.detalleAjusteInventarioCollection = detalleAjusteInventarioCollection;
    }

    public TipoAjuste getTipoAjuste() {
        return tipoAjuste;
    }

    public void setTipoAjuste(TipoAjuste tipoAjuste) {
        this.tipoAjuste = tipoAjuste;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
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
        if (!(object instanceof AjusteInventario)) {
            return false;
        }
        AjusteInventario other = (AjusteInventario) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.AjusteInventario[ codigo=" + codigo + " ]";
    }

}
