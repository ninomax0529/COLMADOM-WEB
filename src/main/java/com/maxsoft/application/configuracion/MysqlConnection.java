/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maxsoft.application.configuracion;

/**
 *
 * @author maximilianoalmonte
 */

    
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlConnection {
    // Configuración de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/db_colmadom_v1";
    private static final String USER = "root";
    private static final String PASSWORD = "root1234";
    
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Registrar el driver (opcional en JDBC 4.0+)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establecer conexión
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a MySQL 8 colmadom ");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver JDBC no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos");
            e.printStackTrace();
        }
        return connection;
    }
    
    public static void main(String[] args) {
        // Prueba de conexión
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexión cerrada correctamente");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

