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
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "unidad")
@NamedQueries({
    @NamedQuery(name = "Unidad.findAll", query = "SELECT u FROM Unidad u")})
public class Unidad implements Serializable {

    @Size(max = 50)
    @Column(name = "descripcion")
    private String descripcion;
    @Size(max = 6)
    @Column(name = "abreviatura")
    private String abreviatura;
    @Size(max = 50)
    @Column(name = "creada_por")
    private String creadaPor;
    @Basic(optional = false)
    @NotNull
    @Column(name = "habilitada")
    private boolean habilitada;
    @OneToMany(mappedBy = "unidadBase")
    private Collection<Articulo> articuloCollection;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @OneToMany(mappedBy = "unidad")
    private Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection;
    @OneToMany(mappedBy = "unidad")
    private Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection;
    @OneToMany(mappedBy = "unidad")
    private Collection<DetalleTrasladoInventario> detalleTrasladoInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "unidad")
    private Collection<DetalleAjusteInventario> detalleAjusteInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "unidad")
    private Collection<DetalleRecepcionMercancia> detalleRecepcionMercanciaCollection;
    @OneToMany(mappedBy = "unidad")
    private Collection<ArticuloAlmacen> articuloAlmacenCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "unidad")
    private Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "unidadEmpaque")
    private Collection<ArticuloEmpaque> articuloEmpaqueCollection;

    public Unidad() {
    }

    public Unidad(Integer codigo) {
        this.codigo = codigo;
    }

    public Unidad(Integer codigo, boolean habilitada) {
        this.codigo = codigo;
        this.habilitada = habilitada;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }


    public String getCreadaPor() {
        return creadaPor;
    }

    public void setCreadaPor(String creadaPor) {
        this.creadaPor = creadaPor;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    public Collection<DetalleEntradaInventario> getDetalleEntradaInventarioCollection() {
        return detalleEntradaInventarioCollection;
    }

    public void setDetalleEntradaInventarioCollection(Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection) {
        this.detalleEntradaInventarioCollection = detalleEntradaInventarioCollection;
    }

    public Collection<DetalleFacturaDeVenta> getDetalleFacturaDeVentaCollection() {
        return detalleFacturaDeVentaCollection;
    }

    public void setDetalleFacturaDeVentaCollection(Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection) {
        this.detalleFacturaDeVentaCollection = detalleFacturaDeVentaCollection;
    }

    public Collection<DetalleTrasladoInventario> getDetalleTrasladoInventarioCollection() {
        return detalleTrasladoInventarioCollection;
    }

    public void setDetalleTrasladoInventarioCollection(Collection<DetalleTrasladoInventario> detalleTrasladoInventarioCollection) {
        this.detalleTrasladoInventarioCollection = detalleTrasladoInventarioCollection;
    }

    public Collection<DetalleAjusteInventario> getDetalleAjusteInventarioCollection() {
        return detalleAjusteInventarioCollection;
    }

    public void setDetalleAjusteInventarioCollection(Collection<DetalleAjusteInventario> detalleAjusteInventarioCollection) {
        this.detalleAjusteInventarioCollection = detalleAjusteInventarioCollection;
    }

    public Collection<DetalleRecepcionMercancia> getDetalleRecepcionMercanciaCollection() {
        return detalleRecepcionMercanciaCollection;
    }

    public void setDetalleRecepcionMercanciaCollection(Collection<DetalleRecepcionMercancia> detalleRecepcionMercanciaCollection) {
        this.detalleRecepcionMercanciaCollection = detalleRecepcionMercanciaCollection;
    }

    public Collection<ArticuloAlmacen> getArticuloAlmacenCollection() {
        return articuloAlmacenCollection;
    }

    public void setArticuloAlmacenCollection(Collection<ArticuloAlmacen> articuloAlmacenCollection) {
        this.articuloAlmacenCollection = articuloAlmacenCollection;
    }

    public Collection<DetalleSalidaInventario> getDetalleSalidaInventarioCollection() {
        return detalleSalidaInventarioCollection;
    }

    public void setDetalleSalidaInventarioCollection(Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection) {
        this.detalleSalidaInventarioCollection = detalleSalidaInventarioCollection;
    }

    public Collection<ArticuloEmpaque> getArticuloEmpaqueCollection() {
        return articuloEmpaqueCollection;
    }

    public void setArticuloEmpaqueCollection(Collection<ArticuloEmpaque> articuloEmpaqueCollection) {
        this.articuloEmpaqueCollection = articuloEmpaqueCollection;
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
        if (!(object instanceof Unidad)) {
            return false;
        }
        Unidad other = (Unidad) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public boolean getHabilitada() {
        return habilitada;
    }

    public void setHabilitada(boolean habilitada) {
        this.habilitada = habilitada;
    }

    public Collection<Articulo> getArticuloCollection() {
        return articuloCollection;
    }

    public void setArticuloCollection(Collection<Articulo> articuloCollection) {
        this.articuloCollection = articuloCollection;
    }
    
}
