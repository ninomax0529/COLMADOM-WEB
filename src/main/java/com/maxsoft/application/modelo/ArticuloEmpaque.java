/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.Basic;
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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "articulo_empaque")
@NamedQueries({
    @NamedQuery(name = "ArticuloEmpaque.findAll", query = "SELECT a FROM ArticuloEmpaque a")})
public class ArticuloEmpaque implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Size(max = 60)
    @Column(name = "nombre_articulo")
    private String nombreArticulo;
    @Size(max = 25)
    @Column(name = "nombre_empaque")
    private String nombreEmpaque;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "factor_conversion")
    private BigDecimal factorConversion;
    @Size(max = 50)
    @Column(name = "codigo_barra")
    private String codigoBarra;
    @Column(name = "precio_compra")
    private BigDecimal precioCompra;
    @Column(name = "precio_venta")
    private BigDecimal precioVenta;
    @Column(name = "se_compra_en")
    private Boolean seCompraEn;
    @Column(name = "se_vende_en")
    private Boolean seVendeEn;
    @Column(name = "es_empaque_base")
    private Boolean esEmpaqueBase;
    @Column(name = "estado")
    private Boolean estado;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Size(max = 50)
    @Column(name = "creado_por")
    private String creadoPor;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<DetalleTrasladoInventario> detalleTrasladoInventarioCollection;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<DetalleAjusteInventario> detalleAjusteInventarioCollection;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<DetalleRecepcionMercancia> detalleRecepcionMercanciaCollection;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<MovimientoInventario> movimientoInventarioCollection;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<DetalleOrdendeDeCompra> detalleOrdendeDeCompraCollection;
    @OneToMany(mappedBy = "articuloEmpaque")
    private Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
    @JoinColumn(name = "unidad_empaque", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Unidad unidadEmpaque;

    public ArticuloEmpaque() {
    }

    public ArticuloEmpaque(Integer codigo) {
        this.codigo = codigo;
    }

    public ArticuloEmpaque(Integer codigo, BigDecimal factorConversion) {
        this.codigo = codigo;
        this.factorConversion = factorConversion;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
    }

    public String getNombreEmpaque() {
        return nombreEmpaque;
    }

    public void setNombreEmpaque(String nombreEmpaque) {
        this.nombreEmpaque = nombreEmpaque;
    }

    public BigDecimal getFactorConversion() {
        return factorConversion;
    }

    public void setFactorConversion(BigDecimal factorConversion) {
        this.factorConversion = factorConversion;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Boolean getSeCompraEn() {
        return seCompraEn;
    }

    public void setSeCompraEn(Boolean seCompraEn) {
        this.seCompraEn = seCompraEn;
    }

    public Boolean getSeVendeEn() {
        return seVendeEn;
    }

    public void setSeVendeEn(Boolean seVendeEn) {
        this.seVendeEn = seVendeEn;
    }

    public Boolean getEsEmpaqueBase() {
        return esEmpaqueBase;
    }

    public void setEsEmpaqueBase(Boolean esEmpaqueBase) {
        this.esEmpaqueBase = esEmpaqueBase;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
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

    public Collection<MovimientoInventario> getMovimientoInventarioCollection() {
        return movimientoInventarioCollection;
    }

    public void setMovimientoInventarioCollection(Collection<MovimientoInventario> movimientoInventarioCollection) {
        this.movimientoInventarioCollection = movimientoInventarioCollection;
    }

    public Collection<DetalleOrdendeDeCompra> getDetalleOrdendeDeCompraCollection() {
        return detalleOrdendeDeCompraCollection;
    }

    public void setDetalleOrdendeDeCompraCollection(Collection<DetalleOrdendeDeCompra> detalleOrdendeDeCompraCollection) {
        this.detalleOrdendeDeCompraCollection = detalleOrdendeDeCompraCollection;
    }

    public Collection<DetalleSalidaInventario> getDetalleSalidaInventarioCollection() {
        return detalleSalidaInventarioCollection;
    }

    public void setDetalleSalidaInventarioCollection(Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection) {
        this.detalleSalidaInventarioCollection = detalleSalidaInventarioCollection;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public Unidad getUnidadEmpaque() {
        return unidadEmpaque;
    }

    public void setUnidadEmpaque(Unidad unidadEmpaque) {
        this.unidadEmpaque = unidadEmpaque;
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
        if (!(object instanceof ArticuloEmpaque)) {
            return false;
        }
        ArticuloEmpaque other = (ArticuloEmpaque) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return  nombreArticulo;
    }
    
}
