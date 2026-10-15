/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.compra;

import com.maxsoft.application.modelo.DetalleOrdendeDeCompra;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.repo.RecepcionMercanciaRepo;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecepcionMercanciaServiceImpl implements RecepcionMercanciaService {

    private final RecepcionMercanciaRepo recepcionRepo;
    private final ArticuloService articuloService;
    private final OrdenDeCompraService ordenCompraService;
    private EntradaDeInventarioService entradaInventarioService;

    public RecepcionMercanciaServiceImpl(RecepcionMercanciaRepo recepcionRepo,
            ArticuloService articuloService,
            OrdenDeCompraService ordenCompraService,
            EntradaDeInventarioService entradaInventarioService
    ) {
        this.recepcionRepo = recepcionRepo;
        this.articuloService = articuloService;
        this.ordenCompraService = ordenCompraService;
        this.entradaInventarioService = entradaInventarioService;
    }

    @Override
    @Transactional
    public RecepcionMercancia procesarRecepcion(RecepcionMercancia recepcion) {

        // 1. Asignar la relación bidireccional en el detalle
        if (recepcion.getDetalleRecepcionMercanciaCollection() != null) {
            recepcion.getDetalleRecepcionMercanciaCollection().forEach(o -> {

                o.setCodigo(null); // Asegurar que sea una inserción nueva
                o.setRecepcionMercancia(recepcion);
            });
        }

        // 2. Cambiar estado de la recepción a PROCESADO (si usas Enum o String)
//    recepcion.setEstado(EstadoDocumento.PROCESADO); // o recepcion.setEstado("PROCESADO");
        // 3. Guardar la Recepción de Mercancía
        RecepcionMercancia recepcionDB = recepcionRepo.save(recepcion);

        List<DetalleRecepcionMercancia> detalle = this.recepcionRepo.getDetalle(recepcionDB.getCodigo());

        EntradaInventario entradaCreada = entradaInventarioService.crearEntradaPorRecepcion(recepcionDB, detalle);

        actualizarOrdenDeCompra(recepcionDB);

        return recepcionDB;
    }

    private void actualizarOrdenDeCompra1(RecepcionMercancia rec) {
        
        if (rec.getOrdenCompra()== null) {
            return; // recepción sin orden: nada que actualizar
        }

        // Recargar la orden dentro de la transacción (la de la vista está desconectada)
        OrdenDeCompra orden = this.ordenCompraService.buscarPorCodigo(rec.getOrdenCompra().getCodigo())
                .orElseThrow(() -> new IllegalStateException("La orden de compra ya no existe."));

        if (orden.getDetalleOrdendeDeCompraCollection() == null
                || orden.getDetalleOrdendeDeCompraCollection().isEmpty()) {
            throw new IllegalStateException("La orden " + orden.getNumero() + " no tiene detalle.");
        }

        for (DetalleRecepcionMercancia det : rec.getDetalleRecepcionMercanciaCollection()) {
            
            DetalleOrdendeDeCompra linea = buscarLineaOrden(orden, det);

            BigDecimal recibida = det.getCantidadRecibida() != null ? det.getCantidadRecibida() : BigDecimal.ZERO;
            BigDecimal pendiente = linea.getPendiente() != null ? linea.getPendiente() : linea.getCantidad();

            if (recibida.compareTo(pendiente) > 0) {
                throw new IllegalStateException("Se intenta recibir " + recibida.stripTrailingZeros().toPlainString()
                        + " de " + det.getDescripcionArticulo() + " y solo hay "
                        + pendiente.stripTrailingZeros().toPlainString() + " pendiente en la orden.");
            }

            BigDecimal recibidaAntes = linea.getCantidadRecibida() != null ? linea.getCantidadRecibida() : BigDecimal.ZERO;
            linea.setCantidadRecibida(recibidaAntes.add(recibida));
            linea.setPendiente(pendiente.subtract(recibida));
        }

        // COMPLETADA si no queda nada pendiente en ninguna línea; si no, PARCIAL
        boolean completa = orden.getDetalleOrdendeDeCompraCollection().stream()
                .allMatch(l -> {
                    BigDecimal p = l.getPendiente() != null ? l.getPendiente() : l.getCantidad();
                    return p.compareTo(BigDecimal.ZERO) <= 0;
                });

        orden.setEstado(completa ? "COMPLETADA" : "PARCIAL");
        orden.setCompletada(completa ? "SI" : "NO");

        this.ordenCompraService.guardar(orden);
    }
    
    
    private void actualizarOrdenDeCompra(RecepcionMercancia rec) {
        
    if (rec.getOrdenCompra()== null) {
        return; // recepción sin orden: nada que actualizar
    }

    // Recargar la orden dentro de la transacción (la de la vista está desconectada)
    OrdenDeCompra orden = this.ordenCompraService.buscarPorCodigo(rec.getOrdenCompra().getCodigo())
            .orElseThrow(() -> new IllegalStateException("La orden de compra ya no existe."));

    for (DetalleRecepcionMercancia det : rec.getDetalleRecepcionMercanciaCollection()) {
        // Mismo artículo + mismo empaque identifica la línea (la vista de órdenes no permite duplicarlos)
        DetalleOrdendeDeCompra linea = orden.getDetalleOrdendeDeCompraCollection().stream()
                .filter(l -> l.getArticulo().getCodigo().equals(det.getArticulo().getCodigo())
                        && l.getArticuloEmpaque() != null && det.getArticuloEmpaque() != null
                        && l.getArticuloEmpaque().getCodigo().equals(det.getArticuloEmpaque().getCodigo()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "El artículo " + det.getDescripcionArticulo() + " no pertenece a la orden."));

        BigDecimal pendiente = linea.getPendiente() != null ? linea.getPendiente() : linea.getCantidad();
        BigDecimal recibida = det.getCantidadRecibida();

        if (recibida.compareTo(pendiente) > 0) {
            throw new IllegalStateException("Se intenta recibir más de lo pendiente de "
                    + det.getDescripcionArticulo() + ".");
        }

        BigDecimal recibidaAntes = linea.getCantidadRecibida() != null ? linea.getCantidadRecibida() : BigDecimal.ZERO;
        linea.setCantidadRecibida(recibidaAntes.add(recibida));
        linea.setPendiente(pendiente.subtract(recibida));
    }

    // Estado: COMPLETADA si no queda nada pendiente en ninguna línea; si no, PARCIAL
    boolean completa = orden.getDetalleOrdendeDeCompraCollection().stream()
            .allMatch(l -> {
                BigDecimal p = l.getPendiente() != null ? l.getPendiente() : l.getCantidad();
                return p.compareTo(BigDecimal.ZERO) <= 0;
            });

    orden.setEstado(completa ? "COMPLETADA" : "PARCIAL");
    orden.setCompletada(completa ? "SI" : "NO");

    this.ordenCompraService.guardar(orden);
}

    private DetalleOrdendeDeCompra buscarLineaOrden(OrdenDeCompra orden, DetalleRecepcionMercancia det) {
        
        List<DetalleOrdendeDeCompra> delArticulo = orden.getDetalleOrdendeDeCompraCollection().stream()
                .filter(l -> l.getArticulo() != null && det.getArticulo() != null
                && mismoId(l.getArticulo().getCodigo(), det.getArticulo().getCodigo()))
                .toList();

        // 1) Mismo artículo + mismo empaque
        for (DetalleOrdendeDeCompra l : delArticulo) {
            
            if (l.getArticuloEmpaque() != null && det.getArticuloEmpaque() != null
                    && mismoId(l.getArticuloEmpaque().getCodigo(), det.getArticuloEmpaque().getCodigo())
                    ) {
                return l;
            }
        }

        // 2) Orden antigua: la línea no tiene empaque y es la única de ese artículo
        if (delArticulo.size() == 1 && delArticulo.get(0).getArticuloEmpaque() == null) {
            return delArticulo.get(0);
        }

        throw new IllegalStateException("No se encontró en la orden " + orden.getNumero()
                + " la línea del artículo " + det.getDescripcionArticulo()
                + (det.getArticuloEmpaque() != null ? " (empaque " + det.getArticuloEmpaque().getCodigo() + ")" : "") + ".");
    }

    private boolean mismoId(Object a, Object b) {
        return a != null && b != null && String.valueOf(a).equals(String.valueOf(b));
    }

    @Override
    public List<RecepcionMercancia> getLista() {
        return recepcionRepo.findAll();
    }

    @Override
    public RecepcionMercancia buscarPorId(Long id) {

        // 3. Si viene de una Orden de Compra, actualizar cantidades recibidas y pendientes
//        if (recepcion.getTipoDocumento().getCodigo()==8 && recepcion.getNumeroDocumento()!= null) {
//            OrdenDeCompra oc = ordenCompraService.buscarPorCodigo(Integer.valueOf(recepcion.getNombreDocumento())).get();
//            
//            if (oc != null) {
//                
//                boolean todoCompletado = true;
//
//                for (DetalleOrdendeDeCompra detOc : oc.getDetalleOrdendeDeCompraCollection()) {
//                    
//                    for (DetalleRecepcionMercancia detRec : recepcion.getDetalleRecepcionMercanciaCollection()) {
//                        if (detOc.getArticulo() != null && detRec.getArticulo() != null &&
//                                detOc.getArticulo().getCodigo().equals(detRec.getArticulo().getCodigo())) {
//
//                            BigDecimal recAnterior = detOc.getCantidadRecibida() != null ? detOc.getCantidadRecibida() : BigDecimal.ZERO;
//                            BigDecimal nuevaRecibida = recAnterior.add(detRec.getCantidadRecibida());
//
//                            detOc.setCantidadRecibida(nuevaRecibida);
//
//                            BigDecimal pendiente = detOc.getCantidad().subtract(nuevaRecibida);
//                            detOc.setPendiente(pendiente.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pendiente);
//                        }
//                    }
//
//                    if (detOc.getPendiente() != null && detOc.getPendiente().compareTo(BigDecimal.ZERO) > 0) {
//                        todoCompletado = false;
//                    }
//                }
//
//                if (todoCompletado) {
//                    oc.setCompletada("SI");
//                    oc.setEstado("COMPLETADA");
//                } else {
//                    oc.setEstado("PARCIAL");
//                }
//
//                ordenCompraService.guardar(oc);
//            }
//        }
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<DetalleRecepcionMercancia> getDetalle(int op) {
        return recepcionRepo.getDetalle(op);
    }

}
