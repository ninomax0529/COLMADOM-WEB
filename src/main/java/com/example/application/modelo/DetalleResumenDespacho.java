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
@Table(name = "detalle_resumen_despacho")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleResumenDespacho.findAll", query = "SELECT d FROM DetalleResumenDespacho d")})
public class DetalleResumenDespacho implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "envasado")
    private int envasado;
    @Basic(optional = false)
    @NotNull
    @Column(name = "despachado")
    private int despachado;
    @Basic(optional = false)
    @NotNull
    @Column(name = "despacho_acumulado")
    private int despachoAcumulado;
    @Basic(optional = false)
    @NotNull
    @Column(name = "envasdo_acumulada")
    private int envasdoAcumulada;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "nombre_producto")
    private String nombreProducto;
    @JoinColumn(name = "resumen_despacho", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private ResumenDespacho resumenDespacho;
    @JoinColumn(name = "producto", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Producto producto;

    public DetalleResumenDespacho() {
    }

    public DetalleResumenDespacho(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleResumenDespacho(Integer codigo, int envasado, int despachado, int despachoAcumulado, int envasdoAcumulada, String nombreProducto) {
        this.codigo = codigo;
        this.envasado = envasado;
        this.despachado = despachado;
        this.despachoAcumulado = despachoAcumulado;
        this.envasdoAcumulada = envasdoAcumulada;
        this.nombreProducto = nombreProducto;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public int getEnvasado() {
        return envasado;
    }

    public void setEnvasado(int envasado) {
        this.envasado = envasado;
    }

    public int getDespachado() {
        return despachado;
    }

    public void setDespachado(int despachado) {
        this.despachado = despachado;
    }

    public int getDespachoAcumulado() {
        return despachoAcumulado;
    }

    public void setDespachoAcumulado(int despachoAcumulado) {
        this.despachoAcumulado = despachoAcumulado;
    }

    public int getEnvasdoAcumulada() {
        return envasdoAcumulada;
    }

    public void setEnvasdoAcumulada(int envasdoAcumulada) {
        this.envasdoAcumulada = envasdoAcumulada;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public ResumenDespacho getResumenDespacho() {
        return resumenDespacho;
    }

    public void setResumenDespacho(ResumenDespacho resumenDespacho) {
        this.resumenDespacho = resumenDespacho;
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
        if (!(object instanceof DetalleResumenDespacho)) {
            return false;
        }
        DetalleResumenDespacho other = (DetalleResumenDespacho) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleResumenDespacho[ codigo=" + codigo + " ]";
    }
    
}
