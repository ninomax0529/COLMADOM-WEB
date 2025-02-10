/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

import com.example.application.data.OperacionEmpacadoraRepo;
import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.DetalleOperacionEmpacadora;
import com.example.application.modelo.OperacionEmpacadora;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Maximiliano
 */
@Service
public class OperacionEmpacadoraImpl implements OperacionEmpacadoraService {

    @Autowired
    OperacionEmpacadoraRepo oprRepo;

    @Override
    public OperacionEmpacadora guardar(OperacionEmpacadora opem) {

        OperacionEmpacadora op = null;
        try {

            op = oprRepo.save(opem);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return op;
    }

    @Override
    public List<OperacionEmpacadora> getLista() {

        return oprRepo.findAll();
    }

    @Override
    public List<DetalleOperacionEmpacadora> getDetalleOperacion(OperacionEmpacadora op) {

        List<DetalleOperacionEmpacadora> lista = null;

        try {

            System.out.println("getNombreSupervisor : " + op.getNombreSupervisor());
            lista = oprRepo.getDetalleOperacion(op.getCodigo());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static void main(String[] args) {

    }

    @Override
    public OperacionEmpacadora getOperacionEmpacadora(int turno, Date fecha) {

        Date fec;
        OperacionEmpacadora op = null;
        System.out.println("Fecha " + fecha + "  turno : " + turno);

//        String fechastr = new SimpleDateFormat("yyyy-MM-dd").format(fecha);
//            fec = new SimpleDateFormat("yyyy-MM-dd").parse(fechastr);
//            System.out.println("Fecha " + fec);
        op = oprRepo.getOperacionEmpacadora(turno, fecha);
//        System.out.println("Turno " + op.getNombreTurno());

        return op;
    }

    @Override
    public List<DetalleOperacionEmpacadora> getDetalleOperacion(OperacionEmpacadora op, int empacadora, int producto) {

        List<DetalleOperacionEmpacadora> lista = null;
        int codOp = op.getCodigo();

        try {

            lista = oprRepo.getDetalleOperacion(codOp, empacadora, producto);

        } catch (Exception e) {
            System.out.println("Erro : " + e.getMessage());
        }

        return lista;
    }

    @Override
    public List<DetalleMovimientoProducto> getDetalleMovimientoProducto(OperacionEmpacadora op) {

        List<DetalleMovimientoProducto> lista = null;

        try {

            lista = oprRepo.getDetalleMovimientoProducto(op.getCodigo());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    @Override
    public Double getEmpacado(int turno, Date fecha, int product) {

        Double empacado = this.oprRepo.getEmpacado(turno, fecha, product);

        return empacado == null ? 0 : empacado;
    }

    @Override
    public OperacionEmpacadora getOperacionEmpacadora(int turno, int estado, Date fecha) {

        Date fec;
        OperacionEmpacadora op;
        System.out.println("Fecha " + 1 + "  turno : " + turno);

        op = oprRepo.getOperacionEmpacadora(turno,estado, fecha);


        return op;

    }

    @Override
    public Double getEmpacado(Date fecha, int product) {
               
        Double empacado = this.oprRepo.getEmpacado(fecha, product);

        return empacado == null ? 0 : empacado;
    }

    @Override
    public Double getEmpacadoPorSilo(Date fecha, int silo) {
       
                  
        Double empacado = this.oprRepo.getEmpacadoPorSilo(fecha, silo);

        return empacado == null ? 0 : empacado;
    }

}
