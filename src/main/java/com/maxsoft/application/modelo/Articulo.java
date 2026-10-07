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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "articulo")
@NamedQueries({
    @NamedQuery(name = "Articulo.findAll", query = "SELECT a FROM Articulo a")})
public class Articulo implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "numero")
    private int numero;
    @Lob
    @Size(max = 65535)
    @Column(name = "descripcion")
    private String descripcion;
    @Size(max = 80)
    @Column(name = "codigo_de_barra")
    private String codigoDeBarra;
    @Basic(optional = false)
    @NotNull
    @Column(name = "exento_itbis")
    private boolean exentoItbis;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventariable")
    private boolean inventariable;
    @Basic(optional = false)
    @NotNull
    @Column(name = "permitir_venta_sin_existencia")
    private boolean permitirVentaSinExistencia;
    @Size(max = 25)
    @Column(name = "modelo")
    private String modelo;
    @Size(max = 80)
    @Column(name = "marca")
    private String marca;
    @Size(max = 80)
    @Column(name = "nombre_linea")
    private String nombreLinea;
    @Basic(optional = false)
    @NotNull
    @Column(name = "para_venta")
    private boolean paraVenta;
    @Basic(optional = false)
    @NotNull
    @Column(name = "para_consumo")
    private boolean paraConsumo;
    @Size(max = 80)
    @Column(name = "ruta_img")
    private String rutaImg;
    @Basic(optional = false)
    @NotNull
    @Column(name = "compuesto")
    private boolean compuesto;
    @Basic(optional = false)
    @NotNull
    @Column(name = "venta_agranel")
    private boolean ventaAgranel;
    @Size(max = 50)
    @Column(name = "creado_por")
    private String creadoPor;
    @Basic(optional = false)
    @NotNull
    @Column(name = "perecedero")
    private boolean perecedero;
    @Basic(optional = false)
    @NotNull
    @Column(name = "habilitado")
    private boolean habilitado;
    @Size(max = 20)
    @Column(name = "nombre_unidad_base")
    private String nombreUnidadBase;
    @JoinColumn(name = "unidad_base", referencedColumnName = "codigo")
    @ManyToOne
    private Unidad unidadBase;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "categoria")
    private Integer categoria;
    @Column(name = "sub_categoria")
    private Integer subCategoria;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "existencia")
    private BigDecimal existencia;
    @Column(name = "maximo")
    private BigDecimal maximo;
    @Column(name = "minimo")
    private BigDecimal minimo;
    @Column(name = "precio_compra")
    private BigDecimal precioCompra;
    @Column(name = "precio_venta")
    private BigDecimal precioVenta;
    @Column(name = "precio_venta_con_itbis")
    private Double precioVentaConItbis;
    @Column(name = "precio_venta_anterior")
    private Double precioVentaAnterior;
    @Column(name = "tipo_articulo")
    private Integer tipoArticulo;
    @Column(name = "margen_beneficio")
    private Double margenBeneficio;
    @Column(name = "porciento_utilidad")
    private Double porcientoUtilidad;
    @Column(name = "linea_articulo")
    private Integer lineaArticulo;
    @Column(name = "secuencia_documento")
    private Integer secuenciaDocumento;
    @Column(name = "itbis_gravado")
    private Double itbisGravado;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Column(name = "fecha_vencimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaVencimiento;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleTrasladoInventario> detalleTrasladoInventarioCollection;
    @JoinColumn(name = "unidad_de_venta", referencedColumnName = "codigo")
    @ManyToOne
    private UnidadDeVenta unidadDeVenta;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleAjusteInventario> detalleAjusteInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleRecepcionMercancia> detalleRecepcionMercanciaCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<MovimientoInventario> movimientoInventarioCollection;
    @OneToMany(mappedBy = "articulo")
    private Collection<DetalleOrdendeDeCompra> detalleOrdendeDeCompraCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<ArticuloAlmacen> articuloAlmacenCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<ArticuloEmpaque> articuloEmpaqueCollection;

    public Articulo() {
    }

    public Articulo(Integer codigo) {
        this.codigo = codigo;
    }

    public Articulo(Integer codigo, int numero, boolean exentoItbis, boolean inventariable, boolean permitirVentaSinExistencia, boolean paraVenta, boolean paraConsumo, boolean compuesto, boolean ventaAgranel, boolean perecedero, boolean habilitado) {
        this.codigo = codigo;
        this.numero = numero;
        this.exentoItbis = exentoItbis;
        this.inventariable = inventariable;
        this.permitirVentaSinExistencia = permitirVentaSinExistencia;
        this.paraVenta = paraVenta;
        this.paraConsumo = paraConsumo;
        this.compuesto = compuesto;
        this.ventaAgranel = ventaAgranel;
        this.perecedero = perecedero;
        this.habilitado = habilitado;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }


    public String getCodigoDeBarra() {
        return codigoDeBarra;
    }

    public void setCodigoDeBarra(String codigoDeBarra) {
        this.codigoDeBarra = codigoDeBarra;
    }

    public Integer getCategoria() {
        return categoria;
    }

    public void setCategoria(Integer categoria) {
        this.categoria = categoria;
    }

    public Integer getSubCategoria() {
        return subCategoria;
    }

    public void setSubCategoria(Integer subCategoria) {
        this.subCategoria = subCategoria;
    }

    public BigDecimal getExistencia() {
        return existencia;
    }

    public void setExistencia(BigDecimal existencia) {
        this.existencia = existencia;
    }

    public BigDecimal getMaximo() {
        return maximo;
    }

    public void setMaximo(BigDecimal maximo) {
        this.maximo = maximo;
    }

    public BigDecimal getMinimo() {
        return minimo;
    }

    public void setMinimo(BigDecimal minimo) {
        this.minimo = minimo;
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

    public Double getPrecioVentaConItbis() {
        return precioVentaConItbis;
    }

    public void setPrecioVentaConItbis(Double precioVentaConItbis) {
        this.precioVentaConItbis = precioVentaConItbis;
    }

    public Double getPrecioVentaAnterior() {
        return precioVentaAnterior;
    }

    public void setPrecioVentaAnterior(Double precioVentaAnterior) {
        this.precioVentaAnterior = precioVentaAnterior;
    }

    public boolean getExentoItbis() {
        return exentoItbis;
    }

    public void setExentoItbis(boolean exentoItbis) {
        this.exentoItbis = exentoItbis;
    }


    public boolean getPermitirVentaSinExistencia() {
        return permitirVentaSinExistencia;
    }

    public void setPermitirVentaSinExistencia(boolean permitirVentaSinExistencia) {
        this.permitirVentaSinExistencia = permitirVentaSinExistencia;
    }

    public Integer getTipoArticulo() {
        return tipoArticulo;
    }

    public void setTipoArticulo(Integer tipoArticulo) {
        this.tipoArticulo = tipoArticulo;
    }

    public Double getMargenBeneficio() {
        return margenBeneficio;
    }

    public void setMargenBeneficio(Double margenBeneficio) {
        this.margenBeneficio = margenBeneficio;
    }

    public Double getPorcientoUtilidad() {
        return porcientoUtilidad;
    }

    public void setPorcientoUtilidad(Double porcientoUtilidad) {
        this.porcientoUtilidad = porcientoUtilidad;
    }


    public Integer getLineaArticulo() {
        return lineaArticulo;
    }

    public void setLineaArticulo(Integer lineaArticulo) {
        this.lineaArticulo = lineaArticulo;
    }

    public String getNombreLinea() {
        return nombreLinea;
    }

    public void setNombreLinea(String nombreLinea) {
        this.nombreLinea = nombreLinea;
    }

    public boolean getParaVenta() {
        return paraVenta;
    }

    public void setParaVenta(boolean paraVenta) {
        this.paraVenta = paraVenta;
    }

    public boolean getParaConsumo() {
        return paraConsumo;
    }

    public void setParaConsumo(boolean paraConsumo) {
        this.paraConsumo = paraConsumo;
    }

    public String getRutaImg() {
        return rutaImg;
    }

    public void setRutaImg(String rutaImg) {
        this.rutaImg = rutaImg;
    }


    public Integer getSecuenciaDocumento() {
        return secuenciaDocumento;
    }

    public void setSecuenciaDocumento(Integer secuenciaDocumento) {
        this.secuenciaDocumento = secuenciaDocumento;
    }

    public Double getItbisGravado() {
        return itbisGravado;
    }

    public void setItbisGravado(Double itbisGravado) {
        this.itbisGravado = itbisGravado;
    }

    public boolean getVentaAgranel() {
        return ventaAgranel;
    }

    public void setVentaAgranel(boolean ventaAgranel) {
        this.ventaAgranel = ventaAgranel;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
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

    public UnidadDeVenta getUnidadDeVenta() {
        return unidadDeVenta;
    }

    public void setUnidadDeVenta(UnidadDeVenta unidadDeVenta) {
        this.unidadDeVenta = unidadDeVenta;
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
        if (!(object instanceof Articulo)) {
            return false;
        }
        Articulo other = (Articulo) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean getInventariable() {
        return inventariable;
    }

    public void setInventariable(boolean inventariable) {
        this.inventariable = inventariable;
    }

  
    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }


    public boolean getCompuesto() {
        return compuesto;
    }

    public void setCompuesto(boolean compuesto) {
        this.compuesto = compuesto;
    }
  
    public boolean getPerecedero() {
        return perecedero;
    }

    public void setPerecedero(boolean perecedero) {
        this.perecedero = perecedero;
    }

    public boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public String getNombreUnidadBase() {
        return nombreUnidadBase;
    }

    public void setNombreUnidadBase(String nombreUnidadBase) {
        this.nombreUnidadBase = nombreUnidadBase;
    }

    public Unidad getUnidadBase() {
        return unidadBase;
    }

    public void setUnidadBase(Unidad unidadBase) {
        this.unidadBase = unidadBase;
    }
    
}
