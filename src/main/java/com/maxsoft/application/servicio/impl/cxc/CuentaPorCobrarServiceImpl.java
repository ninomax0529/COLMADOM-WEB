package com.maxsoft.application.servicio.impl.cxc;

import com.maxsoft.application.dto.ResumenCreditoDTO;
import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.modelo.CuentaPorCobrar;
import com.maxsoft.application.modelo.DetalleReciboDeIngreso;
import com.maxsoft.application.modelo.FacturaDeVenta;
import com.maxsoft.application.modelo.HistorialLimiteCredito;
import com.maxsoft.application.modelo.ReciboDeIngreso;
import com.maxsoft.application.repo.ClienteRepo;
import com.maxsoft.application.repo.CuentaPorCobrarRepo;
import com.maxsoft.application.repo.HistorialLimiteCreditoRepo;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaPorCobrarServiceImpl implements CuentaPorCobrarService {

    private final CuentaPorCobrarRepo repo;
    private final HistorialLimiteCreditoRepo historialRepo;
    private final ClienteRepo clienteRepo;

    public CuentaPorCobrarServiceImpl(CuentaPorCobrarRepo repo,
            HistorialLimiteCreditoRepo historialRepo, ClienteRepo clienteRepo) {
        this.repo = repo;
        this.historialRepo = historialRepo;
        this.clienteRepo = clienteRepo;
    }

    // ============================================================
    // MOVIMIENTOS
    // ============================================================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaPorCobrar registrarFacturaCredito(FacturaDeVenta factura, String usuario) {

        if (factura == null || factura.getCodigo() == null) {
            throw new IllegalArgumentException("La factura a crédito no es válida.");
        }
        if (factura.getCliente() == null || factura.getCliente().getCodigo() == null) {
            throw new IllegalStateException("Seleccione el cliente: una venta a crédito debe quedar a nombre de alguien.");
        }
        // Lo devuelto antes de pasar a crédito (p. ej. delivery sin liquidar) no se debe
        BigDecimal monto = nz(factura.getTotal()).subtract(factura.getTotalDevuelto()).max(BigDecimal.ZERO);
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException("La factura a crédito #" + factura.getCodigo() + " no tiene monto.");
        }
        // Ya está si tiene más débitos de factura que reversos (un reverso permite volver a cargarla)
        if (repo.contarMovimientosFactura(factura.getCodigo(), CuentaPorCobrar.FACTURA)
                > repo.contarMovimientosFactura(factura.getCodigo(), CuentaPorCobrar.REVERSO_CREDITO)) {
            throw new IllegalStateException("La factura #" + factura.getCodigo() + " ya está en el estado de cuenta.");
        }

        Integer clienteId = factura.getCliente().getCodigo();
        // Bloquea al cliente antes de validar para que dos ventas a la vez no pasen el límite
        BigDecimal balance = bloquear(clienteId);
        validarCredito(clienteId, balance, monto);

        return registrar(clienteId, balance, CuentaPorCobrar.FACTURA, factura, null,
                "FAC-" + factura.getCodigo(),
                "Factura a crédito #" + factura.getCodigo(),
                monto, BigDecimal.ZERO, usuario, factura.getFechaCreacion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaPorCobrar registrarPago(ReciboDeIngreso recibo, DetalleReciboDeIngreso detalle, String usuario) {

        FacturaDeVenta factura = detalle.getFactura();
        BigDecimal monto = nz(detalle.getTotal());
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException("El pago a la factura #" + factura.getCodigo() + " debe ser mayor que cero.");
        }
        Integer clienteId = factura.getCliente() != null ? factura.getCliente().getCodigo()
                : recibo.getCliente() != null ? recibo.getCliente().getCodigo() : null;
        if (clienteId == null) {
            throw new IllegalStateException("La factura #" + factura.getCodigo() + " no tiene cliente.");
        }

        BigDecimal balance = bloquear(clienteId);
        String concepto = "Pago recibo #" + recibo.getCodigo() + " a factura #" + factura.getCodigo();
        return registrar(clienteId, balance, CuentaPorCobrar.RECIBO, factura, recibo,
                "RI-" + recibo.getCodigo(), concepto,
                BigDecimal.ZERO, monto, usuario, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaPorCobrar registrarAnulacionFactura(FacturaDeVenta factura, String motivo, String usuario) {

        Integer clienteId = factura.getCliente().getCodigo();
        BigDecimal balance = bloquear(clienteId);
        BigDecimal valor = saldoFactura(factura.getCodigo());
        if (valor.signum() <= 0) {
            return null; // ya estaba saldada: no cambia lo que debe el cliente
        }
        String concepto = "Anulación factura #" + factura.getCodigo()
                + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : "");
        return registrar(clienteId, balance, CuentaPorCobrar.ANULACION_FACTURA, factura, null,
                "FAC-" + factura.getCodigo(), concepto,
                BigDecimal.ZERO, valor, usuario, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaPorCobrar registrarReversoCredito(FacturaDeVenta factura, String motivo, String usuario) {

        if (factura.getCliente() == null || factura.getCliente().getCodigo() == null) {
            return null;
        }
        Integer clienteId = factura.getCliente().getCodigo();
        BigDecimal balance = bloquear(clienteId);
        BigDecimal valor = saldoFactura(factura.getCodigo());
        if (valor.signum() <= 0) {
            return null;
        }
        String concepto = "Factura #" + factura.getCodigo() + " vuelve a contado"
                + (motivo != null && !motivo.isBlank() ? ". " + motivo.trim() : "");
        return registrar(clienteId, balance, CuentaPorCobrar.REVERSO_CREDITO, factura, null,
                "FAC-" + factura.getCodigo(), concepto,
                BigDecimal.ZERO, valor, usuario, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaPorCobrar registrarAnulacionRecibo(ReciboDeIngreso recibo, FacturaDeVenta factura, String motivo, String usuario) {

        BigDecimal acreditado = nz(repo.creditoDeRecibo(recibo.getCodigo(), factura.getCodigo()));
        if (acreditado.signum() <= 0) {
            return null; // ese pago no estaba en el estado de cuenta (p. ej. factura de contado)
        }
        Integer clienteId = factura.getCliente() != null ? factura.getCliente().getCodigo()
                : recibo.getCliente() != null ? recibo.getCliente().getCodigo() : null;
        if (clienteId == null) {
            throw new IllegalStateException("La factura #" + factura.getCodigo() + " no tiene cliente.");
        }
        BigDecimal balance = bloquear(clienteId);
        String concepto = "Anulación recibo #" + recibo.getCodigo() + " (pago a factura #" + factura.getCodigo() + ")"
                + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : "");
        return registrar(clienteId, balance, CuentaPorCobrar.ANULACION_RECIBO, factura, recibo,
                "ANU-RI-" + recibo.getCodigo(), concepto,
                acreditado, BigDecimal.ZERO, usuario, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaPorCobrar registrarDevolucion(FacturaDeVenta factura, BigDecimal monto, String motivo, String usuario) {

        if (monto == null || monto.signum() <= 0) {
            return null;
        }
        Integer clienteId = factura.getCliente().getCodigo();
        BigDecimal balance = bloquear(clienteId);
        BigDecimal valor = monto.min(saldoFactura(factura.getCodigo()));
        if (valor.signum() <= 0) {
            return null; // la factura ya está saldada en el estado de cuenta
        }
        String concepto = "Devolución factura #" + factura.getCodigo()
                + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : "");
        return registrar(clienteId, balance, CuentaPorCobrar.DEVOLUCION, factura, null,
                "DEV-FAC-" + factura.getCodigo(), concepto,
                BigDecimal.ZERO, valor, usuario, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CuentaPorCobrar registrarAjuste(Integer cliente, BigDecimal monto, boolean esDebito,
            String concepto, String usuario) {

        BigDecimal valor = nz(monto);
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("El monto del ajuste debe ser mayor que cero.");
        }
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("Indique el motivo del ajuste.");
        }
        BigDecimal balance = bloquear(cliente);
        return registrar(cliente, balance,
                esDebito ? CuentaPorCobrar.AJUSTE_DEBITO : CuentaPorCobrar.AJUSTE_CREDITO,
                null, null, "AJUSTE", concepto.trim(),
                esDebito ? valor : BigDecimal.ZERO,
                esDebito ? BigDecimal.ZERO : valor,
                usuario, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal saldoFactura(Integer factura) {
        return nz(repo.saldoFactura(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean facturaEnEstadoDeCuenta(FacturaDeVenta factura) {

        return factura != null && factura.getCodigo() != null
                && esCredito(factura)
                && repo.existeMovimientoFactura(factura.getCodigo(), CuentaPorCobrar.FACTURA);
    }

    // ============================================================
    // LÍMITE DE CRÉDITO
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public void validarCredito(Integer cliente, BigDecimal montoNuevo) {

        // Verificación previa (sin bloqueo); la definitiva se hace al registrar la factura
        BigDecimal balance = clienteRepo.findById(cliente).map(Cliente::getBalance).orElse(BigDecimal.ZERO);
        validarCredito(cliente, nz(balance), nz(montoNuevo));
    }

    private void validarCredito(Integer clienteId, BigDecimal balance, BigDecimal monto) {

        Cliente cliente = clienteRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("El cliente " + clienteId + " no existe."));

        if (Boolean.FALSE.equals(cliente.getHabilitado())) {
            throw new IllegalStateException("El cliente " + cliente.getNombre()
                    + " está deshabilitado y no se le puede fiar.");
        }

        BigDecimal limite = clienteRepo.leerLimiteCredito(clienteId);
        if (limite == null) {
            return; // sin límite definido
        }
        BigDecimal nuevoBalance = balance.add(monto);
        if (nuevoBalance.compareTo(limite) > 0) {
            BigDecimal disponible = limite.subtract(balance).max(BigDecimal.ZERO);
            throw new IllegalStateException("Límite de crédito excedido para " + cliente.getNombre()
                    + ". Límite RD$ " + fmt(limite)
                    + ", debe RD$ " + fmt(balance)
                    + ", disponible RD$ " + fmt(disponible)
                    + ". Esta factura: RD$ " + fmt(monto) + ".");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCreditoDTO obtenerResumen(Integer cliente) {

        Cliente c = clienteRepo.findById(cliente)
                .orElseThrow(() -> new IllegalArgumentException("El cliente " + cliente + " no existe."));
        BigDecimal balance = nz(c.getBalance());
        BigDecimal limite = c.getLimiteCredito();
        BigDecimal disponible = limite != null ? limite.subtract(balance).max(BigDecimal.ZERO) : null;
        return new ResumenCreditoDTO(limite, balance, disponible);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrar> getEstadoDeCuenta(Integer cliente) {
        return repo.findEstadoDeCuenta(cliente);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registrarCambioLimite(Integer cliente, BigDecimal limiteAnterior, BigDecimal limiteNuevo,
            String motivo, String usuario) {

        if (iguales(limiteAnterior, limiteNuevo)) {
            return;
        }
        HistorialLimiteCredito h = new HistorialLimiteCredito();
        h.setCliente(clienteRepo.getReferenceById(cliente));
        h.setLimiteAnterior(limiteAnterior);
        h.setLimiteNuevo(limiteNuevo);
        h.setMotivo(motivo != null && !motivo.isBlank() ? motivo.trim()
                : (limiteAnterior == null ? "Límite inicial" : "Cambio de límite"));
        h.setUsuario(usuario);
        h.setFecha(new Date());
        historialRepo.save(h);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialLimiteCredito> getHistorialLimite(Integer cliente) {
        return historialRepo.findByCliente(cliente);
    }

    // ============================================================
    // AUXILIARES
    // ============================================================
    private CuentaPorCobrar registrar(Integer clienteId, BigDecimal balanceAnterior, String tipo,
            FacturaDeVenta factura, ReciboDeIngreso recibo, String numeroDocumento, String concepto,
            BigDecimal debito, BigDecimal credito, String usuario, Date fecha) {

        BigDecimal balanceNuevo = balanceAnterior.add(debito).subtract(credito);
        Date ahora = new Date();

        CuentaPorCobrar mov = new CuentaPorCobrar();
        mov.setCliente(clienteRepo.getReferenceById(clienteId));
        mov.setFecha(fecha != null ? fecha : ahora);
        mov.setTipoMovimiento(tipo);
        mov.setFactura(factura);
        mov.setReciboIngreso(recibo);
        mov.setNumeroDocumento(numeroDocumento);
        mov.setConcepto(concepto != null && concepto.length() > 255 ? concepto.substring(0, 255) : concepto);
        mov.setDebito(debito);
        mov.setCredito(credito);
        mov.setBalanceAnterior(balanceAnterior);
        mov.setBalanceNuevo(balanceNuevo);
        mov.setUsuario(usuario != null ? usuario : "Admin");
        mov.setFechaCreacion(ahora);

        CuentaPorCobrar guardado = repo.save(mov);
        clienteRepo.actualizarBalance(clienteId, balanceNuevo);
        return guardado;
    }

    private BigDecimal bloquear(Integer clienteId) {
        return nz(clienteRepo.bloquearYLeerBalance(clienteId));
    }

    private static boolean esCredito(FacturaDeVenta f) {
        return f.getTipoVenta() != null
                && Objects.equals(f.getTipoVenta().getCodigo(), TIPO_VENTA_CREDITO);
    }

    private static boolean iguales(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static String fmt(BigDecimal v) {
        return String.format("%,.2f", v.setScale(2, RoundingMode.HALF_UP));
    }
}
