# Backlog Termin 1

Datenformat: JSON

## Sensoren

Sprache: Python \
jeder Sensor laeuft in einem eigenem Prozess \
Typen: Temperatur, Luftfeuchtikeit, Regen, Windgeschwindigkeit
Sendeintervall (gesteuert duch Datengenerierungsintervall. Date werden dann direkt gesendet) ()


Attribute:

- Name (PK)
- Typ
- Wert
- Einheit
- IP *(TBD)*
- Port *(TBD)*

Methoden

- Konfiguration des Sockets
- Datensimulationsalgorithmus (sinvolle Werte, morgens kaelter, mittags waermer, abends kaelter)
- Wetterdaten sollten nich zu absurd sein (kein Regen bei 60 Grad C)
- Senden der Daten via UDP
- Fehlerbehandlung

## Wetterstation

Sprache: Java

Attribute:

- Name (PK)
- Socket *(TBD)*

Methoden:

- Konfiguration der Sockets
- SensorDaten auf der Commandozeile ausgeben
- implementieren des HTTP-Servers ( für GET) mit http-Antworten (Header (z.b. mit content-length) und body)
- Website darf beliebig einfach sein
- komplettes Verarbeiten der GET Anfragen
- Antwort 200 oder 404
- implementieren der REST-API: URL's definieren (eine URL pro Sensor und Unterscheidung zw Historie und Echtzeitdaten)
- Speichern der Daten in Postgre-Datenbank
- empfangen der Sensordaten
- verarbeitung der Seonsordaten (parsen und speichern)
- Fehlerbehandlung (insbesondere bei Socketfunktionen)
- Empfangspuffer fuer HTTP-Anfragen (\r\n\r\n suchen ua)


Datenstruktur der Sensordaten JSON:

- Attribute des Sensor Objekts
- Timestamp

## Deployment script


- Sensoren und Wetterstationen initialisieren
- Sensoren und Wetterstationen konfigurieren (Sendeintervall, id's, )
- festlegen, welche Sensoren mit welcher Wetterstation verbunden sind *(?)*

- 4 Sensoren pro Wetterstationen initialisieren 
- 3 Wetterstationen initalisieren (die voneninander Unterscheidbar sind)
- festlegen, welche Sensoren mit welcher Wetterstation verbunden sind

## Tests

- Performance des Webserver testen (z.b. ganz viele curl Nachrichten)
- Wie viele Sensordaten kann die Wetterstation verarbeiten?
