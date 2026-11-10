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
        String host = System.getenv("MYSQLHOST") != null ? System.getenv("MYSQLHOST") : "127.0.0.1";
        String port = System.getenv("MYSQLPORT") != null ? System.getenv("MYSQLPORT") : "3306";
        String bdName = System.getenv("MYSQLDATABASE") != null ? System.getenv("MYSQLDATABASE") : "glucosa_api";
        String user = System.getenv("MYSQLUSER") != null ? System.getenv("MYSQLUSER") : "admin";
        String password = System.getenv("MYSQLPASSWORD") != null ? System.getenv("MYSQLPASSWORD") : "root";
        
        String url = "jdbc:mysql://" + host + ":" + port + "/" + bdName;
        String parametros = "?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&characterEncoding=utf-8";
        
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url + parametros, user, password);
            return conn; 
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
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
}