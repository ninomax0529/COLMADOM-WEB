 package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.AjusteInventario;
import com.maxsoft.application.modelo.DetalleAjusteInventario;
import com.maxsoft.application.repo.AjusteInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.AjusteInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AjusteInventarioServiceImpl implements AjusteInventarioService {

    private final AjusteInventarioRepo ajusteRepo;
    private final EntradaDeInventarioService entradaInventarioService;
    private final SalidaInventarioService salidaInventarioService;

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

        // 1. Validaciones de entrada
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("No se pueden procesar ajustes sin detalles.");
        }
        if (ajuste.getTipoAjuste() == null) {
            throw new IllegalArgumentException("El tipo de ajuste es requerido.");
        }

        // 2. Asociar cabecera y detalles
        ajuste.setFechaRegistro(new Date());
        detalles.forEach(d -> {
            d.setAjusteInventario(ajuste);
            d.setCodigo(null);
        });
        ajuste.setDetalleAjusteInventarioCollection(detalles);

        // 3. Persistir el ajuste principal para obtener su ID generado
        AjusteInventario ajusteGuardado = ajusteRepo.saveAndFlush(ajuste);

        // 4. Delegar la generación del documento de inventario a su respectivo servicio
        // Codigo 1 = Incremento (Entrada) | Codigo 2 = Decremento (Salida)
        if (ajusteGuardado.getTipoAjuste().getCodigo() == 1) {
            this.entradaInventarioService.crearEntradaPorAjuste(ajusteGuardado, detalles);
        } else {
            this.salidaInventarioService.crearSalidaPorAjuste(ajusteGuardado, detalles);
        }

        return ajusteGuardado;
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