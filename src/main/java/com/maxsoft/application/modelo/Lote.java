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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lote de un artículo: mercancía que entró junta, con un mismo vencimiento.
 * La existencia del lote está por almacén en {@link LoteAlmacen} (unidad
 * base).
 */
@Entity
@Table(name = "lote")
public class Lote implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Número del lote creado al activar el control de lotes con la existencia que ya había. */
    public static final String NUMERO_INICIAL = "INICIAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @JoinColumn(name = "articulo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Articulo articulo;

    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    /** Null = sin vencimiento (nunca se considera vencido). */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDateTime fechaEntrada;

    /** Costo por unidad base al entrar. */
    @Column(name = "costo_unitario")
    private BigDecimal costoUnitario;

    @JoinColumn(name = "proveedor", referencedColumnName = "codigo")
    @ManyToOne
    private Proveedor proveedor;

    /** Documento que creó el lote, p. ej. REC-15, ENT-8, INICIAL. */
    @Column(name = "documento_origen", length = 60)
    private String documentoOrigen;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "creado_por", length = 50)
    private String creadoPor;

    public Lote() {
    }

    /** true si tiene vencimiento y ya pasó (vence hoy = todavía se puede vender). */
    public boolean isVencido(LocalDate hoy) {
        return fechaVencimiento != null && fechaVencimiento.isBefore(hoy);
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public String getDocumentoOrigen() {
        return documentoOrigen;
    }

    public void setDocumentoOrigen(String documentoOrigen) {
        this.documentoOrigen = documentoOrigen;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Lote other)) {
            return false;
        }
        return codigo != null && codigo.equals(other.codigo);
    }

    @Override
    public String toString() {
        return numeroLote;
    }
}
