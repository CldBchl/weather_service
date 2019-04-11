###Verteilte Systeme Prakitkum
##Anforderungen
#Gesamtsystem
Das Gesamtsystem besteht aus drei Komponenten: Sensoren, Wetterstation und Wetterdienst. Die Sensoren erfassen Wetterdaten, welche über UDP (bzw. später über MQTT) an eine Wetterstation übermittelt werden. Mehrere Wetterstationen (min. 3) senden die gesammelten Sensordaten über RPC an einen Wetterdienst. Der Wetterdienst ist redundant auf mehrere Server verteilt. Außerdem können die Daten der einzelnen Wetterstationen über Http mit einem Webbrowser eingesehen werden.

*Frage: Ein Wetterdienst oder mehrere*

##Komponenten 
#Sensor:
- erfasst entweder Temparatur, Luftfeuchtigkeit, Regen oder Windgeschwindigkeit
- sendet Daten über UDP (MQTT) an Wetterstation
- Implementierung: Python
- jeder Sensor wird in einem eigenen Prozess simuliert 

#Wetterstation:
- mindestens 3 Wetterstationen
- speichert alle empfangenen Daten persistent je Sensor, Zugriff auf alle Daten einzeln möglich 
- Implentierung: Java 

#Wetterdienst:
- muss Anforderungen an Ausfallsicherheit und Performance erfüllen 
- Er ist redundant ausgelegt sein und betreibt min. 3 Server parallel
- jeder Server des Wetterdienst ist mit min. einer Wetterstation verbunden
- empfängt die gesammelten Sensordaten von den Wetterstation über RPC 
- die empfangenen Daten werden persisten gespeichert
*Frage: Welche Empfangslogik liegt zwischen Wetterstation und Wetterdienst*
- Implentierung: Java 

##Systemdesign:
- jede Wetterstation empfängt Daten von je einem Temparatur-, Luftfeuchtigkeits-, Regen- oder Windgeschwindigkeitssensor
- es gibt drei Wetterstationen 
- es gibt drei Wetterdienste 