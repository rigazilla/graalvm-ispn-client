if [ x$GRAALVM_HOME == x ]; then
echo Please set GRAALVM_HOME
exit
fi

PATH=$GRAALVM_HOME/bin:$PATH
JAVA_HOME=$GRAALVM_HOME

mvn clean
#mvn -Pnative -Dagent exec:exec@java-agent
mvn -Pnative -Dagent -DskipTests package
target/example-app