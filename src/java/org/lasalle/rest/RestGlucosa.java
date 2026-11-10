/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.lasalle.controller.UsuarioController;
import org.lasalle.model.Usuario;
import com.google.gson.Gson;
import org.lasalle.controller.MedidaController;
import org.lasalle.model.Medida;

/**
 *
 * @author Maria Jose
 */

@Path("glucosa")
public class RestGlucosa {
    @Path("saludar")
    @GET
    @Produces (MediaType.APPLICATION_JSON)
    public Response saludar(){
        String saludo = """
                        {"Saludo": "Hola desde java"}
                        """;
        return Response.ok(saludo).build();
    }
    
    @Path("saludarCumpleanios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarCumpleanios(
            @QueryParam("nombre") String nombre
    ){
        String out = """
                     {"response": "Feliz cumple %s"}
                     """;
        out = String.format(out,nombre);
        return Response.ok(out).build();
        
    }
    
    @GET
    @Path("getAllUsuarios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuarios(){
        UsuarioController uc = new UsuarioController();
        List<Usuario> lista = new ArrayList();
        Gson gson = new Gson();
        String out = "";
        try {
            lista = uc.getUsuarios();
            out = gson.toJson(lista);
        } catch (Exception e){
            e.printStackTrace();
            out = """
                  {"result": "Empty table"}
                  """;
        }
        return Response.ok(out).build();
    }
    
    @GET
    @Path("getAllMedida")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMedida(){
        MedidaController uc = new MedidaController();
        List<Medida> lista = new ArrayList();
        Gson gson = new Gson();
        String out = "";
        try {
            lista = uc.getMedida();
            out = gson.toJson(lista);
        } catch (Exception e) {
            e.printStackTrace();
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudieron obtener las medidas: " + e.getMessage());
        }
        return Response.ok(out).build();
    }
    
