# deps
apt update 
apt upgrade 
apt install -y git 
apt install -y curl 
apt install -y gradle 
apt install -y zip
apt install -y unzip

# java
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 24.0.2-graalce

# build
git clone https:github.com/sudo-adduser-jordan/protohackers ~/protohackers
gradle -p ~/protohackers build 
 
# run
java -jar app/build/libs/app.jar 
# pkill -f server.jar

