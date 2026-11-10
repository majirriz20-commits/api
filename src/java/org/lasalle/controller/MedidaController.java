/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.connection.ConectionMySql;
import org.lasalle.model.Medida;
/**
 *
 * @author Maria Jose
 */
public class MedidaController {
     public List<Medida> getMedida() throws SQLException{
        //declaramos la estrucutra de datos
        List<Medida> lista = new ArrayList<>();
        //Declaramos la sentencia a ejecutar
        String sql = "SELECT * FROM medida"; 
        ConectionMySql connMysql = new ConectionMySql();
        Connection conn = connMysql.open();
        PreparedStatement pstm = conn.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        
        while(rs.next()){
            Medida medidas = new Medida();
            medidas.setIdMedida(rs.getInt("id_medida"));
            medidas.setIdUsuario(rs.getInt("id_usuairo"));
            medidas.setGlucosa(rs.getInt("glucosa"));
            medidas.setFechaHora(rs.getString("fechaHora"));
            medidas.setContexto(rs.getString("contexto"));
            medidas.setNotas(rs.getString("notas"));
            lista.add(medidas);
        }
        rs.close();
        connMysql.close();
        conn.close();
        return lista;
     }
     
    /**
     * Inserta una medida de glucosa en la tabla medida.
     * Nota: la columna del usuario en la base de datos se llama "id_usuairo" (con el error de tipeo original).
     * @param medida datos de la medida a registrar
     * @return id_medida generado por MySQL, o -1 si no se pudo obtener
     */
    public int insertMedida(Medida medida) throws SQLException{
        String sql = "INSERT INTO medida "
                   + "(id_usuairo, glucosa, fechaHora, contexto, notas) "
                   + "VALUES (?, ?, COALESCE(?, CURDATE()), ?, ?)";
        
        ConectionMySql connMysql = new ConectionMySql();
        Connection conn = connMysql.open();
        try (PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setInt(1, medida.getIdUsuario());
            pstm.setInt(2, medida.getGlucosa());
            pstm.setString(3, medida.getFechaHora());
            pstm.setString(4, medida.getContexto());
            pstm.setString(5, medida.getNotas());
            
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
}
