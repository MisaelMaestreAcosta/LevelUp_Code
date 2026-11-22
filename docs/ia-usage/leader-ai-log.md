# Bitácora de uso de inteligencia artificial

| Campo | Contenido |
|---|---|
| **Proyecto** | LevelUp Code |
| **Líder técnico** | Misael Andrés Maestre Acosta |
| **Herramienta de IA** | Codex |
| **Fase del proyecto** | Integración final y preparación de la sustentación |

## 1. Tablas ajenas al proyecto

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Identificar por qué el esquema mostraba tablas que no formaban parte de LevelUp Code. |
| **Consulta** | «Sabes por que aparecen esas tablas que no pertenecen al proyecto?». |
| **Respuesta** | Se buscó el nombre de tres tablas demo en el código y los scripts. El maestro limpia una lista explícita de tablas de LevelUp Code, por lo que conserva los objetos de otros ejercicios que existan en el mismo esquema. |

## 2. Inventario y limpieza del esquema

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Retirar las tablas de prácticas externas sin perder el avance de los estudiantes. |
| **Consulta** | «Quiero dejarlo limpio, sin tablas externas». |
| **Respuesta** | Se identificaron cinco tablas demo con seis filas en total y se verificó que ninguna clave foránea del proyecto apuntara a ellas. Se retiraron usando la papelera de Oracle y se comprobó que quedaron 31 tablas activas de LevelUp Code. |

## 3. Ubicación de la limpieza de Oracle

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Confirmar en qué base y esquema comprobar el cambio desde SQL Developer. |
| **Consulta** | «En donde hiciste eso?». |
| **Respuesta** | Se indicó que la limpieza se realizó por JDBC en Oracle local con el esquema `LEVELUP_ADMIN` y el servicio `XEPDB1`. Para ver el resultado se podía refrescar el árbol de tablas. Las cinco tablas quedaban disponibles en la papelera. |

## 4. Diseño del paquete de gamificación

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Evaluar si la cantidad de paquetes, funciones y procedimientos era apropiada para el proyecto. |
| **Consulta** | «Piensas que, un solo paquete, esas funciones y esos procedimientos estan bien para el proyecto?». |
| **Respuesta** | Se explicó que un paquete puede reunir operaciones con la misma responsabilidad, como calcular el nivel y aplicar XP. También se identificó una diferencia entre la validación de Java y Oracle al completar lecciones. |

## 5. Procedimiento para iniciar lecciones

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Identificar una mejora concreta en la lógica PL/SQL existente. |
| **Consulta** | «No agregarias ningun otra funcion, procedimiento o disparador?». |
| **Respuesta** | Se propuso `SP_INICIAR_LECCION` para iniciar el progreso de manera segura y conservar el estado de las lecciones ya completadas. Para la racha se sugirió reutilizar la función existente. |

## 6. Validación de gamificación para la sustentación

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Corregir el flujo de lecciones y los datos demo antes de practicar la exposición. |
| **Consulta** | «Necesito que todo este perfecto en la base de datos para empezar a estudiarla y sacar adelante la sustentacion». |
| **Respuesta** | Se implementó el inicio de lecciones en PL/SQL, se exigió el estado `EN_CURSO` antes de completar y se alineó el nivel de la cuenta demo con su XP. Pasaron 22 controles PL/SQL y 9 pruebas de integración Java–Oracle. |

## 7. Rama para integrar la corrección de gamificación

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Determinar cómo entregar a otro integrante la versión actualizada de los scripts y el DAO. |
| **Consulta** | «Ok, para que mi compañero tenga la version actualizada, cual rama tengo que hacer pull request a develop?». |
| **Respuesta** | Se recomendó usar `feature/database-plsql` como rama de origen y `develop` como destino. Como los cambios aún estaban en el árbol local, primero había que guardarlos y publicarlos. |

## 8. Publicación de la corrección de lecciones

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Dejar disponible la corrección para abrir el pull request de gamificación. |
| **Consulta** | «Publica los cambios en feature/database-plsql, y yo hago el pull request». |
| **Respuesta** | Se preparó la rama existente y se publicó el commit `601b8a8`, que contiene la corrección de progreso y el ajuste del nivel demo. |

