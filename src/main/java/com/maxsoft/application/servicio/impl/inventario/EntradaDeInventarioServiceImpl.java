package com.maxsoft.application.servicio.impl.inventario;

import com.maxsoft.application.dto.SolicitudDevolucionDto;
import com.maxsoft.application.modelo.Almacen;
import com.maxsoft.application.modelo.Articulo;
import com.maxsoft.application.modelo.DetalleEntradaInventario;
import com.maxsoft.application.modelo.DetalleFacturaDeVenta;
import com.maxsoft.application.modelo.EntradaInventario;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.TipoDocumento;
import com.maxsoft.application.modelo.TipoMovimiento;
import com.maxsoft.application.repo.EntradaDeInventarioRepo;
import com.maxsoft.application.servicio.interfaces.inventario.EntradaDeInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.MovimientoInventarioService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoDocumentoService;
import com.maxsoft.application.servicio.interfaces.inventario.TipoMovimientoService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EntradaDeInventarioServiceImpl implements EntradaDeInventarioService {

    private final EntradaDeInventarioRepo entradaRepo;
    private final MovimientoInventarioService movimientoService;
    TipoMovimientoService tipoMovimientoService;
    TipoDocumentoService tipoDocumentoService;

    @Autowired
    public EntradaDeInventarioServiceImpl(EntradaDeInventarioRepo entradaRepo,
            MovimientoInventarioService movimientoService,
            TipoMovimientoService tipoMovimientoService,
            TipoDocumentoService tipoDocumentoService
    ) {
        this.entradaRepo = entradaRepo;
        this.movimientoService = movimientoService;
        this.tipoDocumentoService = tipoDocumentoService;
        this.tipoMovimientoService = tipoMovimientoService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EntradaInventario guardar(EntradaInventario obj, String usuario) {

        // 1. Guardar la cabecera
        EntradaInventario entradaGuardada = entradaRepo.save(obj);

        String numDocumento = "ENT-" + (entradaGuardada.getCodigo() != null
                ? entradaGuardada.getCodigo()
                : System.currentTimeMillis());

        TipoDocumento tp = this.tipoDocumentoService.getTipoDocumento(1);
        TipoMovimiento tm = this.tipoMovimientoService.getTipoMovimientoa(1);

        // 2. Usar la colección del parámetro 'obj' recibido en lugar de 'entradaGuardada'
        Collection<DetalleEntradaInventario> detalles = obj.getDetalleEntradaInventarioCollection();

        if (detalles != null && !detalles.isEmpty()) {

            for (DetalleEntradaInventario detalle : detalles) {

                // Asignar manualmente la cabecera ya persistida a cada detalle
                detalle.setEntradaInventario(entradaGuardada);

//                Articulo articuloProxy = detalle.getArticulo();
//                if (articuloProxy != null && articuloProxy.getCodigo() != null) {
                Articulo articulo = detalle.getArticulo();
                // Cargar el artículo fresco desde el repositorio para evitar Lazy Proxy / inventariable null
//                    Articulo articulo = articuloRepo.findById(articuloProxy.getCodigo())
//                            .orElse(articuloProxy);
//
//                    boolean esInventariable = articulo.getInventariable() == null || Boolean.TRUE.equals(articulo.getInventariable());
//
//                    if (esInventariable) {
                double cantidadEntrante = detalle.getCantidadRecibida() != null ? detalle.getCantidadRecibida() : 0.0;

                if (cantidadEntrante > 0) {

                    movimientoService.registrarMovimiento(
                            articulo,
                            tm,
                            tp,
                            numDocumento,
                            cantidadEntrante,
                            usuario,
                            obj.getComentario()
                    );
                }
//                    }
//                }
            }
        }

        return entradaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntradaInventario> getLista() {
        return entradaRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DetalleEntradaInventario> getDetalle(int obj) {
        return entradaRepo.getDetalle(obj);
    }

    @Transactional
    @Override
    public EntradaInventario crearEntradaPorDevolucion(FacturaDeVenta factura, SolicitudDevolucionDto solicitud) {

        EntradaInventario entrada = new EntradaInventario();
        Date fechaActual = new Date();

        // 1. Asignar Fechas
        entrada.setFecha(fechaActual);
        entrada.setFechaCreacion(fechaActual);
        entrada.setFechaActualizacion(fechaActual);
        entrada.setFechaContabilizacion(fechaActual);

        // 2. Tipos de Documento y Movimiento (Tipos Integer según tu modelo)
        entrada.setTipoDocumento(3); // ID del Tipo de Documento para 'Devolución'
        entrada.setNumeroDocumento(factura.getCodigo()); // Guardamos el número de factura como documento de origen

        entrada.setTipoEntrada(1); // ID del tipo de movimiento de entrada por devolución
        entrada.setNombreTipoEntrada("DEVOLUCION DE VENTA");

        // 3. Moneda (Valores por defecto si no están configurados)
        entrada.setMoneda(1);
        entrada.setNombreMoneda("DOP"); // O la moneda correspondiente en tu sistema

        // 4. Usuario y Observaciones
        entrada.setNombreUsuario(solicitud.getUsuario());
        entrada.setAnulada(false);

        String tipoDevStr = Boolean.TRUE.equals(factura.getAnulada()) ? "Devolución Total" : "Devolución Parcial";
        entrada.setComentario(tipoDevStr + " de Factura #" + factura.getCodigo() + ". Motivo: " + solicitud.getMotivo());

        // 5. Construir Colección de Detalles
        List<DetalleEntradaInventario> detallesList = new ArrayList<>();

        for (SolicitudDevolucionDto.ItemDevolucionDto itemDev : solicitud.getItems()) {

            DetalleFacturaDeVenta detFactura = factura.getDetalleFacturaDeVentaCollection().stream()
                    .filter(d -> d.getCodigo().equals(itemDev.getIdDetalleFactura()))
                    .findFirst()
                    .orElse(null);

            if (detFactura != null && Boolean.TRUE.equals(detFactura.getArticulo().getInventariable())) {

                DetalleEntradaInventario detEntrada = new DetalleEntradaInventario();

                // Vincular relación bidireccional
                detEntrada.setEntradaInventario(entrada);
                detEntrada.setArticulo(detFactura.getArticulo());
                detEntrada.setCantidadRecibida(itemDev.getCantidadADevolver());
                detEntrada.setArticulo(detFactura.getArticulo());

                double stockActual = detEntrada.getArticulo().getExistencia()
                        != null ? detEntrada.getArticulo().getExistencia() : 0.0;

                detEntrada.setExistenciaActual(stockActual);

                detEntrada.setCantidadPedida(0.00);
                detEntrada.setCantidadPendiente(0.00);
                detEntrada.setNuevaExistencia(stockActual + itemDev.getCantidadADevolver());
                detEntrada.setNombreAlmacen("General");
                detEntrada.setNombreUnidad("Unidad");
                detEntrada.setUnidad(detFactura.getArticulo().getUnidadEntrada());
                detEntrada.setPrecioCompra(detFactura.getArticulo().getPrecioCompra());
                detEntrada.setAlmacen(new Almacen(1));

                // CORRECCIÓN: Asignar la descripción requerida por Bean Validation (@NotNull)
                detEntrada.setDescripcionArticulo(detFactura.getArticulo().getDescripcion());

                // Si tu entidad DetalleEntradaInventario maneja costos/precios:
                double costo = detFactura.getPrecioCompra() != null ? detFactura.getPrecioCompra() : 0.0;
                double precio = detFactura.getPrecioVenta() != null ? detFactura.getPrecioVenta()
                        : detFactura.getPrecioCompra();

                detEntrada.setCostoUnitario(costo);
                detEntrada.setPrecioVenta(precio);
                detEntrada.setPrecioCompra(detFactura.getArticulo().getPrecioCompra());

                detallesList.add(detEntrada);
            }
        }

        entrada.setDetalleEntradaInventarioCollection(detallesList);

        // Guardar (CascadeType.ALL se encargará de insertar los detalles en la BD)
        return entradaRepo.save(entrada);
    }
}
