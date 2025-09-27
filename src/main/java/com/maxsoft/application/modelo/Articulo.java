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
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author maximilianoalmonte
 */
@Entity
@Table(name = "articulo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Articulo.findAll", query = "SELECT a FROM Articulo a")})
public class Articulo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
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
    @Column(name = "categoria")
    private Integer categoria;
    @Column(name = "sub_categoria")
    private Integer subCategoria;
    @Column(name = "unidad_entrada")
    private Integer unidadEntrada;
    @Column(name = "unidad_salida")
    private Integer unidadSalida;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "existencia")
    private Double existencia;
    @Column(name = "maximo")
    private Double maximo;
    @Column(name = "minimo")
    private Double minimo;
    @Column(name = "apedir")
    private Double apedir;
    @Column(name = "precio_compra")
    private Double precioCompra;
    @Column(name = "precio_compra_anterior")
    private Double precioCompraAnterior;
    @Column(name = "precio_venta")
    private Double precioVenta;
    @Column(name = "precio_venta_con_itbis")
    private Double precioVentaConItbis;
    @Column(name = "precio_venta_anterior")
    private Double precioVentaAnterior;
    @Column(name = "ultimo_suplidor")
    private Integer ultimoSuplidor;
    @Basic(optional = false)
    @NotNull
    @Column(name = "exento_itbis")
    private boolean exentoItbis;
    @Lob
    @Column(name = "imagen")
    private byte[] imagen;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventariable")
    private boolean inventariable;
    @Column(name = "tipo_articulo")
    private Integer tipoArticulo;
    @Column(name = "margen_beneficio")
    private Double margenBeneficio;
    @Column(name = "porciento_utilidad")
    private Double porcientoUtilidad;
    @Size(max = 25)
    @Column(name = "modelo")
    private String modelo;
    @Size(max = 80)
    @Column(name = "marca")
    private String marca;
    @Column(name = "linea_articulo")
    private Integer lineaArticulo;
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
    @Size(max = 30)
    @Column(name = "ruta_img")
    private String rutaImg;
    @Column(name = "embase")
    private Integer embase;
    @Size(max = 50)
    @Column(name = "nombre_embase")
    private String nombreEmbase;
    @Basic(optional = false)
    @NotNull
    @Column(name = "compuesto")
    private boolean compuesto;
    @Column(name = "secuencia_documento")
    private Integer secuenciaDocumento;
    @Column(name = "itbis_gravado")
    private Double itbisGravado;
    @Basic(optional = false)
    @NotNull
    @Column(name = "venta_agranel")
    private boolean ventaAgranel;
    @Size(max = 50)
    @Column(name = "creado_por")
    private String creadoPor;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "perecedero")
    private boolean perecedero;
    @Column(name = "fecha_vencimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaVencimiento;
    @Basic(optional = false)
    @NotNull
    @Column(name = "habilitado")
    private boolean habilitado;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection;
    @JoinColumn(name = "unidad_de_venta", referencedColumnName = "codigo")
    @ManyToOne
    private UnidadDeVenta unidadDeVenta;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articulo")
    private Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection;

    public Articulo() {
    }

    public Articulo(Integer codigo) {
        this.codigo = codigo;
    }

    public Articulo(Integer codigo, int numero, boolean exentoItbis, boolean inventariable, boolean paraVenta, boolean paraConsumo, boolean compuesto, boolean ventaAgranel, boolean perecedero, boolean habilitado) {
        this.codigo = codigo;
        this.numero = numero;
        this.exentoItbis = exentoItbis;
        this.inventariable = inventariable;
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

    public Integer getUnidadEntrada() {
        return unidadEntrada;
    }

    public void setUnidadEntrada(Integer unidadEntrada) {
        this.unidadEntrada = unidadEntrada;
    }

    public Integer getUnidadSalida() {
        return unidadSalida;
    }

    public void setUnidadSalida(Integer unidadSalida) {
        this.unidadSalida = unidadSalida;
    }

    public Double getExistencia() {
        return existencia;
    }

    public void setExistencia(Double existencia) {
        this.existencia = existencia;
    }

    public Double getMaximo() {
        return maximo;
    }

    public void setMaximo(Double maximo) {
        this.maximo = maximo;
    }

    public Double getMinimo() {
        return minimo;
    }

    public void setMinimo(Double minimo) {
        this.minimo = minimo;
    }

    public Double getApedir() {
        return apedir;
    }

    public void setApedir(Double apedir) {
        this.apedir = apedir;
    }

    public Double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(Double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public Double getPrecioCompraAnterior() {
        return precioCompraAnterior;
    }

    public void setPrecioCompraAnterior(Double precioCompraAnterior) {
        this.precioCompraAnterior = precioCompraAnterior;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
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

    public Integer getUltimoSuplidor() {
        return ultimoSuplidor;
    }

    public void setUltimoSuplidor(Integer ultimoSuplidor) {
        this.ultimoSuplidor = ultimoSuplidor;
    }

    public boolean getExentoItbis() {
        return exentoItbis;
    }

    public void setExentoItbis(boolean exentoItbis) {
        this.exentoItbis = exentoItbis;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public boolean getInventariable() {
        return inventariable;
    }

    public void setInventariable(boolean inventariable) {
        this.inventariable = inventariable;
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

    public Integer getEmbase() {
        return embase;
    }

    public void setEmbase(Integer embase) {
        this.embase = embase;
    }

    public String getNombreEmbase() {
        return nombreEmbase;
    }

    public void setNombreEmbase(String nombreEmbase) {
        this.nombreEmbase = nombreEmbase;
    }

    public boolean getCompuesto() {
        return compuesto;
    }

    public void setCompuesto(boolean compuesto) {
        this.compuesto = compuesto;
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

    public boolean getPerecedero() {
        return perecedero;
    }

    public void setPerecedero(boolean perecedero) {
        this.perecedero = perecedero;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    @XmlTransient
    public Collection<DetalleEntradaInventario> getDetalleEntradaInventarioCollection() {
        return detalleEntradaInventarioCollection;
    }

    public void setDetalleEntradaInventarioCollection(Collection<DetalleEntradaInventario> detalleEntradaInventarioCollection) {
        this.detalleEntradaInventarioCollection = detalleEntradaInventarioCollection;
    }

    @XmlTransient
    public Collection<DetalleFacturaDeVenta> getDetalleFacturaDeVentaCollection() {
        return detalleFacturaDeVentaCollection;
    }

    public void setDetalleFacturaDeVentaCollection(Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection) {
        this.detalleFacturaDeVentaCollection = detalleFacturaDeVentaCollection;
    }

    public UnidadDeVenta getUnidadDeVenta() {
        return unidadDeVenta;
    }

    public void setUnidadDeVenta(UnidadDeVenta unidadDeVenta) {
        this.unidadDeVenta = unidadDeVenta;
    }

    @XmlTransient
    public Collection<DetalleSalidaInventario> getDetalleSalidaInventarioCollection() {
        return detalleSalidaInventarioCollection;
    }

    public void setDetalleSalidaInventarioCollection(Collection<DetalleSalidaInventario> detalleSalidaInventarioCollection) {
        this.detalleSalidaInventarioCollection = detalleSalidaInventarioCollection;
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
        return "com.maxsoft.application.modelo.Articulo[ codigo=" + codigo + " ]";
    }
    
}
