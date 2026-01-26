/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.util;
import com.vaadin.flow.component.UI;

public class SonidoUtil {

    public static void reproducir(String archivo) {
        UI.getCurrent().getPage().executeJs(
            """
            const audio = new Audio($0);
            audio.volume = 0.6;
            audio.play();
            """,
            "/sounds/" + archivo
        );
    }
}
