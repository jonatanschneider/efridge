# EFridge - Gruppe 5
Ziel des IT-Projektes ist die Erstellung eines verteilten Systems zur Unterstützung zweier neuer Werke der Firma eFridge.com in USA und China.

Weiterführende Informationen sind im [Wiki](https://git.thm.de/verteilte-systeme-2020-efridge/gruppe-5/-/wikis) zu finden.

## Prerequisites
* Docker
* Maven
* Java 14

## Setup
1. Maven Abhängigkeiten (`pom.xml`) installieren 
1. `docker-compose run` im Projektverzeichnis ausführen, um den Message Broker sowie die benötigten Datenbanken zur
Verfügung zu stellen.
1. [HQ](src\main\java\de\thm\mni\vs\gruppe5\hq\Headquarter.java) starten
1. [Factory](src\main\java\de\thm\mni\vs\gruppe5\factory\Factory.java) starten
    * Pro Standort eine Instanz ausführen
    * Konfiguration über [Programmargumente](https://git.thm.de/verteilte-systeme-2020-efridge/gruppe-5/-/wikis/Teilanwendungen/SupportCenter#konfiguration)
1. [SupportCenter](src\main\java\de\thm\mni\vs\gruppe5\support\SupportCenter.java) starten
    * Pro Standort eine Instanz ausführen
    * Konfiguration über [Programmargumente](https://git.thm.de/verteilte-systeme-2020-efridge/gruppe-5/-/wikis/Teilanwendungen/Factory#konfiguration)
1. Lieferanten starten
    * [CoolMechanics](src\main\java\de\thm\mni\vs\gruppe5\supplier\CoolMechanics.java)
    * [ElectroStuff](src\main\java\de\thm\mni\vs\gruppe5\supplier\ElectroStuff.java)

## Usage
Steuerung der Anwendung über die REST-Schnittstelle des `HQ`. Hierzu wird hilfsweise ein [Command Line Interface](https://git.thm.de/verteilte-systeme-2020-efridge/gruppe-5/-/wikis/Command-Line-Interface)
zur Verfügung gestellt.
