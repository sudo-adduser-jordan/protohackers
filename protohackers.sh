apt update
apt upgrade
apt install git
apt install curl
apt install gradle

apt install zip
apt install unzip
curl -s "https://get.sdkman.io" | bash
sdk install java 24.0.2-graalce

git clone https:github.com/sudo-adduser-jordan/protohackers
gradle build protohackers
 
java -jar server.jar &
# pkill server.jar

