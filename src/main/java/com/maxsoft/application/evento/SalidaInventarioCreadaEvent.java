/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.evento;

import java.util.List;

public class SalidaInventarioCreadaEvent {

    private final Integer idSalida;
    private final String numeroDocumento; // Número de Factura o Documento Origen
    private final String usuario;
    private final List<ItemSalidaDto> items;

    public SalidaInventarioCreadaEvent(Integer idSalida, String numeroDocumento, String usuario, List<ItemSalidaDto> items) {
        this.idSalida = idSalida;
        this.numeroDocumento = numeroDocumento;
        this.usuario = usuario;
        this.items = items;
    }

    public Integer getIdSalida() { 
        return idSalida; 
    }

    public String getNumeroDocumento() { 
        return numeroDocumento; 
    }

    public String getUsuario() { 
        return usuario; 
    }

    public List<ItemSalidaDto> getItems() { 
        return items; 
    }

  

    // =========================================================================
    // DTO ESTÁTICO INTERNO PARA CADA ÍTEM DE LA SALIDA DE INVENTARIO
    // =========================================================================
    public static class ItemSalidaDto {
        private final Integer idArticulo;
        private final Double cantidad;

        public ItemSalidaDto(Integer idArticulo, Double cantidad) {
            this.idArticulo = idArticulo;
            this.cantidad = cantidad;
        }

        public Integer getIdArticulo() { 
            return idArticulo; 
        }

        public Integer getCodigoArticulo() { 
            return idArticulo; // Alias por compatibilidad de nombres
        }

        public Double getCantidad() { 
            return cantidad; 
        }
    }
}