LISEZ MOI for YajHFC 0.5.0
==========================

A PROPOS DE
-----------

YajHFC (Yet Another Java HylaFAX Client) est un client pour le serveur de fax Hylafax
(http://www.hylafax.org/) ecrit en Java.

Caracteristiques:
* Faxer des documents au format PostScript ou PDF et de nombreux autres formats
* Reception de faxes
* Support de la generation de pages de couvertures depuis des modeles
* Visualiser les faxs envoyes et recus
* Carnet d'adresse (les entrée peuvent provenir d'annuaire LDAP ou base SQL)
* Parametrage des colonnes affichees par boite de dialogue
* Support de 10 langues : Chinois,Anglais, Allemand, Espagnol,Francais,Grec,Italien, Polonais, Russe,Turc

Page d'accueil: http://www.yajhfc.de/
email:    Jonas Wolz <info@yajhfc.de>

INSTALLATION
------------

Pres requis:
* JRE 5.0 ou superieur (Java 6 recommande)
* Un serveur Hylafax operationnel sur votre reseau (bien sur ;-) )
* Un programme pour visualiser les fichiers TIFF (voir aussi les FAQ)
* Optionnel: Un visualiseur Postscript (e.g. Ghostview), GhostScript
     et tiff2pdf (librairie libtiff-utils)

Pour Installer YajHFC Telechargez le fichier yajhfc-0_5_0.jar dans un repertoire
sur votre machine.
Pour l'executer, utilisez la commande: "java -jar yajhfc-0_5_0.jar"
(Sous Windows vous pouvez habituellement double cliquer sur le fichier jar.)

LICENCE
-------

YajHFC est un logiciel libre sous licence GPL.
Visualisez le fichier COPYING pour plus de details.

CODE SOURCE
-----------
Pour les instructions decrivants comment compiler YajHFC depuis les sources
SVP visualisez le fichier BUILDING.txt dans ce repertoire.
YajHFC utilise les paquets suivants :
(1) La librairie gnu.hylafax (paquets core et inet-ftp)
	depuis http://gnu-hylafax.sourceforge.net
(2) TableLayout.jar
    depuis https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar)
    depuis http://java.sun.com/developer/techDocs/hi/repository/
(4) Apache Commons Logging depuis http://commons.apache.org/logging/
    en tant que dependance de (1)

Des copies des fichiers requis pur (2), (3) et (4) peuvent etre trouves
dans le sous repertoire jar de l'archive des sources.

YajHFC a ete originellement ecrit comme un premier projet Java
(plus que les programmes "Hello world" avances) plus complexe utilisant l'IDE Eclipse.

