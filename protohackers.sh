#!/bin/bash

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
git clone https://github.com/sudo-adduser-jordan/protohackers ~/protohackers
cd ~/protohackers
# git checkout 970df26b9dcfb0fbac74c266a5915fdbb3a24cb3 # solution 0
# git checkout 970df26b9dcfb0fbac74c266a5915fdbb3a24cb3 # solution 1
./gradlew build

# open port
ufw allow 42069/tcp

# run
# java -jar ~/protohackers/app/build/libs/app.jar 
java -jar ~/protohackers/app/build/libs/app.jar &
# pkill -f server.jar

