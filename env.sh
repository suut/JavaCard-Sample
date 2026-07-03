#!/bin/false

if [[ -v JAVA_HOME ]]; then
	return
fi

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME:$PATH"
alias gp="$JAVA_HOME/bin/java -jar $HOME/JavaCard/gp.jar"
alias jcard="$JAVA_HOME/bin/java -jar $HOME/JavaCard/jcard.jar"
