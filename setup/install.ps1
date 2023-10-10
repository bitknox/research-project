cd beast
mvn clean install -DskipTests
cd ..

docker build -t beast -f ./config/Dockerfile .
