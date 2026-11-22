-- Retos, soluciones, casos de prueba, versiones y misiones profesionales.
CREATE OR REPLACE PACKAGE PKG_RETOS AS
    PROCEDURE bloquear_estado_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
    PROCEDURE bloquear_revision_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
    PROCEDURE casos_catalogo(p_filas OUT SYS_REFCURSOR);
    PROCEDURE casos_protegidos(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
    PROCEDURE casos_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
    PROCEDURE casos_version(p_filas OUT SYS_REFCURSOR, p_id_version IN NUMBER);
    PROCEDURE catalogo_editor(p_filas OUT SYS_REFCURSOR);
    PROCEDURE catalogo_retos(p_filas OUT SYS_REFCURSOR);
    PROCEDURE guardar_version(p_id_reto IN NUMBER, p_id_admin IN NUMBER);
    PROCEDURE guias_retos(p_filas OUT SYS_REFCURSOR);
    PROCEDURE insertar_reto(
        p_id_leccion IN NUMBER,
        p_titulo IN VARCHAR2,
        p_enunciado IN CLOB,
        p_tipo IN VARCHAR2,
        p_dificultad IN VARCHAR2,
        p_xp IN NUMBER,
        p_id_reto OUT NUMBER
    );
    PROCEDURE misiones_publicadas(p_filas OUT SYS_REFCURSOR, p_codigo IN VARCHAR2);
    PROCEDURE reto_editor(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
    PROCEDURE reto_publicado(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
    PROCEDURE retos_finales(p_filas OUT SYS_REFCURSOR);
    PROCEDURE retos_leccion(p_filas OUT SYS_REFCURSOR, p_id_leccion IN NUMBER);
    PROCEDURE solucion_referencia(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
    PROCEDURE versiones_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER);
END PKG_RETOS;
/

CREATE OR REPLACE PACKAGE BODY PKG_RETOS AS
    PROCEDURE bloquear_estado_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT estado,revision FROM Retos WHERE id_reto=p_id_reto FOR UPDATE;
    END bloquear_estado_reto;

    PROCEDURE bloquear_revision_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT revision,id_leccion,tipo_reto FROM Retos WHERE id_reto=p_id_reto FOR UPDATE;
    END bloquear_revision_reto;

    PROCEDURE casos_catalogo(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT cp.id_caso, cp.id_reto, cp.entrada, cp.salida_esperada, cp.es_oculto FROM Casos_Prueba cp JOIN
                Retos r ON r.id_reto = cp.id_reto JOIN Lecciones le ON le.id_leccion = r.id_leccion JOIN Modulos m
                ON m.id_modulo = le.id_modulo JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje WHERE lg.activo =
                1 AND r.estado= 'PUBLICADO' ORDER BY cp.id_reto, cp.id_caso;
    END casos_catalogo;

    PROCEDURE casos_protegidos(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT entrada, salida_esperada, es_oculto FROM Casos_Prueba c WHERE c.id_reto = p_id_reto AND EXISTS
                (SELECT 1 FROM Requisito_Caso_Prueba rc WHERE rc.id_reto = c.id_reto) ORDER BY c.id_caso;
    END casos_protegidos;

    PROCEDURE casos_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_caso, entrada, salida_esperada, es_oculto FROM Casos_Prueba WHERE id_reto = p_id_reto ORDER
                BY id_caso ASC;
    END casos_reto;

    PROCEDURE casos_version(p_filas OUT SYS_REFCURSOR, p_id_version IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT numero,entrada,salida_esperada,es_oculto FROM Version_Caso_Prueba WHERE id_version=p_id_version
                ORDER BY numero;
    END casos_version;

    PROCEDURE catalogo_editor(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_reto,titulo_reto,estado,revision FROM Retos ORDER BY id_reto DESC;
    END catalogo_editor;

    PROCEDURE catalogo_retos(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT r.id_reto, r.id_leccion, r.titulo_reto, r.enunciado, r.tipo_reto, r.dificultad,
                r.exp_recompensa, r.orden_secuencia, r.revision, rc.codigo_inicial, rc.limite_tiempo_ms,
                rq.opciones, rq.respuesta_correcta, rp.codigo_ejemplo, ro.fragmentos, ro.orden_correcto, l.titulo
                AS leccion_titulo, l.contenido AS leccion_contenido, l.orden_secuencia AS leccion_orden,
                l.exp_recompensa AS leccion_exp, m.id_modulo, m.nombre_modulo, m.descripcion AS modulo_desc,
                m.orden_secuencia AS modulo_orden, lg.id_lenguaje, lg.nombre AS lenguaje_nombre, lg.descripcion AS
                lenguaje_desc, lg.icono_url, lg.activo FROM Retos r JOIN Lecciones l ON l.id_leccion = r.id_leccion
                JOIN Modulos m ON m.id_modulo = l.id_modulo JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje
                LEFT JOIN Reto_Codigo rc ON rc.id_reto = r.id_reto LEFT JOIN Reto_Quiz rq ON rq.id_reto = r.id_reto
                LEFT JOIN Reto_Prediccion rp ON rp.id_reto = r.id_reto LEFT JOIN Reto_Ordenamiento ro ON ro.id_reto
                = r.id_reto WHERE lg.activo = 1 AND r.estado= 'PUBLICADO' ORDER BY lg.id_lenguaje,
                m.orden_secuencia, l.orden_secuencia, r.orden_secuencia;
    END catalogo_retos;

    PROCEDURE guardar_version(p_id_reto IN NUMBER, p_id_admin IN NUMBER) IS
    BEGIN
        INSERT INTO
            Version_Reto(id_reto,revision,titulo_reto,enunciado,tipo_reto,dificultad,exp_recompensa,codigo_inicial,limite_tiempo_ms,opciones,respuesta_correcta,solucion_referencia,estado,id_administrador)
            SELECT
            r.id_reto,r.revision,r.titulo_reto,r.enunciado,r.tipo_reto,r.dificultad,r.exp_recompensa,c.codigo_inicial,c.limite_tiempo_ms,q.opciones,q.respuesta_correcta,s.codigo_solucion,r.estado,p_id_admin
            FROM Retos r LEFT JOIN Reto_Codigo c ON c.id_reto=r.id_reto LEFT JOIN Reto_Quiz q ON
            q.id_reto=r.id_reto LEFT JOIN Solucion_Referencia s ON s.id_reto=r.id_reto WHERE r.id_reto=p_id_reto;
            UPDATE Version_Reto v SET guia_didactica=(SELECT guia_didactica FROM Retos WHERE id_reto=v.id_reto)
                WHERE v.id_reto=p_id_reto AND v.revision=(SELECT revision FROM Retos WHERE id_reto=p_id_reto);
            UPDATE Version_Reto v SET codigo_inicial=(SELECT codigo_ejemplo FROM Reto_Prediccion WHERE
                id_reto=v.id_reto) WHERE v.id_reto=p_id_reto AND v.revision=(SELECT revision FROM Retos WHERE
                id_reto=p_id_reto) AND tipo_reto= 'PREDICCION' ;
            UPDATE Version_Reto v SET opciones=(SELECT fragmentos FROM Reto_Ordenamiento WHERE
                id_reto=v.id_reto),respuesta_correcta=(SELECT orden_correcto FROM Reto_Ordenamiento WHERE
                id_reto=v.id_reto) WHERE v.id_reto=p_id_reto AND v.revision=(SELECT revision FROM Retos WHERE
                id_reto=p_id_reto) AND tipo_reto= 'ORDENAR' ;
            INSERT INTO Version_Caso_Prueba (id_version,numero,entrada,salida_esperada,es_oculto) SELECT
                v.id_version,ROW_NUMBER() OVER(ORDER BY c.id_caso),c.entrada,c.salida_esperada,c.es_oculto FROM
                Version_Reto v JOIN Retos r ON r.id_reto=v.id_reto AND r.revision=v.revision JOIN Casos_Prueba c ON
                c.id_reto=r.id_reto WHERE r.id_reto=p_id_reto;
    END guardar_version;

    PROCEDURE guias_retos(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT id_reto,guia_didactica FROM Retos WHERE guia_didactica IS NOT NULL;
    END guias_retos;

    PROCEDURE insertar_reto(
        p_id_leccion IN NUMBER,
        p_titulo IN VARCHAR2,
        p_enunciado IN CLOB,
        p_tipo IN VARCHAR2,
        p_dificultad IN VARCHAR2,
        p_xp IN NUMBER,
        p_id_reto OUT NUMBER
    ) IS
    BEGIN
        INSERT INTO Retos (id_leccion, titulo_reto, enunciado, tipo_reto, dificultad, exp_recompensa,
            orden_secuencia) VALUES (p_id_leccion, p_titulo, p_enunciado, p_tipo, p_dificultad, p_xp, (SELECT
            NVL(MAX(orden_secuencia), 0) + 1 FROM Retos WHERE id_leccion = p_id_leccion)) RETURNING id_reto INTO
            p_id_reto;
    END insertar_reto;

    PROCEDURE misiones_publicadas(p_filas OUT SYS_REFCURSOR, p_codigo IN VARCHAR2) IS
    BEGIN
        OPEN p_filas FOR
            SELECT m.codigo, m.id_reto, r.titulo_reto, m.cliente, m.encargo,
        req.orden, req.descripcion, rc.id_caso
        FROM Mision_Profesional m
        JOIN Retos r ON r.id_reto=m.id_reto
        JOIN Lecciones l ON l.id_leccion=r.id_leccion
        JOIN Modulos mo ON mo.id_modulo=l.id_modulo
        JOIN Lenguajes lang ON lang.id_lenguaje=mo.id_lenguaje
        LEFT JOIN Requisito_Mision req ON req.codigo_mision=m.codigo
        LEFT JOIN Requisito_Caso_Prueba rc ON rc.codigo_mision=req.codigo_mision AND rc.orden_requisito=req.orden
        WHERE r.estado= 'PUBLICADO' AND lang.activo=1
        AND (p_codigo IS NULL OR m.codigo = p_codigo)
        ORDER BY m.codigo, req.orden, rc.id_caso;
    END misiones_publicadas;

    PROCEDURE reto_editor(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT r.id_reto, r.id_leccion, r.titulo_reto, r.enunciado, r.tipo_reto, r.dificultad,
                r.exp_recompensa, r.orden_secuencia, r.revision, rc.codigo_inicial, rc.limite_tiempo_ms,
                rq.opciones, rq.respuesta_correcta, rp.codigo_ejemplo, ro.fragmentos, ro.orden_correcto, l.titulo
                AS leccion_titulo, l.contenido AS leccion_contenido, l.orden_secuencia AS leccion_orden,
                l.exp_recompensa AS leccion_exp, m.id_modulo, m.nombre_modulo, m.descripcion AS modulo_desc,
                m.orden_secuencia AS modulo_orden, lg.id_lenguaje, lg.nombre AS lenguaje_nombre, lg.descripcion AS
                lenguaje_desc, lg.icono_url, lg.activo FROM Retos r JOIN Lecciones l ON l.id_leccion = r.id_leccion
                JOIN Modulos m ON m.id_modulo = l.id_modulo JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje
                LEFT JOIN Reto_Codigo rc ON rc.id_reto = r.id_reto LEFT JOIN Reto_Quiz rq ON rq.id_reto = r.id_reto
                LEFT JOIN Reto_Prediccion rp ON rp.id_reto = r.id_reto LEFT JOIN Reto_Ordenamiento ro ON ro.id_reto
                = r.id_reto WHERE r.id_reto=p_id_reto;
    END reto_editor;

    PROCEDURE reto_publicado(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT r.id_reto, r.id_leccion, r.titulo_reto, r.enunciado, r.tipo_reto, r.dificultad,
                r.exp_recompensa, r.orden_secuencia, r.revision, rc.codigo_inicial, rc.limite_tiempo_ms,
                rq.opciones, rq.respuesta_correcta, rp.codigo_ejemplo, ro.fragmentos, ro.orden_correcto, l.titulo
                AS leccion_titulo, l.contenido AS leccion_contenido, l.orden_secuencia AS leccion_orden,
                l.exp_recompensa AS leccion_exp, m.id_modulo, m.nombre_modulo, m.descripcion AS modulo_desc,
                m.orden_secuencia AS modulo_orden, lg.id_lenguaje, lg.nombre AS lenguaje_nombre, lg.descripcion AS
                lenguaje_desc, lg.icono_url, lg.activo FROM Retos r JOIN Lecciones l ON l.id_leccion = r.id_leccion
                JOIN Modulos m ON m.id_modulo = l.id_modulo JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje
                LEFT JOIN Reto_Codigo rc ON rc.id_reto = r.id_reto LEFT JOIN Reto_Quiz rq ON rq.id_reto = r.id_reto
                LEFT JOIN Reto_Prediccion rp ON rp.id_reto = r.id_reto LEFT JOIN Reto_Ordenamiento ro ON ro.id_reto
                = r.id_reto WHERE r.id_reto = p_id_reto AND r.estado= 'PUBLICADO' AND lg.activo=1;
    END reto_publicado;

    PROCEDURE retos_finales(p_filas OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_filas FOR
            SELECT f.id_reto FROM Final_Modulo f JOIN Retos r ON r.id_reto=f.id_reto JOIN Lecciones l ON
                l.id_leccion=r.id_leccion WHERE l.id_modulo=f.id_modulo;
    END retos_finales;

    PROCEDURE retos_leccion(p_filas OUT SYS_REFCURSOR, p_id_leccion IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT r.id_reto, r.id_leccion, r.titulo_reto, r.enunciado, r.tipo_reto, r.dificultad,
                r.exp_recompensa, r.orden_secuencia, r.revision, rc.codigo_inicial, rc.limite_tiempo_ms,
                rq.opciones, rq.respuesta_correcta, rp.codigo_ejemplo, ro.fragmentos, ro.orden_correcto, l.titulo
                AS leccion_titulo, l.contenido AS leccion_contenido, l.orden_secuencia AS leccion_orden,
                l.exp_recompensa AS leccion_exp, m.id_modulo, m.nombre_modulo, m.descripcion AS modulo_desc,
                m.orden_secuencia AS modulo_orden, lg.id_lenguaje, lg.nombre AS lenguaje_nombre, lg.descripcion AS
                lenguaje_desc, lg.icono_url, lg.activo FROM Retos r JOIN Lecciones l ON l.id_leccion = r.id_leccion
                JOIN Modulos m ON m.id_modulo = l.id_modulo JOIN Lenguajes lg ON lg.id_lenguaje = m.id_lenguaje
                LEFT JOIN Reto_Codigo rc ON rc.id_reto = r.id_reto LEFT JOIN Reto_Quiz rq ON rq.id_reto = r.id_reto
                LEFT JOIN Reto_Prediccion rp ON rp.id_reto = r.id_reto LEFT JOIN Reto_Ordenamiento ro ON ro.id_reto
                = r.id_reto WHERE r.id_leccion = p_id_leccion AND r.estado= 'PUBLICADO' ORDER BY r.orden_secuencia
                ASC;
    END retos_leccion;

    PROCEDURE solucion_referencia(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT codigo_solucion FROM Solucion_Referencia WHERE id_reto=p_id_reto;
    END solucion_referencia;

    PROCEDURE versiones_reto(p_filas OUT SYS_REFCURSOR, p_id_reto IN NUMBER) IS
    BEGIN
        OPEN p_filas FOR
            SELECT * FROM Version_Reto WHERE id_reto=p_id_reto ORDER BY revision DESC;
    END versiones_reto;
END PKG_RETOS;
/
