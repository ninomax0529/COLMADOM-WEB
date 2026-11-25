package com.maxsoft.application.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/** Un artículo contado: lo que decía el sistema, lo contado y la diferencia (unidad base). */
@Entity
@Table(name = "detalle_conteo_inventario")
public class DetalleConteoInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conteo", referencedColumnName = "codigo")
    private ConteoInventario conteo;

    @Column(name = "articulo", nullable = false)
    private Integer articulo;

    @Column(name = "descripcion_articulo")
    private String descripcionArticulo;

    /** Clase ABC el día del conteo. */
    @Column(name = "clase")
    private String clase;

    @Column(name = "existencia_sistema", nullable = false)
    private BigDecimal existenciaSistema;

    @Column(name = "cantidad_contada", nullable = false)
    private BigDecimal cantidadContada;

    /** Contada menos sistema: positiva sobró, negativa faltó. */
    @Column(name = "diferencia", nullable = false)
    private BigDecimal diferencia;

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public ConteoInventario getConteo() {
        return conteo;
    }

    public void setConteo(ConteoInventario conteo) {
        this.conteo = conteo;
    }

    public Integer getArticulo() {
        return articulo;
    }

    public void setArticulo(Integer articulo) {
        this.articulo = articulo;
    }

    public String getDescripcionArticulo() {
        return descripcionArticulo;
    }

    public void setDescripcionArticulo(String descripcionArticulo) {
        this.descripcionArticulo = descripcionArticulo;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public BigDecimal getExistenciaSistema() {
        return existenciaSistema;
    }

    public void setExistenciaSistema(BigDecimal existenciaSistema) {
        this.existenciaSistema = existenciaSistema;
    }

    public BigDecimal getCantidadContada() {
        return cantidadContada;
    }

    public void setCantidadContada(BigDecimal cantidadContada) {
        this.cantidadContada = cantidadContada;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }
}
