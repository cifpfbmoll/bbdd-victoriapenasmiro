#PRUEBAS SQLInjection
Se ha intentado realizar un SQL Injection a través del metodo _ejecutarQueryPeligrosa()_, concretamente un insert pero no ha funcionado.
El usuario nos da una String, por lo que en la sentencia está contruida con una comilla simple para cerrar,
lo cual ya hace de bloqueo para que no se pueda hacer el insert. En cambio, si fuera un integer y
no hubieramos puesto la comilla simple si que hubiera funcionado.
    
Por lo tanto, como el parámetro que espera es un string, podemos hackear la BBDD poniendo algún parámetro
que se complete con la comilla simple que está preparada en el Statement. El ejemplo que he realizado ha sido
introducir por teclado lo siguiente:
    
**amstel' or 't=t**
    
De esta forma he obtenido todos los resultados de la tabla serves.
