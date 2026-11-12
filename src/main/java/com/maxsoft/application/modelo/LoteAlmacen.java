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
import java.util.Objects;

/**
 * Existencia de un lote en un almacén, en unidad base. La suma de los lotes
 * de un artículo en un almacén debe ser igual a articulo_almacen.existencia.
 */
@Entity
@Table(name = "lote_almacen")
public class LoteAlmacen implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @JoinColumn(name = "lote", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Lote lote;

    @JoinColumn(name = "almacen", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Almacen almacen;

    @Column(name = "existencia", nullable = false)
    private BigDecimal existencia = BigDecimal.ZERO;

    public LoteAlmacen() {
    }

    public LoteAlmacen(Lote lote, Almacen almacen) {
        this.lote = lote;
        this.almacen = almacen;
        this.existencia = BigDecimal.ZERO;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Almacen almacen) {
        this.almacen = almacen;
    }

    public BigDecimal getExistencia() {
        return existencia != null ? existencia : BigDecimal.ZERO;
    }

    public void setExistencia(BigDecimal existencia) {
        this.existencia = existencia;
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
        if (!(obj instanceof LoteAlmacen other)) {
            return false;
        }
        return codigo != null && codigo.equals(other.codigo);
    }
}
