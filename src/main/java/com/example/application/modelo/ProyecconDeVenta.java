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
@Table(name = "proyeccon_de_venta")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProyecconDeVenta.findAll", query = "SELECT p FROM ProyecconDeVenta p")})
public class ProyecconDeVenta implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 40)
    @Column(name = "nombre_usuario")
    private String nombreUsuario;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total")
    private int total;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "proyeccionVenta")
    private Collection<DetalleProyeccionDeVenta> detalleProyeccionDeVentaCollection;
    @JoinColumn(name = "usaurio", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Usuariop usaurio;

    public ProyecconDeVenta() {
    }

    public ProyecconDeVenta(Integer codigo) {
        this.codigo = codigo;
    }

    public ProyecconDeVenta(Integer codigo, Date fecha, Date fechaRegistro, String nombreUsuario, int total) {
        this.codigo = codigo;
        this.fecha = fecha;
        this.fechaRegistro = fechaRegistro;
        this.nombreUsuario = nombreUsuario;
        this.total = total;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @XmlTransient
    public Collection<DetalleProyeccionDeVenta> getDetalleProyeccionDeVentaCollection() {
        return detalleProyeccionDeVentaCollection;
    }

    public void setDetalleProyeccionDeVentaCollection(Collection<DetalleProyeccionDeVenta> detalleProyeccionDeVentaCollection) {
        this.detalleProyeccionDeVentaCollection = detalleProyeccionDeVentaCollection;
    }

    public Usuariop getUsaurio() {
        return usaurio;
    }

    public void setUsaurio(Usuariop usaurio) {
        this.usaurio = usaurio;
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
        if (!(object instanceof ProyecconDeVenta)) {
            return false;
        }
        ProyecconDeVenta other = (ProyecconDeVenta) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.example.application.modelo.ProyecconDeVenta[ codigo=" + codigo + " ]";
    }
    
}
