LÉAME (README) de YajHFC 0.4.4
==============================

ACERCA DE
---------

YajHFC (Yet another java HylaFAX client) es un cliente para el servidor HylaFAX 
(http://www.hylafax.org) escrito en Java.

Características:
* Envío de faxes en PostScript, PDF y otros formatos
* Transmisión bajo demanda (polling) de faxes
* Soporte para generar páginas de portada a partir de plantillas
* Visualización de faxes enviados y recibidos
* Agenda telefónica (los datos se pueden leer, de manera opcional, desde bases de datos SQL o directorios LDAP)
* Selección de columnas visibles (configurable desde cuadro de diálogo)
* Disponible en diez idiomas: alemán, chino, español, francés, griego, inglés, italiano, polaco, ruso y turco

Página principal: http://yajhfc.berlios.de/
E-mail: 	  Jonas Wolz <jwolz@freenet.de>

INSTALACIÓN
-----------

Requisitos:
* JRE 5.0 o superior (se recomienda Java 6.0)
* Servidor HylaFAX funcionando en la red (obviamente ;-) )
* Algún programa para ver archivos TIFF (consultar la FAQ)
* Opcional: un visor PostScript (por ejemplo, Ghostview), GhostScript
  y tiff2pdf (integrado en libtiff-utils)

Para instalar YajHFC descargue el archivo yajhfc-0_4_3.jar en algún lugar 
de su disco.
Para ejecutarlo, utilice: "java -jar yajhfc-0_4_3.jar"
(En Windows seguramente sólo sea necesario ejecutarlo pulsando dos veces sobre el archivo).

LICENCIA
--------

YajHFC es software libre licenciado bajo la GPL.
Consulte el archivo COPYING para más detalles.

CÓDIGO FUENTE
-------------

Para obtener las instrucciones donde se describe cómo compilar YajHFC desde
el código fuente, revise el archivo BUILDING.txt que está en este directorio.

YajHFC utiliza los siguientes paquetes:
(1) La biblioteca gnu.hylafax (núcleo y paquete inet-ftp)
    de http://gnu-hylafax.sourceforge.net/
(2) Tablelayout.jar
    de https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar) 
    de http://java.sun.com/developer/techDocs/hi/repository/
(4) "Apache Commons Logging" de http://commons.apache.org/logging/
    como dependencia de (1)

Las copias de los archivos requeridos en (2), (3) y (4) también se pueden encontrar
en el subdirectorio jar en el archivador original.

YajHFC se escribió en un principio como un proyecto más complicado 
de Java (más avanzado que los programas "Hola mundo") utilizando la IDE Eclipse
(y ha recorrido un largo camino desde entonces... ;-) ).

CAMBIOS
-------

0.4.4:
- Soporte mejorado para MacOS (cambios de diseño, principalmente)
- Soporte para introducir conversores de archivo personalizados
- Acceso a los ajustes avanzados desde la interfaz gráfica
- Soporte para la impresión de agendas telefónicas
- Impresión de faxes mejorada
- Soporte para la etiqueta @CCNameAndFax@ en las páginas de portada HTML
- Reenvío de faxes a múltiples destinatarios
- Consola de registros para ver los eventos en tiempo real
- Separación del formato utilizado para la visualización y el envío de faxes
- Lista de módems editable por el usuario
- Nuevo parámetro en línea de comandos ("override-setting")
- Corrección de varios errores

0.4.3:
- Nueva característica "Ver registro" para los faxes enviados
- Posibilidad de desactivar el mensaje de notificación de la bandeja del sistema
- Filtros para los elementos de la agenda telefónica
- Soporte para "resolución ampliada" (p. ej. USEXVRES=yes)
- Nuevo parámetro en línea de órdenes: --modem
- Soporte para TCP/IP y puerto virtual de impresión mediante tuberías con nombre (FIFO)
- Soporte para predeterminar/omitir la configuración en /etc/yajhfc
- Corrección de varios errores
- Disponibilidad de paquetes RPM y DEB para YajHFC

0.4.2a:
Se corrige un error por el cual no se almacenan los ajustes cuando no existe un archivo de configuración antiguo.

0.4.2:
- Se añade la traducción al polaco
- Soporte para leer destinatarios desde archivos de texto
- Posibilidad de definir el estado deseado de la ventana desde línea de comandos
- "Modo sólo envío" que no muestra la ventana principal (cuando se 
  envía un documento y no se especifican los parámetros --background ni --noclose)
- Posibilidad de especificar opciones arbitrarias de HylaFAX para los nuevos trabajos de fax
- Solución temporal a un error de Java que hacía que YajHFC no almacenara sus ajustes cuando estaba en ejecución
  mientras el usuario cerraba la sesión en Windows 7, Vista y (a veces) XP. 
- Página de portada predeterminada en formato HTML para evitar problemas con las codificaciones de caracteres distintas de ISO-8859-1
- El programa de instalación de Windows ahora ofrece, de manera opcional, instalar tiff2pdf y GhostScript
- Corrección de varios errores, mejoras y limpieza de código

0.4.1:
- Soporte para listas de distribución en la agenda telefónica (sólo para XML+JDBC)
- Soporte de CSV para agendas telefónicas (p. ej. para importar/exportar)
- Soporte para establecer el "indicador de archivar" (doneop) para los faxes enviados
- Barra de búsqueda rápida de faxes en la ventana principal
- Corrección de algunos errores

0.4.0:
- Se añade la traducción al griego
- Posibilidad de responder manualmente a una llamada de voz (como la función "faxanswer")
- Verificación de actualización
- Soporte del directorio para los faxes archivados
- Soporte para visualizar y enviar faxes como un archivo único (en formato PDF, TIFF o PS)
- La agenda telefónica incluye los campos más comunes
- Soporte para poner un icono en la bandeja del sistema con Java 6 
- Cuadro de diálogo de opciones mejorado
- Soporte mejorado en línea de comandos (se puede utilizar YajHFC 
  para enviar faxes sin confirmación por parte del usuario)
- Incluye un método de ofuscación sencilla para las contraseñas
- Limpieza del código interno

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
- La biblioteca principal gnu.hylafax se utiliza ahora de forma predeterminada (la cual corrige algunos errores)

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
- YajHFC debería ejecutarse ahora con la versión incluida de la biblioteca gnu.hylafax (¡no se ha verificado del todo!)

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

* ¿Más traducciones? (mirar abajo)

YajHFC utiliza GNU gettext para las traducciones por lo que resulta muy sencillo 
integrarlas en el programa.
Para el traductor que crea una nueva traducción (sin traducir la documentación)
le llevará unas pocas horas el trabajo inicial y una media hora por cada nueva
traducción. No se necesita un conocimiento "avanzado" para hacerlo (básicamente
sólo se necesita saber trabajar con un editor de textos, y si se instala un programa
especializado resulta más sencillo aún).

Así que, si le gustaría tener YajHFC traducido en tu idioma, los voluntarios
son siempre bienvenidos. ;-)