    /**
     * Registra un usuario nuevo en la tabla users.
     * Ruta: POST /api/glucosa/registraUsuario
     * Content-Type: application/x-www-form-urlencoded
     */
    @POST
    @Path("registraUsuario")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registraUsuario(
            @FormParam("nombre") String nombre,
            @FormParam("primer_apellido") String primerApellido,
            @FormParam("segundo_apellido") String segundoApellido,
            @FormParam("fecha_nacimiento") String fechaNacimiento,
            @FormParam("tipo_diabetes") String tipoDiabetes,
            @FormParam("email") String email
    ){
        //se limpian los espacios en blanco de cada parametro
        nombre = limpiar(nombre);
        primerApellido = limpiar(primerApellido);
        segundoApellido = limpiar(segundoApellido);
        tipoDiabetes = limpiar(tipoDiabetes);
        email = limpiar(email);
        
        //validaciones de campos obligatorios y longitudes (segun el DDL de la tabla users)
        if (nombre == null) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'nombre' es obligatorio");
        }
        if (nombre.length() > 45) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'nombre' no puede superar los 45 caracteres");
        }
        if (primerApellido == null) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'primer_apellido' es obligatorio");
        }
        if (primerApellido.length() > 45) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'primer_apellido' no puede superar los 45 caracteres");
        }
        if (segundoApellido != null && segundoApellido.length() > 45) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'segundo_apellido' no puede superar los 45 caracteres");
        }
        if (tipoDiabetes != null && tipoDiabetes.length() > 30) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'tipo_diabetes' no puede superar los 30 caracteres");
        }
        if (email != null && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'email' no tiene un formato valido");
        }
        if (email != null && email.length() > 50) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'email' no puede superar los 50 caracteres");
        }
        
        //la fecha de nacimiento es opcional, pero si viene debe tener formato yyyy-MM-dd
        String fechaNac = normalizarFecha(fechaNacimiento);
        if (fechaNacimiento != null && !fechaNacimiento.isBlank() && fechaNac == null) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'fecha_nacimiento' debe tener el formato yyyy-MM-dd (ejemplo: 1997-11-23)");
        }
        if (fechaNac != null && LocalDate.parse(fechaNac).isAfter(LocalDate.now())) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'fecha_nacimiento' no puede ser una fecha futura");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setPrimerApellido(primerApellido);
        usuario.setSegundoApellido(segundoApellido);
        usuario.setFechaNacimiento(fechaNac);
        usuario.setTipoDiabetes(tipoDiabetes);
        usuario.setEmail(email);
        //fecha_registro se deja en null para que MySQL use CURDATE()
        
        try {
            UsuarioController uc = new UsuarioController();
            
            //el email es opcional, pero si viene no se permite repetido
            if (email != null && uc.existeEmail(email)) {
                return error(Response.Status.CONFLICT, "Ya existe un usuario registrado con el email " + email);
            }
            
            int idUsuario = uc.insertUsuario(usuario);
            
            if (idUsuario <= 0) {
                return error(Response.Status.INTERNAL_SERVER_ERROR, "El usuario se inserto pero no se pudo obtener el id generado");
            }
            
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("result", "ok");
            body.put("mensaje", "Usuario registrado correctamente");
            body.put("id_usuario", idUsuario);
            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new Gson().toJson(body))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error al registrar el usuario: " + e.getMessage());
        }
    }
    
    /**
     * Registra una medida de glucosa nueva en la tabla medida.
     * Ruta: POST /api/glucosa/registrarMedida
     * Content-Type: application/x-www-form-urlencoded
     */
    @POST
    @Path("registrarMedida")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarMedida(
            @FormParam("id_usuario") String idUsuario,
            @FormParam("glucosa") String glucosa,
            @FormParam("fecha_hora") String fechaHora,
            @FormParam("contexto") String contexto,
            @FormParam("notas") String notas
    ){
        contexto = limpiar(contexto);
        notas = limpiar(notas);
        
        //validacion del usuario
        if (idUsuario == null || idUsuario.isBlank()) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'id_usuario' es obligatorio");
        }
        int idUsuarioInt;
        try {
            idUsuarioInt = Integer.parseInt(idUsuario.trim());
        } catch (NumberFormatException e) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'id_usuario' debe ser un numero entero");
        }
        if (idUsuarioInt <= 0) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'id_usuario' debe ser mayor que cero");
        }
        
        //validacion de la glucosa
        if (glucosa == null || glucosa.isBlank()) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'glucosa' es obligatorio");
        }
        int glucosaInt;
        try {
            glucosaInt = Integer.parseInt(glucosa.trim());
        } catch (NumberFormatException e) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'glucosa' debe ser un numero entero (mg/dL)");
        }
        if (glucosaInt <= 0 || glucosaInt > 1000) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'glucosa' debe estar entre 1 y 1000 mg/dL");
        }
        
        //la fecha es opcional, si no viene MySQL usa CURDATE()
        String fecha = normalizarFecha(fechaHora);
        if (fechaHora != null && !fechaHora.isBlank() && fecha == null) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'fecha_hora' debe tener el formato yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss");
        }
        
        //validaciones de longitud segun el DDL de la tabla medida
        if (contexto != null && contexto.length() > 100) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'contexto' no puede superar los 100 caracteres");
        }
        if (notas != null && notas.length() > 100) {
            return error(Response.Status.BAD_REQUEST, "El parametro 'notas' no puede superar los 100 caracteres");
        }
        
        Medida medida = new Medida();
        medida.setIdUsuario(idUsuarioInt);
        medida.setGlucosa(glucosaInt);
        medida.setFechaHora(fecha);
        medida.setContexto(contexto);
        medida.setNotas(notas);
        
        try {
            UsuarioController uc = new UsuarioController();
            if (!uc.existeUsuario(idUsuarioInt)) {
                return error(Response.Status.NOT_FOUND, "No existe un usuario con id_usuario " + idUsuarioInt);
            }
            
            MedidaController mc = new MedidaController();
            int idMedida = mc.insertMedida(medida);
            
            if (idMedida <= 0) {
                return error(Response.Status.INTERNAL_SERVER_ERROR, "La medida se inserto pero no se pudo obtener el id generado");
            }
            
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("result", "ok");
            body.put("mensaje", "Medida de glucosa registrada correctamente");
            body.put("id_medida", idMedida);
            body.put("id_usuario", idUsuarioInt);
            body.put("glucosa", glucosaInt);
            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new Gson().toJson(body))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error al registrar la medida: " + e.getMessage());
        }
    }
    
    /**
     * Recorta los espacios de un parametro y devuelve null si viene vacio.
     */
    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
    
    /**
     * Convierte una fecha recibida como texto a formato yyyy-MM-dd.
     * Acepta "yyyy-MM-dd", "yyyy-MM-ddTHH:mm:ss" o "yyyy-MM-dd HH:mm:ss".
     * @return la fecha en formato yyyy-MM-dd o null si no se pudo interpretar
     */
    private String normalizarFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        try {
            return LocalDate.parse(limpio).toString();
        } catch (DateTimeParseException e) {
            if (limpio.length() > 10) {
                try {
                    return LocalDate.parse(limpio.substring(0, 10)).toString();
                } catch (DateTimeParseException e2) {
                    return null;
                }
            }
            return null;
        }
    }
    
    /**
     * Construye una respuesta JSON de error.
     */
    private Response error(Response.Status status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("result", "error");
        body.put("mensaje", mensaje);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new Gson().toJson(body))
                .build();
    }
    
}