## 9. Revisión de la base de datos

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Evaluar el modelo relacional, los scripts y la lógica de Oracle antes de la sustentación. |
| **Consulta** | «Analiza minuciosamente toda la base de datos, scripts, funciones, procedimientos, disparadores y dime sinceramente si esta base de datos sí está a la altura. Califícala del 1 al 5». |
| **Respuesta** | Codex verificó las 31 tablas y los objetos PL/SQL en Oracle. Estimó una nota técnica de 4,1/5 y señaló tres mejoras concretas: proteger el catálogo al reinstalar, evitar que una edición pierda vínculos de las misiones y calcular la racha al consultar el perfil. |

## 10. Correcciones sin ampliar la aplicación

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Corregir problemas de integridad sin agregar funcionalidades ni reconstruir el esquema. |
| **Consulta** | «Soluciona lo que puedas solucionar sin implementar nuevas funciones, ni poner en riesgo el funcionamiento de la aplicación». |
| **Respuesta** | Se protegió la carga inicial contra la ejecución sobre un catálogo existente, se corrigió la consulta de la racha y se conservaron los casos vinculados a misiones al editar un reto. Pasaron 67 pruebas de Maven, incluidas 12 de integración con Oracle. |

## 11. Publicación de los arreglos de integridad

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Subir por separado las correcciones del catálogo, la racha y las misiones. |
| **Consulta** | «Publicalos en la rama que de database-plsql y yo hago el pull request». |
| **Respuesta** | Se publicaron tres commits en `feature/database-plsql`: `add525a` para la protección del catálogo, `593f703` para la racha calculada y `6ecc630` para conservar los casos de las misiones. |

## 12. Evaluación después de los arreglos

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Estimar qué impacto tenían las correcciones sobre la nota técnica de la base de datos. |
| **Consulta** | «Con esos cambios como quedaria la nota de mi base de datos». |
| **Respuesta** | Se estimó una mejora de 4,1 a 4,4 sobre 5 con los mismos criterios. El despliegue y la integridad mejoraron, y quedó pendiente validar una instalación completa desde un esquema vacío. |

## 13. Criterios para alcanzar cinco sobre cinco

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Priorizar las mejoras que todavía podían fortalecer la integridad y el despliegue. |
| **Consulta** | «Que hay que hacer para que sea un 5?». |
| **Respuesta** | Se propuso reforzar restricciones, proteger vínculos de misiones en Oracle y verificar tanto una instalación limpia como una actualización con datos. También se recomendó probar que dos sesiones no puedan acreditar XP dos veces por la misma lección. |

## 14. Riesgo de aplicar restricciones antes de exponer

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Decidir cuándo probar restricciones nuevas sin afectar la instalación de la sustentación. |
| **Consulta** | «Si tu haces esos cambios no pondrias en riesgo el funcionamiento de la aplicacion?». |
| **Respuesta** | Se explicó que una regla más estricta puede hacer fallar un flujo si Java todavía intenta realizar una escritura que Oracle ya rechaza. Se recomendó probar en un esquema separado y mantener para la exposición la versión verificada. |

## 15. Mejoras del modelo relacional

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Identificar restricciones útiles que refuercen las relaciones existentes. |
| **Consulta** | «Que mejoras se pueden hacer en el modelo relacional y la integridad de los datos?». |
| **Respuesta** | Se propuso validar que el reto final pertenezca al mismo módulo, relacionar el estado de la lección con su fecha de finalización, limitar las vidas a enteros de cero a cinco y asegurar una sola fila de actividad por día. |

## 16. Impacto de las restricciones en Java y en los seeds

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Distinguir los cambios de bajo riesgo de los que necesitan ajustes coordinados. |
| **Consulta** | «Estos cambios afectarian el funcionamiento de la aplicacion?». |
| **Respuesta** | Se explicó que las restricciones sobre vidas y fechas encajan en los flujos actuales. Validar el módulo del reto final obliga a revisar los datos y scripts que lo crean. Cambiar el borrado en cascada de casos exige probar el borrado de un reto y sus vínculos. |

## 17. Diferencia entre cursor y vista

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Entender una pregunta del profesor y relacionarla con el gráfico de experiencia. |
| **Consulta** | «En clase, mi profesor le preguntó a uno de mis compañeros qué había utilizado para una de estas opciones, si cursor o vistas. No entendí la pregunta, ¿me puedes explicar?». |
| **Respuesta** | Una vista es una consulta guardada con nombre. Un cursor permite recorrer las filas de una consulta. La función de racha usa un cursor explícito. En la versión de ese momento, el gráfico agrupaba la XP por fecha con una consulta SQL enviada desde Java. |

