/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caja_turno")
public class CajaTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false)
    private BigDecimal montoApertura; // Fondo inicial en caja (ej. RD$ 2,000)

    private BigDecimal montoCierreReal; // Lo que el cajero contó físicamente al cerrar

    private BigDecimal ventasEfectivoEsperadas; // Calculado por el sistema (Ventas + Apertura)

    private BigDecimal ventasTarjeta; // Total cobrado con datáfono / tarjeta

    private BigDecimal ventasFiado; // Total registrado a crédito

    private BigDecimal diferencia; // Sobrante (+) o Faltante (-)

    @Column(nullable = false)
    private String cajeroApertura; // Usuario o nombre del cajero que abrió

    private String cajeroCierre; // Usuario que cerró

    @Column(nullable = false)
    private String estado; // "ABIERTA", "CERRADA"

    @Column(columnDefinition = "TEXT")
    private String observaciones;
    // Relación con el desglose de efectivo al cerrar
    @OneToMany(mappedBy = "cajaTurno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DesgloseCaja> desgloses = new ArrayList<>();

    public List<DesgloseCaja> getDesgloses() {
        return desgloses;
    }

    public void setDesgloses(List<DesgloseCaja> desgloses) {
        this.desgloses = desgloses;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoApertura() {
        return montoApertura;
    }

    public void setMontoApertura(BigDecimal montoApertura) {
        this.montoApertura = montoApertura;
    }

    public BigDecimal getMontoCierreReal() {
        return montoCierreReal;
    }

    public void setMontoCierreReal(BigDecimal montoCierreReal) {
        this.montoCierreReal = montoCierreReal;
    }

    public BigDecimal getVentasEfectivoEsperadas() {
        return ventasEfectivoEsperadas;
    }

    public void setVentasEfectivoEsperadas(BigDecimal ventasEfectivoEsperadas) {
        this.ventasEfectivoEsperadas = ventasEfectivoEsperadas;
    }

    public BigDecimal getVentasTarjeta() {
        return ventasTarjeta;
    }

    public void setVentasTarjeta(BigDecimal ventasTarjeta) {
        this.ventasTarjeta = ventasTarjeta;
    }

    public BigDecimal getVentasFiado() {
        return ventasFiado;
    }

    public void setVentasFiado(BigDecimal ventasFiado) {
        this.ventasFiado = ventasFiado;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getCajeroApertura() {
        return cajeroApertura;
    }

    public void setCajeroApertura(String cajeroApertura) {
        this.cajeroApertura = cajeroApertura;
    }

    public String getCajeroCierre() {
        return cajeroCierre;
    }

    public void setCajeroCierre(String cajeroCierre) {
        this.cajeroCierre = cajeroCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
