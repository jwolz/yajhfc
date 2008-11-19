LÉEME (README) de YajHFC 0.3.7
==============================

ACERCA DE
---------

YajHFC (Yet another java HylaFAX client) es un cliente para el servidor HylaFAX 
(http://www.hylafax.org) escrito en Java.

Características:
* Envío de faxes en formato PostScript o PDF
* Transmisión bajo demanda (polling) de faxes
* Soporte para generar páginas de portada a partir de plantillas
* Visualización de faxes enviados y recibidos
* Agenda telefónica
* Selección de columnas visibles (seleccionable desde cuadro de diálogo)
* Disponible en cuatro idiomas: Inglés, Francés, Alemán y Español

Página principal: http://www.yajhfc.de.vu/
email:    Jonas Wolz <jwolz@freenet.de>

INSTALACIÓN
-----------

Requisitos:
* JRE 5.0 (o superior) 
* Servidor HylaFAX funcionando en red (obviamente ;-) )
* Algún programa para ver archivos TIFF (revisar el documento FAQ.txt)
* Opcional: visor PostScript (por ejemplo, Ghostview)

Para instalar YajHFC descarga el archivo yajhfc-0_3_7.jar en algún lugar 
de tu disco.
Para ejecutarlo, utiliza: "java -jar yajhfc-0_3_7.jar"
(En Windows seguramente sólo te haga falta ejecutarlo pulsando dos veces sobre el archivo).

LICENCIA
--------

YajHFC es software libre licenciado bajo la GPL.
Revisa el archivo COPYING para más detalles.

CÓDIGO FUENTE
-------------

Para obtener las instrucciones donde se describe cómo compilar YajHFC desde
el código fuente, revisa el archivo BUILDING.txt que está en este directorio.

YajHFC utiliza los siguientes paquetes:
(1) La librería gnu.hylafax (núcleo y paquete inet-ftp)
    de http://gnu-hylafax.sourceforge.net/
(2) Tablelayout.jar
    de https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar) 
    de http://java.sun.com/developer/techDocs/hi/repository/
(4) "Apache Commons Logging" de http://commons.apache.org/logging/
    como dependencia de (1)

Las copias de los archivos requeridos en (2), (3) y (4) también se pueden encontrar
en el subdirectorio jar en el archivo original.

YajHFC se escribió en un principio como un proyecto más complicado 
de Java (más avanzado que los programas "Hola mundo") utilizando la IDE Eclipse.

CAMBIOS
-------

0.3.9:
- Se añaden las traducciones de italiano y turco
- "Búsqueda rápida" para las agendas telefónicas
- Varias correcciones de errores y pequeñas mejoras

0.3.8a:
- Corrección de un error para las agendas telefónicas en formato XML
- Pequeñas mejoras en la interfaz de usuario

0.3.8:
- Ventana mejorada de la agenda telefónica con soporte para múltiples agendas telefónicas
- Soporte para guardar el estado de los faxes leídos / no leídos en la tabla de una base de datos central
- Panel gráfico para añadir controladores JDBC y complementos

0.3.7:
- Nuevo cuadro de diálogo simplificado para el envío de faxes
- Soporte para las páginas de portada en formato HTML
- Soporte para las páginas de portada en formatos XSL:FO y ODT utilizando un complemento (plugin)
- Mejor soporte para los complementos (plugins)
- Cambio en el sistema de registros para usar las API de registros de Java
- Ahora se utiliza gnu.hylafax 1.0.1
- Corrección de algunos errores

0.3.6:
- Corrección de algunos errores
- Soporte para seleccionar múltiples entradas de la agenda telefónica en en cuadro de diálogo de envío
- Nuevo argumento de línea de comandos ("--logfile") que permite el registro en un archivo en modo de depuración
- Soporte para el "modo desconectado" como posible solución a algunos errores de HylaFAX
- Soporte para seleccionar el módem a utilizar

