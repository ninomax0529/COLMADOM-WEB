/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;


import com.maxsoft.application.evento.VentaRealizadaEvent;
import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.repo.FacturaDeventaRepo;
import com.maxsoft.application.servicio.interfaces.inventario.InventarioService;
import com.maxsoft.application.servicio.interfaces.venta.CajaService;
import com.maxsoft.application.servicio.interfaces.venta.FacturaDeVentaService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import java.math.BigDecimal;
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
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public FacturaDeVenta procesarVenta(TicketVenta ticketVenta, String nombreUsuario) {

        // 1. Validación de Caja
        Optional<CajaTurno> cajaAbiertaOpt = cajaService.obtenerCajaAbierta();
        if (cajaAbiertaOpt.isEmpty()) {
            throw new IllegalStateException("Debe abrir una caja antes de registrar movimientos de POS");
        }
        CajaTurno turnoActual = cajaAbiertaOpt.get();

        // 2. Validaciones de Negocio e Inventario
        if (ticketVenta.getItems() == null || ticketVenta.getItems().isEmpty()) {
            throw new IllegalArgumentException("La factura no tiene detalle.");
        }

        for (DetalleFacturaDeVenta det : ticketVenta.getItems()) {
            if (det.getCantidad() <= 0) {
                throw new IllegalArgumentException("El artículo '" + det.getDescripcionArticulo() + "' tiene la cantidad en cero.");
            }
            // Validar stock antes de intentar procesar
            if (det.getArticulo() != null && det.getArticulo().getCodigo()!= null) {
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
            factura.setNombreDelivery("na");
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

        // 5. Registrar Movimiento de Caja
        cajaService.registrarMovimientoPos(
                turnoActual.getId().intValue(),
                facturaGuardada.getTipoVenta().getNombre(),
                BigDecimal.valueOf(facturaGuardada.getTotal()),
                "Ingreso por venta",
                nombreUsuario
        );

        // 6. Publicación del Evento (Descuenta inventario en segundo plano pero dentro de la misma transacción)
        List<VentaRealizadaEvent.ItemVentaDto> itemsDto = facturaGuardada.getDetalleFacturaDeVentaCollection().stream()
                .filter(item -> item.getArticulo() != null)
                .map(item -> new VentaRealizadaEvent.ItemVentaDto(item.getArticulo().getCodigo(), item.getCantidad()))
                .collect(Collectors.toList());

        eventPublisher.publishEvent(new VentaRealizadaEvent(facturaGuardada.getCodigo(), itemsDto));

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
