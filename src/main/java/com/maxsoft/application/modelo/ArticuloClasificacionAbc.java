package com.maxsoft.application.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Último resultado de la clasificación ABC de un artículo. Lo escribe solo
 * ClasificacionAbcService (cada noche, al arrancar si está viejo o con
 * "Recalcular"); no se edita a mano.
 */
@Entity
@Table(name = "articulo_clasificacion_abc")
public class ArticuloClasificacionAbc implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Código del artículo. */
    @Id
    @Column(name = "articulo")
    private Integer articulo;

    /** A / B / C combinada, o N (nuevo). */
    @Column(name = "clase", nullable = false, length = 1)
    private String clase;

    @Column(name = "clase_valor", length = 1)
    private String claseValor;

    @Column(name = "clase_rotacion", length = 1)
    private String claseRotacion;

    @Column(name = "sin_movimiento", nullable = false)
    private Boolean sinMovimiento = false;

    @Column(name = "ventas", nullable = false)
    private BigDecimal ventas = BigDecimal.ZERO;

    @Column(name = "facturas", nullable = false)
    private Integer facturas = 0;

    @Column(name = "unidades", nullable = false)
    private BigDecimal unidades = BigDecimal.ZERO;

    @Column(name = "porciento_valor", nullable = false)
    private BigDecimal porcientoValor = BigDecimal.ZERO;

    @Column(name = "porciento_rotacion", nullable = false)
    private BigDecimal porcientoRotacion = BigDecimal.ZERO;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    public Integer getArticulo() {
        return articulo;
    }

    public void setArticulo(Integer articulo) {
        this.articulo = articulo;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public String getClaseValor() {
        return claseValor;
    }

    public void setClaseValor(String claseValor) {
        this.claseValor = claseValor;
    }

    public String getClaseRotacion() {
        return claseRotacion;
    }

    public void setClaseRotacion(String claseRotacion) {
        this.claseRotacion = claseRotacion;
    }

    public Boolean getSinMovimiento() {
        return sinMovimiento;
    }

    public void setSinMovimiento(Boolean sinMovimiento) {
        this.sinMovimiento = sinMovimiento;
    }

    public BigDecimal getVentas() {
        return ventas;
    }

    public void setVentas(BigDecimal ventas) {
        this.ventas = ventas;
    }

    public Integer getFacturas() {
        return facturas;
    }

    public void setFacturas(Integer facturas) {
        this.facturas = facturas;
    }

    public BigDecimal getUnidades() {
        return unidades;
    }

    public void setUnidades(BigDecimal unidades) {
        this.unidades = unidades;
    }

    public BigDecimal getPorcientoValor() {
        return porcientoValor;
    }

    public void setPorcientoValor(BigDecimal porcientoValor) {
        this.porcientoValor = porcientoValor;
    }

    public BigDecimal getPorcientoRotacion() {
        return porcientoRotacion;
    }

    public void setPorcientoRotacion(BigDecimal porcientoRotacion) {
        this.porcientoRotacion = porcientoRotacion;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }
}
