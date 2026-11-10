/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.util.ArrayList;
import java.util.List;
import org.lasalle.connection.ConectionMySql;
import org.lasalle.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
/**
 *
 * @author Maria Jose
 */
public class UsuarioController {
    public List<Usuario> getUsuarios() throws SQLException{
        //declaramos la estrucutra de datos
        List<Usuario> lista = new ArrayList<>();
        //Declaramos la sentencia a ejecutar
        String sql = "SELECT * FROM users"; 
        ConectionMySql connMysql = new ConectionMySql();
        Connection conn = connMysql.open();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        
        while(rs.next()){
            Usuario user = new Usuario();
            user.setIdUsuario(rs.getInt("id_usuario"));
            user.setNombre(rs.getString("nombre"));
            user.setPrimerApellido(rs.getString("primer_apellido"));
            user.setSegundoApellido(rs.getString("segundo_apellido"));
            user.setFechaNacimiento(rs.getString("fecha_nacimiento"));
            user.setTipoDiabetes(rs.getString("tipo_diabetes"));
            user.setFechaRegistro(rs.getString("fecha_registro"));
            user.setEmail(rs.getString("email"));
            lista.add(user);
        }
        rs.close();
        connMysql.close();
        conn.close();
        return lista;
    }
    
    /**
     * Inserta un usuario en la tabla users.
     * La fecha de registro se toma del objeto; si viene nula se usa la fecha actual.
     * @param user datos del usuario a registrar (nombre y primer apellido son obligatorios)
     * @return id_usuario generado por MySQL, o -1 si no se pudo obtener
     */
    public int insertUsuario(Usuario user) throws SQLException{
        String sql = "INSERT INTO users "
                   + "(nombre, primer_apellido, segundo_apellido, fecha_nacimiento, tipo_diabetes, fecha_registro, email) "
                   + "VALUES (?, ?, ?, ?, ?, COALESCE(?, CURDATE()), ?)";
        
        ConectionMySql connMysql = new ConectionMySql();
        Connection conn = connMysql.open();
        try (PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, user.getNombre());
            pstm.setString(2, user.getPrimerApellido());
            pstm.setString(3, user.getSegundoApellido());
            pstm.setString(4, user.getFechaNacimiento());
            pstm.setString(5, user.getTipoDiabetes());
            pstm.setString(6, user.getFechaRegistro());
            pstm.setString(7, user.getEmail());
            
            pstm.executeUpdate();
            
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } finally {
            connMysql.close();
            conn.close();
        }
        return -1;
    }
    
    /**
     * Verifica si existe un usuario con el id indicado.
     * Se usa antes de insertar una medida porque la tabla medida no tiene llave foranea.
     */
    public boolean existeUsuario(int idUsuario) throws SQLException{
        String sql = "SELECT 1 FROM users WHERE id_usuario = ?";
        ConectionMySql connMysql = new ConectionMySql();
        Connection conn = connMysql.open();
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idUsuario);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } finally {
            connMysql.close();
            conn.close();
        }
    }
    
    /**
     * Verifica si ya existe un usuario con el email indicado.
     * La tabla users no tiene restriccion UNIQUE sobre email,
     * por eso el duplicado se valida antes de insertar.
     */
    public boolean existeEmail(String email) throws SQLException{
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";
        ConectionMySql connMysql = new ConectionMySql();
        Connection conn = connMysql.open();
        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, email);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } finally {
            connMysql.close();
            conn.close();
        }
    }
}
