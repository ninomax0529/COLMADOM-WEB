/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.ArticuloAlmacen;
import com.maxsoft.application.modelo.DetalleTrasladoInventario;
import com.maxsoft.application.modelo.TrasladoInventario;
import com.maxsoft.application.repo.TrasladoInventarioRepo;
import com.maxsoft.application.servicio.interfaces.EstadoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloAlmacenService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TrasladoInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class TrasladoInventarioServiceImpl implements TrasladoInventarioService {

    private final TrasladoInventarioRepo trasladoRepo;
    private final ArticuloAlmacenService articuloAlmacenService;
    EstadoDocumentoService estadoDocumentoService;
    private EntradaDeInventarioService entradaInventarioService;

    SalidaInventarioService SalidaInventarioService;

    @Autowired
    public TrasladoInventarioServiceImpl(TrasladoInventarioRepo trasladoRepo,
            ArticuloAlmacenService articuloAlmacenService,
            EstadoDocumentoService estadoDocumentoService,
            EntradaDeInventarioService entradaInventarioService,
            SalidaInventarioService SalidaInventarioService
    ) {

        this.trasladoRepo = trasladoRepo;
        this.articuloAlmacenService = articuloAlmacenService;
        this.estadoDocumentoService = estadoDocumentoService;
        this.entradaInventarioService = entradaInventarioService;
        this.SalidaInventarioService = SalidaInventarioService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrasladoInventario procesarTraslado(TrasladoInventario traslado) {

        // -----------------------------------------------------------------
        // 1. VALIDACIONES DE NEGOCIO PREVIAS
        // -----------------------------------------------------------------
        if (traslado.getAlmacenOrigen() == null || traslado.getAlmacenDestino() == null) {
            throw new IllegalArgumentException("Los almacenes de origen y destino son obligatorios.");
        }

        if (traslado.getAlmacenOrigen().getCodigo().equals(traslado.getAlmacenDestino().getCodigo())) {
            throw new IllegalArgumentException("El almacén de origen y destino no pueden ser el mismo.");
        }

        if (traslado.getDetalleTrasladoInventarioCollection() == null || traslado.getDetalleTrasladoInventarioCollection().isEmpty()) {
            throw new IllegalArgumentException("El traslado debe contener al menos un detalle de artículo.");
        }

        // -----------------------------------------------------------------
        // 2. PROCESAMIENTO DE STOCK ARTÍCULO POR ARTÍCULO
        // -----------------------------------------------------------------
        for (DetalleTrasladoInventario detalle : traslado.getDetalleTrasladoInventarioCollection()) {

            Integer idArticulo = detalle.getArticulo().getCodigo();
            Integer idOrigen = traslado.getAlmacenOrigen().getCodigo();
            Integer idDestino = traslado.getAlmacenDestino().getCodigo();
            BigDecimal cantidadTransferir = detalle.getCantidadEnviada();

            if (cantidadTransferir == null || cantidadTransferir.compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException("La cantidad enviada debe ser mayor a cero para el artículo ID: " + idArticulo);
            }

            // A) Verificar y actualizar stock en Almacén Origen
            ArticuloAlmacen stockOrigen = articuloAlmacenService.buscarPorArticuloYAlmacen(idArticulo, idOrigen)
                    .orElseThrow(() -> new IllegalStateException(
                    "El artículo '" + detalle.getArticulo().getDescripcion() + "' no está registrado en el almacén de origen."));

            if (stockOrigen.getExistencia().compareTo(cantidadTransferir) < 0) {

                throw new IllegalStateException("Stock insuficiente en origen para el artículo: "
                        + detalle.getArticulo().getDescripcion()
                        + ". Disponible: " + stockOrigen.getExistencia()
                        + ", Requerido: " + cantidadTransferir);
            }

            // Descontar del origen
//            stockOrigen.setExistencia(stockOrigen.getExistencia().subtract(cantidadTransferir));
//            articuloAlmacenService.guardar(stockOrigen);

            // B) Verificar o crear registro de stock en Almacén Destino
            ArticuloAlmacen stockDestino = articuloAlmacenService.buscarPorArticuloYAlmacen(idArticulo, idDestino)
                    .orElseGet(() -> {
                        // Si el producto nunca ha existido en el almacén destino, se crea el registro inicial con stock 0
                        ArticuloAlmacen nuevoStock = new ArticuloAlmacen();
                        nuevoStock.setArticulo(detalle.getArticulo());
//                        nuevoStock.setAlmacen(stockDestino.getAlmacen());
                        nuevoStock.setExistencia(BigDecimal.ZERO);
                        nuevoStock.setMinimo(BigDecimal.ZERO);
                        nuevoStock.setMaximo(BigDecimal.ZERO);
                        return nuevoStock;
                    });

            // Sumar al destino
//            stockDestino.setExistencia(stockDestino.getExistencia().add(cantidadTransferir));
//            articuloAlmacenService.guardar(stockDestino);

            // Vinculación bidireccional del detalle con la cabecera
            detalle.setTraslado(traslado);

            // Si el traslado es directo, la cantidad recibida es igual a la enviada
            detalle.setCantidadRecibida(cantidadTransferir);
        }

        // -----------------------------------------------------------------
        // 3. ACTUALIZACIÓN DE ESTADOS Y PERSISTENCIA FINAL
        // -----------------------------------------------------------------
        traslado.setFechaEmision(new Date());

        traslado.setEstado(this.estadoDocumentoService.getEstado(3));

        traslado = trasladoRepo.save(traslado);

        List<DetalleTrasladoInventario> lista = traslado.getDetalleTrasladoInventarioCollection()
                .stream()
                .toList();

        this.entradaInventarioService.crearEntradaPorTraslado(traslado, lista);

        this.SalidaInventarioService.crearSalidaPorTraslado(traslado, lista);

        // Guarda la cabecera y en cascada guarda todos los detalles
        return traslado;
    }
}
