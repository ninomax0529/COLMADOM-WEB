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
@Table(name = "merma")
@NamedQueries({
    @NamedQuery(name = "Merma.findAll", query = "SELECT m FROM Merma m")})
public class Merma implements Serializable {

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
    @Size(max = 500)
    @Column(name = "observacion")
    private String observacion;
    @Size(max = 100)
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "merma")
    private Collection<DetalleMerma> detalleMermaCollection;
    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacen;
    @JoinColumn(name = "salida_inventario", referencedColumnName = "codigo")
    @ManyToOne
    private SalidaInventario salidaInventario;
    @JoinColumn(name = "tipo_merma", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private TipoMerma tipoMerma;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne
    private Usuario usuario;

    public Merma() {
    }

    public Merma(Integer codigo) {
        this.codigo = codigo;
    }

    public Merma(Integer codigo, Date fecha, Date fechaRegistro) {
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Collection<DetalleMerma> getDetalleMermaCollection() {
        return detalleMermaCollection;
    }

    public void setDetalleMermaCollection(Collection<DetalleMerma> detalleMermaCollection) {
        this.detalleMermaCollection = detalleMermaCollection;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public SalidaInventario getSalidaInventario() {
        return salidaInventario;
    }

    public void setSalidaInventario(SalidaInventario salidaInventario) {
        this.salidaInventario = salidaInventario;
    }

    public TipoMerma getTipoMerma() {
        return tipoMerma;
    }

    public void setTipoMerma(TipoMerma tipoMerma) {
        this.tipoMerma = tipoMerma;
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
        if (!(object instanceof Merma)) {
            return false;
        }
        Merma other = (Merma) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.Merma[ codigo=" + codigo + " ]";
    }
    
}
