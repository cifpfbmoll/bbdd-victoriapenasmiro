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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author victoriapenas
 * //reiniciar servicio mysql si se me queda colgado: brew services restart mysql
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
    
    public static Connection obtenerConexion() throws SQLException {
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
            System.out.println("3. Transaciones.");
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
                    menuTransacciones();
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
    
    /*pruebas SQLInjection
    Se ha intentado realizar un SQL Injection, concretamente un insert pero no ha funcionado.
    El usuario nos da una String, por lo que en la sentencia está contruida con una comilla simple para cerrar,
    lo cual ya hace de bloqueo para que no se pueda hacer el insert. En cambio, si fuera un integer y
    no hubieramos puesto la comilla simple si que hubiera funcionado.
    
    Por lo tanto, como el parámetro que espera es un string, podemos hackear la BBDD poniendo algún parámetro
    que se complete con la comilla simple que está preparada en el Statement. El ejemplo que he realizado ha sido
    introducir por teclado lo siguiente:
    
    amstel' or 't=t
    
    De esta forma he obtenido todos los resultados de la tabla serves.
    */
    
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
        String query = "";//variable donde guardare la query
        String auxValor = ""; //auxiliar para recoger los valores a modificar
        String auxCondicion = ""; //aux para recoger la condicion a tener en cuenta en la query
        ArrayList <String> campos; //en esta lista guardaré los campos que quiere modificar el usuario
        int filasAfectadas = 0; //contador para recoger el num de updates realizados
        boolean condicion = obtenerCondicion();//pregunto al usuario si el update será con una condicion
        String tabla = pedirNombreTabla();
        //preparo la sentencia **importante, una vez asignada la query al PreparedStatement no puedo modificarla más adelante**
        PreparedStatement pst;
        int updates = pedirNumUpdates(tabla);
        campos = pedirCampos(updates);        
        for (int i = 0; i<campos.size();i++){
            //en funcion de la condicion tendré una query u otra
            query = recuperarQuery(condicion, tabla, campos.get(i), i+1);
            //ya tengo la estructura de la query, pues preparo la sentencia
            pst = con.prepareStatement(query);
            System.out.println("dime el nuevo valor para la columna " + campos.get(i));
            auxValor = lector.nextLine();
            /*indico el nuevo valor a machacar. He puesto un String, pero en el
            caso de que se haya indicado una columna con tipo de dato numerico, fallará*/
            pst.setString(1, auxValor);
            //en el caso de que tenga una condicion, le asigno el parámetro 2
            if (condicion){
                System.out.println("Cuál es el valor de la condición?");
                auxCondicion = lector.nextLine();
                pst.setString(2, auxCondicion);
            }
            
            System.out.println("La sql que lanza es esta:" + query);//esto me ayuda a comprobar que la query está bien contruida
            filasAfectadas = pst.executeUpdate(); //esto informa de los cambios realizados
            System.out.println("Se han aplicado los updates en " + filasAfectadas + " filas");
            if (pst != null) pst.close (); //cierro el recurso PreparedStatement, lo cierro aqui porqué lo he instanciado dentro del for
        }
        
        if (con != null) con.close (); //cierra el objeto Connection llamado con
    }
    
    public static int pedirNumUpdates(String tabla){
        Scanner lector = new Scanner(System.in);
        System.out.println("¿Cuántos cambios en la tabla " + tabla + " quieres realizar?");
        int updates = lector.nextInt();
        return updates;
    }
    
    public static String pedirNombreTabla(){
        Scanner lector = new Scanner(System.in);
        System.out.println("¿Qué tabla quieres modificar?");
        System.out.println("Las existentes son: \nbar\nbeer\ndrinker\nfrequents\nlikes\nserves");
        String tabla = lector.nextLine();
        return tabla;
    }
    
    public static String recuperarQuery(boolean condicion, String tabla, String campo, int numCambio){
        Scanner lector = new Scanner(System.in);
        String auxCampo = ""; //aux para recoger el nombre del campo de la condicion (where)
        String query;
        if (condicion == false){
            query = "update " + tabla + " set " + campo + " = ?";
            }
        else{
            System.out.println("Cambio " + numCambio + " : Dime el nombre del campo que tiene la condicion:");
            auxCampo = lector.nextLine();
            query = "update " + tabla + " set " + campo + " = ? where " + auxCampo + "= ?";
        }
        return query;        
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
            System.out.println("Dime el nombre del campo " + (i+1) + " que vas a modificar para el update num " + i + ":");
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
    
    public static void menuTransacciones() throws SQLException{
        Scanner lector = new Scanner(System.in);
        int opcion;
        boolean salir = false;
        do{
            System.out.println("---------------------- M E N U ----------------------");
            System.out.println("1. Actualización simple.");
            System.out.println("2. Transaccion_1.");
            System.out.println("3. Transaccion_2.");
            System.out.println("4. Salir.");
            System.out.println("Dime una opcion: ");
            opcion = lector.nextInt();
            switch(opcion){
                case 1:
                    actualizacionSimple();
                    break;
                case 2:
                    transaccion1();
                    break;
                case 3:
                    transaccion2();
                    break;
                case 4:
                    salir = true;
                    break;
                default:
                    System.out.println("La opcion indicada no existe, indica otra.");
            }
        }while(salir == false);     
    }
    
    /**
     * Incluye en esta opción dos sentencias update de una de las tablas, en las
     * que se le pida al usuario qué campo quiere actualizar de dicha tabla y el valor del mismo. 
    */
    /*PRUEBAS EJERCICIO 2.a - funcion actualizacionSimple()
    PRIMERA QUERY: Realizo el siguiente update: update beer set brewer = 'vicky 3' -> FUNCIONA TODO OK
    SEGUNDA QUERY: update beer set producto = 'hola' -> La columna producto no existe, por lo tanto esta query falla.
    
    ¿Se actualiza la tabla si falla la primera sentencia? Si la primera query falla, no se ejecuta el update
    
    ¿Y si falla la segunda se actualiza la primera?
    Si la segunda prueba falla, el primer update que ha funcionado correctamente ha quedado grabado,
    mientras que el segundo que se ha lanzado no se ha ejecutado.
    */
    public static void actualizacionSimple() throws SQLException{
        Scanner lector = new Scanner(System.in);
        Connection con = obtenerConexion();
        String query = "";//variable donde guardare la query
        String auxValor = "";//aux donde almacenaremos el nuevo valor de la columna donde se realizará el update
        String auxCondicion = "";//aux donde almacenaremos el valor del where en el caso de que la query tengo condicion
        PreparedStatement pst;
        int filasAfectadas = 0; //contador para recoger el num de updates realizados
        System.out.println("Vamos a preparar dos updates sobre una tabla de la db daw.");
        boolean condicion = obtenerCondicion();//pregunto al usuario si el update será con una condicion
        String tabla = pedirNombreTabla();
        /*aqui no utilizo el método pedirCampos porqué me devuelve una arrayList
        y como el cambio solo será de una columna, de este modo es más simple*/
        String campo = "";
        for (int i = 0; i<2; i++){//preparamos dos queries
            System.out.print("Update " + (i+1) + ". ");
            System.out.println("Dime el nombre de la columna de la tabla " + tabla + " que quieres modificar");
            campo = lector.nextLine();
            query = recuperarQuery(condicion,tabla,campo,1);
            pst = con.prepareStatement(query);
            System.out.println("Dime el nuevo valor para la columna " + campo);
            auxValor = lector.nextLine();
            /*indico el nuevo valor a machacar. He puesto un String, pero en el
            caso de que se haya indicado una columna con tipo de dato numerico, fallará*/
            pst.setString(1, auxValor);
            if (condicion){
                System.out.println("Qué valor debe tener el campo de la condicion");
                auxCondicion = lector.nextLine();
                pst.setString(2, auxCondicion);
            }
            System.out.println("La sql que se va a lanzar es:" + pst.toString());//imprimo la query
            filasAfectadas = pst.executeUpdate(); //esto informa de los cambios realizados
            System.out.println("Se ha aplicado el update en " + filasAfectadas + " filas.");
            pst.close();//cierro los recursos
        }
        con.close();//cierro los recursos
    }
    /*EJERCICIO 2.b - Incluye una transacción que se compone de tres sentencias de actualización
    sobre una tabla, aunque habrá una cuarta sentencia de actualización que no
    forma parte de la transacción.
    PREGUNTAS:
    
    1. ¿Se actualiza la tabla si falla la primera, segunda o tercera sentencia?
    RESPUESTA: No se actualiza, las pruebas que he realizado son:
        1.1 Sentencia 1. Poner una cadena de más de 20 caracteres en la primera sentencia, como el
    tipo de dato en la tabla es un varchar(20) ha entrado en la exception y se ha ejecutado el rollback.
        2.1 Sentencia 2. Poner un double que supere el tipo de dato decimal (5,2). No ha guardado el update
    de la sentencia 1, ha hecho rollback de todo.
    2. ¿Y si se ejecuta correctamente las tres primeras sentencias que forman
    parte de la transacción y falla la última qué ocurre?
    RESPUESTA: No se actualiza, la prueba que hecho es desconectar el programa antes de ejecutar la ultima query
    3. ¿Qué ocurre si dejas el autocommit a false y ejecutas el apartado b y luego el a?
    En mi caso en particular, si dejo el autocommit a false, al ejecutar el apartado abro una nueva conexion a la BBDD,
    por lo tanto, se reinicia la conexion y el autocommit está a true, de forma que no tiene impacto.
    
    Si se reutilizase la misma conexión a la base de datos, como en el apartado a, no hago un con.commit() no se grabarían los cambios.
    
    */
    public static void transaccion1() throws SQLException{
        int filas;
        PreparedStatement pst = null;
        Connection con = obtenerConexion();
        try {
            con.setAutoCommit(false);
            System.out.println("Actualización campo name de la tabla beer");
            pst = crearPst(con,pst,"beer","name","name");
            filas = pst.executeUpdate();
            System.out.println("Se ha aplicado el update en " + filas + " filas.");
            
            System.out.println("Actualización campo price tabla serves");
            pst = crearPst(con,pst,"serves","price","beer");
            filas = pst.executeUpdate();
            System.out.println("Se ha aplicado el update en " + filas + " filas.");
            
            System.out.println("Actualización tabla frequents");
            pst = crearPst(con,pst,"frequents","times_a_week","drinker");
            filas = pst.executeUpdate();
            System.out.println("Se ha aplicado el update en " + filas + " filas.");
            
            con.commit();

        } catch (SQLException ex) {
            con.rollback();
            System.out.println("SQLSTATE " + ex.getSQLState() + "SQLMESSAGE" + ex.getMessage());
            System.out.println("Hago rollback");

        } finally{
            con.setAutoCommit(true);
            pst.close();
            con.close();
        }
    }
    
    /**
     * Creamos un objeto de tipo PreparedStatement listo para lanzar executeUpdate
     * @return devolvemos el PreparedStatement que hemos creado.
     */
    public static PreparedStatement crearPst(Connection con, PreparedStatement pst, String tabla, String columna, String campoCondicion) throws SQLException{
        Scanner lector = new Scanner(System.in);
        String auxCadena;
        int auxEntero;
        double auxDecimal;
        String tipoColumna;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        String query = "update " + tabla + " set " + columna + " = ? where " + campoCondicion + " =?";
        pst = con.prepareStatement(query);
        
        //dos porque tengo dos parametros ?
        for (int i = 0; i<2 ; i++){
            if (i == 0){
                System.out.println("Dime el nuevo valor del campo " + columna);
                rs = pst.executeQuery("select " + columna + " from " + tabla);
            }
            else{
                System.out.println("Dime el valor actual del campo " + campoCondicion + " para que se ejecute el update");
                rs = pst.executeQuery("select " + campoCondicion + " from " + tabla);
            }
            //obtengo el tipo de dato de la columna antes de setearla
            rsmd = rs.getMetaData();
            tipoColumna = rsmd.getColumnTypeName(1);
            System.out.println(rsmd.getColumnTypeName(1));
            //en funcion del tipo de dato necesito un seteo u otro
            if (tipoColumna == "VARCHAR"){
                auxCadena = lector.nextLine();
                pst.setString(i+1, auxCadena);
            } else if (tipoColumna != "DECIMAL"){
                auxEntero = Integer.parseInt(lector.nextLine());
                pst.setInt(i+1, auxEntero);
            }else{
                auxDecimal = Double.parseDouble(lector.nextLine());
                pst.setDouble(i+1, auxDecimal);
            }
        }
        
        return pst;
    }
    
    /*EJERCICIO 2.c: Transacción_2.
    Replica el apartado anterior en un nuevo método, pero incluyendo un savepoint a
    partir de la segunda sentencia.
    ¿Qué ocurre si falla la segunda sentencia?
    RESPUESTA: Se ha hecho el commit del primer update 
    ¿Y si falla la tercera?
    REPSUESTA: Idem que en el caso anterior, se hace el commit unicamente del primer update,
    aunque la segunda sentencia haya ido bien, el rollback retrocede todo lo que se ha ejecutado
    hasta el punto1.
    */
    public static void transaccion2() throws SQLException{
        int filas;
        PreparedStatement pst = null;
        Connection con = obtenerConexion();
        Savepoint punto1 = null;
        try {
            con.setAutoCommit(false);
            //1 SENTENCIA SQL
            System.out.println("Actualización campo name de la tabla beer");
            pst = crearPst(con,pst,"beer","name","name");
            filas = pst.executeUpdate();
            System.out.println("Se ha aplicado el update en " + filas + " filas.");
            punto1 = con.setSavepoint();
            //2 SENTENCIA SQL
            System.out.println("Actualización campo price tabla serves");
            pst = crearPst(con,pst,"serves","price","beer");
            filas = pst.executeUpdate();
            System.out.println("Se ha aplicado el update en " + filas + " filas.");
            //3 SENTENCIA SQL
            System.out.println("Actualización tabla frequents");
            pst = crearPst(con,pst,"frequents","times_a_week","drinker");
            filas = pst.executeUpdate();
            System.out.println("Se ha aplicado el update en " + filas + " filas.");
            
            con.commit();

        } catch (SQLException ex) {
            con.rollback(punto1);
            con.commit();
            System.out.println("SQLSTATE " + ex.getSQLState() + "SQLMESSAGE" + ex.getMessage());
            System.out.println("Hago rollback");

        } finally{
            con.setAutoCommit(true);
            pst.close();
            con.close();
        }
    }
}
