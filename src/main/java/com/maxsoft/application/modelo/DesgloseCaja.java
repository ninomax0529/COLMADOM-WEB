/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;


import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "desglose_caja")
public class DesgloseCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caja_turno_id", nullable = false)
    private CajaTurno cajaTurno;

    @Column(nullable = false)
    private BigDecimal denominacion; // Ej: 2000.0, 1000.0, 500.0, 200.0, 100.0, 50.0, 25.0, 10.0, 5.0, 1.0

    @Column(nullable = false)
    private int cantidad; // Cuántas unidades de esta denominación hay (ej: 5 billetes de 1000)

    @Column(nullable = false)
    private BigDecimal subtotal; // denominacion * cantidad

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CajaTurno getCajaTurno() { return cajaTurno; }
    public void setCajaTurno(CajaTurno cajaTurno) { this.cajaTurno = cajaTurno; }

    public BigDecimal getDenominacion() { return denominacion; }
    public void setDenominacion(BigDecimal denominacion) { this.denominacion = denominacion; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad; 
        if (this.denominacion != null) {
            this.subtotal = this.denominacion.multiply(BigDecimal.valueOf(cantidad));
        }
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}