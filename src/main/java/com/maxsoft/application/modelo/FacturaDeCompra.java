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
@Table(name = "factura_de_compra")
@NamedQueries({
    @NamedQuery(name = "FacturaDeCompra.findAll", query = "SELECT f FROM FacturaDeCompra f")})
public class FacturaDeCompra implements Serializable {

    @Size(max = 25)
    @Column(name = "ncf")
    private String ncf;
    @Size(max = 25)
    @Column(name = "numero_factura")
    private String numeroFactura;
    @Size(max = 120)
    @Column(name = "nombre_proveedor")
    private String nombreProveedor;
    @Lob
    @Size(max = 65535)
    @Column(name = "concepto")
    private String concepto;
    @Size(max = 85)
    @Column(name = "nombre_usaurio")
    private String nombreUsaurio;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_itbis")
    private BigDecimal totalItbis;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_isr")
    private BigDecimal totalIsr;
    @Size(max = 25)
    @Column(name = "numero")
    private String numero;
    @Size(max = 10)
    @Column(name = "nombre_estado")
    private String nombreEstado;
    @JoinColumn(name = "condicion_pago", referencedColumnName = "codigo")
    @ManyToOne
    private CondicionPago condicionPago;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Column(name = "fecha_vencimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaVencimiento;
    @Column(name = "orden_compra")
    private Integer ordenCompra;
    @Column(name = "sub_total")
    private BigDecimal subTotal;
    @Column(name = "otros_impuesto")
    private BigDecimal otrosImpuesto;
    @Column(name = "total_impuesto")
    private BigDecimal totalImpuesto;
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "total_a_pagar")
    private BigDecimal totalAPagar;
    @Column(name = "total_pagado")
    private BigDecimal totalPagado;
    @Column(name = "total_pendiente")
    private BigDecimal totalPendiente;
    @Column(name = "anulada")
    private Boolean anulada;
    @Column(name = "fecha_anulada")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAnulada;
    @Column(name = "secuencia_documento")
    private Integer secuenciaDocumento;
    @JoinColumn(name = "estado", referencedColumnName = "codigo")
    @ManyToOne
    private EstadoFactura estado;
    @JoinColumn(name = "proveedor", referencedColumnName = "codigo")
    @ManyToOne
    private Proveedor proveedor;
    @JoinColumn(name = "recepcion_mercancia", referencedColumnName = "codigo")
    @ManyToOne
    private RecepcionMercancia recepcionMercancia;
    @JoinColumn(name = "tipo_compra", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private TipoCompra tipoCompra;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne
    private Usuario usuario;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "facturaDeCompra")
    private Collection<DetalleFacturaDeCompra> detalleFacturaDeCompraCollection;

    public FacturaDeCompra() {
    }

    public FacturaDeCompra(Integer codigo) {
        this.codigo = codigo;
    }

    public FacturaDeCompra(Integer codigo, BigDecimal totalItbis, BigDecimal totalIsr) {
        this.codigo = codigo;
        this.totalItbis = totalItbis;
        this.totalIsr = totalIsr;
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

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public Integer getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(Integer ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getNombreUsaurio() {
        return nombreUsaurio;
    }

    public void setNombreUsaurio(String nombreUsaurio) {
        this.nombreUsaurio = nombreUsaurio;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getTotalItbis() {
        return totalItbis;
    }

    public void setTotalItbis(BigDecimal totalItbis) {
        this.totalItbis = totalItbis;
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

    public BigDecimal getTotalImpuesto() {
        return totalImpuesto;
    }

    public void setTotalImpuesto(BigDecimal totalImpuesto) {
        this.totalImpuesto = totalImpuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalAPagar() {
        return totalAPagar;
    }

    public void setTotalAPagar(BigDecimal totalAPagar) {
        this.totalAPagar = totalAPagar;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public BigDecimal getTotalPendiente() {
        return totalPendiente;
    }

    public void setTotalPendiente(BigDecimal totalPendiente) {
        this.totalPendiente = totalPendiente;
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

    public Integer getSecuenciaDocumento() {
        return secuenciaDocumento;
    }

    public void setSecuenciaDocumento(Integer secuenciaDocumento) {
        this.secuenciaDocumento = secuenciaDocumento;
    }

    public String getNombreEstado() {
        return nombreEstado;
    }

    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public RecepcionMercancia getRecepcionMercancia() {
        return recepcionMercancia;
    }

    public void setRecepcionMercancia(RecepcionMercancia recepcionMercancia) {
        this.recepcionMercancia = recepcionMercancia;
    }

    public TipoCompra getTipoCompra() {
        return tipoCompra;
    }

    public void setTipoCompra(TipoCompra tipoCompra) {
        this.tipoCompra = tipoCompra;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Collection<DetalleFacturaDeCompra> getDetalleFacturaDeCompraCollection() {
        return detalleFacturaDeCompraCollection;
    }

    public void setDetalleFacturaDeCompraCollection(Collection<DetalleFacturaDeCompra> detalleFacturaDeCompraCollection) {
        this.detalleFacturaDeCompraCollection = detalleFacturaDeCompraCollection;
    }

    /**
     * @return the condicionPago
     */
    public CondicionPago getCondicionPago() {
        return condicionPago;
    }

    /**
     * @param condicionPago the condicionPago to set
     */
    public void setCondicionPago(CondicionPago condicionPago) {
        this.condicionPago = condicionPago;
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
        if (!(object instanceof FacturaDeCompra)) {
            return false;
        }
        FacturaDeCompra other = (FacturaDeCompra) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.FacturaDeCompra[ codigo=" + codigo + " ]";
    }

    public String getNcf() {
        return ncf;
    }

    public void setNcf(String ncf) {
        this.ncf = ncf;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

}