## 18. Consulta SQL escrita en Java

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Comprobar si el gráfico de experiencia infringía la regla del profesor. |
| **Consulta** | «El nos dijo que no podiamos quemar codigo de base de datos en el programa de java, aqui lo estamos haciendo?». |
| **Respuesta** | Se confirmó que el repositorio del gráfico tenía el texto de un `SELECT` en Java. El identificador del usuario se pasaba como parámetro, pero la consulta seguía definida en el programa. Cuando se confirmó que todo `SELECT` estaba prohibido, se revisó el alcance en la capa de datos. |

## 19. Retirar las consultas SELECT del código Java

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Cumplir el requisito de la asignatura de mantener las sentencias `SELECT` en Oracle. |
| **Consulta** | «Ok, pues necesito arreglar eso, que no haya ningún select dentro del programa». |
| **Respuesta** | Se revisaron los repositorios y se trasladaron a paquetes PL/SQL las consultas y las subconsultas incluidas en escrituras. Java pasó a invocar los procedimientos por JDBC. Las consultas que entregan filas utilizan `SYS_REFCURSOR`. |

## 20. Organización de los scripts de instalación

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Simplificar la instalación y distinguirla de una actualización sobre datos existentes. |
| **Consulta** | «Quiero organizar mejor lo de los scripts, hay muchos, ¿me ayudas con eso?». |
| **Respuesta** | Se redujo el script maestro a un archivo que llama a los 19 scripts en orden mediante `@@`. La guía explica cuándo usar el maestro, cuándo ejecutar `17_data_access.sql` y dónde están las pruebas PL/SQL. |

## 21. Comprobación de scripts pendientes

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Saber qué ejecutar para usar el Java actualizado con Oracle. |
| **Consulta** | «Que tengo que hacer para actualizar la base de datos?». |
| **Respuesta** | Se consultó el diagnóstico de solo lectura y la instalación local no reportó scripts pendientes. Para otro esquema que ya tenga las migraciones previas, se indicó ejecutar `17_data_access.sql` como `LEVELUP_ADMIN` con F5. |

## 22. Inventario de objetos PL/SQL

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Contar funciones, procedimientos y disparadores después de trasladar las consultas. |
| **Consulta** | «Ahora cuantos funciones, procedimientos y disparadores hay=». |
| **Respuesta** | Se contaron tres funciones, 78 procedimientos públicos, un procedimiento privado y dos disparadores. Se distinguieron los procedimientos de gamificación, diagnóstico y acceso desde Java. |

## 23. Mantenibilidad de un paquete con muchas operaciones

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Evaluar si convenía reducir la cantidad de procedimientos o reorganizarlos. |
| **Consulta** | «69 procedimientos no son muchos?». |
| **Respuesta** | Se precisó que eran 68 en el paquete de acceso y 79 en total. Se recomendó agruparlos por responsabilidad y consolidar únicamente los que hicieran prácticamente lo mismo. |

## 24. División de los procedimientos por responsabilidad

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Facilitar la ubicación de los procedimientos sin alterar su SQL ni sus parámetros. |
| **Consulta** | «Sí, mejora la organización», después de preguntar si 68 procedimientos en un solo paquete eran demasiados. |
| **Respuesta** | Los 68 procedimientos de acceso se repartieron entre `PKG_USUARIOS`, `PKG_CATALOGO`, `PKG_RETOS`, `PKG_SEGUIMIENTO` y `PKG_ESPACIO_ESTUDIANTE`. Se verificaron las firmas y los cuerpos, se compilaron los paquetes en Oracle y pasaron 68 pruebas. |

## 25. Verificación de los paquetes instalados

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Confirmar que Oracle compiló todos los módulos y que el Java actualizado podía leer datos. |
| **Consulta** | «Puedes verificar que este bien? ya ejecute el script». |
| **Respuesta** | Se verificaron los siete paquetes y sus cuerpos en estado `VALID`. Java cargó el perfil, los tres lenguajes, los 28 días de actividad, los cuatro requisitos de Tienda Horizonte y el cuaderno. El diagnóstico no mostró scripts pendientes. |

