-- =============================================================================
-- LevelUp Code - Ampliación pedagógica de las lecciones existentes
-- Conserva IDs, módulos, progreso y recompensas. Solo mejora el CLOB contenido.
-- =============================================================================

MERGE INTO Lecciones dst
USING (
    SELECT id_leccion, nuevo_contenido
    FROM (
        SELECT l.id_leccion,
               CASE
                   WHEN l.titulo LIKE 'Herencia, Clases Abstractas%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Comprender cómo una clase abstracta define un contrato común y cómo el despacho dinámico permite que Java elija en tiempo de ejecución la implementación correcta.

2. CONCEPTOS CLAVE
La herencia expresa una relación «es un». Una clase abstracta puede contener estado, métodos implementados y métodos abstractos. Una subclase usa extends y debe implementar los métodos pendientes. Cuando una referencia del tipo base apunta a una subclase, la JVM ejecuta la versión sobrescrita del objeto real.

3. PROCEDIMIENTO
Primero identifica el comportamiento común. Después declara una clase abstracta y un método abstracto. Crea subclases con @Override. Finalmente guarda los objetos en una colección del tipo base y llama al mismo método para observar el polimorfismo.

4. EJEMPLO RAZONADO
Si Animal define sonido() y Perro lo sobrescribe, Animal mascota = new Perro(); ejecuta el sonido de Perro. El tipo de la referencia controla qué operaciones son visibles y el objeto real determina qué implementación se ejecuta.

5. ERRORES FRECUENTES
No confundas sobrecarga con sobrescritura. Evita heredar solo para reutilizar código. Usa @Override para detectar firmas incorrectas y recuerda que una clase abstracta no se instancia directamente.

6. COMPRUEBA TU APRENDIZAJE
Diseña una clase abstracta Figura con area(), crea Circulo y Rectangulo, y explica qué método se ejecuta al recorrer una List<Figura>.~')

                   WHEN l.titulo LIKE 'Protocolos Abstractos%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Distinguir entre contratos formales con abc.ABC y polimorfismo por comportamiento mediante duck typing en Python.

2. CONCEPTOS CLAVE
Una clase que hereda de ABC puede marcar operaciones obligatorias con @abstractmethod. Python también permite trabajar con cualquier objeto que ofrezca los métodos esperados, aunque no comparta una clase base. Este enfoque se resume en «si se comporta como el protocolo esperado, puede usarse».

3. PROCEDIMIENTO
Importa ABC y abstractmethod. Declara una clase base, marca el método requerido y crea implementaciones concretas. Luego escribe una función que invoque ese método sin preguntar por el tipo exacto. La prueba debe incluir dos objetos diferentes con el mismo comportamiento.

4. EJEMPLO RAZONADO
Una función procesar(objeto) que llama objeto.ejecutar() funciona con cualquier implementación válida. ABC ayuda a detectar clases incompletas al instanciarlas y duck typing mantiene el código flexible.

5. ERRORES FRECUENTES
No uses isinstance para cada implementación. No olvides self ni el decorador @abstractmethod. Evita bases abstractas cuando un protocolo pequeño y claro sea suficiente.

6. COMPRUEBA TU APRENDIZAJE
Crea un contrato Exportador con exportar(datos) e implementaciones JSON y CSV. Explica qué error aparece si una subclase no implementa el método.~')

                   WHEN l.titulo LIKE 'Funciones Virtuales Puras%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Entender cómo C++ implementa el polimorfismo dinámico con funciones virtuales, clases abstractas y la tabla virtual.

2. CONCEPTOS CLAVE
Una función virtual pura se declara con = 0 y convierte la clase en abstracta. Una llamada mediante referencia o puntero a la clase base se resuelve según el tipo real del objeto. La vtable es el mecanismo habitual usado por el compilador para localizar la implementación correcta.

3. PROCEDIMIENTO
Declara una interfaz base con un destructor virtual. Añade una función virtual pura. Implementa la función con override en cada clase derivada. Usa referencias o punteros inteligentes a la base para evitar slicing y conservar el comportamiento dinámico.

4. EJEMPLO RAZONADO
Si Figura& referencia un Circulo, llamar area() ejecuta Circulo::area(). Si el objeto se copia por valor como Figura, se pierde la parte derivada. Por eso las colecciones polimórficas suelen almacenar std::unique_ptr<Figura>.

5. ERRORES FRECUENTES
No omitas el destructor virtual en una base polimórfica. Evita punteros propietarios sin RAII. Usa override para detectar diferencias de firma y no confundas enlace estático con dinámico.

6. COMPRUEBA TU APRENDIZAJE
Modela una interfaz Notificable con enviar(), implementa Correo y SMS, y explica por qué vector<unique_ptr<Notificable>> conserva el polimorfismo.~')

                   WHEN l.titulo LIKE 'Búsqueda Binaria%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Aplicar búsqueda binaria correctamente, justificar su complejidad O(log n) y reconocer la precondición de ordenamiento.

2. IDEA CENTRAL
En cada iteración se compara el objetivo con el elemento central. Si son iguales termina. Si el objetivo es menor se descarta la mitad derecha y, si es mayor, se descarta la mitad izquierda. Reducir el espacio a la mitad produce crecimiento logarítmico.

3. INVARIANTE Y PASOS
El objetivo, si existe, siempre permanece dentro del intervalo [izquierda, derecha]. Calcula el punto medio sin desbordamiento, compara, actualiza un límite y repite mientras el intervalo sea válido. Define antes si devolverás índice, booleano o posición de inserción.

4. CASOS LÍMITE
Prueba una colección vacía, un solo elemento, objetivo en los extremos, objetivo ausente y valores repetidos. Para encontrar la primera aparición, guarda el resultado y continúa hacia la izquierda.

5. IMPLEMENTACIÓN EN ESTE LENGUAJE
~') || CASE UPPER(lg.nombre)
                            WHEN 'JAVA' THEN TO_CLOB(q'~En Java usa int medio = izquierda + (derecha - izquierda) / 2. Con Arrays.binarySearch revisa su contrato, porque un resultado negativo codifica el punto de inserción.~')
                            WHEN 'PYTHON' THEN TO_CLOB(q'~En Python usa medio = (izquierda + derecha) // 2. El módulo bisect ofrece posiciones de inserción y ayuda a manejar repetidos sin reimplementar el algoritmo.~')
                            ELSE TO_CLOB(q'~En C++ usa std::midpoint cuando esté disponible o izquierda + (derecha - izquierda) / 2. lower_bound devuelve el primer elemento no menor que el objetivo.~')
                        END || TO_CLOB(q'~

6. COMPRUEBA TU APRENDIZAJE
Traza manualmente la búsqueda de 23 en [3, 8, 12, 17, 23, 31, 40] y anota los límites y el punto medio de cada iteración.~')

                   WHEN l.titulo LIKE 'Gestión de Memoria y Colecciones%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Comprender cómo ArrayList administra una colección dinámica y por qué agregar elementos suele ser O(1) amortizado.

2. MODELO MENTAL
ArrayList guarda referencias en un arreglo interno. Cuando se agota la capacidad crea un arreglo mayor y copia los elementos. Esa copia ocasional cuesta O(n), pero repartida entre muchas inserciones produce costo amortizado constante.

3. OPERACIONES IMPORTANTES
get y set son O(1). Agregar al final suele ser O(1) amortizado. Insertar o eliminar al inicio requiere desplazar elementos y cuesta O(n). contains también es O(n), salvo que se use una estructura como HashSet para búsquedas frecuentes.

4. USO SEGURO
Programa contra List cuando no dependas de detalles concretos. Usa generics para conservar seguridad de tipos. Recorre con iteradores cuando necesites eliminar durante la iteración y define una capacidad inicial si conoces el tamaño aproximado.

5. ERRORES FRECUENTES
No confundas size con capacidad. Evita eliminar elementos dentro de un for-each. No uses ArrayList para inserciones constantes al principio sin evaluar otra estructura.

6. COMPRUEBA TU APRENDIZAJE
Compara el costo de agregar diez mil elementos al final, al inicio y de buscar duplicados con ArrayList frente a HashSet.~')

                   WHEN l.titulo LIKE 'Listas Dinámicas y Conteo%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Entender el crecimiento dinámico de list y la relación entre referencias, mutabilidad y ciclo de vida de objetos en Python.

2. MODELO MENTAL
Una lista almacena referencias a objetos y reserva capacidad adicional para evitar realocar en cada append. Dos variables pueden apuntar a la misma lista, por lo que una modificación realizada mediante una referencia será visible desde la otra.

3. OPERACIONES Y COSTOS
append y pop al final suelen ser O(1) amortizado. Acceder por índice es O(1). Insertar o eliminar al inicio cuesta O(n). Buscar con in recorre la lista y cuesta O(n). Para pertenencia frecuente considera set.

4. COPIAS Y MUTABILIDAD
asignar b = a no copia. a.copy() o a[:] crean una copia superficial. Los objetos internos mutables siguen compartidos. Para estructuras anidadas independientes utiliza copy.deepcopy solo cuando sea realmente necesario.

5. ERRORES FRECUENTES
Evita modificar una lista mientras la recorres. No uses una lista mutable como valor por defecto de una función. Distingue identidad con is de igualdad con ==.

6. COMPRUEBA TU APRENDIZAJE
Predice el resultado de dos variables que comparten una lista, después repite usando una copia superficial y explica la diferencia.~')

                   WHEN l.titulo LIKE 'Determinismo RAII%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Aplicar RAII para administrar memoria y otros recursos de forma determinista en C++.

2. IDEA CENTRAL
RAII vincula la adquisición de un recurso al constructor y su liberación al destructor. Al salir del ámbito, incluso por una excepción, los objetos locales se destruyen automáticamente. Esto permite manejar memoria, archivos, bloqueos y conexiones sin llamadas manuales dispersas.

3. HERRAMIENTAS
std::vector administra almacenamiento contiguo y capacidad dinámica. std::unique_ptr expresa propiedad exclusiva. std::shared_ptr se reserva para propiedad realmente compartida y usa conteo de referencias. Las referencias y punteros no propietarios no deben prolongar la vida del objeto.

4. REGLA DE CERO
Prefiere clases formadas por tipos que ya administran sus recursos. Así no necesitas escribir destructor, constructor de copia ni operadores especiales. Si una clase posee un recurso manual, estudia la regla de cinco.

5. ERRORES FRECUENTES
Evita new y delete explícitos en código de aplicación. No construyas dos smart pointers desde el mismo puntero crudo. No uses shared_ptr como solución predeterminada.

6. COMPRUEBA TU APRENDIZAJE
Transforma un arreglo creado con new en std::vector y reemplaza un objeto propietario por std::unique_ptr. Explica cuándo se libera cada recurso.~')

                   WHEN l.titulo LIKE 'Recursividad, Casos Base%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Diseñar funciones recursivas que terminen, entender la pila de llamadas y decidir cuándo conviene una solución iterativa.

2. TRES PARTES OBLIGATORIAS
Toda recursión necesita un caso base que detenga el proceso, un paso recursivo que resuelva una versión más pequeña y una medida de progreso que se acerque al caso base. Si falta cualquiera de estas partes puede aparecer recursión infinita.

3. PILA DE LLAMADAS
Cada llamada conserva parámetros, variables locales y el punto de retorno. Al llegar al caso base, las llamadas se resuelven en orden inverso. La profundidad consume memoria O(n), aunque el trabajo total dependa del número de llamadas.

4. MÉTODO DE DISEÑO
Define el problema para una entrada pequeña. Escribe primero el caso base. Supón que la función resuelve correctamente el subproblema y combina ese resultado. Finalmente prueba entradas mínimas, normales e inválidas.

5. EJEMPLO Y RIESGOS EN ESTE LENGUAJE
~') || CASE UPPER(lg.nombre)
                            WHEN 'JAVA' THEN TO_CLOB(q'~factorial(4) crea cuatro marcos antes de regresar. Java no garantiza optimización de recursión de cola, así que profundidades grandes pueden producir StackOverflowError.~')
                            WHEN 'PYTHON' THEN TO_CLOB(q'~factorial(4) crea cuatro marcos antes de regresar. Python limita la profundidad recursiva y no optimiza llamadas de cola, por lo que muchos problemas lineales conviene resolverlos con ciclos.~')
                            ELSE TO_CLOB(q'~factorial(4) crea cuatro marcos antes de regresar. C++ no garantiza eliminar la recursión de cola y una profundidad excesiva puede agotar la pila.~')
                        END || TO_CLOB(q'~

6. COMPRUEBA TU APRENDIZAJE
Dibuja la pila para suma(4) = 4 + suma(3), identifica el caso base y escribe una versión iterativa equivalente.~')

                   WHEN l.titulo LIKE 'Merge Sort%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Comprender merge sort como algoritmo de división y conquista, implementar la mezcla ordenada y analizar su complejidad.

2. DIVIDE, RESUELVE Y COMBINA
Divide la colección en dos mitades hasta obtener segmentos de un elemento. Ordena recursivamente cada mitad. Después mezcla ambas recorriéndolas con dos índices y copiando siempre el menor elemento disponible.

3. COMPLEJIDAD
Hay log n niveles de división y en cada nivel se procesan n elementos durante las mezclas. Por eso el tiempo es O(n log n) en el mejor, promedio y peor caso. La implementación habitual requiere O(n) memoria auxiliar.

4. ESTABILIDAD
Si durante un empate se toma primero el elemento de la mitad izquierda, el algoritmo conserva el orden relativo de elementos equivalentes. Esta propiedad es útil al ordenar registros por varios criterios.

5. ERRORES FRECUENTES
Revisa cuidadosamente los límites de cada mitad. No olvides copiar los elementos restantes. Evita crear demasiadas colecciones temporales cuando puedas reutilizar un búfer auxiliar.

6. COMPRUEBA TU APRENDIZAJE
Separa [8, 3, 6, 2, 7, 1] hasta casos unitarios y escribe el resultado de cada mezcla. Compara el número de niveles con log2(n).~')

                   WHEN l.titulo LIKE 'Pilas (LIFO)%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Usar una pila para resolver problemas donde el último elemento agregado debe procesarse primero.

2. MODELO LIFO
Las operaciones esenciales son push, pop y peek o top. Todas deben ser O(1). La pila aparece en deshacer acciones, evaluación de expresiones, navegación, llamadas a funciones y recorridos en profundidad.

3. VALIDACIÓN DE EXPRESIONES
Recorre los caracteres. Al encontrar un símbolo de apertura, apílalo. Al encontrar uno de cierre, la pila no puede estar vacía y el elemento superior debe ser su pareja. Al terminar, la expresión es válida únicamente si la pila quedó vacía.

4. IMPLEMENTACIÓN EN ESTE LENGUAJE
~') || CASE UPPER(lg.nombre)
                            WHEN 'JAVA' THEN TO_CLOB(q'~Prefiere Deque<T> con ArrayDeque en lugar de la clase heredada Stack. Usa push, pop y peek y verifica isEmpty antes de extraer.~')
                            WHEN 'PYTHON' THEN TO_CLOB(q'~Una list funciona bien con append y pop al final. Evita pop(0), porque desplaza todos los elementos.~')
                            ELSE TO_CLOB(q'~Usa std::stack<T>, que expone push, pop, top y empty. Recuerda que pop no devuelve el elemento, por lo que debes leer top primero.~')
                        END || TO_CLOB(q'~

5. ERRORES FRECUENTES
No extraigas de una pila vacía. No aceptes cierres que no coincidan con la apertura superior. Incluye pruebas con texto vacío, anidamiento y orden incorrecto.

6. COMPRUEBA TU APRENDIZAJE
Traza la pila para {[()]} y para {[(])}. Explica en qué carácter se detecta el segundo error.~')

                   WHEN l.titulo LIKE 'Colas (FIFO)%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Modelar procesos por orden de llegada y distinguir una cola FIFO de una cola de prioridad.

2. MODELO FIFO
enqueue agrega al final y dequeue retira del frente. Ambas operaciones deben ser O(1). Una cola representa turnos, tareas pendientes, mensajes y recorridos BFS. Una cola de prioridad, en cambio, atiende primero el elemento con mayor o menor prioridad.

3. SIMULACIÓN DE TURNOS
Define qué información guarda cada solicitud. Encola las llegadas, procesa desde el frente y registra tiempos de espera. Si existen prioridades, especifica cómo resolver empates para mantener un comportamiento predecible.

4. IMPLEMENTACIÓN EN ESTE LENGUAJE
~') || CASE UPPER(lg.nombre)
                            WHEN 'JAVA' THEN TO_CLOB(q'~Usa Queue<T> con ArrayDeque para FIFO y PriorityQueue para prioridades. offer, poll y peek expresan claramente las operaciones de cola.~')
                            WHEN 'PYTHON' THEN TO_CLOB(q'~Usa collections.deque con append y popleft para FIFO. Para prioridades utiliza heapq con tuplas de prioridad y orden de llegada.~')
                            ELSE TO_CLOB(q'~Usa std::queue<T> para FIFO y std::priority_queue<T> para prioridades. Define un comparador cuando el orden natural no represente la regla del negocio.~')
                        END || TO_CLOB(q'~

5. ERRORES FRECUENTES
No implementes una cola eliminando el primer elemento de un arreglo dinámico. Verifica la cola vacía y documenta si un número menor significa mayor prioridad.

6. COMPRUEBA TU APRENDIZAJE
Simula la llegada de Ana, Luis y Marta, procesa dos turnos y agrega a Pablo. Escribe el estado de la cola después de cada operación.~')

                   WHEN l.titulo LIKE 'Árboles Binarios%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Representar árboles binarios y diferenciar los recorridos preorden, inorden y postorden.

2. ESTRUCTURA
Cada nodo contiene un valor y referencias a un hijo izquierdo y uno derecho. La raíz no tiene padre, las hojas no tienen hijos y la altura mide la ruta más larga hacia una hoja. Un árbol binario de búsqueda mantiene valores menores a la izquierda y mayores a la derecha según su regla de comparación.

3. RECORRIDOS
Preorden visita nodo, izquierda y derecha. Inorden visita izquierda, nodo y derecha, y en un BST produce valores ordenados. Postorden visita izquierda, derecha y nodo, útil para liberar o calcular resultados desde las hojas.

4. COMPLEJIDAD
Recorrer todos los nodos cuesta O(n). Buscar en un BST balanceado cuesta O(log n), pero en un árbol degenerado puede costar O(n). La forma del árbol es tan importante como la cantidad de datos.

5. ERRORES FRECUENTES
Define claramente cómo manejar duplicados. Comprueba nodo nulo antes de acceder a sus hijos. No confundas árbol binario con árbol binario de búsqueda.

6. COMPRUEBA TU APRENDIZAJE
Dibuja un árbol con raíz 8 e inserta 3, 10, 1, 6 y 14. Escribe sus tres recorridos y calcula la altura.~')

                   WHEN l.titulo LIKE 'Grafos y Recorridos%' THEN TO_CLOB(q'~1. OBJETIVO DE APRENDIZAJE
Modelar relaciones mediante grafos y elegir correctamente entre los recorridos BFS y DFS.

2. MODELO DE GRAFO
Un grafo contiene vértices y aristas. Puede ser dirigido o no dirigido, ponderado o no ponderado. La lista de adyacencia suele ser eficiente para grafos dispersos, mientras que una matriz facilita consultar conexiones a costa de O(V²) memoria.

3. BFS
BFS usa una cola y explora por niveles. En un grafo no ponderado encuentra caminos con el menor número de aristas. Marca cada vértice al encolarlo para evitar duplicados.

4. DFS
DFS usa recursión o una pila explícita y avanza en profundidad antes de retroceder. Es útil para componentes conexos, detección de ciclos, orden topológico y exploración de posibilidades.

5. COMPLEJIDAD Y ERRORES
Con lista de adyacencia, ambos recorridos cuestan O(V + E). Mantén un conjunto de visitados para evitar ciclos infinitos. No uses BFS simple cuando las aristas tienen pesos diferentes; evalúa Dijkstra.

6. COMPRUEBA TU APRENDIZAJE
Construye un grafo A-B, A-C, B-D, C-E y D-E. Escribe un orden BFS y uno DFS válidos desde A e indica la estructura auxiliar usada.~')
               END AS nuevo_contenido
        FROM Lecciones l
        JOIN Modulos m ON m.id_modulo = l.id_modulo
        JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje
    ) clasificado
    WHERE nuevo_contenido IS NOT NULL
) src
ON (dst.id_leccion = src.id_leccion)
WHEN MATCHED THEN UPDATE SET dst.contenido = src.nuevo_contenido;

COMMIT;

SELECT COUNT(*) AS lecciones_ampliadas
FROM Lecciones
WHERE DBMS_LOB.GETLENGTH(contenido) >= 700;
