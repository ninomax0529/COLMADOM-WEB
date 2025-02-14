/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.modelo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "almacen")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Almacen.findAll", query = "SELECT a FROM Almacen a")})
public class Almacen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "descripcion")
    private String descripcion;
    @Column(name = "local")
    private Boolean local;
    @Basic(optional = false)
    @NotNull
    @Column(name = "material")
    private int material;
    @Basic(optional = false)
    @NotNull
    @Column(name = "existencia")
    private double existencia;
    @Basic(optional = false)
    @NotNull
    @Column(name = "capacidad")
    private double capacidad;
    @Basic(optional = false)
    @NotNull
    @Column(name = "extraccion")
    private double extraccion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "molinos")
    private double molinos;
    @Basic(optional = false)
    @NotNull
    @Column(name = "habilitado")
    private boolean habilitado;
    @Basic(optional = false)
    @NotNull
    @Column(name = "altura")
    private double altura;
    @Basic(optional = false)
    @NotNull
    @Column(name = "diametro")
    private double diametro;
    @Basic(optional = false)
    @NotNull
    @Column(name = "estado_material")
    private boolean estadoMaterial;
    @Basic(optional = false)
    @NotNull
    @Column(name = "produccion")
    private boolean produccion;
    @Column(name = "codigo_en_sap")
    private Integer codigoEnSap;
    @Size(max = 10)
    @Column(name = "abreviatura")
    private String abreviatura;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "almacenSilo")
    private Collection<DetalleMedicionSilo> detalleMedicionSiloCollection;
    @JoinColumn(name = "tipo_equipo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private TipoEquipo tipoEquipo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "almacenSilo")
    private Collection<DetalleCementoEmpacadoPorSilo> detalleCementoEmpacadoPorSiloCollection;

    public Almacen() {
    }

    public Almacen(Integer codigo) {
        this.codigo = codigo;
    }

    public Almacen(Integer codigo, String descripcion, int material, double existencia, double capacidad, double extraccion, double molinos, boolean habilitado, double altura, double diametro, boolean estadoMaterial, boolean produccion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.material = material;
        this.existencia = existencia;
        this.capacidad = capacidad;
        this.extraccion = extraccion;
        this.molinos = molinos;
        this.habilitado = habilitado;
        this.altura = altura;
        this.diametro = diametro;
        this.estadoMaterial = estadoMaterial;
        this.produccion = produccion;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getLocal() {
        return local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public int getMaterial() {
        return material;
    }

    public void setMaterial(int material) {
        this.material = material;
    }

    public double getExistencia() {
        return existencia;
    }

    public void setExistencia(double existencia) {
        this.existencia = existencia;
    }

    public double getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(double capacidad) {
        this.capacidad = capacidad;
    }

    public double getExtraccion() {
        return extraccion;
    }

    public void setExtraccion(double extraccion) {
        this.extraccion = extraccion;
    }

    public double getMolinos() {
        return molinos;
    }

    public void setMolinos(double molinos) {
        this.molinos = molinos;
    }

    public boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double getDiametro() {
        return diametro;
    }

    public void setDiametro(double diametro) {
        this.diametro = diametro;
    }

    public boolean getEstadoMaterial() {
        return estadoMaterial;
    }

    public void setEstadoMaterial(boolean estadoMaterial) {
        this.estadoMaterial = estadoMaterial;
    }

    public boolean getProduccion() {
        return produccion;
    }

    public void setProduccion(boolean produccion) {
        this.produccion = produccion;
    }

    public Integer getCodigoEnSap() {
        return codigoEnSap;
    }

    public void setCodigoEnSap(Integer codigoEnSap) {
        this.codigoEnSap = codigoEnSap;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    @XmlTransient
    public Collection<DetalleMedicionSilo> getDetalleMedicionSiloCollection() {
        return detalleMedicionSiloCollection;
    }

    public void setDetalleMedicionSiloCollection(Collection<DetalleMedicionSilo> detalleMedicionSiloCollection) {
        this.detalleMedicionSiloCollection = detalleMedicionSiloCollection;
    }

    public TipoEquipo getTipoEquipo() {
        return tipoEquipo;
    }

    public void setTipoEquipo(TipoEquipo tipoEquipo) {
        this.tipoEquipo = tipoEquipo;
    }

    @XmlTransient
    public Collection<DetalleCementoEmpacadoPorSilo> getDetalleCementoEmpacadoPorSiloCollection() {
        return detalleCementoEmpacadoPorSiloCollection;
    }

    public void setDetalleCementoEmpacadoPorSiloCollection(Collection<DetalleCementoEmpacadoPorSilo> detalleCementoEmpacadoPorSiloCollection) {
        this.detalleCementoEmpacadoPorSiloCollection = detalleCementoEmpacadoPorSiloCollection;
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
        if (!(object instanceof Almacen)) {
            return false;
        }
        Almacen other = (Almacen) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.Almacen[ codigo=" + codigo + " ]";
    }
    
}
