/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.evento.VentaAnuladaEvent;
import com.maxsoft.application.evento.VentaDevueltaEvent;
import com.maxsoft.application.evento.VentaRealizadaEvent;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.repo.FacturaDeventaRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaDeVentaServiceImpl implements FacturaDeVentaService {

    @Autowired
    private FacturaDeventaRepo facttRepo;

    @Autowired
    private CajaService cajaService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    ArticuloService articuloService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public FacturaDeVenta procesarVenta(TicketVenta ticketVenta, String nombreUsuario) {

        // 1. Validación de Caja
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja antes de registrar movimientos de POS"));

        // 2. Validaciones de Negocio e Inventario
        if (ticketVenta.getItems() == null || ticketVenta.getItems().isEmpty()) {
            throw new IllegalArgumentException("La factura no tiene detalle.");
        }

        for (DetalleFacturaDeVenta det : ticketVenta.getItems()) {
            if (det.getCantidad() <= 0) {
                throw new IllegalArgumentException("El artículo '" + det.getDescripcionArticulo() + "' tiene la cantidad en cero.");
            }

            // Validar stock solo si el artículo existe y es inventariable
            if (det.getArticulo() != null && det.getArticulo().getCodigo() != null
                    && Boolean.TRUE.equals(det.getArticulo().getInventariable())) {
                inventarioService.validarStockDisponible(det.getArticulo().getCodigo(), det.getCantidad());
            }
        }

        // 3. Mapeo y Construcción de la Factura
        FacturaDeVenta factura = new FacturaDeVenta();
        factura.setEstadoFactura(ticketVenta.getEstadoFactura());
        factura.setTipoVenta(ticketVenta.getTipoVenta());
        factura.setCliente(ticketVenta.getCliente());
        factura.setNombreCliente(ticketVenta.getNombreCliente());
        factura.setDireccion(ticketVenta.getDireccion());

        if (ticketVenta.getDelivery() != null) {
            factura.setDelivery(ticketVenta.getDelivery());
            factura.setNombreDelivery(ticketVenta.getDelivery().getNombre());
        } else {
            factura.setDelivery(null);
            factura.setNombreDelivery("N/A");
        }

        Date ahora = new Date();
        factura.setFecha(ahora);
        factura.setFechaCreacion(ahora);
        factura.setFechaActualizacion(ahora);
        factura.setNombreUsuario(nombreUsuario);
        factura.setTotal(ticketVenta.getTotalAmount());

        ticketVenta.getItems().forEach(item -> {
            item.setFactura(factura);
            item.setCodigo(null);
        });

        factura.setDetalleFacturaDeVentaCollection(ticketVenta.getItems());

        // 4. Persistencia de la factura
        FacturaDeVenta facturaGuardada = facttRepo.save(factura);

        // 5. Registrar Movimiento de Caja (Usando id como Long)
        cajaService.registrarMovimientoPos(
                turnoActual.getId().intValue(),
                facturaGuardada.getTipoVenta().getNombre(),
                BigDecimal.valueOf(facturaGuardada.getTotal()),
                "Ingreso por venta #" + facturaGuardada.getCodigo(),
                nombreUsuario
        );

        // 6. Publicación del Evento (Corregido)
        List<VentaRealizadaEvent.ItemVentaDto> itemsDto = facturaGuardada.getDetalleFacturaDeVentaCollection().stream()
                .filter(item -> item.getArticulo() != null)
                .map(item -> {
                    // Asegurar la extracción del ID del artículo (probando getCodigo() o getId())
                    Integer idArticulo = item.getArticulo().getCodigo();
                    return new VentaRealizadaEvent.ItemVentaDto(idArticulo, item.getCantidad());
                })
                // Filtrar DTOs cuyo idArticulo no sea nulo
                .filter(dto -> dto.getIdArticulo() != null)
                .collect(Collectors.toList());

// Log de depuración para confirmar cuántos ítems viajan en el evento
        System.out.println(">>> Publicando VentaRealizadaEvent con " + itemsDto.size() + " ítems. ID Factura: " + facturaGuardada.getCodigo());

        eventPublisher.publishEvent(new VentaRealizadaEvent(facturaGuardada.getCodigo(), itemsDto));

        return facturaGuardada;
    }

    @Transactional
    @Override
    public FacturaDeVenta anularVenta(Integer idFactura, String motivoAnulacion, String nombreUsuario) {

        // 1. Validar existencia y estado de la factura
        FacturaDeVenta factura = facttRepo.findById(idFactura)
                .orElseThrow(() -> new IllegalArgumentException("La factura ID " + idFactura + " no existe."));

        if (Boolean.TRUE.equals(factura.getAnulada())) {
            throw new IllegalStateException("La factura #" + idFactura + " ya se encuentra anulada.");
        }

        // 2. Verificar que la caja actual esté abierta para registrar la salida de dinero
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja para procesar la anulación."));

        // 3. Marcar factura como ANULADA
        factura.setAnulada(true);
        factura.setFechaActualizacion(new Date());
        factura.setFechaAnulada(new Date());

        FacturaDeVenta facturaAnulada = facttRepo.save(factura);

        // 4. Revertir dinero de Caja (Movimiento de Egreso o Ajuste)
        cajaService.registrarMovimientoPos(
                turnoActual.getId().intValue(),
                facturaAnulada.getTipoVenta().getNombre(),
                BigDecimal.valueOf(facturaAnulada.getTotal()).negate(),
                "Anulación de Venta #" + facturaAnulada.getCodigo() + ". Motivo: " + motivoAnulacion,
                nombreUsuario
        );

        // 5. Mapear ítems inventariables para el evento de devolución
        List<VentaAnuladaEvent.ItemAnulacionDto> itemsDto = facturaAnulada.getDetalleFacturaDeVentaCollection().stream()
                .filter(item -> item.getArticulo() != null
                && item.getArticulo().getCodigo() != null
                && Boolean.TRUE.equals(item.getArticulo().getInventariable()))
                .map(item -> new VentaAnuladaEvent.ItemAnulacionDto(item.getArticulo().getCodigo(), item.getCantidad()))
                .collect(Collectors.toList());

// 6. Disparar Evento para la Entrada en Kardex e Incremento de Inventario
        eventPublisher.publishEvent(new VentaAnuladaEvent(
                facturaAnulada.getCodigo(),
                nombreUsuario,
                motivoAnulacion,
                itemsDto
        ));

        return facturaAnulada;
    }

    @Transactional
    @Override
    public FacturaDeVenta procesarDevolucion(SolicitudDevolucionDto solicitud) {

        // 1. Validar la factura
        FacturaDeVenta factura = facttRepo.findById(solicitud.getIdFactura())
                .orElseThrow(() -> new IllegalArgumentException("La factura ID " + solicitud.getIdFactura() + " no existe."));

        if (Boolean.TRUE.equals(factura.getAnulada())) {
            throw new IllegalStateException("No se pueden hacer devoluciones sobre una factura anulada.");
        }

        // 2. Verificar caja abierta
        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja para procesar la devolución."));

        BigDecimal montoTotalReembolso = BigDecimal.ZERO;
        List<VentaDevueltaEvent.ItemAnulacionDto> itemsParaKardex = new ArrayList<>();
        boolean todosLosItemsDevueltosCompletos = true;

        // 3. Procesar y validar ítem por ítem
        for (SolicitudDevolucionDto.ItemDevolucionDto itemDev : solicitud.getItems()) {

            DetalleFacturaDeVenta detalle = factura.getDetalleFacturaDeVentaCollection().stream()
                    .filter(d -> d.getCodigo().equals(itemDev.getIdDetalleFactura()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El detalle indicado no pertenece a esta factura."));

            double cantidadPreviaDevuelta = detalle.getCantidad()!= null ? detalle.getCantidad() : 0.0;
            double cantidadDisponible = detalle.getCantidad() - cantidadPreviaDevuelta;

            if (itemDev.getCantidadADevolver() <= 0 || itemDev.getCantidadADevolver() > cantidadDisponible) {
                throw new IllegalArgumentException("La cantidad a devolver (" + itemDev.getCantidadADevolver()
                        + ") para el artículo '" + detalle.getArticulo().getDescripcion()
                        + "' supera lo disponible (" + cantidadDisponible + ").");
            }

            // Actualizar la cantidad devuelta acumulada en el detalle
            double nuevaCantidadDevuelta = cantidadPreviaDevuelta + itemDev.getCantidadADevolver();
            detalle.setCantidad(nuevaCantidadDevuelta);

            // Verificar si este ítem se devolvió por completo
            if (nuevaCantidadDevuelta < detalle.getCantidad()) {
                todosLosItemsDevueltosCompletos = false;
            }

            // Calcular el subtotal del reembolso proporcional (precio Unitario * cantidad devuelta)
            BigDecimal subtotalDevuelto = BigDecimal.valueOf(detalle.getPrecioCompra())
                    .multiply(BigDecimal.valueOf(itemDev.getCantidadADevolver()));
            montoTotalReembolso = montoTotalReembolso.add(subtotalDevuelto);

            // Preparar ítems inventariables para Kardex
            Articulo articulo = detalle.getArticulo();
            if (articulo != null && articulo.getCodigo() != null && Boolean.TRUE.equals(articulo.getInventariable())) {
                itemsParaKardex.add(new VentaDevueltaEvent.ItemAnulacionDto(
                        articulo.getCodigo(),
                        itemDev.getCantidadADevolver()
                ));
            }
        }

        // 4. Si todos los detalles fueron devueltos en su totalidad, se marca la factura como anulada/devuelta total
        boolean esDevolucionTotal = todosLosItemsDevueltosCompletos
                && factura.getDetalleFacturaDeVentaCollection().size() == solicitud.getItems().size();

        if (esDevolucionTotal) {
            factura.setAnulada(true);
            factura.setFechaAnulada(new Date());
        }

        factura.setFechaActualizacion(new Date());
        FacturaDeVenta facturaGuardada = facttRepo.save(factura);

        // 5. Egreso proporcional de dinero en Caja
        cajaService.registrarMovimientoPos(
                turnoActual.getId().intValue(),
                facturaGuardada.getTipoVenta().getNombre(),
                montoTotalReembolso.negate(),
                (esDevolucionTotal ? "Devolución Total" : "Devolución Parcial")
                + " Venta #" + facturaGuardada.getCodigo() + ". Motivo: " + solicitud.getMotivo(),
                solicitud.getUsuario()
        );

        // 6. Publicar evento para actualizar Kardex
        eventPublisher.publishEvent(new VentaDevueltaEvent(
                facturaGuardada.getCodigo(),
                solicitud.getUsuario(),
                solicitud.getMotivo(),
                esDevolucionTotal,
                montoTotalReembolso,
                itemsParaKardex
        ));

        return facturaGuardada;
    }

    @Override
    public FacturaDeVenta guardar(FacturaDeVenta obj) {
        return facttRepo.save(obj);
    }

    @Override
    public List<FacturaDeVenta> getLista() {
        return facttRepo.findAll();
    }

    @Override
    public List<DetalleFacturaDeVenta> getDetalle(int obj) {
        return facttRepo.getDetalle(obj);
    }

}
