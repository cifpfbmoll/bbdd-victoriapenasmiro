/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica8;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

/**
 *
 * @author victoriapenas
 */
public class Practica8 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            mostrarMenu();
        } catch (SQLException ex) {
            System.out.println(ex.getSQLState());
            System.out.println(ex.getMessage());
            System.out.println("No se ha podido conectar a la base de datos");
        }catch (FileNotFoundException ex) {
            System.out.println("No se ha encontrado el fichero de registros.");
        } catch (IOException ex) {
            System.out.println("Se ha producido un error inesperado.");
            System.out.println(ex.getMessage());
            System.out.println(ex.getCause());
        }
    }
    
    public static Connection obtenerConexion () throws SQLException {
        String url = "jdbc:mysql://127.0.0.1:3306/daw";
        return DriverManager.getConnection (url, "root", "");
    }
    
    public static void mostrarMenu() throws SQLException, IOException{
        Scanner lector = new Scanner(System.in);
        int opcion;
        boolean salir = false;
        do{
            System.out.println("---------------------- M E N U ----------------------");
            System.out.println("1. Consulta.");
            System.out.println("2. Actualización.");
            System.out.println("3. Inserción.");
            System.out.println("4. Salir");
            System.out.println("Dime una opcion: ");
            opcion = lector.nextInt();
            switch(opcion){
                case 1:
                    menuConsulta();
                    break;
                case 2:
                    updateTableSeguro();
                    break;
                case 3:
                    break;
                case 4:
                    salir = true;
                    break;
                default:
                    System.out.println("La opcion indicada no existe, indica otra.");
            }
        }while(salir == false);
    }
    
    public static void menuConsulta() throws SQLException, IOException{
        Scanner lector = new Scanner(System.in);
        int opcion;
        boolean salir = false;
        do{
            System.out.println("---------------------- M E N U ----------------------");
            System.out.println("1. Ejecuta query no preparedStatement.");
            System.out.println("2. Ejecuta query con preparedStatement sobre PK.");
            System.out.println("3. Salir.");
            System.out.println("Dime una opcion: ");
            opcion = lector.nextInt();
            switch(opcion){
                case 1:
                    ejecutarQueryPeligrosa();
                    break;
                case 2:
                    ejecutarQuerySegura();
                    break;
                case 3:
                    salir = true;
                    break;
                default:
                    System.out.println("La opcion indicada no existe, indica otra.");
            }
        }while(salir == false);
                    
    }
    
    public static void ejecutarQueryPeligrosa() throws SQLException, FileNotFoundException, IOException{
        Scanner lector = new Scanner(System.in);
        Connection con = obtenerConexion();
        Statement st = con.createStatement();
        System.out.println("Dime una marca de cerveza y te daré su precio en los bares donde está disponible");
        System.out.println("Las opciones son: \n Amstel\n Budweiser\n Corona\n Dixie\n Erdinger\n Full Sail \n");
        String cerveza = lector.nextLine();
        //ResulSet nos devuelve un cursor
        ResultSet rs = st.executeQuery("Select * from serves where beer = '" + cerveza + "'");
        String bar;
        String beer = "";
        Float price;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("logQueriesPeligrosas", true))){
            writer.write(registrarFecha() + " - " + registrarHora());
            writer.newLine();
            writer.write("CERVEZA: " + beer);
            while (rs.next ()) {
                bar = rs.getString("bar");//recupero el valor de la columna bar, que es un varchar
                beer = rs.getString(2);//recuero el valor de la columna 2, que es un varchar
                price = rs.getFloat(3);
                writer.write("BAR: " + bar + " : " + price + "\n");
                System.out.println("BAR: " + bar + " : " + price);
            }
        }finally{
            //Si rs es distinto de null es que se le pudo asignar un recurso
            if (rs != null) rs.close (); //cierra el objeto ResultSet llamado rs.
            if (st != null) st.close ();//cierra el objeto Statement llamado st
            if (con != null) con.close (); //cierra el objeto Connection llamado con
        }        
    }
    
    public static void ejecutarQuerySegura() throws SQLException, IOException{
        Scanner lector = new Scanner(System.in);
        System.out.println("Dime el nombre de un drinker y te enseñaré su direccion");
        System.out.println("Las opciones son: \n Amy\n Ben\n Coy\n Dan\n Eve");
        String name = lector.nextLine();
        String query = "Select * from drinker where name = ?";
        Connection con = obtenerConexion();
        PreparedStatement pst = con.prepareStatement(query);
        pst.setString(1, name);
        ResultSet rs = pst.executeQuery();
       //utilizo el try-with-resources para aplicar el autoclosable de los recursos cargados, asi no hace falta cerrarlos
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("logQueriesSeguras", true))){
            writer.write(registrarFecha() + " - " + registrarHora());
            writer.newLine();
            while (rs.next ()) {
                writer.write("NAME: " + rs.getString(1) + " - ADDRESS: " + rs.getString("address") + "\n");//indico por indice de columna o por nombre
                System.out.println("NAME: " + rs.getString(1) + " - ADDRESS: " + rs.getString("address") + "\n");
            }
        }finally{
            //Si rs es distinto de null es que se le pudo asignar un recurso
            if (rs != null) rs.close (); //cierra el objeto ResultSet llamado rs.
            if (pst != null) pst.close ();//cierra el objeto Statement llamado st
            if (con != null) con.close (); //cierra el objeto Connection llamado con
        }
    }
    
    public static void updateTableSeguro() throws SQLException{
        /*SUPER IMPORTANTE: El nombre de la tabla y de las columnas, no pueden estar con ?, da error de sintaxis*/
        Scanner lector = new Scanner(System.in);
        //conecto a la bbdd
        Connection con = obtenerConexion();
        String auxValor; //auxiliar para recoger los valores a modificar
        String auxCampo; //aux para recoger el nombre del campo de la condicion (where)
        String auxCondicion; //aux para recoger la condicion a tener en cuenta en la query
        ArrayList <String> campos; //en esta lista guardaré los campos que quiere modificar el usuario
        int filasAfectadas = 0; //contador donde iré sumando los cambios
        boolean condicion = obtenerCondicion();//pregunto al usuario si el update será con una condicion
        String query;
        System.out.println("¿Qué tabla quieres modificar?");
        System.out.println("Las existentes son: \nbar\nbeer\ndrinker\nfrequents\nlikes\nserves");
        String tabla = lector.nextLine();
        //preparo la sentencia **importante, una vez asignada la query al PreparedStatement no puedo modificarla más adelante**
        PreparedStatement pst;
        System.out.println("¿Cuántos campos de la tabla " + tabla + " quieres modificar?");
        int updates = Integer.parseInt(lector.nextLine()); //parseo para que el nextInt se se coma el salto de carro
        campos = pedirCampos(updates);        
        for (int i = 0; i<campos.size();i++){
            //en funcion de la condicion tendré una query u otra
            if (condicion == false){
                query = "update " + tabla + " set " + campos.get(i) + " = ?";
            }
            else{
                query = "update " + tabla + " set " + campos.get(i) + " = ? where ? = ?";
            }
            pst = con.prepareStatement(query);
            System.out.println("dime el nuevo valor para la columna " + campos.get(i));
            auxValor = lector.nextLine();
            /*indico el nuevo valor a machacar. He puesto un String, pero en el
            caso de que se haya indicado una columna con tipo de dato numerico, fallará*/
            pst.setString(1, auxValor);
            if (condicion == true){
                System.out.println("Dime el nombre del campo que tiene la condicion:");
                auxCampo = lector.nextLine();
                pst.setString(2, auxCampo);
                System.out.println("Dime el dato que debemos contemplar para que se ejecute el udpate: " + auxCampo + " = ?");
                auxCondicion = lector.nextLine();
                pst.setString(3, auxCondicion);
            }
            
            filasAfectadas += pst.executeUpdate();
            System.out.println("Se han aplicado los updates en " + filasAfectadas + "filas");
        }
    }
    
    public static boolean obtenerCondicion(){
        Scanner lector = new Scanner(System.in);
        boolean query;
        int condicion;
        System.out.println("¿El cambio debe tener una condicion o debe aplicar a todas las filas?. Selecciona una opcion:"
        + "\n 1. Aplicar el cambio a todas las filas." + "\n 2. Aplicar el cambio con una condición.");
        condicion = lector.nextInt();
        if (condicion == 1){
            query = false;
        }
        else{
            query = true;
        }
        return query;
    }
    
    public static ArrayList <String> pedirCampos(int cambios){
        Scanner lector = new Scanner(System.in);
        String auxCampos;
        ArrayList <String> campos = new ArrayList <>();
        for (int i = 0; i<cambios;i++){
            System.out.println("Dime el nombre del campo " + (i+1) + " que vas a modificar:");
            auxCampos = lector.nextLine();
            campos.add(auxCampos);
        }
        return campos;
    }
    
    public static String registrarFecha() throws IOException{
        //fuente:http://lineadecodigo.com/java/obtener-la-hora-en-java/
        Calendar calendario = Calendar.getInstance();
        String fecha = calendario.get(Calendar.DATE)+"/"+
        (calendario.get(Calendar.MONTH) + 1)+"/"+calendario.get(Calendar.YEAR);
            
        return fecha;
    }
    
    public static String registrarHora(){
        Calendar calendario = Calendar.getInstance();
        String hora = calendario.get(Calendar.HOUR_OF_DAY) + ":" +
        calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND);
        
        return hora;
    }
}
