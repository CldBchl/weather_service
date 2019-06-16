## Funktionstest

- Testbeschreibung: 
  - Mit diesem Test sollt die Funktionalität des neu integrierten MQTT-Brokers getestet werden. Dies umfasst die erfolgreiche Subscription der Wetterstationen  sowie das Publishing von Sensordaten. 
  - Ziel ist es, die Demo-Daten von insgesamt 12 Sensoren an vier Wetterstationen per MQTT zu übermitteln. Jeder Sensor schickt 5 Datensätze. Diese Daten sollen dann über eine REST API sowie im Datenspeicher der Wetterservices auslesbar sein. 
- Testdurchführung:
  1. Das Programm mit installDist bauen.
  2. Den MQTT Broker über docker-compose up starten
  3. Die Wetterservices über "test_weatherservices.sh" starten
  4. Die Wetterstationen über "test_weatherpacket.sh" starten
  5. Beenden der Wetterstationen, dann beenden der Wetterservices. 
- Erwartete Testergebnisse: 
  - Die Datensätze werden vollständig und korrekt an die Wetterstation übertragen und können über den Browser ausgelesen werden. 
  - Die Wetterstation überträgt die Daten weiter an den Wetterservice. Die Daten in den Server-Dateien "./serverData/" stimmen mit den im Browser unter 127.0.0.1:5554/sensors/all/history angezeigten Daten überein. 
  - Die Datensätze sind trotz Ausfälle der Server vollständig und bei allen Servern gleich. 
- Ergebnisse: 
  - Bei Abruf der Url /sensors/all/history werden 60 Datensätze angezeigt, dabei stammen jeweils 5 Datensätze von einem Sensor. 
  - In jeder der Dateien 0.txt, 1.txt, 2.txt sind jeweils 20 Datensätze enthalten, welche zusammen den Sensordaten im Browser entsprechen.
  - Die Daten stimmen überein.
  
## Performancetest 

- Testbeschreibung:
  - Es wird gemessen, wie lange der Versand der Sensordaten über den MQTT Broker dauert. Es wird die Zeitspanne zwischen Versand und Erhalt eines Sensor-Datenpakets gemessen. 
- Testdurchführung:  
  - Im Testszenario läuft der MQTT-Broker auf einem AWS EC2 t2.micro Instanz. Die Sensoren und die Wetterstation laufen lokal auf dem selben Rechner. Es werden insgesamt 60 Datenpakete von 12 Sensoren an 4 Wetterstationen übertragen. 
- Erwartete Testergebnisse: : Die Datenübertragung dauert maximal 500 ms.
- Ergebnissse: 
  - Zu Beginn der Datenübertragung brauchen zwei Pakete über 500 ms, vermutlich wird die Übertragung durch die Initialisierung der Komponenten beeinträchtigt.
  - Im Durchschnitt dauert die Übertratung mit 111, 75 ms aber deutlich kürzer als 500 ms. 
  - Die Messergebnisse sind im File "MQTT_Transmission_Time" dokumentiert.
