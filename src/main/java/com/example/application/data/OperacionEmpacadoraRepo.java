/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

import com.example.application.modelo.DetalleMovimientoProducto;
import com.example.application.modelo.DetalleOperacionEmpacadora;
import com.example.application.modelo.OperacionEmpacadora;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Maximiliano
 */
@Repository
public interface OperacionEmpacadoraRepo extends JpaRepository<OperacionEmpacadora, Integer> {

    String str1 = "  select * from  detalle_operacion_empacadora o where operacion_empacadora=:op ";

    @Query(value = str1, nativeQuery = true)
    public List<DetalleOperacionEmpacadora> getDetalleOperacion(@Param("op") int op);

    String str3 = "  select * from  detalle_movimiento_producto o where operacion_empacadora=:op ";

    @Query(value = str3, nativeQuery = true)
    public List<DetalleMovimientoProducto> getDetalleMovimientoProducto(@Param("op") int op);

    String str2 = "  select * from  detalle_operacion_empacadora o"
            + " where operacion_empacadora=:op "
            + " and empacadora=:emp "
            + " and producto=:produ ";

    @Query(value = str2, nativeQuery = true)
    public List<DetalleOperacionEmpacadora> getDetalleOperacion(@Param("op") int op, @Param("emp") int empa, @Param("produ") int produ);

    String str = "  SELECT * from operacion_empacadora  "
            + " where turno=:tn  and date(fecha_inicio)=:fecha ";
    @Query(value = str, nativeQuery = true)
    public OperacionEmpacadora getOperacionEmpacadora(@Param("tn") int tn, @Param("fecha") Date fecha);
    
      String strest = "  SELECT * from operacion_empacadora  "
            + " where turno=:tn and estado=:estadoParam and date(fecha_inicio)=:fecha ";

    @Query(value = strest, nativeQuery = true)
    public OperacionEmpacadora getOperacionEmpacadora(@Param("tn") int tn,@Param("estadoParam") int estado, @Param("fecha") Date fecha);

    String sqlEmpacado = """
                         SELECT  sum(d.empacado_por_hora) as produccion from  operacion_empacadora o
                         
                         INNER JOIN detalle_operacion_empacadora d on o.codigo=d.operacion_empacadora and o.turno=:tn
                         
                          where date(o.fecha_creacion)=:fecha and d.producto=:produ """;
    
     @Query(value = sqlEmpacado, nativeQuery = true)
    public Double  getEmpacado(@Param("tn") int tn, @Param("fecha") Date fecha,@Param("produ") Integer produc);
    
        String sqlTotalEmpacado = """
                         SELECT  sum(d.empacado_por_hora) as produccion from  operacion_empacadora o
                         
                         INNER JOIN detalle_operacion_empacadora d on o.codigo=d.operacion_empacadora 
                         
                          where date(o.fecha_creacion)=:fecha and d.producto=:produ """;
    
     @Query(value = sqlTotalEmpacado, nativeQuery = true)
    public Double  getEmpacado(@Param("fecha") Date fecha,@Param("produ") Integer produc);


}
