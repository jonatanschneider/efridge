start "Docker" cmd /K docker-compose up
pause
start "Headquarter" cmd /K mvn exec:java@HQ
start "Factory US" cmd /K mvn exec:java@US
start "Factory CN" cmd /K mvn exec:java@CN
start "Support Center IN" cmd /K mvn exec:java@IN
start "Support Center MX" cmd /K mvn exec:java@MX
start "Supplier Cool Mechanics" cmd /K mvn exec:java@CM
start "Supplier Electro Stuff" cmd /K mvn exec:java@ES
start "eFridge CLI" cmd /K mvn exec:java@CLI