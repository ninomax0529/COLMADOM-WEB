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
@Table(name = "detalle_cemento_dejado_en_piso")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleCementoDejadoEnPiso.findAll", query = "SELECT d FROM DetalleCementoDejadoEnPiso d")})
public class DetalleCementoDejadoEnPiso implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad_hoy")
    private int cantidadHoy;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad_ayer")
    private int cantidadAyer;
    @Column(name = "entrada")
    private Integer entrada;
    @Column(name = "salida")
    private Integer salida;
    @Size(max = 20)
    @Column(name = "nombre_producto")
    private String nombreProducto;
    @JoinColumn(name = "producto", referencedColumnName = "codigo")
    @ManyToOne
    private Producto producto;
    @JoinColumn(name = "cemento_dejado_en_piso", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private CementoDejadoEnPiso cementoDejadoEnPiso;
    @JoinColumn(name = "tipo_de_funda", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private TipoDeFunda tipoDeFunda;

    public DetalleCementoDejadoEnPiso() {
    }

    public DetalleCementoDejadoEnPiso(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleCementoDejadoEnPiso(Integer codigo, int cantidadHoy, int cantidadAyer) {
        this.codigo = codigo;
        this.cantidadHoy = cantidadHoy;
        this.cantidadAyer = cantidadAyer;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public int getCantidadHoy() {
        return cantidadHoy;
    }

    public void setCantidadHoy(int cantidadHoy) {
        this.cantidadHoy = cantidadHoy;
    }

    public int getCantidadAyer() {
        return cantidadAyer;
    }

    public void setCantidadAyer(int cantidadAyer) {
        this.cantidadAyer = cantidadAyer;
    }

    public Integer getEntrada() {
        return entrada;
    }

    public void setEntrada(Integer entrada) {
        this.entrada = entrada;
    }

    public Integer getSalida() {
        return salida;
    }

    public void setSalida(Integer salida) {
        this.salida = salida;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public CementoDejadoEnPiso getCementoDejadoEnPiso() {
        return cementoDejadoEnPiso;
    }

    public void setCementoDejadoEnPiso(CementoDejadoEnPiso cementoDejadoEnPiso) {
        this.cementoDejadoEnPiso = cementoDejadoEnPiso;
    }

    public TipoDeFunda getTipoDeFunda() {
        return tipoDeFunda;
    }

    public void setTipoDeFunda(TipoDeFunda tipoDeFunda) {
        this.tipoDeFunda = tipoDeFunda;
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
        if (!(object instanceof DetalleCementoDejadoEnPiso)) {
            return false;
        }
        DetalleCementoDejadoEnPiso other = (DetalleCementoDejadoEnPiso) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleCementoDejadoEnPiso[ codigo=" + codigo + " ]";
    }
    
}
