LEGGIMI per YajHFC 0.4.3
========================

INFORMAZIONI
------------

YajHFC (Yet Another Java HylaFAX Client) è un client per il server fax HylaFAX
(http://www.hylafax.org/) scritto in Java.

Funzionalità:
* Invio via fax di documenti PostScript, PDF e vari altri formati
* Fax polling
* Supporto per la generazioni di copertine fax da modelli
* Visualizzazione dei fax inviati e ricevuti
* Rubrica (le voci possono essere opzionalmente lette da database SQL o directory LDAP)
* E' possibile selezionare in una apposita finestra le colonne degli elenchi fax da visualizzare
* Supporto per dieci lingue: Cinese, Inglese, Francese, Tedesco, Greco, Italiano, Polacco, Russo, Spagnolo e Turco

Homepage: http://yajhfc.berlios.de/
email:    Jonas Wolz <jwolz@freenet.de>

INSTALLAZIONE
-------------

Requisiti:
* JRE 5.0 o superiore (si raccomanda Java 6)
* Un server fax HylaFAX attivo sulla rete (ovviamente ;-) )
* Un programma per visualizzare i file TIFF (vedere anche la FAQ)
* Opzionale: un visualizzatore PostScript (es. Ghostview), GhostScript
     e tiff2pdf (dal pacchetto libtiff-utils)

Per installare YajHFC basta scaricare il file yajhfc-0_4_3.jar su una cartella del proprio disco.
Per eseguirlo utilizzare: "java -jar yajhfc-0_4_3.jar"
(In Windows normalmente è anche possibile cliccare due volte sul file jar.)

LICENZA
-------

YajHFC è software libero rilasciato sotto licenza GPL.
Vedere il file COPYING per dettagli.

CODICE SORGENTE
---------------

Per istruzioni su come compilare YajHFC dai sorgenti
vedere il file BUILDING.txt in questa directory.

YajHFC usa i seguenti pacchetti:
(1) La libreria gnu.hylafax (pacchetti core e inet-ftp) 
    da http://gnu-hylafax.sourceforge.net/
(2) TableLayout.jar 
    da https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar) 
    da http://java.sun.com/developer/techDocs/hi/repository/
(4) Apache Commons Logging da http://commons.apache.org/logging/
    come dipendenza di (1)

Copie dei file richiesti ai punti (2), (3) e (4) si trovano anche nella 
sub directory jar dei sorgenti.

YajHFC originariamente era stato scritto come primo progetto Java più complesso
(dei programmi avanzati "Hello world") utilizzando l'IDE Eclipse
(e ha fatto molta strada da allora... ;-) )

MODIFICHE
---------

0.4.3:
- Funzionalità "Visualizza Log" per i fax inviati
- I messaggi nell'area di notifica possono essere disabilitati
- Filtri per gli elementi della rubrica
- Supporto per "Risoluzione estesa" (es. USEXVRES=yes)
- nuovo parametro per la linea di comando: --modem
- Supporto per porte stampante virtuale TCP/IP e named pipe
- Supporto per configurazione predefinita/override in /etc/yajhfc
- Correzioni varie
- Sono ora disponibili i pacchetti RPM e DEB packages per YajHFC.


0.4.2a:
Corretto un difetto che faceva sì che i settaggi non venissero salvati quando non esisteva un
file di configurazione preesistente.

0.4.2:
- Aggiunta traduzione Polacca
- Supporto per la lettura dei destinatari da file di testo
- Lo stato della finestra desiderato può essere specificato da linea di comando
- "Modalità solo invio" senza l'evidenziazione della finestra principale (quando un 
  documento da inviare e né --background né --noclose sono specificati)
- Opzioni arbitrarie di HylaFAX possono essere specificare per i nuovi fax
- Work around per un difetto di Java che faceva sì che YajHFC non salvasse la sua 
  configurazione quando era ancora attivo mentre l'utente chiudeva la sua sessione (log off)
  su Windows 7,  Vista e (a volte) XP
- La copertina fax predefinita è ora in HTML per evitare problemi con caratteri non ISO-8859-1
- Il setup di Windows ora installa opzionalmente tiff2pdf e GhostScript
- Varie altre correzioni, miglioramenti e pulizie del codice

0.4.1:
- Supporto per le liste di distribuzione nella rubrica (solo XML+JDBC)
- Supporto per rubriche CSV (es. per import/export)
- Supporto per impostare l'indicatore "archive" (doneop) per i fax inviati
- Barra di ricerca rapida per i fax nella finestra principale
- Alcune correzioni di errori

