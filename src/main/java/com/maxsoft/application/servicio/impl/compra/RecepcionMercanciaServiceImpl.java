/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.compra;

import com.maxsoft.application.modelo.DetalleOrdendeDeCompra;
import com.maxsoft.application.modelo.DetalleRecepcionMercancia;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeCompra;
import com.maxsoft.application.modelo.OrdenDeCompra;
import com.maxsoft.application.modelo.RecepcionMercancia;
import com.maxsoft.application.modelo.ReciboDeEgreso;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.ArticuloEmpaque;
import com.maxsoft.application.modelo.EstadoDocumento;
import com.maxsoft.application.repo.ArticuloEmpaqueRepo;
import com.maxsoft.application.repo.ArticuloRepo;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import com.maxsoft.application.repo.RecepcionMercanciaRepo;
import com.maxsoft.application.servicio.interfaces.compra.OrdenDeCompraService;
import com.maxsoft.application.servicio.interfaces.compra.RecepcionMercanciaService;
import com.maxsoft.application.servicio.interfaces.cxp.FacturaDeCompraService;
import com.maxsoft.application.servicio.interfaces.cxp.ReciboDeEgresoService;
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
    FacturaDeCompraService facturaDeCompraService;
    ReciboDeEgresoService reciboDeEgresoService;

    @org.springframework.beans.factory.annotation.Autowired
    private ArticuloEmpaqueRepo articuloEmpaqueRepo;

    @org.springframework.beans.factory.annotation.Autowired
    private ArticuloRepo articuloRepo;

    @org.springframework.beans.factory.annotation.Autowired
    private SalidaInventarioService salidaInventarioService;

    public RecepcionMercanciaServiceImpl(RecepcionMercanciaRepo recepcionRepo,
            ArticuloService articuloService,
            OrdenDeCompraService ordenCompraService,
            EntradaDeInventarioService entradaInventarioService,
            FacturaDeCompraService facturaDeCompraService,
            ReciboDeEgresoService reciboDeEgresoService
    ) {
        this.recepcionRepo = recepcionRepo;
        this.articuloService = articuloService;
        this.ordenCompraService = ordenCompraService;
        this.entradaInventarioService = entradaInventarioService;
        this.facturaDeCompraService = facturaDeCompraService;
        this.reciboDeEgresoService=reciboDeEgresoService;

    }

    // =====================================================================
    // ANULACIÓN
    // =====================================================================
    private static final int ESTADO_DOCUMENTO_ANULADO = 4;

    /**
     * Reversa todo lo que hizo procesarRecepcion, en orden inverso:
     * 1) anula los recibos de egreso de su factura (si era de contado, el
     *    efectivo vuelve a la caja), 2) anula la factura de compra, 3) revierte
     *    el costo promedio, 4) saca la mercancía del inventario, 5) devuelve
     *    lo recibido a la orden de compra y 6) marca la recepción como anulada.
     * Si algo falla (p. ej. ya se vendió parte de la mercancía), no se cambia nada.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecepcionMercancia anularRecepcion(Integer codigo, String motivo, String usuario) {

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Indique el motivo de la anulación.");
        }
        String nombreUsuario = usuario != null && !usuario.isBlank() ? usuario : "Admin";

        RecepcionMercancia rec = recepcionRepo.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("La recepción #" + codigo + " no existe."));
        if (rec.getEstadoDocumento() != null
                && (Integer.valueOf(ESTADO_DOCUMENTO_ANULADO).equals(rec.getEstadoDocumento().getCodigo())
                || (rec.getEstadoDocumento().getNombre() != null
                && rec.getEstadoDocumento().getNombre().toLowerCase().contains("anul")))) {
            throw new IllegalStateException("La recepción #" + codigo + " ya está anulada.");
        }

        List<DetalleRecepcionMercancia> detalle = recepcionRepo.getDetalle(codigo);

        // 1 y 2. Pagos y factura de compra
        for (FacturaDeCompra fact : facturaDeCompraService.getFacturasDeRecepcion(codigo)) {
            if (Boolean.TRUE.equals(fact.getAnulada())) {
                continue;
            }
            for (ReciboDeEgreso recibo : reciboDeEgresoService.getRecibosVigentesDeFactura(fact.getCodigo())) {
                if (reciboDeEgresoService.pagaOtrasFacturas(recibo.getCodigo(), fact.getCodigo())) {
                    throw new IllegalStateException("El recibo de egreso #" + recibo.getCodigo()
                            + " también paga otras facturas. Anúlelo manualmente antes de anular la recepción.");
                }
                reciboDeEgresoService.anularReciboDeEgreso(recibo.getCodigo(),
                        "Anulación de recepción #" + codigo + ": " + motivo, nombreUsuario);
            }
            facturaDeCompraService.anularFactura(fact.getCodigo(), "Anulación de recepción #" + codigo + ": " + motivo);
        }

        // 3. Costo promedio (antes de sacar el stock: la fórmula usa la existencia actual)
        revertirCostoPromedio(detalle);

        // 4. Inventario
        salidaInventarioService.crearSalidaPorAnulacionRecepcion(rec, detalle, nombreUsuario, motivo);

        // 5. Orden de compra
        revertirOrdenDeCompra(rec, detalle);

        // 6. Recepción
        rec.setEstadoDocumento(new EstadoDocumento(ESTADO_DOCUMENTO_ANULADO));
        rec.setNombreEstado("Anulada");
        String nota = "ANULADA por " + nombreUsuario + ": " + motivo.trim();
        String comentario = rec.getComentario() != null ? rec.getComentario() + " | " + nota : nota;
        rec.setComentario(comentario.length() > 250 ? comentario.substring(0, 250) : comentario);
        return recepcionRepo.save(rec);
    }

    /**
     * Inverso del costo promedio de la compra:
     * costo anterior = (existencia × costo actual − costo de la compra) / (existencia − cantidad comprada).
     * Si sin esta compra no queda existencia, el costo se deja como está.
     */
    private void revertirCostoPromedio(List<DetalleRecepcionMercancia> detalles) {

        Map<Integer, BigDecimal[]> compraPorArticulo = new LinkedHashMap<>();
        for (DetalleRecepcionMercancia det : detalles) {
            if (det.getArticulo() == null || det.getPrecioUnitario() == null
                    || det.getCantidadRecibida() == null || det.getCantidadRecibida().signum() <= 0) {
                continue;
            }
            BigDecimal factor = det.getFactorConversion() != null && det.getFactorConversion().signum() > 0
                    ? det.getFactorConversion() : BigDecimal.ONE;
            compraPorArticulo.merge(det.getArticulo().getCodigo(),
                    new BigDecimal[]{det.getCantidadRecibida().multiply(factor),
                        det.getCantidadRecibida().multiply(det.getPrecioUnitario())},
                    (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])});
        }

        for (Map.Entry<Integer, BigDecimal[]> e : compraPorArticulo.entrySet()) {
            Articulo articulo = articuloRepo.findById(e.getKey()).orElse(null);
            if (articulo == null || articulo.getPrecioCompra() == null || articulo.getExistencia() == null) {
                continue;
            }
            BigDecimal cantidad = e.getValue()[0];
            BigDecimal costoCompra = e.getValue()[1];
            BigDecimal existenciaSinCompra = articulo.getExistencia().subtract(cantidad);
            if (existenciaSinCompra.signum() <= 0) {
                continue;
            }
            BigDecimal costoAnterior = articulo.getExistencia().multiply(articulo.getPrecioCompra())
                    .subtract(costoCompra)
                    .divide(existenciaSinCompra, 4, RoundingMode.HALF_UP);
            if (costoAnterior.signum() <= 0) {
                continue;
            }
            articulo.setPrecioCompra(costoAnterior.setScale(2, RoundingMode.HALF_UP));
            articuloRepo.save(articulo);

            for (ArticuloEmpaque emp : articuloEmpaqueRepo.findByArticuloCodigo(articulo.getCodigo())) {
                BigDecimal factor = emp.getFactorConversion() != null && emp.getFactorConversion().signum() > 0
                        ? emp.getFactorConversion() : BigDecimal.ONE;
                emp.setPrecioCompra(costoAnterior.multiply(factor).setScale(2, RoundingMode.HALF_UP));
                articuloEmpaqueRepo.save(emp);
            }
        }
    }

    /** Devuelve a la orden de compra lo que esta recepción había recibido. */
    private void revertirOrdenDeCompra(RecepcionMercancia rec, List<DetalleRecepcionMercancia> detalles) {

        if (rec.getOrdenCompra() == null) {
            return;
        }
        OrdenDeCompra orden = this.ordenCompraService.buscarPorCodigo(rec.getOrdenCompra().getCodigo()).orElse(null);
        if (orden == null || orden.getDetalleOrdendeDeCompraCollection() == null) {
            return;
        }

        for (DetalleRecepcionMercancia det : detalles) {
            DetalleOrdendeDeCompra linea = buscarLineaOrden(orden, det);
            BigDecimal recibida = det.getCantidadRecibida() != null ? det.getCantidadRecibida() : BigDecimal.ZERO;
            BigDecimal recibidaAntes = linea.getCantidadRecibida() != null ? linea.getCantidadRecibida() : BigDecimal.ZERO;
            BigDecimal pendiente = linea.getPendiente() != null ? linea.getPendiente() : BigDecimal.ZERO;

            linea.setCantidadRecibida(recibidaAntes.subtract(recibida).max(BigDecimal.ZERO));
            BigDecimal nuevoPendiente = pendiente.add(recibida);
            if (linea.getCantidad() != null) {
                nuevoPendiente = nuevoPendiente.min(linea.getCantidad());
            }
            linea.setPendiente(nuevoPendiente);
        }

        if (!"ANULADA".equalsIgnoreCase(orden.getEstado())) {
            boolean nadaRecibido = orden.getDetalleOrdendeDeCompraCollection().stream()
                    .allMatch(l -> l.getCantidadRecibida() == null || l.getCantidadRecibida().signum() <= 0);
            orden.setEstado(nadaRecibido ? "PENDIENTE" : "PARCIAL");
            orden.setCompletada("NO");
        }
        this.ordenCompraService.guardar(orden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> empaquesVendiendoConPerdida(java.util.Collection<Integer> articulos) {

        List<String> alertas = new java.util.ArrayList<>();
        if (articulos == null) {
            return alertas;
        }
        for (Integer codigo : new java.util.LinkedHashSet<>(articulos)) {
            if (codigo == null) {
                continue;
            }
            for (ArticuloEmpaque emp : articuloEmpaqueRepo.findByArticuloCodigo(codigo)) {
                boolean aLaVenta = !Boolean.FALSE.equals(emp.getSeVendeEn()) && !Boolean.FALSE.equals(emp.getEstado());
                BigDecimal costo = emp.getPrecioCompra();
                BigDecimal precio = emp.getPrecioVenta();
                if (!aLaVenta || costo == null || precio == null || precio.signum() <= 0) {
                    continue;
                }
                if (costo.compareTo(precio) >= 0) {
                    String articulo = emp.getArticulo() != null ? emp.getArticulo().getDescripcion() : emp.getNombreArticulo();
                    String empaque = emp.getDescripcionEmpaque() != null ? emp.getDescripcionEmpaque() : "";
                    alertas.add(articulo + (empaque.isBlank() ? "" : " (" + empaque + ")")
                            + ": costo RD$ " + String.format("%,.2f", costo)
                            + " ≥ precio de venta RD$ " + String.format("%,.2f", precio));
                }
            }
        }
        return alertas;
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

        FacturaDeCompra facturaDeCompra;
        ReciboDeEgreso reciboEgresa;
        // 2. Cambiar estado de la recepción a PROCESADO (si usas Enum o String)
//    recepcion.setEstado(EstadoDocumento.PROCESADO); // o recepcion.setEstado("PROCESADO");
        // 3. Guardar la Recepción de Mercancía
        RecepcionMercancia recepcionDB = recepcionRepo.save(recepcion);

        List<DetalleRecepcionMercancia> detalle = this.recepcionRepo.getDetalle(recepcionDB.getCodigo());

        if (recepcionDB.getProveedor().getTipoSuplidor().getCodigo() == 2) {//Proveedor informal

            facturaDeCompra = this.facturaDeCompraService.procesarFacturaInformal(recepcion, detalle);

        } else {

            facturaDeCompra = this.facturaDeCompraService.procesarFactura(recepcion, detalle);

        }

        if (facturaDeCompra.getCondicionPago().getCodigo() == 1) {//De contado

            System.out.println("facturaDeCompra " + facturaDeCompra);
            reciboEgresa = this.reciboDeEgresoService.procesarReciboDeEgreso(facturaDeCompra);
        }

        EntradaInventario entradaInv = this.entradaInventarioService.crearEntradaPorRecepcion(recepcionDB, detalle);

        actualizarOrdenDeCompra(recepcionDB);

        return recepcionDB;
    }

    private void actualizarOrdenDeCompra1(RecepcionMercancia rec) {

        if (rec.getOrdenCompra() == null) {
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

        if (rec.getOrdenCompra() == null) {
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
                    && mismoId(l.getArticuloEmpaque().getCodigo(), det.getArticuloEmpaque().getCodigo())) {
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
