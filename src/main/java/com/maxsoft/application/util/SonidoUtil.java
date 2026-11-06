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
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const audio = new Audio($0);
        const source = audioCtx.createMediaElementSource(audio);
        const gainNode = audioCtx.createGain();
        
        // 1.0 es el volumen normal, 2.6 es un 260% de volumen
        gainNode.gain.value = 2.6; 
        
        source.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        
        audio.play();
        """, 
        "/sounds/" + archivo
    );
}


//    public static void reproducir(String archivo) {
//        UI.getCurrent().getPage().executeJs(
//            """
//            const audio = new Audio($0);
//            audio.volume = 2.6;
//            audio.play();
//            """,
//            "/sounds/" + archivo
//        );
//    }
}