0.4.0:
- Aggiunta traduzione Greca
- Supporto per rispondere manualmente ad una chiamata (come faxanswer)
- Aggiornato il checker
- Supporto per la directory 'archive'
- Supporto per visualizzare e inviare i fax come file singolo (in formato PDF, TIFF o PS)
- La rubrica include molti campi tra i più comuni ora
- Supporto per l'icona nell'area di notifica con Java 6 
- Migliorata la finestra opzioni
- Migliorato il supporto per riga di comando (YajHFC può essere ora usato
   per inviare fax senza conferma dell'utente)
- Aggiunto un semplice offuscamento per le password
- Molte pulizie a parti interne del codice

0.3.9:
- Aggiunte traduzioni Italiana e Turca
- "Ricerca rapida" per le rubriche
- Molte correzioni di difetti e piccoli miglioramenti 

0.3.8a:
- Correzioni di errori per le rubriche XML
- Alcuni miglioramenti minori all'interfaccia grafica

0.3.8:
- Migliorata la finestra della rubrica con supporto migliore per rubriche multiple
- Supporto per salvare lo stato letto/da leggere dei fax in una tabella di un database comune
- Pannello grafico per aggiungere driver JDBC e plugin
- Aggiunta la traduzione Russa

0.3.7:
- Nuovo dialogo di invio fax semplificato
- Supporto per copertine fax in formato HTML
- Supporto per copertine fax in formato XSL:FO e ODT usando un plugin
- Miglior supporto per i plugin
- Modificata l'interfaccia di log per usare le API Java
- Ora si usa gnu.hylafax 1.0.1
- Alcune correzioni di errori

0.3.6:
- Alcune correzioni di errori
- Supporto per la selezione di voci multiple di rubrica nel dialogo di invio fax
- Nuovo parametro da riga di comando ("--logfile") per loggare su file in modalità debug
- Supporto per "modalità disconnessa" come possibile soluzione ad alcuni bug di HylaFAX
- Supporto per selezionare il modem da utilizzare

0.3.5:
- Alcuni miglioramenti/raffinamenti dell'interfaccia (es.: barra di avanzamento per la lista fax e più icone)
- Rimosse alcune restrizioni sulle colonne visibili 
  (es.: tolto il vincolo che la colonna ID fax debba essere visibile)
- Nuove operazioni sui fax sono ora eseguire solo *dopo* che i fax non sono più "in corso"

0.3.4a:
- versione per correggere un errore: 
  Nella versione 0.3.4 un carattere aggiuntivo extra veniva aggiunto quando un fax era inviato da
  standard input ad una istanza già attiva

0.3.4:
- Supporto per specificare i destinatari su riga di comando
- Sospensione/Riavvio dei fax in uscita
- Aggiunta traduzione Francese
- L'elenco fax evidenziato inizialmente può essere scelto da riga di comando
- La libreria "ufficiale" gnu.hylafax è ora utilizzata di default (corregge alcuni errori)

0.3.3:
- Supporto per il reinvio dei fax
- Supporto per la stampa degli elenchi con i fax ricevuti/inviati
- Sfondo colorato per i fax falliti
- Aggiunto il supporto per rubriche in sola lettura via LDAP
- Supporto per rubriche multiple aperte
- Il programma di setup Windows installa automaticamente la "stampante fax"

0.3.2:
- Correzione per errori sporadici durante l'invio dei fax
- Supporto per lanciare una nuova istanza in background (utile per le "stampanti fax")
- Alcuni nuovi parametri addizionali per riga di comando
- Pulsante aggiorna
- Il build file di ant è incluso tra i sorgenti distribuiti
- Aggiornamenti della documentazione

0.3.1:
- Look&Feel modificabile
- Opzione per evidenziare i nuovi fax nel visualizzatore in modo automatico
- Aggiunta una opzione nell'interfaccia grafica per modificare gli intervalli di aggiornamento dal server HylaFAX
- E' stato aggiunto un semplice dialogo per la ricerca nella rubrica
- YajHFC dovrebbe ora funzionare anche con la versione "ufficiale" della libreria gnu.hylafax (non testato molto bene!)

0.3.0:
- Aggiunto il supporto per convertire le immagini in PostScript prima di accodare un fax
- Aggiunto un pulsante "anteprima" al dialogo di invio fax
- Aggiunto supporto per rubriche in database SQL (via JDBC)

0.2.7a:
- Correzione per una eccezione che accadeva quando si evidenziavano "solo i propri fax"

0.2.7:
- Aggiunta la traduzione Spagnola
- Aggiunta l'opzione per selezionare la lingua 
- Supporto per aggiungere un offset alle date/orari
- Corretto un errore che causava l'evidenziazione di numeri errati quando si
  inviavano fax multipli
  
0.2.6:
- Piccolo workaround per Windows 9x

0.2.5:
- Piccole correzioni

0.2.4:
- Supporto per i filtri
- Supporto per la modalità amministrativa

0.2.3:
- Supporto per numeri/file multipli per i fax inviati
- Inoltro/salvataggio dei fax
- Modifiche interne

0.2.2:
- Aggiunto supporto per "finestra in primo piano"/beep alla ricezione di nuovi fax

0.2.1:
- Aggiunto lo stato letto/da leggere per i fax ricevuti
- Le rubriche sono ora ordinate alfabeticamente
- Alcune modifiche interne

0.2:
- Aggiunto il supporto per le copertine fax
- Aggiunto il supporto per fax polling

DA FARE
-------

Le seguenti funzionalità potrebbero essere aggiunte in futuro:

* Più traduzioni? (vedere sotto)

YajHFC usa il sistema GNU gettext per le traduzioni, quindi è facile
integrarne di nuove nel programma.
Per il traduttore che crea una nuova traduzione (senza tradurre la 
documentazione) ciò richiede poche ore per il lavoro iniziale e circa
un'ora per ogni nuova versione. Non c'è bisogno di una conoscenza tecnica
"avanzata" per farlo (praticamente c'è solo bisogno di saper lavorare con
un editor di testi, installando software specifico ciò diventa ancora più
facile).

Quindi, se si desidera avere YajHFC tradotto nella propria lingua madre,
i volontari sono sempre ben accetti. ;-)


