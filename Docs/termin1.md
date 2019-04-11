## Sensoren
Sprache: Python \
jeder Sensor laeuft in einem eigenem Prozess \
Typen: Temperatur, Luftfeuchtikeit, Regen, Windgeschwindigkeit

Attribute:
- Name (PK)
- Typ
- Wert
- Einheit
- IP *(TBD)*
- Port *(TBD)*

Methoden
- Konfiguration des Sockets
- Datensimulationsalgorithmus
- Senden der Daten via UDP



## Wetterstation:
Sprache: Java\
Attribute:
- Name (PK)
- Socket *(TBD)*

Methoden:
- Konfiguration der Sockets
- implementieren des HTTP-Servers
- implementieren der REST-API
- Speichern der Daten in DB
- empfangen von Daten

Datenstruktur der Sensordaten:
- Attribute des Sensor Objekts
- Timestamp

Tests: 
- 

