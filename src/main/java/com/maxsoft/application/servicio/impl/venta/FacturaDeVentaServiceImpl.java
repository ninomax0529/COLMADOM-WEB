/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.evento.VentaDevueltaEvent;
import com.maxsoft.application.evento.VentaRealizadaEvent;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.repo.FacturaDeventaRepo;
import com.maxsoft.application.servicio.interfaces.inventario.ArticuloService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private EntradaDeInventarioService entradaInventarioService;
    @Autowired
    SalidaInventarioService SalidaInventarioService;

    @Autowired
    ArticuloService articuloService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public FacturaDeVenta procesarVenta(TicketVenta ticketVenta, String nombreUsuario) {

        FacturaDeVenta facturaGuardada = null;

        try {

            // 1. Validación de Caja
            CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException("Debe abrir una caja antes de registrar movimientos de POS"));

            // 2. Validaciones de Negocio e Inventario
            if (ticketVenta.getItems() == null || ticketVenta.getItems().isEmpty()) {
                throw new IllegalArgumentException("La factura no tiene detalle.");
            }

            for (DetalleFacturaDeVenta det : ticketVenta.getItems()) {
                if (det.getCantidad().doubleValue() <= 0) {
                    throw new IllegalArgumentException("El artículo '" + det.getDescripcionArticulo() + "' tiene la cantidad en cero.");
                }

                // Validar stock solo si el artículo existe y es inventariable
                if (det.getArticulo() != null && det.getArticulo().getCodigo() != null
                        && Boolean.TRUE.equals(det.getArticulo().getInventariable())) {

                    System.out.println("det.getArticulo().getCodigo() " + det.getArticulo().getCodigo());
                    System.out.println("d  det.getCantidad()" + det.getCantidad());

                    inventarioService.validarStockDisponible(det.getArticulo().getCodigo(), det.getCantidad().doubleValue());
                }
            }

            // 3. Mapeo y Construcción de la Factura
            FacturaDeVenta factura = new FacturaDeVenta();
            factura.setEstadoFactura(ticketVenta.getEstadoFactura());
            factura.setTipoVenta(ticketVenta.getTipoVenta());
            factura.setCliente(ticketVenta.getCliente());
            factura.setNombreCliente(ticketVenta.getNombreCliente());
            factura.setDireccion(ticketVenta.getDireccion());
            factura.setAnulada(Boolean.FALSE);

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
                item.setCantidadDevuelta(BigDecimal.ZERO);
                item.setPrecioCompra(item.getArticulo().getPrecioCompra());

                item.setCodigo(null);
            });

            factura.setDetalleFacturaDeVentaCollection(ticketVenta.getItems());

            System.out.println("facttRepo " + facttRepo);
            // 4. Persistencia de la factura
            facturaGuardada = facttRepo.saveAndFlush(factura);

            // 3. Generar la Salida de Inventario explícita
            SalidaInventario salida = this.SalidaInventarioService.crearSalidaPorVenta(facturaGuardada);

            System.out.println("Salida :"+salida.getCodigo());
            // 5. Registrar Movimiento de Caja (Usando id como Long)
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    facturaGuardada.getTipoVenta().getNombre(),
                  facturaGuardada.getTotal(),
                    "Ingreso por venta #" + facturaGuardada.getCodigo(),
                    nombreUsuario
            );

            // 6. Publicación del Evento (Corregido)
            List<VentaRealizadaEvent.ItemVentaDto> itemsDto = facturaGuardada.getDetalleFacturaDeVentaCollection().stream()
                    .filter(item -> item.getArticulo() != null)
                    .map(item -> {
                        // Asegurar la extracción del ID del artículo (probando getCodigo() o getId())
                        Integer idArticulo = item.getArticulo().getCodigo();
                        return new VentaRealizadaEvent.ItemVentaDto(idArticulo, item.getCantidad().doubleValue());
                    })
                    // Filtrar DTOs cuyo idArticulo no sea nulo
                    .filter(dto -> dto!= null)
                    .collect(Collectors.toList());

