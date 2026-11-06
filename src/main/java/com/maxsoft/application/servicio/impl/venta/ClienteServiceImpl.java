/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.servicio.impl.venta;

import com.maxsoft.application.modelo.Cliente;
import com.maxsoft.application.repo.ClienteRepo;
import com.maxsoft.application.servicio.interfaces.cxc.CuentaPorCobrarService;
import com.maxsoft.application.servicio.interfaces.venta.ClienteService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    ClienteRepo repo;

    @Autowired
    CuentaPorCobrarService cuentaPorCobrarService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Cliente guardar(Cliente obj) {
        return guardar(obj, obj.getActualizadoPor() != null ? obj.getActualizadoPor() : "Admin", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Cliente guardar(Cliente obj, String usuario, String motivoCambioLimite) {

        BigDecimal nuevo = obj.getLimiteCredito();
        if (nuevo != null && nuevo.signum() < 0) {
            throw new IllegalArgumentException("El límite de crédito no puede ser negativo.");
        }

        // Límite que tenía antes de este cambio (null si el cliente es nuevo)
        BigDecimal anterior = obj.getCodigo() != null ? repo.leerLimiteCredito(obj.getCodigo()) : null;

        Cliente guardado = repo.save(obj);

        boolean cambio = anterior == null ? nuevo != null
                : nuevo == null || anterior.compareTo(nuevo) != 0;
        if (cambio) {
            cuentaPorCobrarService.registrarCambioLimite(guardado.getCodigo(), anterior, nuevo,
                    motivoCambioLimite, usuario);
        }
        return guardado;
    }

    @Override
    public List<Cliente> getLista() {

        return repo.findAll();
    }

}
