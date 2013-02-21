all: cscTranslator.java
	javac -Xlint cscTranslator.java
	jar cvfm cscTranslator.jar manifest.txt cscTranslator*.class
	chmod 755 cscTranslator.jar

clean:
	rm -f *.jar *.class *~ *.bak *.BAK
