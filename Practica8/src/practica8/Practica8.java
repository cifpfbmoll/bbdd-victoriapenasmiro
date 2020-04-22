/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            writer.write("CERVEZA: " + beer);
            while (rs.next ()) {
                bar = rs.getString("bar");//recupero el valor de la columna bar, que es un varchar
                beer = rs.getString(2);//recuero el valor de la columna 2, que es un varchar
                price = rs.getFloat(3);
                writer.write("BAR: " + bar + " : " + price + "\n");
            }
        }finally{
            //Si rs es distinto de null es que se le pudo asignar un recurso
            if (rs != null) rs.close (); //cierra el objeto ResultSet llamado rs.
            if (st != null) st.close ();//cierra el objeto Statement llamado st
            if (con != null) con.close (); //cierra el objeto Connection llamado con
        }        
    }
    
    public static void ejecutarQuerySegura(){
        Scanner lector = new Scanner(System.in);
        System.out.println("Dime una marca de cerveza y te daré su precio en los bares donde está disponible");
        System.out.println("Las opciones son: \n Amstel\n Budweiser\n Corona\n Dixie\n Erdinger\n Full Sail \n");
        String cerveza = lector.nextLine();
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
