LISEZ MOI for YajHFC 0.4.1
==========================

A PROPOS DE
-----------

YajHFC (Yet Another Java HylaFAX Client) est un client pour le serveur de fax Hylafax
(http://www.hylafax.org/) ecrit en Java.

Caracteristiques:
* Faxer des documents au format PostScript ou PDF
* Reception de faxes
* Support de la generation de pages de couvertures depuis des modeles
* Visualiser les faxs envoyes et recus
* Carnet d'adresse
* Parametrage des colonnes affichees par boite de dialogue
* Support de quatres langues : Anglais, Allemand, Espagnol and Francais

Page d'accueil: http://www.yajhfc.de.vu/
email:    Jonas Wolz <jwolz@freenet.de>

INSTALLATION
------------

Pres requis:
* JRE 5.0 ou superieur (Java 6 recommande)
* Un serveur Hylafax operationnel sur votre reseau (bien sur ;-) )
* Un programme pour visualiser les fichiers TIFF (voir aussi les FAQ)
* Optionnel: Un visualiseur Postscript (e.g. Ghostview), GhostScript
     et tiff2pdf (librairie libtiff-utils)

Pour Installer YajHFC Telechargez le fichier yajhfc-0_4_1.jar dans un repertoire
sur votre machine.
Pour l'executer, utilisez la commande: "java -jar yajhfc-0_4_1.jar"
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
