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
@Table(name = "almacen")
@NamedQueries({
    @NamedQuery(name = "Almacen.findAll", query = "SELECT a FROM Almacen a")})
public class Almacen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Size(max = 50)
    @Column(name = "nombre")
    private String nombre;
    @Size(max = 120)
    @Column(name = "ubicacion")
    private String ubicacion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "creado_por")
    private String creadoPor;
    @OneToMany(mappedBy = "almacen")
    private Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "almacenDestino")
    private Collection<TrasladoInventario> trasladoInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "almacenOrigen")
    private Collection<TrasladoInventario> trasladoInventarioCollection1;
    @OneToMany(mappedBy = "almacen")
    private Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection;
    @OneToMany(mappedBy = "almacenEntradaId")
    private Collection<Configuracion> configuracionCollection;
    @OneToMany(mappedBy = "almacenVentaId")
    private Collection<Configuracion> configuracionCollection1;
    @OneToMany(mappedBy = "almacen")
    private Collection<DetalleAjusteInventario> detalleAjusteInventarioCollection;
    @OneToMany(mappedBy = "almacen")
    private Collection<DetalleRecepcionMercancia> detalleRecepcionMercanciaCollection;
    @OneToMany(mappedBy = "almacen")
    private Collection<AjusteInventario> ajusteInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "almacen")
    private Collection<MovimientoInventario> movimientoInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "almacen")
    private Collection<ArticuloAlmacen> articuloAlmacenCollection;
    @OneToMany(mappedBy = "almacen")
    private Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection;
    @OneToMany(mappedBy = "almacen")
    private Collection<RecepcionMercancia> recepcionMercanciaCollection;

    public Almacen() {
    }

    public Almacen(Integer codigo) {
        this.codigo = codigo;
    }

    public Almacen(Integer codigo, Date fechaCreacion, String creadoPor) {
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
        this.creadoPor = creadoPor;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public Collection<DetalleEntradaInventario> getDetalleEntradaInventarioCollection() {
        return detalleEntradaInventarioCollection;
    }

    public void setDetalleEntradaInventarioCollection(Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection) {
        this.detalleEntradaInventarioCollection = detalleEntradaInventarioCollection;
    }

    public Collection<TrasladoInventario> getTrasladoInventarioCollection() {
        return trasladoInventarioCollection;
    }

    public void setTrasladoInventarioCollection(Collection<TrasladoInventario> trasladoInventarioCollection) {
        this.trasladoInventarioCollection = trasladoInventarioCollection;
    }

    public Collection<TrasladoInventario> getTrasladoInventarioCollection1() {
        return trasladoInventarioCollection1;
    }

    public void setTrasladoInventarioCollection1(Collection<TrasladoInventario> trasladoInventarioCollection1) {
        this.trasladoInventarioCollection1 = trasladoInventarioCollection1;
    }

    public Collection<DetalleFacturaDeVenta> getDetalleFacturaDeVentaCollection() {
        return detalleFacturaDeVentaCollection;
    }

    public void setDetalleFacturaDeVentaCollection(Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection) {
        this.detalleFacturaDeVentaCollection = detalleFacturaDeVentaCollection;
    }

    public Collection<Configuracion> getConfiguracionCollection() {
        return configuracionCollection;
    }

    public void setConfiguracionCollection(Collection<Configuracion> configuracionCollection) {
        this.configuracionCollection = configuracionCollection;
    }

    public Collection<Configuracion> getConfiguracionCollection1() {
        return configuracionCollection1;
    }

    public void setConfiguracionCollection1(Collection<Configuracion> configuracionCollection1) {
        this.configuracionCollection1 = configuracionCollection1;
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

    public Collection<AjusteInventario> getAjusteInventarioCollection() {
        return ajusteInventarioCollection;
    }

    public void setAjusteInventarioCollection(Collection<AjusteInventario> ajusteInventarioCollection) {
        this.ajusteInventarioCollection = ajusteInventarioCollection;
    }

    public Collection<MovimientoInventario> getMovimientoInventarioCollection() {
        return movimientoInventarioCollection;
    }

    public void setMovimientoInventarioCollection(Collection<MovimientoInventario> movimientoInventarioCollection) {
        this.movimientoInventarioCollection = movimientoInventarioCollection;
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

    public Collection<RecepcionMercancia> getRecepcionMercanciaCollection() {
        return recepcionMercanciaCollection;
    }

    public void setRecepcionMercanciaCollection(Collection<RecepcionMercancia> recepcionMercanciaCollection) {
        this.recepcionMercanciaCollection = recepcionMercanciaCollection;
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
        if (!(object instanceof Almacen)) {
            return false;
        }
        Almacen other = (Almacen) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return nombre;
    }
    
}
