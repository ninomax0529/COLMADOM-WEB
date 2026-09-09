/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl;

import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.repo.FacturaDeventaRepo;
import com.maxsoft.application.servicio.interfaces.CajaService;
import com.maxsoft.application.servicio.interfaces.FacturaDeVentaService;
import com.maxsoft.application.view.venta.puntoVenta.TicketVenta;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaDeVentaServiceImpl implements FacturaDeVentaService {

    @Autowired
    FacturaDeventaRepo facttRepo;
    @Autowired
    CajaService cajaService;

    @Transactional
    @Override
    public FacturaDeVenta procesarVenta(TicketVenta ticketVenta, String nombreUsuario) {

        System.out.println("cajaService " + cajaService);
//        // 1. Validación de Caja
//        CajaTurno turnoActual = cajaService.obtenerCajaAbierta()
//                .orElseThrow(() -> new IllegalStateException("Debe abrir una caja antes de registrar movimientos de POS"));

        // 1. Validar primero si hay una caja abierta antes de mostrar el diálogo
        Optional<CajaTurno> cajaAbiertaOpt = cajaService.obtenerCajaAbierta();

        if (cajaAbiertaOpt.isEmpty()) {
            Notification.show("Debe abrir una caja antes de registrar movimientos de POS", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }

        CajaTurno turnoActual = cajaAbiertaOpt.get();

        System.out.println("turnoActual " + turnoActual);

        // 2. Validaciones de Negocio
        if (ticketVenta.getItems() == null || ticketVenta.getItems().isEmpty()) {
            throw new IllegalArgumentException("La factura no tiene detalle.");
        }

        for (DetalleFacturaDeVenta det : ticketVenta.getItems()) {

            if (det.getCantidad() <= 0) {

                throw new IllegalArgumentException("El artículo '" + det.getDescripcionArticulo() + "' tiene la cantidad en cero.");
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

        // 4. Persistencia (ambos bajo la misma transacción)
        FacturaDeVenta facturaGuardada = facttRepo.save(factura);

        try {

            System.out.println("facturaGuardada :" + facturaGuardada);
            cajaService.registrarMovimientoPos(
                    turnoActual.getId().intValue(),
                    facturaGuardada.getTipoVenta().getNombre(),
                    BigDecimal.valueOf(facturaGuardada.getTotal()),
                    "Ingreso por venta",
                    nombreUsuario
            );

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }
        return facturaGuardada;
    }

    @Override
    public FacturaDeVenta guardar(FacturaDeVenta obj) {

        return facttRepo.save(obj);
    }

    @Override
    public List<FacturaDeVenta> getLista() {

        List<FacturaDeVenta> lista = null;
        lista = facttRepo.findAll();
//        
        return lista;

    }

    @Override
    public List<DetalleFacturaDeVenta> getDetalle(int obj) {

        List<DetalleFacturaDeVenta> lista = null;
        lista = facttRepo.getDetalle(obj);

        return lista;
    }

}
