/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

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
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "detalle_recibo_de_egreso")
@NamedQueries({
    @NamedQuery(name = "DetalleReciboDeEgreso.findAll", query = "SELECT d FROM DetalleReciboDeEgreso d")})
public class DetalleReciboDeEgreso implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "total")
    private BigDecimal total;
    @Column(name = "balance_pendiente_antes")
    private BigDecimal balancePendienteAntes;
    @Column(name = "balance_pendiente_despues")
    private BigDecimal balancePendienteDespues;
    @Size(max = 250)
    @Column(name = "concepto")
    private String concepto;
    @JoinColumn(name = "factura", referencedColumnName = "codigo")
    @ManyToOne
    private FacturaDeCompra factura;
    @JoinColumn(name = "recibo_egreso", referencedColumnName = "codigo")
    @ManyToOne
    private ReciboDeEgreso reciboEgreso;

    public DetalleReciboDeEgreso() {
    }

    public DetalleReciboDeEgreso(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getBalancePendienteAntes() {
        return balancePendienteAntes;
    }

    public void setBalancePendienteAntes(BigDecimal balancePendienteAntes) {
        this.balancePendienteAntes = balancePendienteAntes;
    }

    public BigDecimal getBalancePendienteDespues() {
        return balancePendienteDespues;
    }

    public void setBalancePendienteDespues(BigDecimal balancePendienteDespues) {
        this.balancePendienteDespues = balancePendienteDespues;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public FacturaDeCompra getFactura() {
        return factura;
    }

    public void setFactura(FacturaDeCompra factura) {
        this.factura = factura;
    }

    public ReciboDeEgreso getReciboEgreso() {
        return reciboEgreso;
    }

    public void setReciboEgreso(ReciboDeEgreso reciboEgreso) {
        this.reciboEgreso = reciboEgreso;
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
        if (!(object instanceof DetalleReciboDeEgreso)) {
            return false;
        }
        DetalleReciboDeEgreso other = (DetalleReciboDeEgreso) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleReciboDeEgreso[ codigo=" + codigo + " ]";
    }
    
}
