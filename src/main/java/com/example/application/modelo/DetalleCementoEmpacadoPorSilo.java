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
@Table(name = "detalle_cemento_empacado_por_silo")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleCementoEmpacadoPorSilo.findAll", query = "SELECT d FROM DetalleCementoEmpacadoPorSilo d")})
public class DetalleCementoEmpacadoPorSilo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cantidad_funda")
    private double cantidadFunda;
    @Basic(optional = false)
    @NotNull
    @Column(name = "funda_empacada")
    private double fundaEmpacada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "funda_paletizada")
    private double fundaPaletizada;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hora_primer_turno")
    private double horaPrimerTurno;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hora_segundo_turno")
    private double horaSegundoTurno;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hora_tercer_turno")
    private double horaTercerTurno;
    @Basic(optional = false)
    @NotNull
    @Column(name = "inventario_final")
    private int inventarioFinal;
    @Size(max = 10)
    @Column(name = "nombre_silo")
    private String nombreSilo;
    @JoinColumn(name = "almacen_silo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacenSilo;
    @JoinColumn(name = "empaque_silo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private CementoEmpacadoPorSilo empaqueSilo;

    public DetalleCementoEmpacadoPorSilo() {
    }

    public DetalleCementoEmpacadoPorSilo(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleCementoEmpacadoPorSilo(Integer codigo, double cantidadFunda, double fundaEmpacada, double fundaPaletizada, double horaPrimerTurno, double horaSegundoTurno, double horaTercerTurno, int inventarioFinal) {
        this.codigo = codigo;
        this.cantidadFunda = cantidadFunda;
        this.fundaEmpacada = fundaEmpacada;
        this.fundaPaletizada = fundaPaletizada;
        this.horaPrimerTurno = horaPrimerTurno;
        this.horaSegundoTurno = horaSegundoTurno;
        this.horaTercerTurno = horaTercerTurno;
        this.inventarioFinal = inventarioFinal;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public double getCantidadFunda() {
        return cantidadFunda;
    }

    public void setCantidadFunda(double cantidadFunda) {
        this.cantidadFunda = cantidadFunda;
    }

    public double getFundaEmpacada() {
        return fundaEmpacada;
    }

    public void setFundaEmpacada(double fundaEmpacada) {
        this.fundaEmpacada = fundaEmpacada;
    }

    public double getFundaPaletizada() {
        return fundaPaletizada;
    }

    public void setFundaPaletizada(double fundaPaletizada) {
        this.fundaPaletizada = fundaPaletizada;
    }

    public double getHoraPrimerTurno() {
        return horaPrimerTurno;
    }

    public void setHoraPrimerTurno(double horaPrimerTurno) {
        this.horaPrimerTurno = horaPrimerTurno;
    }

    public double getHoraSegundoTurno() {
        return horaSegundoTurno;
    }

    public void setHoraSegundoTurno(double horaSegundoTurno) {
        this.horaSegundoTurno = horaSegundoTurno;
    }

    public double getHoraTercerTurno() {
        return horaTercerTurno;
    }

    public void setHoraTercerTurno(double horaTercerTurno) {
        this.horaTercerTurno = horaTercerTurno;
    }

    public int getInventarioFinal() {
        return inventarioFinal;
    }

    public void setInventarioFinal(int inventarioFinal) {
        this.inventarioFinal = inventarioFinal;
    }

    public String getNombreSilo() {
        return nombreSilo;
    }

    public void setNombreSilo(String nombreSilo) {
        this.nombreSilo = nombreSilo;
    }

    public Almacen getAlmacenSilo() {
        return almacenSilo;
    }

    public void setAlmacenSilo(Almacen almacenSilo) {
        this.almacenSilo = almacenSilo;
    }

    public CementoEmpacadoPorSilo getEmpaqueSilo() {
        return empaqueSilo;
    }

    public void setEmpaqueSilo(CementoEmpacadoPorSilo empaqueSilo) {
        this.empaqueSilo = empaqueSilo;
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
        if (!(object instanceof DetalleCementoEmpacadoPorSilo)) {
            return false;
        }
        DetalleCementoEmpacadoPorSilo other = (DetalleCementoEmpacadoPorSilo) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleCementoEmpacadoPorSilo[ codigo=" + codigo + " ]";
    }
    
}
