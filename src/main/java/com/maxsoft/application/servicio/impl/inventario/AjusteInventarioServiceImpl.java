/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.EntradaInventario;        // 👈 Entidad Entrada
import com.maxsoft.application.modelo.DetalleEntradaInventario; // 👈 Detalle Entrada
import com.maxsoft.application.modelo.SalidaInventario;         // 👈 Entidad Salida
import com.maxsoft.application.modelo.DetalleSalidaInventario;  // 👈 Detalle Salida
import com.maxsoft.application.modelo.Usuario;
import com.maxsoft.application.repo.AjusteInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;  // 👈 Servicio Salidas
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AjusteInventarioServiceImpl implements AjusteInventarioService {

    private final AjusteInventarioRepo ajusteRepo;
    private final EntradaDeInventarioService entradaInventarioService; // 👈 Inyectado
    private final SalidaInventarioService salidaInventarioService;   // 👈 Inyectado

    @Autowired
    public AjusteInventarioServiceImpl(AjusteInventarioRepo ajusteRepo,
            EntradaDeInventarioService entradaInventarioService,
            SalidaInventarioService salidaInventarioService) {
        this.ajusteRepo = ajusteRepo;
        this.entradaInventarioService = entradaInventarioService;
        this.salidaInventarioService = salidaInventarioService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjusteInventario procesarAjusteTransaccional(AjusteInventario ajuste,
            List<DetalleAjusteInventario> detalles,
            String usuario) {
        // 1. Validaciones
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("No se pueden procesar ajustes sin detalles.");
        }
        if (ajuste.getTipoAjuste() == null) {
            throw new IllegalArgumentException("El tipo de ajuste es requerido.");
        }

        // 2. Asociar cabecera y guardar documento AjusteInventario
        ajuste.setFechaRegistro(new Date());
        detalles.forEach(d -> {
            d.setAjusteInventario(ajuste);
            d.setCodigo(null);
        });
        ajuste.setDetalleAjusteInventarioCollection(detalles);

        AjusteInventario ajusteGuardado = ajusteRepo.saveAndFlush(ajuste);
        String numDocumento = "AJ-" + ajusteGuardado.getCodigo();

        // 3. Evaluar el Tipo de Ajuste:
        // Codigo 1 = Incremento (Entrada) | Codigo 2 = Decremento (Salida)
        if (ajuste.getTipoAjuste().getCodigo() == 1) {
            generarEntradaPorAjuste(ajusteGuardado, detalles, numDocumento, usuario);
        } else {
            generarSalidaPorAjuste(ajusteGuardado, detalles, numDocumento, usuario);
        }

        return ajusteGuardado;
    }

    // =========================================================================
    // MÉTODOS PRIVADOS AUXILIARES PARA GENERAR LOS DOCUMENTOS
    // =========================================================================
    private void generarEntradaPorAjuste(AjusteInventario ajuste,
            List<DetalleAjusteInventario> detalles,
            String numDoc,
            String usuario) {
        EntradaInventario entrada = new EntradaInventario();

        Date fechaActual = new Date();
        entrada.setFecha(fechaActual);

        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);

        // 2. Tipos de Documento y Movimiento (Tipos Integer según tu modelo)
        entrada.setTipoDocumento(3); // ID del Tipo de Documento para 'Devolución'
        entrada.setNumeroDocumento(ajuste.getCodigo()); // Guardamos el número de factura como documento de origen

        entrada.setTipoEntrada(1); // ID del tipo de movimiento de entrada por devolución
        entrada.setNombreTipoEntrada("DEVOLUCION DE VENTA");

        // 3. Moneda (Valores por defecto si no están configurados)
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP"); // O la moneda correspondiente en tu sistema

        // 4. Usuario y Observaciones
        entrada.setNombreUsuario(ajuste.getUsuario().getNombre());
        entrada.setAnulada(false);
        entrada.setComentario("Entrada por Ajuste de Inventario #" + ajuste.getCodigo() + ". " + ajuste.getObservacion());

        List<DetalleEntradaInventario> detallesEntrada = new ArrayList<>();
        for (DetalleAjusteInventario det : detalles) {

            if (det.getArticulo() != null && det.getCantidad() > 0) {

                DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();
                detEntrada.setArticulo(det.getArticulo());
                detEntrada.setCantidadRecibida(det.getCantidad());
                detEntrada.setDescripcionArticulo(det.getDecripcionArticulo());
                detEntrada.setCostoUnitario(0.0);

                double stockActual = detEntrada.getArticulo().getExistencia()
                        != null ? detEntrada.getArticulo().getExistencia() : 0.0;

                detEntrada.setExistenciaActual(stockActual);

                detEntrada.setCantidadPedida(0.00);
                detEntrada.setCantidadPendiente(0.00);
                detEntrada.setNuevaExistencia(stockActual + det.getCantidad());
                detEntrada.setNombreAlmacen("General");
                detEntrada.setNombreUnidad("Unidad");
                detEntrada.setUnidad(det.getArticulo().getUnidadEntrada());
                detEntrada.setPrecioCompra(det.getArticulo().getPrecioCompra());
                detEntrada.setAlmacen(new Almacen(1));
                detEntrada.setEntradaInventario(entrada);
                detallesEntrada.add(detEntrada);

            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesEntrada);
        entradaInventarioService.guardar(entrada, usuario);
        // Al guardar la Entrada, la clase EntradaInventarioServiceImpl emitirá su 
        // EntradaInventarioCreadaEvent para impactar el Kardex automáticamente.
//        entradaInventarioService.cre (entrada, detallesEntrada);
    }

    private void generarSalidaPorAjuste(AjusteInventario ajuste,
            List<DetalleAjusteInventario> detalles,
            String numDoc,
            String usuario) {

        SalidaInventario salida = new SalidaInventario();
        salida.setFecha(new Date());
        salida.setNumeroDocumento(numDoc);
        salida.setUsuario(new Usuario(1));

        salida.setObservacion("Salida por Ajuste de Inventario #" + ajuste.getCodigo() + ". " + ajuste.getObservacion());

        List<DetalleSalidaInventario> detallesSalida = new ArrayList<>();
        for (DetalleAjusteInventario det : detalles) {

            if (det.getArticulo() != null && det.getCantidad() > 0) {

                DetalleSalidaInventario detSalida = new DetalleSalidaInventario();
                detSalida.setArticulo(det.getArticulo());
                detSalida.setCantidad(det.getCantidad());
                detallesSalida.add(detSalida);

                detSalida.setSalidaInventario(salida);
                detSalida.setArticulo(det.getArticulo());
                detSalida.setDescripcionArticulo(det.getArticulo().getDescripcion());
                detSalida.setCantidad(det.getCantidad());

                double stockActual = det.getArticulo().getExistencia() != null ? det.getArticulo().getExistencia() : 0.0;
                detSalida.setExistenciaAnterior(stockActual);

                detSalida.setExistencia(stockActual); // Nueva existencia (Resta)
                detSalida.setUnidad(det.getArticulo().getUnidadSalida());
                detSalida.setCostoUnitario(det.getArticulo().getPrecioCompra() != null ? det.getArticulo().getPrecioCompra() : 0.0);
                detSalida.setprecioCompra(det.getArticulo().getPrecioCompra() != null ?det.getArticulo().getPrecioCompra() : 0.0);
                detSalida.setPrecioVenta(det.getArticulo().getPrecioVenta() != null
                        ? det.getArticulo().getPrecioVenta() : det.getArticulo().getPrecioVenta());

            }
        }

        salida.setDetalleSalidaInventarioCollection(detallesSalida);

        // Al guardar la Salida, se emitirá SalidaInventarioCreadaEvent para impactar el Kardex
        salidaInventarioService.guardar(salida, usuario);
    }

    @Override
    public AjusteInventario guardar(AjusteInventario obj) {
        return ajusteRepo.save(obj);
    }

    @Override
    public List<AjusteInventario> getLista() {
        return ajusteRepo.findAll();
    }

    @Override
    public List<DetalleAjusteInventario> getDetalle(int codigoAjuste) {
        return (List<DetalleAjusteInventario>) ajusteRepo.findById(codigoAjuste)
                .map(AjusteInventario::getDetalleAjusteInventarioCollection)
                .orElse(new ArrayList<>());
    }
}
//
//import com.maxsoft.application.modelo.AjusteInventario;
//import com.maxsoft.application.modelo.DetalleAjusteInventario;
//import com.maxsoft.application.modelo.TipoDocumento;
//import com.maxsoft.application.modelo.TipoMovimiento;
//import com.maxsoft.application.repo.AjusteInventarioRepo;
//import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
//import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
//import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
//import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//import java.util.List;
//
//@Service
//public class AjusteInventarioServiceImpl implements AjusteInventarioService {
//
//    private final AjusteInventarioRepo ajusteRepo;
//    private final MovimientoInventarioService movimientoService;
//    TipoMovimientoService tipoMovimientoService;
//    TipoDocumentoService tipoDocumentoService;
//
//    @Autowired
//    public AjusteInventarioServiceImpl(AjusteInventarioRepo ajusteRepo,
//            MovimientoInventarioService movimientoService,
//            TipoMovimientoService tipoMovimientoService,
//            TipoDocumentoService tipoDocumentoService) {
//
//        this.ajusteRepo = ajusteRepo;
//        this.movimientoService = movimientoService;
//        this.tipoDocumentoService = tipoDocumentoService;
//        this.tipoMovimientoService = tipoMovimientoService;
//
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public AjusteInventario procesarAjusteTransaccional(AjusteInventario ajuste,
//            List<DetalleAjusteInventario> detalles,
//            String usuario ) {
//
//        if (detalles == null || detalles.isEmpty()) {
//            throw new IllegalArgumentException("No se pueden procesar ajustes sin detalles.");
//        }
//
//        if (ajuste.getTipoAjuste() == null) {
//            throw new IllegalArgumentException("El tipo de ajuste es requerido.");
//        }
//
//        ajuste.setFechaRegistro(new Date());
//        detalles.forEach(d -> {
//            d.setAjusteInventario(ajuste);
//            d.setCodigo(null);
//        });
//        ajuste.setDetalleAjusteInventarioCollection(detalles);
//
//        // Usa saveAndFlush para obtener la clave primaria generada en BD
//        AjusteInventario ajusteGuardado = ajusteRepo.saveAndFlush(ajuste);
//
//        String numDocumento = "AJ-" + ajusteGuardado.getCodigo();
//
//        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(3);
//        TipoMovimiento tm = (ajuste.getTipoAjuste().getCodigo() == 1)
//                ? this.tipoMovimientoService.getTipoMovimientoa(1)
//                : this.tipoMovimientoService.getTipoMovimientoa(2);
//
//        for (DetalleAjusteInventario det : detalles) {
//            if (det.getArticulo() != null
//                    && Boolean.TRUE.equals(det.getArticulo().getInventariable())
//                    && det.getCantidad() != null
//                    && det.getCantidad() > 0) {
//
//                movimientoService.registrarMovimiento(
//                        det.getArticulo(),
//                        tm,
//                        tp,
//                        numDocumento,
//                        det.getCantidad(),
//                        usuario,
//                        ajuste.getObservacion()
//                );
//            }
//        }
//
//        return ajusteGuardado;
//    }
//    
//
//    @Override
//    public AjusteInventario guardar(AjusteInventario obj) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
//
//    @Override
//    public List<AjusteInventario> getLista() {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
//
//    @Override
//    public List<DetalleAjusteInventario> getDetalle(int codigoAjuste) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
//    }
//}
