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
@Table(name = "factura_de_venta")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FacturaDeVenta.findAll", query = "SELECT f FROM FacturaDeVenta f")})
public class FacturaDeVenta implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "numero_documento")
    private Integer numeroDocumento;
    @Column(name = "tipo_documento")
    private Integer tipoDocumento;
    @Column(name = "secuencia_documento")
    private Integer secuenciaDocumento;
    @Column(name = "tipo_ncf")
    private Integer tipoNcf;
    @Size(max = 25)
    @Column(name = "ncf")
    private String ncf;
    @Size(max = 200)
    @Column(name = "nombre_cliente")
    private String nombreCliente;
    @Column(name = "tipo_venta")
    private Integer tipoVenta;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Column(name = "fecha_actualizacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaActualizacion;
    @Lob
    @Size(max = 65535)
    @Column(name = "comentario")
    private String comentario;
    @Column(name = "anulada")
    private Boolean anulada;
    @Column(name = "fecha_anulada")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAnulada;
    @Column(name = "usuario")
    private Integer usuario;
    @Size(max = 80)
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Size(max = 80)
    @Column(name = "anulada_por")
    private String anuladaPor;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "sub_total")
    private Double subTotal;
    @Column(name = "total_descuento")
    private Double totalDescuento;
    @Column(name = "porciento_descuento")
    private Double porcientoDescuento;
    @Column(name = "total_itbis")
    private Double totalItbis;
    @Column(name = "porciento_itbis")
    private Double porcientoItbis;
    @Column(name = "total")
    private Double total;
    @Column(name = "total_abonado")
    private Double totalAbonado;
    @Column(name = "total_pendiente")
    private Double totalPendiente;
    @JoinColumn(name = "cliente", referencedColumnName = "codigo")
    @ManyToOne
    private Cliente cliente;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "factura")
    private Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection;

    public FacturaDeVenta() {
    }

    public FacturaDeVenta(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(Integer numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Integer getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(Integer tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Integer getSecuenciaDocumento() {
        return secuenciaDocumento;
    }

    public void setSecuenciaDocumento(Integer secuenciaDocumento) {
        this.secuenciaDocumento = secuenciaDocumento;
    }

    public Integer getTipoNcf() {
        return tipoNcf;
    }

    public void setTipoNcf(Integer tipoNcf) {
        this.tipoNcf = tipoNcf;
    }

    public String getNcf() {
        return ncf;
    }

    public void setNcf(String ncf) {
        this.ncf = ncf;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Integer getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(Integer tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Date fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

    public Date getFechaAnulada() {
        return fechaAnulada;
    }

    public void setFechaAnulada(Date fechaAnulada) {
        this.fechaAnulada = fechaAnulada;
    }

    public Integer getUsuario() {
        return usuario;
    }

    public void setUsuario(Integer usuario) {
        this.usuario = usuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getAnuladaPor() {
        return anuladaPor;
    }

    public void setAnuladaPor(String anuladaPor) {
        this.anuladaPor = anuladaPor;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }

    public Double getTotalDescuento() {
        return totalDescuento;
    }

    public void setTotalDescuento(Double totalDescuento) {
        this.totalDescuento = totalDescuento;
    }

    public Double getPorcientoDescuento() {
        return porcientoDescuento;
    }

    public void setPorcientoDescuento(Double porcientoDescuento) {
        this.porcientoDescuento = porcientoDescuento;
    }

    public Double getTotalItbis() {
        return totalItbis;
    }

    public void setTotalItbis(Double totalItbis) {
        this.totalItbis = totalItbis;
    }

    public Double getPorcientoItbis() {
        return porcientoItbis;
    }

    public void setPorcientoItbis(Double porcientoItbis) {
        this.porcientoItbis = porcientoItbis;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getTotalAbonado() {
        return totalAbonado;
    }

    public void setTotalAbonado(Double totalAbonado) {
        this.totalAbonado = totalAbonado;
    }

    public Double getTotalPendiente() {
        return totalPendiente;
    }

    public void setTotalPendiente(Double totalPendiente) {
        this.totalPendiente = totalPendiente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @XmlTransient
    public Collection<DetalleFacturaDeVenta> getDetalleFacturaDeVentaCollection() {
        return detalleFacturaDeVentaCollection;
    }

    public void setDetalleFacturaDeVentaCollection(Collection<DetalleFacturaDeVenta> detalleFacturaDeVentaCollection) {
        this.detalleFacturaDeVentaCollection = detalleFacturaDeVentaCollection;
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
        if (!(object instanceof FacturaDeVenta)) {
            return false;
        }
        FacturaDeVenta other = (FacturaDeVenta) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.FacturaDeVenta[ codigo=" + codigo + " ]";
    }
    
}
