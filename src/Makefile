all: java libinssort.so

java:
	javac -cp junit-4.12 *.java

libinssort.so:
	gcc -I$$JAVA_HOME/include -I$$JAVA_HOME/include/linux -shared -fpic -o libtea.so TEA.c -std=gnu99 -g


clean:
	rm libtea.so *.class -f
