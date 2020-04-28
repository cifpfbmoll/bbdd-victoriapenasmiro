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

#PRUEBAS EJERCICIO 2.a
    **PRIMERA QUERY**: Realizo el siguiente update: update beer set brewer = 'vicky 3' -> FUNCIONA TODO OK
    **SEGUNDA QUERY**: update beer set producto = 'hola' -> La columna producto no existe, por lo tanto esta query falla.
    
    ¿Se actualiza la tabla si falla la primera sentencia? Si la primera query falla, no se ejecuta el update
    
    ¿Y si falla la segunda se actualiza la primera?
    Si la segunda prueba falla, el primer update que ha funcionado correctamente ha quedado grabado,
    mientras que el segundo que se ha lanzado no se ha ejecutado.
