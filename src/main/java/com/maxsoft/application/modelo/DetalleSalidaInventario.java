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
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author maximilianoalmonte
 */
@Entity
@Table(name = "detalle_salida_inventario")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleSalidaInventario.findAll", query = "SELECT d FROM DetalleSalidaInventario d")})
public class DetalleSalidaInventario implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad")
    private double cantidad;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "existencia")
    private Double existencia;
    @Column(name = "precio")
    private Double precio;
    @Column(name = "valor")
    private Double valor;
    @Column(name = "existencia_anterior")
    private Double existenciaAnterior;
    @Column(name = "cantidad_solicitada")
    private Double cantidadSolicitada;
    @Column(name = "almacen")
    private Integer almacen;
    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;
    @JoinColumn(name = "salida_inventario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private SalidaInventario salidaInventario;
    @JoinColumn(name = "unidad", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Unidad unidad;

    public DetalleSalidaInventario() {
    }

    public DetalleSalidaInventario(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleSalidaInventario(Integer codigo, String descripcionArticulo, double cantidad) {
        this.codigo = codigo;
        this.descripcionArticulo = descripcionArticulo;
        this.cantidad = cantidad;
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

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public Double getExistencia() {
        return existencia;
    }

    public void setExistencia(Double existencia) {
        this.existencia = existencia;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Double getExistenciaAnterior() {
        return existenciaAnterior;
    }

    public void setExistenciaAnterior(Double existenciaAnterior) {
        this.existenciaAnterior = existenciaAnterior;
    }

    public Double getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(Double cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public Integer getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Integer almacen) {
        this.almacen = almacen;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public SalidaInventario getSalidaInventario() {
        return salidaInventario;
    }

    public void setSalidaInventario(SalidaInventario salidaInventario) {
        this.salidaInventario = salidaInventario;
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
        if (!(object instanceof DetalleSalidaInventario)) {
            return false;
        }
        DetalleSalidaInventario other = (DetalleSalidaInventario) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleSalidaInventario[ codigo=" + codigo + " ]";
    }
    
}
