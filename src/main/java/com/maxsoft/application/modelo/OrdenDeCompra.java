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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "orden_de_compra")
@NamedQueries({
    @NamedQuery(name = "OrdenDeCompra.findAll", query = "SELECT o FROM OrdenDeCompra o")})
public class OrdenDeCompra implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Size(max = 25)
    @Column(name = "numero")
    private String numero;
    @Size(max = 100)
    @Column(name = "nombre_proveedor")
    private String nombreProveedor;
    @Size(max = 45)
    @Column(name = "rnc")
    private String rnc;
    @Column(name = "fecha_de_entrega")
    @Temporal(TemporalType.DATE)
    private Date fechaDeEntrega;
    @Size(max = 255)
    @Column(name = "comentario")
    private String comentario;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "subtotal")
    private BigDecimal subtotal;
    @Column(name = "total_impuesto")
    private BigDecimal totalImpuesto;
    @Column(name = "total_descuento")
    private BigDecimal totalDescuento;
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "anulada")
    private Boolean anulada;
    @Size(max = 10)
    @Column(name = "estado")
    private String estado;
    @Column(name = "usuario")
    private Integer usuario;
    @Column(name = "autorizada")
    private Boolean autorizada;
    @Column(name = "fecha_autorizacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAutorizacion;
    @Column(name = "fecha_anulada")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAnulada;
    @Column(name = "autorizador")
    private Integer autorizador;
    @Size(max = 45)
    @Column(name = "nombre_autorizador")
    private String nombreAutorizador;
    @Size(max = 4)
    @Column(name = "completada")
    private String completada;
    @Column(name = "total_itbis")
    private BigDecimal totalItbis;
    @Column(name = "total_a_pagar")
    private BigDecimal totalAPagar;
    @Column(name = "total_isr")
    private BigDecimal totalIsr;
    @Column(name = "otros_impuesto")
    private BigDecimal otrosImpuesto;
    @Basic(optional = false)
    @NotNull
    @Column(name = "facturada")
    private boolean facturada;
    @Column(name = "fecha_facturada")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFacturada;
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
    @Column(name = "secuencia_documento")
    private Integer secuenciaDocumento;
    @Column(name = "cotizacion")
    private Integer cotizacion;
    @Column(name = "solicitud")
    private Integer solicitud;
    @Size(max = 50)
    @Column(name = "nombre_solicitante")
    private String nombreSolicitante;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ordenDeCompra")
    private Collection<DetalleOrdendeDeCompra> detalleOrdendeDeCompraCollection;
    @JoinColumn(name = "moneda", referencedColumnName = "codigo")
    @ManyToOne
    private Moneda moneda;
    @JoinColumn(name = "plazo", referencedColumnName = "codigo")
    @ManyToOne
    private Plazo plazo;
    @JoinColumn(name = "proveedor", referencedColumnName = "codigo")
    @ManyToOne
    private Proveedor proveedor;
    @JoinColumn(name = "tipo_compra", referencedColumnName = "codigo")
    @ManyToOne
    private TipoCompra tipoCompra;

    public OrdenDeCompra() {
    }

    public OrdenDeCompra(Integer codigo) {
        this.codigo = codigo;
    }

    public OrdenDeCompra(Integer codigo, boolean facturada) {
        this.codigo = codigo;
        this.facturada = facturada;
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

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getRnc() {
        return rnc;
    }

    public void setRnc(String rnc) {
        this.rnc = rnc;
    }

    public Date getFechaDeEntrega() {
        return fechaDeEntrega;
    }

    public void setFechaDeEntrega(Date fechaDeEntrega) {
        this.fechaDeEntrega = fechaDeEntrega;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalImpuesto() {
        return totalImpuesto;
    }

    public void setTotalImpuesto(BigDecimal totalImpuesto) {
        this.totalImpuesto = totalImpuesto;
    }

    public BigDecimal getTotalDescuento() {
        return totalDescuento;
    }

    public void setTotalDescuento(BigDecimal totalDescuento) {
        this.totalDescuento = totalDescuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getUsuario() {
        return usuario;
    }

    public void setUsuario(Integer usuario) {
        this.usuario = usuario;
    }

    public Boolean getAutorizada() {
        return autorizada;
    }

    public void setAutorizada(Boolean autorizada) {
        this.autorizada = autorizada;
    }

    public Date getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(Date fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public Date getFechaAnulada() {
        return fechaAnulada;
    }

    public void setFechaAnulada(Date fechaAnulada) {
        this.fechaAnulada = fechaAnulada;
    }

    public Integer getAutorizador() {
        return autorizador;
    }

    public void setAutorizador(Integer autorizador) {
        this.autorizador = autorizador;
    }

    public String getNombreAutorizador() {
        return nombreAutorizador;
    }

    public void setNombreAutorizador(String nombreAutorizador) {
        this.nombreAutorizador = nombreAutorizador;
    }

    public String getCompletada() {
        return completada;
    }

    public void setCompletada(String completada) {
        this.completada = completada;
    }

    public BigDecimal getTotalItbis() {
        return totalItbis;
    }

    public void setTotalItbis(BigDecimal totalItbis) {
        this.totalItbis = totalItbis;
    }

    public BigDecimal getTotalAPagar() {
        return totalAPagar;
    }

    public void setTotalAPagar(BigDecimal totalAPagar) {
        this.totalAPagar = totalAPagar;
    }

    public BigDecimal getTotalIsr() {
        return totalIsr;
    }

    public void setTotalIsr(BigDecimal totalIsr) {
        this.totalIsr = totalIsr;
    }

    public BigDecimal getOtrosImpuesto() {
        return otrosImpuesto;
    }

    public void setOtrosImpuesto(BigDecimal otrosImpuesto) {
        this.otrosImpuesto = otrosImpuesto;
    }

    public boolean getFacturada() {
        return facturada;
    }

    public void setFacturada(boolean facturada) {
        this.facturada = facturada;
    }

    public Date getFechaFacturada() {
        return fechaFacturada;
    }

    public void setFechaFacturada(Date fechaFacturada) {
        this.fechaFacturada = fechaFacturada;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getSecuenciaDocumento() {
        return secuenciaDocumento;
    }

    public void setSecuenciaDocumento(Integer secuenciaDocumento) {
        this.secuenciaDocumento = secuenciaDocumento;
    }

    public Integer getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Integer cotizacion) {
        this.cotizacion = cotizacion;
    }

    public Integer getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(Integer solicitud) {
        this.solicitud = solicitud;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public Collection<DetalleOrdendeDeCompra> getDetalleOrdendeDeCompraCollection() {
        return detalleOrdendeDeCompraCollection;
    }

    public void setDetalleOrdendeDeCompraCollection(Collection<DetalleOrdendeDeCompra> detalleOrdendeDeCompraCollection) {
        this.detalleOrdendeDeCompraCollection = detalleOrdendeDeCompraCollection;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public Plazo getPlazo() {
        return plazo;
    }

    public void setPlazo(Plazo plazo) {
        this.plazo = plazo;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public TipoCompra getTipoCompra() {
        return tipoCompra;
    }

    public void setTipoCompra(TipoCompra tipoCompra) {
        this.tipoCompra = tipoCompra;
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
        if (!(object instanceof OrdenDeCompra)) {
            return false;
        }
        OrdenDeCompra other = (OrdenDeCompra) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.OrdenDeCompra[ codigo=" + codigo + " ]";
    }
    
}
