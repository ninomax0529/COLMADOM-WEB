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
@Table(name = "detalle_medicion_silo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleMedicionSilo.findAll", query = "SELECT d FROM DetalleMedicionSilo d")})
public class DetalleMedicionSilo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "medida")
    private Double medida;
    @Size(max = 45)
    @Column(name = "extraccion")
    private String extraccion;
    @Size(max = 45)
    @Column(name = "molinos")
    private String molinos;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_final_tonelada")
    private double inventarioFinalTonelada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_final_funda")
    private double inventarioFinalFunda;
    @Basic(optional = false)
    @NotNull
    @Column(name = "pies")
    private int pies;
    @Basic(optional = false)
    @NotNull
    @Column(name = "pulgada")
    private int pulgada;
    @Column(name = "entrada_ajuste")
    private Double entradaAjuste;
    @JoinColumn(name = "medicion_silo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private MedicionSilo medicionSilo;
    @JoinColumn(name = "almacen_silo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacenSilo;

    public DetalleMedicionSilo() {
    }

    public DetalleMedicionSilo(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleMedicionSilo(Integer codigo, double inventarioFinalTonelada, double inventarioFinalFunda, int pies, int pulgada) {
        this.codigo = codigo;
        this.inventarioFinalTonelada = inventarioFinalTonelada;
        this.inventarioFinalFunda = inventarioFinalFunda;
        this.pies = pies;
        this.pulgada = pulgada;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Double getMedida() {
        return medida;
    }

    public void setMedida(Double medida) {
        this.medida = medida;
    }

    public String getExtraccion() {
        return extraccion;
    }

    public void setExtraccion(String extraccion) {
        this.extraccion = extraccion;
    }

    public String getMolinos() {
        return molinos;
    }

    public void setMolinos(String molinos) {
        this.molinos = molinos;
    }

    public double getInventarioFinalTonelada() {
        return inventarioFinalTonelada;
    }

    public void setInventarioFinalTonelada(double inventarioFinalTonelada) {
        this.inventarioFinalTonelada = inventarioFinalTonelada;
    }

    public double getInventarioFinalFunda() {
        return inventarioFinalFunda;
    }

    public void setInventarioFinalFunda(double inventarioFinalFunda) {
        this.inventarioFinalFunda = inventarioFinalFunda;
    }

    public int getPies() {
        return pies;
    }

    public void setPies(int pies) {
        this.pies = pies;
    }

    public int getPulgada() {
        return pulgada;
    }

    public void setPulgada(int pulgada) {
        this.pulgada = pulgada;
    }

    public Double getEntradaAjuste() {
        return entradaAjuste;
    }

    public void setEntradaAjuste(Double entradaAjuste) {
        this.entradaAjuste = entradaAjuste;
    }

    public MedicionSilo getMedicionSilo() {
        return medicionSilo;
    }

    public void setMedicionSilo(MedicionSilo medicionSilo) {
        this.medicionSilo = medicionSilo;
    }

    public Almacen getAlmacenSilo() {
        return almacenSilo;
    }

    public void setAlmacenSilo(Almacen almacenSilo) {
        this.almacenSilo = almacenSilo;
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
        if (!(object instanceof DetalleMedicionSilo)) {
            return false;
        }
        DetalleMedicionSilo other = (DetalleMedicionSilo) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleMedicionSilo[ codigo=" + codigo + " ]";
    }
    
}
