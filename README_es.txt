README de YajHFC 0.2.6 
======================

ACERCA DE
---------

YajHFC (Yet another java HylaFAX client) es un cliente para el servidor HylaFAX (http://www.hylafax.org) escrito en Java.

Características:
* Envío de faxes en formato PostScript o PDF
* Recepción de faxes
* Soporte para plantillas de portada
* Visor de envío y recepción de faxes
* Agenda telefónica
* Selección de columnas visibles
* Disponible en dos idiomas: inglés y alemán

Página principal: http://www.yajhfc.de.vu/
email:    Jonas Wolz <jwolz@freenet.de>

INSTALACIÓN
-----------

Requisitos:
* JRE 5.0 (o superior) 
* Servidor HylaFAX funcionando en red (obviamente ;-))
* Algún programa para ver ficheros TIFF (revisa FAQ.txt)
* Opcional: visor PostScript (por ejemplo, Ghostview)

Para instalar YajHFC descarga el fichero yajhfc-0_2_6.jar en algún lugar tu disco.
Para ejecutarlo, utiliza: "java -jar yajhfc-0_2_6.jar"
(En Windows seguramente sólo te haga falta ejecutarlo pulsando dos veces sobre el archivo).

LICENCIA
--------

YajHFC es software libre licenciado bajo la GPL.
Revisa el archivo (COPYIMG para más detalles.

CÓDIGO FUENTE
-------------

YajHFC utiliza los siguientes paquetes:

(1) Una versión ligeramente modificada del paquete gnu.hylafax
    (original de http://www.net-foundry.com/java/gnu/hylafax/)
(2) Tablelayout.jar
    de https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar) 
    de http://java.sun.com/developer/techDocs/hi/repository/ 

YajHFC se escribió en un principio como un proyecto más complicado de Java (más avanzado que los programas "Hola mundo") utilizando la IDE Ecplipse.

CAMBIOS
-------

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
- Se añade soporte para la cubierta de fax
- Se añade soporte para la recepción de faxes

POR HACER
---------

Las siguientes características posiblemente se añadirán en el futuro:

* Soporte para agenda telefónica en base de datos SQL (vía JDBC).
