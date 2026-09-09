/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_caja")
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caja_turno_id", nullable = false)
    private CajaTurno cajaTurno;

    @Column(nullable = false)
    private String tipo; // "RETIRO", "GASTO_MENOR", "INGRESO_EXTRA"

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private String descripcion; // Ej: "Compra de 2 bolsas de hielo para el colmado"

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String usuario;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CajaTurno getCajaTurno() { return cajaTurno; }
    public void setCajaTurno(CajaTurno cajaTurno) { this.cajaTurno = cajaTurno; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}