// Log de depuración para confirmar cuántos ítems viajan en el evento
            System.out.println(">>> Publicando VentaRealizadaEvent con " + itemsDto.size() + " ítems. ID Factura: " + facturaGuardada.getCodigo());

//            eventPublisher.publishEvent(new VentaRealizadaEvent(facturaGuardada.getCodigo(), itemsDto));

        } catch (Exception ex) {
            System.err.println("Error procesando venta: " + ex.getMessage());
            ex.printStackTrace();
            throw ex; // <--- Importante para que @Transactional haga rollback
        }

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
        
              // 5. Mapear ítems inventariables para el evento de devolución
        List<DetalleFacturaDeVenta> itemsDto = facturaAnulada.getDetalleFacturaDeVentaCollection()
                .stream()
                .filter(item -> Boolean.TRUE.equals(item.getArticulo().getInventariable()))
                .map(item ->  item)
                .collect(Collectors.toList());

        this.entradaInventarioService.crearEntradaPorAnulacionVenta(factura, itemsDto);

        // 4. Revertir dinero de Caja (Movimiento de Egreso o Ajuste)
        cajaService.registrarMovimientoPos(
                turnoActual.getId().intValue(),
                facturaAnulada.getTipoVenta().getNombre(),
                facturaAnulada.getTotal().negate(),
                "Anulación de Venta #" + facturaAnulada.getCodigo() + ". Motivo: " + motivoAnulacion,
                nombreUsuario
        );

        // 5. Mapear ítems inventariables para el evento de devolución
//        List<VentaAnuladaEvent.ItemAnulacionDto> itemsDto = facturaAnulada.getDetalleFacturaDeVentaCollection().stream()
//                .filter(item -> item.getArticulo() != null
//                && item.getArticulo().getCodigo() != null
//                && Boolean.TRUE.equals(item.getArticulo().getInventariable()))
//                .map(item -> new VentaAnuladaEvent.ItemAnulacionDto(item.getArticulo().getCodigo(), item.getCantidad()))
//                .collect(Collectors.toList());

