/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.connection;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Maria Jose
 */
public class ConectionMySql {
    
    Connection conn; 
    
    public Connection open(){
        // Detectar variables de entorno de Railway (si existen), o usar los valores locales por defecto
        String host = envOrDefault("MYSQLHOST", "127.0.0.1");
        String port = envOrDefault("MYSQLPORT", "3306");
        String bdName = envOrDefault("MYSQLDATABASE", "glucosa_api");
        String user = envOrDefault("MYSQLUSER", "admin");
        String password = envOrDefault("MYSQLPASSWORD", "root");
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + bdName;
        String parametros = "?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&characterEncoding=utf-8";
        
        try {
        // Tu código actual de conexión
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    } catch (Exception e) {
        System.out.println("Base de datos no disponible. Ejecutando sin BD: " + e.getMessage());
        return null; // Retorna null para que la app siga funcionando sin trabarse
    }
    }
    
    public void close(){
        if(conn != null){
            try{
                conn.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    // Devuelve el valor de la variable de entorno, o el valor por defecto si es nula o vacía
    private static String envOrDefault(String name, String defaultValue){
        String value = System.getenv(name);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
}