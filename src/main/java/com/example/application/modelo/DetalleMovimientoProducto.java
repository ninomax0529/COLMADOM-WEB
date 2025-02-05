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
@Table(name = "detalle_movimiento_producto")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleMovimientoProducto.findAll", query = "SELECT d FROM DetalleMovimientoProducto d")})
public class DetalleMovimientoProducto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "nombre_producto")
    private String nombreProducto;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_inicial")
    private int inventarioInicial;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_final")
    private int inventarioFinal;
    @Basic(optional = false)
    @NotNull
    @Column(name = "despacho")
    private int despacho;
    @Basic(optional = false)
    @NotNull
    @Column(name = "porduccion_por_inventario")
    private int porduccionPorInventario;
    @Basic(optional = false)
    @NotNull
    @Column(name = "porduccion_por_empacadora")
    private int porduccionPorEmpacadora;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_inicial_sap")
    private int inventarioInicialSap;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_final_sap")
    private int inventarioFinalSap;
    @Basic(optional = false)
    @NotNull
    @Column(name = "produccion_acumulada")
    private int produccionAcumulada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "dspacho_acumulado")
    private int dspachoAcumulado;
    @JoinColumn(name = "operacion_empacadora", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private OperacionEmpacadora operacionEmpacadora;
    @JoinColumn(name = "producto", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Producto producto;

    public DetalleMovimientoProducto() {
    }

    public DetalleMovimientoProducto(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleMovimientoProducto(Integer codigo, String nombreProducto, int inventarioInicial, int inventarioFinal, int despacho, int porduccionPorInventario, int porduccionPorEmpacadora, int inventarioInicialSap, int inventarioFinalSap, int produccionAcumulada, int dspachoAcumulado) {
        this.codigo = codigo;
        this.nombreProducto = nombreProducto;
        this.inventarioInicial = inventarioInicial;
        this.inventarioFinal = inventarioFinal;
        this.despacho = despacho;
        this.porduccionPorInventario = porduccionPorInventario;
        this.porduccionPorEmpacadora = porduccionPorEmpacadora;
        this.inventarioInicialSap = inventarioInicialSap;
        this.inventarioFinalSap = inventarioFinalSap;
        this.produccionAcumulada = produccionAcumulada;
        this.dspachoAcumulado = dspachoAcumulado;
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

    public int getInventarioInicial() {
        return inventarioInicial;
    }

    public void setInventarioInicial(int inventarioInicial) {
        this.inventarioInicial = inventarioInicial;
    }

    public int getInventarioFinal() {
        return inventarioFinal;
    }

    public void setInventarioFinal(int inventarioFinal) {
        this.inventarioFinal = inventarioFinal;
    }

    public int getDespacho() {
        return despacho;
    }

    public void setDespacho(int despacho) {
        this.despacho = despacho;
    }

    public int getPorduccionPorInventario() {
        return porduccionPorInventario;
    }

    public void setPorduccionPorInventario(int porduccionPorInventario) {
        this.porduccionPorInventario = porduccionPorInventario;
    }

    public int getPorduccionPorEmpacadora() {
        return porduccionPorEmpacadora;
    }

    public void setPorduccionPorEmpacadora(int porduccionPorEmpacadora) {
        this.porduccionPorEmpacadora = porduccionPorEmpacadora;
    }

    public int getInventarioInicialSap() {
        return inventarioInicialSap;
    }

    public void setInventarioInicialSap(int inventarioInicialSap) {
        this.inventarioInicialSap = inventarioInicialSap;
    }

    public int getInventarioFinalSap() {
        return inventarioFinalSap;
    }

    public void setInventarioFinalSap(int inventarioFinalSap) {
        this.inventarioFinalSap = inventarioFinalSap;
    }

    public int getProduccionAcumulada() {
        return produccionAcumulada;
    }

    public void setProduccionAcumulada(int produccionAcumulada) {
        this.produccionAcumulada = produccionAcumulada;
    }

    public int getDspachoAcumulado() {
        return dspachoAcumulado;
    }

    public void setDspachoAcumulado(int dspachoAcumulado) {
        this.dspachoAcumulado = dspachoAcumulado;
    }

    public OperacionEmpacadora getOperacionEmpacadora() {
        return operacionEmpacadora;
    }

    public void setOperacionEmpacadora(OperacionEmpacadora operacionEmpacadora) {
        this.operacionEmpacadora = operacionEmpacadora;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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
        if (!(object instanceof DetalleMovimientoProducto)) {
            return false;
        }
        DetalleMovimientoProducto other = (DetalleMovimientoProducto) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleMovimientoProducto[ codigo=" + codigo + " ]";
    }
    
}
