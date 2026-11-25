package com.maxsoft.application.modelo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conteo cíclico de un almacén: los artículos contados un día y los ajustes
 * que se generaron por las diferencias. Ver ConteoCiclicoService.
 */
@Entity
@Table(name = "conteo_inventario")
public class ConteoInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "almacen", nullable = false)
    private Integer almacen;

    @Column(name = "nombre_almacen")
    private String nombreAlmacen;

    @Column(name = "nombre_usuario")
    private String nombreUsuario;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "articulos", nullable = false)
    private Integer articulos = 0;

    @Column(name = "con_diferencia", nullable = false)
    private Integer conDiferencia = 0;

    /** Ajuste de inventario (INCREMENTO) por lo que sobró; null si no hubo. */
    @Column(name = "ajuste_incremento")
    private Integer ajusteIncremento;

    /** Ajuste de inventario (DISMINUCION) por lo que faltó; null si no hubo. */
    @Column(name = "ajuste_disminucion")
    private Integer ajusteDisminucion;

    @OneToMany(mappedBy = "conteo", cascade = CascadeType.ALL)
    private List<DetalleConteoInventario> detalles = new ArrayList<>();

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getAlmacen() {
        return almacen;
    }

    public void setAlmacen(Integer almacen) {
        this.almacen = almacen;
    }

    public String getNombreAlmacen() {
        return nombreAlmacen;
    }

    public void setNombreAlmacen(String nombreAlmacen) {
        this.nombreAlmacen = nombreAlmacen;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Integer getArticulos() {
        return articulos;
    }

    public void setArticulos(Integer articulos) {
        this.articulos = articulos;
    }

    public Integer getConDiferencia() {
        return conDiferencia;
    }

    public void setConDiferencia(Integer conDiferencia) {
        this.conDiferencia = conDiferencia;
    }

    public Integer getAjusteIncremento() {
        return ajusteIncremento;
    }

    public void setAjusteIncremento(Integer ajusteIncremento) {
        this.ajusteIncremento = ajusteIncremento;
    }

    public Integer getAjusteDisminucion() {
        return ajusteDisminucion;
    }

    public void setAjusteDisminucion(Integer ajusteDisminucion) {
        this.ajusteDisminucion = ajusteDisminucion;
    }

    public List<DetalleConteoInventario> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleConteoInventario> detalles) {
        this.detalles = detalles;
    }
}
