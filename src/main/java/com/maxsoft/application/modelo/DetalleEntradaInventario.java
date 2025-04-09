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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "detalle_entrada_inventario")
@NamedQueries({
    @NamedQuery(name = "DetalleEntradaInventario.findAll", query = "SELECT d FROM DetalleEntradaInventario d")})
public class DetalleEntradaInventario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;
    @Size(max = 10)
    @Column(name = "nombre_unidad")
    private String nombreUnidad;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "cantidad_pedida")
    private Double cantidadPedida;
    @Column(name = "cantidad_recibida")
    private Double cantidadRecibida;
    @Column(name = "cantidad_pendiente")
    private Double cantidadPendiente;
    @Column(name = "precio_compra")
    private Double precioCompra;
    @Column(name = "precio_compra_anterior")
    private Double precioCompraAnterior;
    @Column(name = "costo_unitario")
    private Double costoUnitario;
    @Column(name = "orden_compra")
    private Integer ordenCompra;
    @Size(max = 20)
    @Column(name = "numero_orden")
    private String numeroOrden;
    @Size(max = 30)
    @Column(name = "numero_factura")
    private String numeroFactura;
    @Column(name = "suplidor")
    private Integer suplidor;
    @Size(max = 50)
    @Column(name = "nombre_suplidor")
    private String nombreSuplidor;
    @Column(name = "existencia_actual")
    private Double existenciaActual;
    @Column(name = "nueva_existencia")
    private Double nuevaExistencia;
    @Column(name = "precio_venta_anterior")
    private Double precioVentaAnterior;
    @Column(name = "precio_venta")
    private Double precioVenta;
    @Size(max = 20)
    @Column(name = "nombre_almacen")
    private String nombreAlmacen;
    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacen;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
    @JoinColumn(name = "entrada_inventario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private EntradaInventario entradaInventario;
    @JoinColumn(name = "unidad", referencedColumnName = "codigo")
    @ManyToOne
    private Unidad unidad;

    public DetalleEntradaInventario() {
    }

    public DetalleEntradaInventario(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleEntradaInventario(Integer codigo, String descripcionArticulo) {
        this.codigo = codigo;
        this.descripcionArticulo = descripcionArticulo;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDescripcionArticulo() {
        return descripcionArticulo;
    }

    public void setDescripcionArticulo(String descripcionArticulo) {
        this.descripcionArticulo = descripcionArticulo;
    }

    public String getNombreUnidad() {
        return nombreUnidad;
    }

    public void setNombreUnidad(String nombreUnidad) {
        this.nombreUnidad = nombreUnidad;
    }

    public Double getCantidadPedida() {
        return cantidadPedida;
    }

    public void setCantidadPedida(Double cantidadPedida) {
        this.cantidadPedida = cantidadPedida;
    }

    public Double getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(Double cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public Double getCantidadPendiente() {
        return cantidadPendiente;
    }

    public void setCantidadPendiente(Double cantidadPendiente) {
        this.cantidadPendiente = cantidadPendiente;
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

    public Double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(Double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public Integer getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(Integer ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(String numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public Integer getSuplidor() {
        return suplidor;
    }

    public void setSuplidor(Integer suplidor) {
        this.suplidor = suplidor;
    }

    public String getNombreSuplidor() {
        return nombreSuplidor;
    }

    public void setNombreSuplidor(String nombreSuplidor) {
        this.nombreSuplidor = nombreSuplidor;
    }

    public Double getExistenciaActual() {
        return existenciaActual;
    }

    public void setExistenciaActual(Double existenciaActual) {
        this.existenciaActual = existenciaActual;
    }

    public Double getNuevaExistencia() {
        return nuevaExistencia;
    }

    public void setNuevaExistencia(Double nuevaExistencia) {
        this.nuevaExistencia = nuevaExistencia;
    }

    public Double getPrecioVentaAnterior() {
        return precioVentaAnterior;
    }

    public void setPrecioVentaAnterior(Double precioVentaAnterior) {
        this.precioVentaAnterior = precioVentaAnterior;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getNombreAlmacen() {
        return nombreAlmacen;
    }

    public void setNombreAlmacen(String nombreAlmacen) {
        this.nombreAlmacen = nombreAlmacen;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public EntradaInventario getEntradaInventario() {
        return entradaInventario;
    }

    public void setEntradaInventario(EntradaInventario entradaInventario) {
        this.entradaInventario = entradaInventario;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
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
        if (!(object instanceof DetalleEntradaInventario)) {
            return false;
        }
        DetalleEntradaInventario other = (DetalleEntradaInventario) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleEntradaInventario[ codigo=" + codigo + " ]";
    }
    
}
