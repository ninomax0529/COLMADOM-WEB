/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.modelo;

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
 * @author Maximiliano
 */
@Entity
@Table(name = "detalle_proyeccion_de_venta")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleProyeccionDeVenta.findAll", query = "SELECT d FROM DetalleProyeccionDeVenta d")})
public class DetalleProyeccionDeVenta implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "nombre_producto")
    private String nombreProducto;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad")
    private int cantidad;
    @Basic(optional = false)
    @NotNull
    @Column(name = "despachada")
    private int despachada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "diferencia")
    private int diferencia;
    @JoinColumn(name = "producto", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Producto producto;
    @JoinColumn(name = "proyeccion_venta", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private ProyecconDeVenta proyeccionVenta;

    public DetalleProyeccionDeVenta() {
    }

    public DetalleProyeccionDeVenta(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleProyeccionDeVenta(Integer codigo, String nombreProducto, int cantidad, int despachada, int diferencia) {
        this.codigo = codigo;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.despachada = despachada;
        this.diferencia = diferencia;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getDespachada() {
        return despachada;
    }

    public void setDespachada(int despachada) {
        this.despachada = despachada;
    }

    public int getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(int diferencia) {
        this.diferencia = diferencia;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public ProyecconDeVenta getProyeccionVenta() {
        return proyeccionVenta;
    }

    public void setProyeccionVenta(ProyecconDeVenta proyeccionVenta) {
        this.proyeccionVenta = proyeccionVenta;
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
        if (!(object instanceof DetalleProyeccionDeVenta)) {
            return false;
        }
        DetalleProyeccionDeVenta other = (DetalleProyeccionDeVenta) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleProyeccionDeVenta[ codigo=" + codigo + " ]";
    }
    
}
