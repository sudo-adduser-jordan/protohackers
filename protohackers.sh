#!/bin/bash

# deps
apt update 
apt upgrade -y 
apt install -y git 
apt install -y zip
apt install -y unzip
apt install -y curl 

# open port
ufw allow 42069/tcp

# java
curl -s "https://get.sdkman.io" | bash
# source "$HOME/.sdkman/bin/sdkman-init.sh"
source "/root/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.2-graalce
sdk install gradle 9.0.0

# build
git clone https://github.com/sudo-adduser-jordan/protohackers ~/protohackers
cd ~/protohackers
gradle ~/protohackers d2
# gradle ~/protohackers d1
# gradle ~/protohackers d0
# gradle ~/Documents/GitHub/protohackers d2
# git checkout 970df26b9dcfb0fbac74c266a5915fdbb3a24cb3 # solution 0
# git checkout 970df26b9dcfb0fbac74c266a5915fdbb3a24cb3 # solution 1

