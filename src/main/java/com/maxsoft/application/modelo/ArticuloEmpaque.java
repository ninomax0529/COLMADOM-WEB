/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "articulo_empaque")
public class ArticuloEmpaque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo", nullable = false)
    private Articulo articulo;

   @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "unidad_empaque", nullable = false)
    private UnidadDeVenta unidadEmpaque; // Ej: CAJA, FARDO, PAQUETE, UNIDAD

    @Column(name = "factor_conversion", nullable = false, precision = 12, scale = 4)
    private BigDecimal factorConversion = BigDecimal.ONE; // Cuántas unidades base contiene este empaque

    @Column(name = "codigo_barra", length = 50)
    private String codigoBarra;

    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "es_empaque_base")
    private Boolean esEmpaqueBase = false;

    @Column(name = "es_empaque_compra")
    private Boolean esEmpaqueCompra = false;

    @Column(name = "es_empaque_venta")
    private Boolean esEmpaqueVenta = false;

    @Column(name = "estado")
    private Boolean estado = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "creado_por", length = 50)
    private String creadoPor;

    // Getters y Setters
    public Integer getCodigo() { return codigo; }
    public void setCodigo(Integer codigo) { this.codigo = codigo; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }

    public UnidadDeVenta getUnidadEmpaque() { return unidadEmpaque; }
    public void setUnidadEmpaque(UnidadDeVenta unidadEmpaque) { this.unidadEmpaque = unidadEmpaque; }

    public BigDecimal getFactorConversion() { return factorConversion; }
    public void setFactorConversion(BigDecimal factorConversion) { this.factorConversion = factorConversion; }

    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public Boolean getEsEmpaqueBase() { return esEmpaqueBase; }
    public void setEsEmpaqueBase(Boolean esEmpaqueBase) { this.esEmpaqueBase = esEmpaqueBase; }

    public Boolean getEsEmpaqueCompra() { return esEmpaqueCompra; }
    public void setEsEmpaqueCompra(Boolean esEmpaqueCompra) { this.esEmpaqueCompra = esEmpaqueCompra; }

    public Boolean getEsEmpaqueVenta() { return esEmpaqueVenta; }
    public void setEsEmpaqueVenta(Boolean esEmpaqueVenta) { this.esEmpaqueVenta = esEmpaqueVenta; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
}