// 6. Disparar Evento para la Entrada en Kardex e Incremento de Inventario
//        eventPublisher.publishEvent(new VentaAnuladaEvent(
//                facturaAnulada.getCodigo(),
//                nombreUsuario,
//                motivoAnulacion,
//                itemsDto
//        ));

        return facturaAnulada;
    }

    @Transactional
    @Override
    public FacturaDeVenta procesarDevolucion(SolicitudDevolucionDto solicitud) {

        FacturaDeVenta facturaGuardada = null;

        try {

            // 1. Buscar la factura
            FacturaDeVenta factura = facttRepo.findById(solicitud.getIdFactura())
                    .orElseThrow(() -> new IllegalArgumentException("La factura ID " + solicitud.getIdFactura() + " no existe."));

            if (Boolean.TRUE.equals(factura.getAnulada())) {
                throw new IllegalStateException("No se pueden realizar devoluciones sobre una factura totalmente anulada.");
            }

            // 2. Verificar caja abierta para egreso del dinero reembolsado
            CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
                    .orElseThrow(() -> new IllegalStateException("Debe abrir una caja para procesar la devolución."));

            BigDecimal montoTotalReembolso = BigDecimal.ZERO;
            List<VentaDevueltaEvent.ItemAnulacionDto> itemsParaKardex = new ArrayList<>();

            // 3. Validar y procesar cada ítem enviado
            for (SolicitudDevolucionDto.ItemDevolucionDto itemDev : solicitud.getItems()) {

                DetalleFacturaDeVenta detalle = factura.getDetalleFacturaDeVentaCollection().stream()
                        .filter(d -> d.getCodigo().equals(itemDev.getIdDetalleFactura()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("El detalle enviado no pertenece a la factura #" + factura.getCodigo()));

                double devueltoPrevio = detalle.getCantidadDevuelta() != null ? detalle.getCantidadDevuelta().doubleValue() : 0.0;
                double disponibleParaDevolver = detalle.getCantidad().doubleValue() - devueltoPrevio;

                if (itemDev.getCantidadADevolver() <= 0 || itemDev.getCantidadADevolver() > disponibleParaDevolver) {
                    throw new IllegalArgumentException("Cantidad inválida para " + detalle.getArticulo().getDescripcion()
                            + ". Máximo disponible para devolver: " + disponibleParaDevolver);
                }

                // Actualizar acumulado devuelto en la BD
                detalle.setCantidadDevuelta(BigDecimal.valueOf(devueltoPrevio + itemDev.getCantidadADevolver()));

                // CORRECCIÓN: Usar precio de VENTA (o precio unitario cobrado), NO precio de compra
                Double precioVentaReal = detalle.getPrecioVenta() != null ? detalle.getPrecioVenta().doubleValue()
                        : detalle.getPrecioCompra().doubleValue();
                
                BigDecimal subtotal = BigDecimal.valueOf(precioVentaReal)
                        .multiply(BigDecimal.valueOf(itemDev.getCantidadADevolver()));
                montoTotalReembolso = montoTotalReembolso.add(subtotal);

                // Si el artículo es inventariable, se prepara para el Kardex
                Articulo articulo = detalle.getArticulo();
                if (articulo != null && Boolean.TRUE.equals(articulo.getInventariable())) {

                    itemsParaKardex.add(new VentaDevueltaEvent.ItemAnulacionDto(
                            articulo.getCodigo(),
                            itemDev.getCantidadADevolver()
                    ));
                }
            }

            // 4. Evaluar si la devolución terminó cubriendo TODOS los ítems de la factura
            boolean esDevolucionTotal = factura.getDetalleFacturaDeVentaCollection().stream()
                    .allMatch(d -> {
                        double devuelto = d.getCantidadDevuelta() != null ? d.getCantidadDevuelta().doubleValue() : 0.0;
                        return Double.compare(devuelto, d.getCantidad().doubleValue() ) == 0;
                    });

            if (esDevolucionTotal) {
                factura.setAnulada(true);
                factura.setFechaAnulada(new Date());
            }

            factura.setFechaActualizacion(new Date());
            facturaGuardada = facttRepo.save(factura);

            // -----------------------------------------------------------------------------------
            // NUEVO PASO 4.1: Crear formalmente la EntradaInventario en la Base de Datos
            // -----------------------------------------------------------------------------------
            EntradaInventario entradaCreada = entradaInventarioService.crearEntradaPorDevolucion(facturaGuardada, solicitud);

            // 5. Egreso de Dinero en Caja POS
            // NOTA: Aseguramos la lectura del tipo de venta/documento evitando nulls
            String tipoVentaNombre = (facturaGuardada.getTipoVenta() != null)
                    ? facturaGuardada.getTipoVenta().getNombre()
                    : "EFECTIVO"; // Valor fallback seguro si viniera nulo

            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    tipoVentaNombre,
                    montoTotalReembolso.negate(),
                    (esDevolucionTotal ? "Devolución Total" : "Devolución Parcial")
                    + " Factura #" + facturaGuardada.getCodigo() + ". Motivo: " + solicitud.getMotivo(),
                    solicitud.getUsuario()
            );

            // 6. Publicar evento para Kardex
            eventPublisher.publishEvent(new VentaDevueltaEvent(
                    facturaGuardada.getCodigo(),
                    entradaCreada.getCodigo(), // <--- Pasa el código de entrada generado
                    solicitud.getUsuario(),
                    solicitud.getMotivo(),
                    esDevolucionTotal,
                    itemsParaKardex
            ));

        } catch (Exception ex) {

            ex.printStackTrace();
        }

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

    @Override
    public FacturaDeVenta getFactura(int codigo) {

        return facttRepo.getFactura(codigo);

    }

}
