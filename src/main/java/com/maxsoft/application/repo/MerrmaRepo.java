/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.maxsoft.application.repo;

import com.maxsoft.application.modelo.DetalleMerma;
import com.maxsoft.application.modelo.Merma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MerrmaRepo extends JpaRepository<Merma, Integer> {
    
     String strDet = "  SELECT * FROM  detalle_merma  where merma=:m ";
    @Query(value = strDet, nativeQuery = true)
    public List<DetalleMerma> getDetalle(int m);
}
