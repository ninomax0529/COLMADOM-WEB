/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "configuracion")
@NamedQueries({
    @NamedQuery(name = "Configuracion.findAll", query = "SELECT c FROM Configuracion c")})
public class Configuracion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Size(max = 150)
    @Column(name = "nombre_empresa")
    private String nombreEmpresa;
    @Size(max = 20)
    @Column(name = "rnc")
    private String rnc;
    @Column(name = "permitir_cambio_almacen")
    private Boolean permitirCambioAlmacen;
    @JoinColumn(name = "almacen_entrada_id", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacenEntradaId;
    @JoinColumn(name = "almacen_venta_id", referencedColumnName = "codigo")
    @ManyToOne
    private Almacen almacenVentaId;

    // Clasificación ABC (ver ClasificacionAbcService)
    @Column(name = "abc_dias_periodo")
    private Integer abcDiasPeriodo;
    @Column(name = "abc_limite_a")
    private java.math.BigDecimal abcLimiteA;
    @Column(name = "abc_limite_b")
    private java.math.BigDecimal abcLimiteB;
    @Column(name = "abc_dias_nuevo")
    private Integer abcDiasNuevo;
    @Column(name = "abc_ultimo_calculo")
    private java.time.LocalDateTime abcUltimoCalculo;

    // Mínimo y máximo sugeridos (días); ver ClasificacionAbcUtil.PoliticaStock
    @Column(name = "abc_dias_reposicion")
    private Integer abcDiasReposicion;
    @Column(name = "abc_seguridad_a")
    private Integer abcSeguridadA;
    @Column(name = "abc_seguridad_b")
    private Integer abcSeguridadB;
    @Column(name = "abc_seguridad_c")
    private Integer abcSeguridadC;
    @Column(name = "abc_compra_a")
    private Integer abcCompraA;
    @Column(name = "abc_compra_b")
    private Integer abcCompraB;
    @Column(name = "abc_compra_c")
    private Integer abcCompraC;

    public Configuracion() {
    }

    public Configuracion(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getRnc() {
        return rnc;
    }

    public void setRnc(String rnc) {
        this.rnc = rnc;
    }

    public Boolean getPermitirCambioAlmacen() {
        return permitirCambioAlmacen;
    }

    public void setPermitirCambioAlmacen(Boolean permitirCambioAlmacen) {
        this.permitirCambioAlmacen = permitirCambioAlmacen;
    }

    public Almacen getAlmacenEntradaId() {
        return almacenEntradaId;
    }

    public void setAlmacenEntradaId(Almacen almacenEntradaId) {
        this.almacenEntradaId = almacenEntradaId;
    }

    public Almacen getAlmacenVentaId() {
        return almacenVentaId;
    }

    public void setAlmacenVentaId(Almacen almacenVentaId) {
        this.almacenVentaId = almacenVentaId;
    }

    public Integer getAbcDiasPeriodo() {
        return abcDiasPeriodo;
    }

    public void setAbcDiasPeriodo(Integer abcDiasPeriodo) {
        this.abcDiasPeriodo = abcDiasPeriodo;
    }

    public java.math.BigDecimal getAbcLimiteA() {
        return abcLimiteA;
    }

    public void setAbcLimiteA(java.math.BigDecimal abcLimiteA) {
        this.abcLimiteA = abcLimiteA;
    }

    public java.math.BigDecimal getAbcLimiteB() {
        return abcLimiteB;
    }

    public void setAbcLimiteB(java.math.BigDecimal abcLimiteB) {
        this.abcLimiteB = abcLimiteB;
    }

    public Integer getAbcDiasNuevo() {
        return abcDiasNuevo;
    }

    public void setAbcDiasNuevo(Integer abcDiasNuevo) {
        this.abcDiasNuevo = abcDiasNuevo;
    }

    public java.time.LocalDateTime getAbcUltimoCalculo() {
        return abcUltimoCalculo;
    }

    public void setAbcUltimoCalculo(java.time.LocalDateTime abcUltimoCalculo) {
        this.abcUltimoCalculo = abcUltimoCalculo;
    }

    public Integer getAbcDiasReposicion() {
        return abcDiasReposicion;
    }

    public void setAbcDiasReposicion(Integer abcDiasReposicion) {
        this.abcDiasReposicion = abcDiasReposicion;
    }

    public Integer getAbcSeguridadA() {
        return abcSeguridadA;
    }

    public void setAbcSeguridadA(Integer abcSeguridadA) {
        this.abcSeguridadA = abcSeguridadA;
    }

    public Integer getAbcSeguridadB() {
        return abcSeguridadB;
    }

    public void setAbcSeguridadB(Integer abcSeguridadB) {
        this.abcSeguridadB = abcSeguridadB;
    }

    public Integer getAbcSeguridadC() {
        return abcSeguridadC;
    }

    public void setAbcSeguridadC(Integer abcSeguridadC) {
        this.abcSeguridadC = abcSeguridadC;
    }

    public Integer getAbcCompraA() {
        return abcCompraA;
    }

    public void setAbcCompraA(Integer abcCompraA) {
        this.abcCompraA = abcCompraA;
    }

    public Integer getAbcCompraB() {
        return abcCompraB;
    }

    public void setAbcCompraB(Integer abcCompraB) {
        this.abcCompraB = abcCompraB;
    }

    public Integer getAbcCompraC() {
        return abcCompraC;
    }

    public void setAbcCompraC(Integer abcCompraC) {
        this.abcCompraC = abcCompraC;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Configuracion)) {
            return false;
        }
        Configuracion other = (Configuracion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.Configuracion[ id=" + id + " ]";
    }
    
}
