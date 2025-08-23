apt update
apt upgrade
apt install git
apt install curl
apt install ufw
apt install unzip
apt install zip
apt install gradle

curl -s "https://get.sdkman.io" | bash
sdk install java 24.0.2-graalce

git clone https:github.com/sudo-adduser-jordan/protohackers
gradle build

sudo ufw allow 8080/tcp
sudo ufw reload
curl ifconfig.me

java -jar your-app.jar
java -jar your-app.jar --host 127.0.0.1 --port 8080

# telnet <your-public-ip> 8080
# or
# curl http://<your-public-ip>:8080

# apt install docker
# docker build -t my-java-app .
# docker run -p 8080:8080 my-java-app

