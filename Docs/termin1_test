# Testdoku Termin 1

## Funktionstest

- Testbeschreibung: Eine Wetterstation und vier Sensoren werden über das Skrit "test_weatherpacket.sh" gestartet. Jeder Sensor überträgt fünf Datensätze und beendet dann die Datenübertragung. 
- Erwartete Testergebnisse: Die Datensätze werden vollständig und korrekt an die Wetterstation übertragen und können über den Browser ausgelesen werden.
- Ergebnisse: Bei Abruf der Url /sensors/all/history werden 20 Datensätze angezeigt, dabei stammen jeweils 5 Datensätze von einem Sensor. Bei Abruf der einzelnen Sensoren werden jeweils nur 5 Datensätze angezeigt.


## Performancetest

- Testbeschreibung: Es soll über Apache Bench getestet werden, wieviel Requests pro Sekunde bearbeitet werden können und ob der Server auch parallele Anfragen verarbeiten kann.
Dafür führen wir den Befehl "ab -n 5000 -c 100 127.0.0.1:5554/"
- Erwartete Testergebnisse: Wir erwarten 10'000 Requests/ pro Sekunde und erwarten, dass unser Server bis zu 50 gleichzeitige Anfragen verarbeiten kann. 
- Ergebnisse: Die Erwartungen bezüglich der Performance werden nicht erfüllt, denn wir schaffen nur rund 2'000 Requests in der Sekunde. Unsere Erwartungen bezüglich Concurrency werden erfüllt. 

Server Software:        
Server Hostname:        127.0.0.1
Server Port:            5554

Document Path:          /
Document Length:        129 bytes

Concurrency Level:      50
Time taken for tests:   2.300 seconds
Complete requests:      5000
Failed requests:        0
Total transferred:      1040000 bytes
HTML transferred:       645000 bytes
Requests per second:    2174.26 [#/sec] (mean)
Time per request:       22.996 [ms] (mean)
Time per request:       0.460 [ms] (mean, across all concurrent requests)
Transfer rate:          441.65 [Kbytes/sec] received

Connection Times (ms)
              min  mean[+/-sd] median   max
Connect:        0    0   0.4      0       8
Processing:     2   23   3.6     22      46
Waiting:        1   22   3.6     22      46
Total:          8   23   3.5     22      46

Percentage of the requests served within a certain time (ms)
  50%     22
  66%     23
  75%     24
  80%     25
  90%     27
  95%     29
  98%     33
  99%     36
 100%     46 (longest request)


