## Funktionstest

- Testbeschreibung: 
  - Zuerst wird ein Wetterservice, der als Thrift Server die WeatherAPI implementiert hat, durch Ausführen des Skripts "test_weatherservices.sh" gestartet. 
  - Danach werden eine Wetterstation und vier Sensoren über das Skrit "test_weatherpacket.sh" gestartet. 
  - Jeder Sensor überträgt fünf Datensätze und beendet dann die Datenübertragung. 
  - Danach werden zunächst die Wetterstation und die Sensoren und danach der Wetterservice beendet. 
- Erwartete Testergebnisse: 
  1. Die Datensätze werden vollständig und korrekt an die Wetterstation übertragen und können über den Browser ausgelesen werden. 
  2. Die Wetterstation überträgt die Daten weiter an den Wetterservice. Die Daten in der Datei "./serverData/start/0.txt" stimmen inhaltlich mit den im Browser unter 127.0.0.1:5554/sensors/all/history angezeigten Daten überein. 
  3. Die Datensätze sind trotz Ausfälle der Server vollständig und bei allen Servern gleich
- Ergebnisse: 
  - Bei Abruf der Url /sensors/all/history werden 20 Datensätze angezeigt, dabei stammen jeweils 5 Datensätze von einem Sensor. 
  - In der Datei 0.txt sind ebenfalls 20 Datensätze enthalten, welche den Sensordaten im Browser entsprechen.
  - Die Daten stimmern überein
  
## Performancetest 

- Testbeschreibung:
  - Es wird gemessen, wie lange der Server braucht, um nach einem Ausfall erneut zu starten   
  - Erwartete Testergebnisse: Der Server benötigt maximal 50 ms um neu zu starten
  - Ergebnissse: der Reboot dauert durchschnittlich 42,8 ms
  
Messungen: 

- 27 ms
- 47 ms
- 73 ms
- 38 ms
- 29 ms