/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.services;

import com.example.application.modelo.CementoEmpacadoPorSilo;
import com.example.application.modelo.DetalleCementoEmpacadoPorSilo;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Maximiliano
 */
public interface CementoEmcadoPorSiloService {

    public CementoEmpacadoPorSilo guardar(CementoEmpacadoPorSilo cementoEmpacado);

    public List<CementoEmpacadoPorSilo> getLista();

    public CementoEmpacadoPorSilo getCementoEmcadoPorSilo(int codigo);

    public CementoEmpacadoPorSilo getCementoEmcadoPorSilo(Date fecha);

    public List<DetalleCementoEmpacadoPorSilo> getDetalle(int cementoEmcado);
}
