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
@Table(name = "detalle_control_de_funda_vacia")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleControlDeFundaVacia.findAll", query = "SELECT d FROM DetalleControlDeFundaVacia d")})
public class DetalleControlDeFundaVacia implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "articulo")
    private int articulo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "nombre_articulo")
    private String nombreArticulo;
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
    @Column(name = "inventario_fisico")
    private int inventarioFisico;
    @Basic(optional = false)
    @NotNull
    @Column(name = "diferencia")
    private int diferencia;
    @Basic(optional = false)
    @NotNull
    @Column(name = "diferencia_acumulada")
    private int diferenciaAcumulada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "empacada")
    private int empacada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "recibida")
    private int recibida;
    @Basic(optional = false)
    @NotNull
    @Column(name = "funda_rota")
    private int fundaRota;
    @Basic(optional = false)
    @NotNull
    @Column(name = "porciento_funda_rota")
    private double porcientoFundaRota;
    @JoinColumn(name = "control_funda_vacia", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private ControlDeFundaVacia controlFundaVacia;

    public DetalleControlDeFundaVacia() {
    }

    public DetalleControlDeFundaVacia(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleControlDeFundaVacia(Integer codigo, int articulo, String nombreArticulo, int inventarioInicial, int inventarioFinal, int inventarioFisico, int diferencia, int diferenciaAcumulada, int empacada, int recibida, int fundaRota, double porcientoFundaRota) {
        this.codigo = codigo;
        this.articulo = articulo;
        this.nombreArticulo = nombreArticulo;
        this.inventarioInicial = inventarioInicial;
        this.inventarioFinal = inventarioFinal;
        this.inventarioFisico = inventarioFisico;
        this.diferencia = diferencia;
        this.diferenciaAcumulada = diferenciaAcumulada;
        this.empacada = empacada;
        this.recibida = recibida;
        this.fundaRota = fundaRota;
        this.porcientoFundaRota = porcientoFundaRota;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public int getArticulo() {
        return articulo;
    }

    public void setArticulo(int articulo) {
        this.articulo = articulo;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
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

    public int getInventarioFisico() {
        return inventarioFisico;
    }

    public void setInventarioFisico(int inventarioFisico) {
        this.inventarioFisico = inventarioFisico;
    }

    public int getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(int diferencia) {
        this.diferencia = diferencia;
    }

    public int getDiferenciaAcumulada() {
        return diferenciaAcumulada;
    }

    public void setDiferenciaAcumulada(int diferenciaAcumulada) {
        this.diferenciaAcumulada = diferenciaAcumulada;
    }

    public int getEmpacada() {
        return empacada;
    }

    public void setEmpacada(int empacada) {
        this.empacada = empacada;
    }

    public int getRecibida() {
        return recibida;
    }

    public void setRecibida(int recibida) {
        this.recibida = recibida;
    }

    public int getFundaRota() {
        return fundaRota;
    }

    public void setFundaRota(int fundaRota) {
        this.fundaRota = fundaRota;
    }

    public double getPorcientoFundaRota() {
        return porcientoFundaRota;
    }

    public void setPorcientoFundaRota(double porcientoFundaRota) {
        this.porcientoFundaRota = porcientoFundaRota;
    }

    public ControlDeFundaVacia getControlFundaVacia() {
        return controlFundaVacia;
    }

    public void setControlFundaVacia(ControlDeFundaVacia controlFundaVacia) {
        this.controlFundaVacia = controlFundaVacia;
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
        if (!(object instanceof DetalleControlDeFundaVacia)) {
            return false;
        }
        DetalleControlDeFundaVacia other = (DetalleControlDeFundaVacia) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleControlDeFundaVacia[ codigo=" + codigo + " ]";
    }
    
}
