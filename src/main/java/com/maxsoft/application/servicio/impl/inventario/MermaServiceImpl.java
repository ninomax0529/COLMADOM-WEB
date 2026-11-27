/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import com.maxsoft.application.modelo.SalidaInventario;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.MovimientoInventario;
import com.maxsoft.application.repo.MovimientoInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.repo.MerrmaRepo;
import com.maxsoft.application.servicio.interfaces.inventario.MermaService;
import com.maxsoft.application.servicio.interfaces.inventario.SalidaInventarioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MermaServiceImpl implements MermaService {

    @Autowired
    MerrmaRepo repo;
    @Autowired
    SalidaInventarioService salidaInventarioService;
    @Autowired
    EntradaDeInventarioService entradaService;
    @Autowired
    MovimientoInventarioRepo movimientoRepo;
    @Autowired
    com.maxsoft.application.repo.EmpresaRepo empresaRepo;

    @Override
    public List<Merma> getLista() {
        return repo.findAll();
    }

    @Override
    public List<DetalleMerma> getDetalle(Integer merma) {
        return repo.getDetalle(merma);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Merma> buscar(java.time.LocalDate desde, java.time.LocalDate hasta, Integer tipoMerma, Integer almacen) {
        // Sin límite = fechas extremas (un parámetro de fecha null complica la consulta)
        java.time.LocalDate d = desde != null ? desde : java.time.LocalDate.of(1900, 1, 1);
        java.time.LocalDate h = hasta != null ? hasta : java.time.LocalDate.of(9999, 12, 31);
        return repo.buscar(java.sql.Date.valueOf(d), java.sql.Date.valueOf(h), tipoMerma, almacen);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Merma procesarMerma(Merma merma, List<DetalleMerma> detalles, String usuario) {

        detalles.forEach(d -> {
            d.setCodigo(null);
            d.setMerma(merma);
        });

        merma.setNombreUsuario(usuario);
        merma.setDetalleMermaCollection(detalles);

        Merma mermadb = repo.save(merma);

        // La merma queda enlazada a la salida que descontó la existencia
        // (columna "Salida N°" de la consulta y base para anularla)
        SalidaInventario salida = this.salidaInventarioService.crearSalidaPorMerma(mermadb, detalles);
        mermadb.setSalidaInventario(salida);

        return repo.save(mermadb);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Merma anular(Integer codigoMerma, String motivo, String usuario) {

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Indique el motivo de la anulación.");
        }
        Merma merma = repo.findById(codigoMerma)
                .orElseThrow(() -> new IllegalArgumentException("No existe la merma #" + codigoMerma));
        if (merma.isAnulada()) {
            throw new IllegalStateException("La merma #" + codigoMerma + " ya está anulada.");
        }
        if (merma.getSalidaInventario() == null) {
            throw new IllegalStateException("La merma #" + codigoMerma + " no tiene su salida de inventario "
                    + "enlazada (es anterior a la corrección); devuelva la existencia con un ajuste.");
        }

        // Lo que realmente salió, lote por lote, según el kardex de su salida
        List<MovimientoInventario> salidas = movimientoRepo.findPorDocumento(
                "SAL-" + merma.getSalidaInventario().getCodigo(), 2);

        String user = usuario != null && !usuario.isBlank() ? usuario : "SISTEMA";
        merma.setAnulada(true);
        merma.setFechaAnulacion(new java.util.Date());
        merma.setMotivoAnulacion(motivo.trim().length() > 255 ? motivo.trim().substring(0, 255) : motivo.trim());
        merma.setUsuarioAnulacion(user);

        EntradaInventario entrada = entradaService.crearEntradaPorAnulacionMerma(merma, salidas, user);
        merma.setEntradaAnulacion(entrada.getCodigo());

        return repo.save(merma);
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] comprobantePdf(Integer codigoMerma) {
        Merma merma = repo.findById(codigoMerma)
                .orElseThrow(() -> new IllegalArgumentException("No existe la merma #" + codigoMerma));
        return com.maxsoft.application.reporte.inventario.ComprobanteMermaPdf.generar(
                merma, repo.getDetalle(codigoMerma), empresaRepo.findFirstByOrderByCodigoAsc().orElse(null));
    }

}
