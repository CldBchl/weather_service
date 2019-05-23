# Testdoku Termin 2

## Funktionstest

- Testbeschreibung: 
  - Zuerst wird ein Wetterservice, der als Thrift Server die WeatherAPI implementiert hat, durch Ausführen des Skripts "test_weatherservices.sh" gestartet. 
  - Danach werden eine Wetterstation und vier Sensoren über das Skrit "test_weatherpacket.sh" gestartet. 
  - Jeder Sensor überträgt fünf Datensätze und beendet dann die Datenübertragung. 
  - Danach werden zunächst die Wetterstation und die Sensoren und danach der Wetterservice beendet. 
- Erwartete Testergebnisse: 
  1. Die Datensätze werden vollständig und korrekt an die Wetterstation übertragen und können über den Browser ausgelesen werden. 
  2. Die Wetterstation überträgt die Daten weiter an den Wetterservice. Die Daten in der Datei "./serverData/start/0.txt" stimmen inhaltlich mit den im Browser unter 127.0.0.1:5554/sensors/all/history angezeigten Daten überein. 
- Ergebnisse: 
  - Bei Abruf der Url /sensors/all/history werden 20 Datensätze angezeigt, dabei stammen jeweils 5 Datensätze von einem Sensor. 
  - In der Datei 0.txt sind ebenfalls 20 Datensätze enthalten, welche den Sensordaten im Browser entsprechen. 

## Performancetest

- Testbeschreibung: 
  - Das Ziel des Test ist es zu bestimmen, wieviele Stationen und deren Requestst vom Wetterdienst (und insbesondere der Thrift-Server) in einer akzeptablen Bearbeitungszeit bedient werden können. 
  - Wir messen hierzu die Verarbeitungszeit der "sendWeatherReport" Methode und erhöhen die Anzahl der gestarteten Wetterstationen bei jedem Durchgang auf jeweils 3, 6, 12, 24 Wetterstationen.
- Erwartete Testergebnisse: Wir erwarten, dass die Bearbeitungszeit nicht merklich ansteigt, da auch die internen Prozesse der Wetterstationen (Sensordatenempfangen und verarbeiten) die Frequenz der Anfrage drosseln. 
- Ergebnisse: 
  - Im Dokument "termin2_MessdatenPerformancetest.pdf" sind die Bearbeitungszeiten der verschiedenen Versuche festgehalten. 
  - Wie erwartet ist keine Erhöhung der Bearbeitungszeit zu beobachten. Überraschenderweise ist die Bearbeitung bei 20 Stationen sogar zügiger als bei weniger Stationen.
  - Es hat sich allerdings gezeigt, dass nicht der Server sondern das Starten der Stationen das Bottleneck ist: das Starten von 24 Stationen haben wir abgebrochen, da die Initialisierung auch nach über einer Minute noch nicht abgeschlossen war. Deswegen wurde der Versuch mit 20 Stationen durchgeführt. 


