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
@Table(name = "detalle_operacion_empacadora")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DetalleOperacionEmpacadora.findAll", query = "SELECT d FROM DetalleOperacionEmpacadora d")})
public class DetalleOperacionEmpacadora implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hora_inicio")
    private int horaInicio;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hora_final")
    private int horaFinal;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "nombre_empacadora")
    private String nombreEmpacadora;
    @Column(name = "paletizadora")
    private Integer paletizadora;
    @Size(max = 20)
    @Column(name = "nombre_paletizadora")
    private String nombrePaletizadora;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "nombre_producto")
    private String nombreProducto;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "empacado_por_hora")
    private Double empacadoPorHora;
    @Column(name = "acumulado")
    private Double acumulado;
    @Column(name = "minuto_trabajado")
    private Double minutoTrabajado;
    @Size(max = 10)
    @Column(name = "hora")
    private String hora;
    @Column(name = "silo")
    private Integer silo;
    @Size(max = 30)
    @Column(name = "nombre_silo")
    private String nombreSilo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "editable")
    private boolean editable;
    @Basic(optional = false)
    @NotNull
    @Column(name = "funda_rota")
    private int fundaRota;
    @Basic(optional = false)
    @NotNull
    @Column(name = "funda_fallida")
    private int fundaFallida;
    @JoinColumn(name = "operacion_empacadora", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private OperacionEmpacadora operacionEmpacadora;
    @JoinColumn(name = "empacadora", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Equipo empacadora;
    @JoinColumn(name = "producto", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Producto producto;

    public DetalleOperacionEmpacadora() {
    }

    public DetalleOperacionEmpacadora(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleOperacionEmpacadora(Integer codigo, int horaInicio, int horaFinal, String nombreEmpacadora, String nombreProducto, boolean editable, int fundaRota, int fundaFallida) {
        this.codigo = codigo;
        this.horaInicio = horaInicio;
        this.horaFinal = horaFinal;
        this.nombreEmpacadora = nombreEmpacadora;
        this.nombreProducto = nombreProducto;
        this.editable = editable;
        this.fundaRota = fundaRota;
        this.fundaFallida = fundaFallida;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public int getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(int horaInicio) {
        this.horaInicio = horaInicio;
    }

    public int getHoraFinal() {
        return horaFinal;
    }

    public void setHoraFinal(int horaFinal) {
        this.horaFinal = horaFinal;
    }

    public String getNombreEmpacadora() {
        return nombreEmpacadora;
    }

    public void setNombreEmpacadora(String nombreEmpacadora) {
        this.nombreEmpacadora = nombreEmpacadora;
    }

    public Integer getPaletizadora() {
        return paletizadora;
    }

    public void setPaletizadora(Integer paletizadora) {
        this.paletizadora = paletizadora;
    }

    public String getNombrePaletizadora() {
        return nombrePaletizadora;
    }

    public void setNombrePaletizadora(String nombrePaletizadora) {
        this.nombrePaletizadora = nombrePaletizadora;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Double getEmpacadoPorHora() {
        return empacadoPorHora;
    }

    public void setEmpacadoPorHora(Double empacadoPorHora) {
        this.empacadoPorHora = empacadoPorHora;
    }

    public Double getAcumulado() {
        return acumulado;
    }

    public void setAcumulado(Double acumulado) {
        this.acumulado = acumulado;
    }

    public Double getMinutoTrabajado() {
        return minutoTrabajado;
    }

    public void setMinutoTrabajado(Double minutoTrabajado) {
        this.minutoTrabajado = minutoTrabajado;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public Integer getSilo() {
        return silo;
    }

    public void setSilo(Integer silo) {
        this.silo = silo;
    }

    public String getNombreSilo() {
        return nombreSilo;
    }

    public void setNombreSilo(String nombreSilo) {
        this.nombreSilo = nombreSilo;
    }

    public boolean getEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public int getFundaRota() {
        return fundaRota;
    }

    public void setFundaRota(int fundaRota) {
        this.fundaRota = fundaRota;
    }

    public int getFundaFallida() {
        return fundaFallida;
    }

    public void setFundaFallida(int fundaFallida) {
        this.fundaFallida = fundaFallida;
    }

    public OperacionEmpacadora getOperacionEmpacadora() {
        return operacionEmpacadora;
    }

    public void setOperacionEmpacadora(OperacionEmpacadora operacionEmpacadora) {
        this.operacionEmpacadora = operacionEmpacadora;
    }

    public Equipo getEmpacadora() {
        return empacadora;
    }

    public void setEmpacadora(Equipo empacadora) {
        this.empacadora = empacadora;
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
        if (!(object instanceof DetalleOperacionEmpacadora)) {
            return false;
        }
        DetalleOperacionEmpacadora other = (DetalleOperacionEmpacadora) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.DetalleOperacionEmpacadora[ codigo=" + codigo + " ]";
    }
    
}
