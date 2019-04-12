# Aufgabe 2 RPC (Remote Procedure Call)

- Erstellen der Thrift Datei

## Wetterstation

- Thrift API implementieren (server)
- Datenstruktur "currentState"

## Wetterdienst

Sprache: Java \
Wetterdienst kuemmert sich darum, dass er die Daten von den Wetterstationen bekommt

Attribute:

- Name
- IP
- Socket der verbundenen Wetterstation 

- Thrift API implementieren (client)
- Datenbank für persistente Datenspeicherung

Datenstruktur um Status der Wetterstationen abzubilden:

- Name (Wetterdienst)
- aktuelle Messwerte der Sensoren

## Deployment-Script erweitern

- Wetterdienste initialisieren 
- festlegen, von welchen Wetterstationen die Daten empfangen werden sollen

## Tests
