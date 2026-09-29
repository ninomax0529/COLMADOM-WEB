/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.compra;

import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.repo.RecepcionMercanciaRepo;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;

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
        
        List<DetalleRecepcionMercancia> detalle=this.recepcionRepo.getDetalle(recepcionDB.getCodigo());
        

        EntradaInventario entradaCreada = entradaInventarioService.crearEntradaPorRecepcion(recepcionDB, detalle);
        
        return recepcionDB;
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
       return  recepcionRepo.getDetalle(op);
    }

}
