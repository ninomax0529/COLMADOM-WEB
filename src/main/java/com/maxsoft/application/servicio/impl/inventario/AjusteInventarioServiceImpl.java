/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.AjusteInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AjusteInventarioServiceImpl implements AjusteInventarioService {

    private final AjusteInventarioRepo ajusteRepo;
    private final MovimientoInventarioService movimientoService;
    TipoMovimientoService tipoMovimientoService;
    TipoDocumentoService tipoDocumentoService;

    @Autowired
    public AjusteInventarioServiceImpl(AjusteInventarioRepo ajusteRepo,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService) {

        this.ajusteRepo = ajusteRepo;
        this.movimientoService = movimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjusteInventario procesarAjusteTransaccional(AjusteInventario ajuste,
            List<DetalleAjusteInventario> detalles,
            String usuario) {
        // 1. Validaciones previas
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("No se pueden procesar ajustes sin detalles.");
        }

        if (ajuste.getTipoAjuste() == null) {
            throw new IllegalArgumentException("El tipo de ajuste es requerido.");
        }

        // 2. Vincular cabecera y detalles
        ajuste.setFechaRegistro(new Date());
        detalles.forEach(d -> {
            d.setAjusteInventario(ajuste);
            d.setCodigo(null);
        });
        ajuste.setDetalleAjusteInventarioCollection(detalles);

        // 3. Guardar documento AjusteInventario
        AjusteInventario ajusteGuardado = ajusteRepo.save(ajuste);

        // 4. Determinar tipo de movimiento para el Kardex
//        String tipoMovimiento = "INCREMENTO".equalsIgnoreCase(ajuste.getTipoAjuste().getDescripcion())
//                ? "AJUSTE_INCREMENTO"
//                : "AJUSTE_DECREMENTO";

        String numDocumento = "AJ-" + (ajusteGuardado.getCodigo() != null ? ajusteGuardado.getCodigo() : System.currentTimeMillis());

        TipoMovimiento tm;
        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(3);

        if (ajuste.getTipoAjuste().getCodigo() == 1) {
            tm = this.tipoMovimientoService.getTipoMovimientoa(1);
        } else {
            tm = this.tipoMovimientoService.getTipoMovimientoa(2);
        }

        // 5. Registrar cada artículo en movimiento_inventario (actualiza stock y valida existencias)
        for (DetalleAjusteInventario det : detalles) {

            if (det.getCantidad() != null && det.getCantidad() > 0) {

                movimientoService.registrarMovimiento(
                        det.getArticulo(),
                        tm,
                        tp,
                        numDocumento,
                        det.getCantidad(),
                        usuario,
                        ajuste.getObservacion()
                );
            }
        }

        return ajusteGuardado;
    }

    @Override
    public AjusteInventario guardar(AjusteInventario obj) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<AjusteInventario> getLista() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<DetalleAjusteInventario> getDetalle(int codigoAjuste) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