MODIFICATIONS
-------------
0.5.0:
- Support d'un cache local de la liste de faxs, certainement un demarrage de l'application plus rapide
- Support d'un acces direct aux files d'attente recvq et doneq Experimental qui outrepasse le serveur Hylafax (et l'authetification). 
	Cela devrait permettre une charge moindre du serveur pour les grandes files comme seulement les fichiers modifies seront mis a jour. Cela demande cependant des tests, mais .... 
	(tout retour sur cette fonction et la bienvenue !)
- Acceleration de l'affichage de la boite de dialogue des options
- Bouton "Test connection" dans le dialogue d'ouverture
- Support de plusieur serveurs
- Support de plusisuers identites
- Listes des faxs peut etre sauvee au format CSV, HTML or XML 
- Support de raccourcis claviers configurables


0.4.4:
- Amelioration du support MAC OS (Principalement des changements graphiques)
- Support de convertion de fichier personalisee
- Acces aux parametres avances en mode graphique
- Support de l'impression des annuaires
- Amelioration de l'impression des faxs
- Support du tag @@CCNameAndFax@@ dans les pages de garde HTML
- Renvoi des fax pour plusieurs destinataires
- Console Log pour voir les logs en temps reel
- Separation du format d'affichage et d'envoi des faxs
- Liste de modems editable par l'utilisateur
- nouveau "override-setting" parametre de ligne de commande
- Corrections de Bugs



0.4.3:
- "Visualiser log" pour les fax envoyés
- Le message de bas de page peut être inibé
- Filtres pour le carnet d'adresse
- Support d'une "resolution Etendue" (i.e. USEXVRES=yes)
- Nouveaux paramètres de ligne de commande : --modem
- Support des ports d'imprimante virtuelle TCP/IP et named pipe
- Support de surcharge de configurations par default dans /etc/yajhfc
- Quelques corrections de bugs
- Pacquets RPM et DEB de YajHFC maintenat disponibles

0.4.2a:
Correction du bug d'enregistrement des options quand aucune ancienne version d'options n'existait.

0.4.2:
- Traduction polonaise ajouté
- Support de la lecture des récipiendaires dans un fichier texte
- L'état de la fenêtre peut être spécifié dans la ligne de commande
- "mode envoi seulement" sans affichage de la fenêtre principale (Quand un
  document est envoyé et ni --background ou --noclose n'est spécifié)
- Des options Hylafax peuvent être spécifiés pour les nouveaux travaus de Fax
- Contournement du bug java empechant YajHFC d'enregistrer ses paramètres quand 
  il est en cours et que l'utilisateur quite windows sous Windows 7, Vista et (quelquefois) XP
- La page de garde par défaut est maintenant en HTML pour éliminer les problèmes avec les jeux de caractères non ISO-8859-1
- Le programme d'installation peut de façon optionnelle installer tiff2pdf et GhostScript
- D'autres corrections de bug, amélioration et nettoyage de code

0.4.1:
- Support des listes de diffusion pour les carnet d'adresse (XML+JDBC seulement)
- Support des carnets d'adresse au format CSV (e.g. pour import/export)
- Support d'activation du "archive flag" (doneop) pour les faxes envoyes
- Barre de recherche rapide de Faxs dans la fenetre principale
- Quelques corrections de Bugs

0.4.0:
- Installation en grec ajoutee
- Support pour ajouter manuellement un appel telephonique
- Mise a jour automatique
- Support d'un repertoire d'archive
- Support de lavisualisation et de l'envoi de fax comme un seul fichier (format PDF, TIFF ou PS)
- Le carnet d'adresse inclus plus de champs classiques maintenant
- Supprot d'un barre d'icones en Java 6
- Boite de dialogue d'options amelioree 
- Ameliration du support de ligne de commande (YajHFC peut maintenant etre 
	utilise pour envoyer des fax sans confirmation utilisateur)
- Quelques codages simple de mots de passe 
- Beaucoup de nettoyage de code



0.3.9:
- Traduction italiennes et turques ajoutees
- "Recherche rapide" Pour les carnets d'adresse
- Beaucoup de corections de bugs et quelques ameliorations

0.3.8:
- Amelioration de la fenetre d'annuaire avec un meilleur support du multi annuaire
- Support de l'nregistrement de l'etat lu/non lu des faxs dans une base de donnee centrale.
- Panneau graphique pour ajouter des pilotes JDBC et des modules

0.3.7:
- Nouvelle boite de dialoguen d'envoie simplifiee
- Support des pages de garde au format HTML
- Support des pages de garde au format XSL:FO et ODT avec l'utilisation d'un module (plugin)
- Meilleur support des modules (plugin)
- Gestion des logs modifiée pour utiliser les APIs Java
- Maintenant utilise gnu.hylafax 1.0.1
- Correction de bugs

0.3.6:
- Correction de bugs
- Support de la selection multiple sur les contacts pour la boite d'envoi
- Nouvel argument de ligne de commande ("--logfile") pour tracer un fichier en mode debug
- Suport du "mode deconnecte" comme possible contournement de certains bugs Hylafax
- Support du choix du modem a utiliser

0.3.5:
- Ameliorations de l'interface utilisateur /"polissage" (i.e. bare de progression pour la liste de faxs et plus d'icones)
- Supprimee quelques restrictions sur les colonne visibles
  (i.e.: L'identifiant de travail n'a plus a etre visible)
- De nouvelle actions sont maintenant executees seulement "apres" que les fax ne soient plus "en cours"

0.3.4a:
- Correction de bugs:
  dans la version 0.3.4 un caractere etait ajouté en fin  quand un fax etait soumis par
  l'entree standard a une instance en cours d'execution

0.3.4:
- Support de la specification du recipiendaire en ligne de commande
- Suspendre/Reprendre les travaux de fax
- Traduction francaise ajoutee
- Onglet affiche au demarrage peut etre specifie en ligne de commande
- La version "standard" de la librairie gnu.hylafax est maintenant utilisee par defaut (cela corrige certains bugs)

0.3.3:
- Support du renvoi de faxs
- Impression des tables faxs recus/envoyes supporte
- Fond en couleur pour les travaux echoues
- Annuaire telephonique LDAP supporte en lecture seule
- Support de l'ouverture de plusieurs annuaires telephoniques
- Programme d'installation automatique Windows avec une "imprimante fax"

0.3.2:
- Correction de bug pour des erreur sporadiques lors de l'envoie de fax
- Support du lancement d'une nouvelle instance en tache de fond (Utile pour "imprimantes fax")
- Quelques parametres de ligne de commande supplementaires
- Bouton de rafraichissement de l'ecran
- Fichier de compilation ant inclu dans la distribution des sources
- Mise à jour de la documentation

0.3.1:
- Apparence modifiable
- option pour afficher les nouveaux faxs automatiquement dans un visualiseur
- UI option pour modifier l'intervalle de mise à jour de la boite de reception du serveur HylaFAX
- Boite de dialogue de recherche dans l'annuaire telephonique
- YajHFC devrait maintenant fonctionner avec la version officielle de la bibliotheque gnu.hylafax (Pas entierement teste!)

0.3.0:
- Support de la convertion des images en postscript avant d'envoyer un fax
- Bouton de pre visualisation ajoute à la boite de dialogue d'envoi
- Support des annuaires dans des bases SQL (utilsation de JDBC)

0.2.7a:
- Correction de Bug pour une exception qui apparaissait a l'affichage "uniquement mes faxs"

0.2.7:
- Ajout de la tracduction espagnole
- Ajout d'une option de selection de langue
- Support du decalage horaire pour les valeurs dates
- Correction de Bug qui affichait un mauvais numero lors de l'envoi des faxs multiples


0.2.6:
- Petit changements pour windows 98

0.2.5:
- Petites corections de Bugs

0.2.4:
- Support des filtres
- Support du mode administrateur

0.2.3:
- Support des envois multiples , numeros/fichiers
- Renvois/Sauvegarde des faxs
- Changements internes

0.2.2:
- Support "Fenetre toujours visible"/bip sur arrivee de fax

0.2.1:
- Statut Lu/Non lupour les fax recus
- Les anuaires sont maintenant tries
- Quelques changements internes

0.2:
- Support des page de garde de fax
- Support de la file d'envoi de fax

A FAIRE
-------

Les fonctions suivantes devraient etre ajoutees dans le futur:

* Plus de traductions? (Voir ci dessous)

YajHFC utilise GNU gettext pour les traductions, aussi, il est facile d'integrer
d'autres langues dans le programme.
Pour les traducteurs qui creent une nouvelle traduction (sans traduire la documentation)
cela demandera quelques heures pour le travail initial et approxivement
une heure pour chaque nouvelle version.
Vous n'avez pas besoin de competences techniques "avancees" pour faire cela
(Basiquement vous avez seulement besoin de savoir utiliser un editeur de texte,
en installant un logiciel specialise, c'est un peu plus facile).

Aussi, si vous voulez avoir YajHFC traduit dans votre langue,
les volontaires sont toujours les bienvenus. ;-)
