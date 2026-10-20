/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.TipoMerma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoMermaRepo extends JpaRepository<TipoMerma, Integer> {
    
    

    List<TipoMerma> findByActivoTrueOrderByDescripcionAsc();
}
