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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Maximiliano
 */
@Entity
@Table(name = "detalle_liquidacion_delivery")
@NamedQueries({
    @NamedQuery(name = "DetalleLiquidacionDelivery.findAll", query = "SELECT d FROM DetalleLiquidacionDelivery d")})
public class DetalleLiquidacionDelivery implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo")
    private Integer codigo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "tipo_pago")
    private String tipoPago;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "monto")
    private BigDecimal monto;
    @JoinColumn(name = "cliente", referencedColumnName = "codigo")
    @ManyToOne
    private Cliente cliente;
    @JoinColumn(name = "factura", referencedColumnName = "codigo")
    @OneToOne(optional = false)
    private FacturaDeVenta factura;
    @JoinColumn(name = "liquidacion_delivery", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private LiquidacionDelivery liquidacionDelivery;

    public DetalleLiquidacionDelivery() {
    }

    public DetalleLiquidacionDelivery(Integer codigo) {
        this.codigo = codigo;
    }

    public DetalleLiquidacionDelivery(Integer codigo, String tipoPago, BigDecimal monto) {
        this.codigo = codigo;
        this.tipoPago = tipoPago;
        this.monto = monto;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public FacturaDeVenta getFactura() {
        return factura;
    }

    public void setFactura(FacturaDeVenta factura) {
        this.factura = factura;
    }

    public LiquidacionDelivery getLiquidacionDelivery() {
        return liquidacionDelivery;
    }

    public void setLiquidacionDelivery(LiquidacionDelivery liquidacionDelivery) {
        this.liquidacionDelivery = liquidacionDelivery;
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
        if (!(object instanceof DetalleLiquidacionDelivery)) {
            return false;
        }
        DetalleLiquidacionDelivery other = (DetalleLiquidacionDelivery) object;
        if ((this.codigo == null && other.codigo != null) || (this.codigo != null && !this.codigo.equals(other.codigo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.maxsoft.application.modelo.DetalleLiquidacionDelivery[ codigo=" + codigo + " ]";
    }
    
}
