/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.util;

/**
 *
 * @author maximilianoalmonte
 */


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Almacena temporalmente objetos para navegación entre vistas.
 * Ideal para pasar entidades o datos entre rutas en Vaadin Flow.
 */
public class NavigationContext {

    private static final Map<String, Object> CONTEXT = new HashMap<>();

    public static String store(Object obj) {
        String key = UUID.randomUUID().toString();
        CONTEXT.put(key, obj);
        return key;
    }

    @SuppressWarnings("unchecked")
    public static <T> T retrieve(String key, Class<T> type) {
        Object value = CONTEXT.remove(key); // se elimina al usar
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}