## 26. Comparación entre consultas en Java y procedimientos de Oracle

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Entender qué se ganó al retirar el SQL de consulta del código Java. |
| **Consulta** | «Digo la anterior a esa, cuando llamabamos select dentro de el codigo». |
| **Respuesta** | Se explicó que Oracle ejecutaba el cálculo en ambas versiones. Antes recibía el SQL desde Java. La versión nueva recibe una llamada al procedimiento, cumple el requisito del curso y permite cambiar la consulta en Oracle si se preserva su contrato. |

## 27. Publicación de los cambios

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Publicar los cambios en la rama de base de datos para preparar el pull request a `develop`. |
| **Consulta** | «¿Hago pull request de la rama a develop?» y, tras revisar que quedaban cambios locales, «Ok pues súbelos». |
| **Respuesta** | Se publicaron en `feature/database-plsql` dos commits. `2079c35` instaló los paquetes por módulos y `290ec35` cambió las consultas de Java por llamadas a procedimientos de Oracle. El pull request #29 aparece integrado en el historial del repositorio. |

## 28. Preparación del estudio de Oracle

| Campo | Contenido |
|---|---|
| **Fecha** | 19 de noviembre de 2026 |
| **Objetivo** | Organizar una guía breve del modelo y de las llamadas de la aplicación para la sustentación. |
| **Consulta** | «Necesito estudiar todo sobre la base de datos, tablas, funciones, procedimientos, disparadores, en que parte de el codigo se llama a cada uno y demas». |
| **Respuesta** | Se organizaron las tablas por responsabilidad y se localizaron las llamadas desde Java. Se preparó un índice con archivos y líneas de los procedimientos y se explicó el recorrido de un reto y del gráfico de XP. |

## 29. Evaluación de respuestas y gamificación

| Campo | Contenido |
|---|---|
| **Fecha** | 21 de noviembre de 2026 |
| **Objetivo** | Explicar qué parte del proyecto determina el resultado de un reto y qué parte lo registra. |
| **Consulta** | «¿Quién determina si el código o la respuesta es correcta? [...] No entiendo». |
| **Respuesta** | Java califica la respuesta. En un reto de código compara la salida obtenida con la esperada en los casos de prueba y, en un quiz, compara la opción elegida. `SP_REGISTRAR_INTENTO_RETO` recibe el veredicto, guarda el intento y aplica las reglas de XP y vidas. |

## 30. Revisión de un cambio local en UserRepository

| Campo | Contenido |
|---|---|
| **Fecha** | 21 de noviembre de 2026 |
| **Objetivo** | Verificar si se había borrado parte de la interfaz de persistencia. |
| **Consulta** | «Creo que borre algo en userrepository revisa eso». |
| **Respuesta** | Se comparó la versión guardada con `origin/develop`. Todos los métodos seguían presentes. El único cambio era un import de `UserService` para resolver la referencia en el Javadoc. |

## 31. Alcance del historial de versiones

| Campo | Contenido |
|---|---|
| **Fecha** | 21 de noviembre de 2026 |
| **Objetivo** | Explicar el alcance del versionado de retos, el diagnóstico y los permisos de Oracle. |
| **Consulta** | «El historial empieza con una copia de referencia para el contenido previo a la migración de versiones […] Explícame esto más claramente». |
| **Respuesta** | La migración conserva una copia del reto en ese momento. Antes de guardar un intento, el repositorio verifica la revisión bajo bloqueo y `TRG_INTENTO_VERSION` vincula el envío con la versión publicada. También se explicó cómo `USER_ERRORS` y `USER_OBJECTS` ayudan a diagnosticar la instalación y por qué separar el propietario de la cuenta de aplicación sería una mejora de despliegue. |

## 32. Documentación de clases y registro del uso de IA

| Campo | Contenido |
|---|---|
| **Fecha** | 22 de noviembre de 2026 |
| **Objetivo** | Preparar dos archivos para documentar el diseño del proyecto y el trabajo del líder técnico. |
| **Consulta** | «Dentro de la carpeta docs necesito que crees un diagrama de clases en mermaid del proyecto, y dentro de ia-usage crea un archivo md de mi que soy el lider». |
| **Respuesta** | Se creó el diagrama de clases Mermaid con vistas de dominio, flujo de Java y acceso a Oracle. Se creó la bitácora del líder con la estructura de fecha, objetivo, consulta y respuesta. |
