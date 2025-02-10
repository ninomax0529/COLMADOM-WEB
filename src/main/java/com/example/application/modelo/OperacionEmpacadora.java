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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "operacion_empacadora")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OperacionEmpacadora.findAll", query = "SELECT o FROM OperacionEmpacadora o")})
public class OperacionEmpacadora implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Column(name = "fecha_actualicion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaActualicion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;
    @Column(name = "fecha_final")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFinal;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hora_inicio")
    @Temporal(TemporalType.TIME)
    private Date horaInicio;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hora__final")
    @Temporal(TemporalType.TIME)
    private Date horaFinal;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "nombre_turno")
    private String nombreTurno;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Basic(optional = false)
    @NotNull
    @Column(name = "control")
    private int control;
    @Basic(optional = false)
    @NotNull
    @Column(name = "operador")
    private int operador;
    @Basic(optional = false)
    @NotNull
    @Column(name = "supervisor")
    private int supervisor;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "nombre_control")
    private String nombreControl;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "nombre_supervisor")
    private String nombreSupervisor;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "nombre_operador")
    private String nombreOperador;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "nombre_estado")
    private String nombreEstado;
    @Size(max = 80)
    @Column(name = "cerrado_por")
    private String cerradoPor;
    @Basic(optional = false)
    @NotNull
    @Column(name = "anulado")
    private boolean anulado;
    @Size(max = 60)
    @Column(name = "anulado_por")
    private String anuladoPor;
    @Column(name = "fecha_anulado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAnulado;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "operacionEmpacadora")
    private Collection<DetalleMovimientoProducto> detalleMovimientoProductoCollection;
    @JoinColumn(name = "turno", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Turno turno;
    @JoinColumn(name = "usuario", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Usuariop usuario;
    @JoinColumn(name = "estado", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private EstadoTurno estado;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "operacionEmpacadora")
    private Collection<DetalleOperacionEmpacadora> detalleOperacionEmpacadoraCollection;

    public OperacionEmpacadora() {
    }

    public OperacionEmpacadora(Integer codigo) {
        this.codigo = codigo;
    }

    public OperacionEmpacadora(Integer codigo, Date fechaCreacion, Date fechaInicio, Date horaInicio, Date horaFinal, String nombreTurno, String nombreUsuario, int control, int operador, int supervisor, String nombreControl, String nombreSupervisor, String nombreOperador, String nombreEstado, boolean anulado) {
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
        this.fechaInicio = fechaInicio;
        this.horaInicio = horaInicio;
        this.horaFinal = horaFinal;
        this.nombreTurno = nombreTurno;
        this.nombreUsuario = nombreUsuario;
        this.control = control;
        this.operador = operador;
        this.supervisor = supervisor;
        this.nombreControl = nombreControl;
        this.nombreSupervisor = nombreSupervisor;
        this.nombreOperador = nombreOperador;
        this.nombreEstado = nombreEstado;
        this.anulado = anulado;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaActualicion() {
        return fechaActualicion;
    }

    public void setFechaActualicion(Date fechaActualicion) {
        this.fechaActualicion = fechaActualicion;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public Date getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Date horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Date getHoraFinal() {
        return horaFinal;
    }

    public void setHoraFinal(Date horaFinal) {
        this.horaFinal = horaFinal;
    }

    public String getNombreTurno() {
        return nombreTurno;
    }

    public void setNombreTurno(String nombreTurno) {
        this.nombreTurno = nombreTurno;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public int getOperador() {
        return operador;
    }

    public void setOperador(int operador) {
        this.operador = operador;
    }

    public int getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(int supervisor) {
        this.supervisor = supervisor;
    }

    public String getNombreControl() {
        return nombreControl;
    }

    public void setNombreControl(String nombreControl) {
        this.nombreControl = nombreControl;
    }

    public String getNombreSupervisor() {
        return nombreSupervisor;
    }

    public void setNombreSupervisor(String nombreSupervisor) {
        this.nombreSupervisor = nombreSupervisor;
    }

    public String getNombreOperador() {
        return nombreOperador;
    }

    public void setNombreOperador(String nombreOperador) {
        this.nombreOperador = nombreOperador;
    }

    public String getNombreEstado() {
        return nombreEstado;
    }

    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    public String getCerradoPor() {
        return cerradoPor;
    }

    public void setCerradoPor(String cerradoPor) {
        this.cerradoPor = cerradoPor;
    }

    public boolean getAnulado() {
        return anulado;
    }

    public void setAnulado(boolean anulado) {
        this.anulado = anulado;
    }

    public String getAnuladoPor() {
        return anuladoPor;
    }

    public void setAnuladoPor(String anuladoPor) {
        this.anuladoPor = anuladoPor;
    }

    public Date getFechaAnulado() {
        return fechaAnulado;
    }

    public void setFechaAnulado(Date fechaAnulado) {
        this.fechaAnulado = fechaAnulado;
    }

    @XmlTransient
    public Collection<DetalleMovimientoProducto> getDetalleMovimientoProductoCollection() {
        return detalleMovimientoProductoCollection;
    }

    public void setDetalleMovimientoProductoCollection(Collection<DetalleMovimientoProducto> detalleMovimientoProductoCollection) {
        this.detalleMovimientoProductoCollection = detalleMovimientoProductoCollection;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Usuariop getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuariop usuario) {
        this.usuario = usuario;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    @XmlTransient
    public Collection<DetalleOperacionEmpacadora> getDetalleOperacionEmpacadoraCollection() {
        return detalleOperacionEmpacadoraCollection;
    }

    public void setDetalleOperacionEmpacadoraCollection(Collection<DetalleOperacionEmpacadora> detalleOperacionEmpacadoraCollection) {
        this.detalleOperacionEmpacadoraCollection = detalleOperacionEmpacadoraCollection;
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
        if (!(object instanceof OperacionEmpacadora)) {
            return false;
        }
        OperacionEmpacadora other = (OperacionEmpacadora) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.OperacionEmpacadora[ codigo=" + codigo + " ]";
    }
    
}
