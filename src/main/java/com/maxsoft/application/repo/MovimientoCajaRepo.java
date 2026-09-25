/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;


import com.maxsoft.application.modelo.CajaTurno;
import com.maxsoft.application.modelo.MovimientosCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoCajaRepo extends JpaRepository<MovimientosCaja, Integer> {
    
    // Listar todos los movimientos de un turno específico
    List<MovimientosCaja> findByCajaTurno(CajaTurno cajaTurnoId);
}