0.3.5:
- Algunas mejoras/"pulidos" de la interfaz gráfica (UI), por ejemplo, la barra de progreso en la lista 
  de faxes y más iconos
- Eliminación de algunas restricciones para las columnas visibles
  (por ejemplo, no es necesario que sea visible el identificador / ID del trabajo)
- Las nuevas acciones en los faxes se ejecutan ahora sólo *después* de que los faxes no se 
  encuentren "en progreso"

0.3.4:
- Soporte para especificar el destinatario desde línea de comandos
- Suspender / Retomar trabajos de fax
- Se añade la traducción en francés
- Posibilidad de establecer mediante valor en línea de comandos la pestaña que se muestra al iniciar
- La librería principal gnu.hylafax se utiliza ahora de forma predeterminada (la cual corrige algunos errores)

0.3.3:
- Soporte para reenvío de faxes
- Soporte para impresión de tablas con listado de faxes recibidos / enviados
- Color de fondo distinto para trabajos de fax erróneos
- Soporte añadido para agenda telefónica LDAP de sólo lectura
- Soporte para apertura múltiple de agendas telefónicas
- Programa de instalación para Windows con instalación automática de "impresora de fax"

0.3.2:
- Corrección de errores esporádicos cuando se envían faxes
- Soporte para iniciar una nueva instancia del programa en segundo plano (útil para "impresoras de faxes")
- Algunos parámetros adicionales nuevos de línea de comandos
- Botón "Actualizar"
- Archivo de construcción ant. incluido en las fuentes de distribución
- Actualización de la documentación

0.3.1:
- Interfaz intercambiable
- Opción para mostrar los nuevos faxes en el visor de forma automática
- Opción para cambiar el intervalo de verificación de faxes del servidor HylaFAX
- Cuadro de diálogo sencillo para buscar en la agenda de teléfonos
- YajHFC debería ejecutarse ahora con la versión incluida de la librería gnu.hylafax (¡no se ha verificado del todo!)

0.3.0:
- Se añade soporte para convertir las imágenes a PostScript antes de enviar el fax
- Se añade un botón de "previsualización" en el cuadro de diálogo de Envío
- Se añade soporte para agendas telefónicas en bases de datos SQL (utilizando JDBC)

0.2.7a:
- Se corrige un error que ocurría cuando se mostraban "sólo los propios faxes"

0.2.7:
- Se añade la traducción al español
- Se añade una opción para seleccionar el idioma 
- Soporte para añadir la diferencia horaria a los campos de fecha
- Se corrige un error que causaba que se mostraran números incorrectos cuando se enviaban múltiples faxes

0.2.6:
- Pequeños ajustes para Windows 9x

0.2.5:
- Pequeños errores

0.2.4:
- Soporte para filtros
- Soporte en modo Administrador

0.2.3:
- Soporte para múltiples números/filas para los faxes enviados
- Reenvío/guardar faxes
- Cambios internos

0.2.2:
- Se añade soporte para "traer al frente la ventana / pitido" en los nuevos faxes

0.2.1:
- Se añade el estado leído/no leído para los faxes recibidos
- Agenda telefónica en orden alfabético
- Algunos cambios internos

0.2:
- Se añade soporte para la portada de fax
- Se añade soporte para la transmisión bajo demanda (polling) de faxes

POR HACER
---------

Las siguientes características posiblemente se añadirán en el futuro:

* Más traducciones (mirar abajo)

YajHFC utiliza GNU gettext para las traducciones por lo que resulta muy sencillo 
integrarlas en el programa.
Para el traductor que crea una nueva traducción (sin traducir la documentación)
le llevará unas pocas horas el trabajo inicial y una media hora por cada nueva
traducción. No necesitas un conocimiento "avanzado" para hacerlo (básicamente
sólo necesitas saber trabajar con un editor de textos, y si instalas un programa
especializado resulta más sencillo aún).

Así que, si te gustaría tener YajHFC traducido en tu idioma, los voluntarios
son siempre bienvenidos. ;-)

