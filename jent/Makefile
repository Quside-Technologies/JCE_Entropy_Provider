auto:
	gcc -Wno-incompatible-pointer-types -shared -o ./libQusideQRNGjent.so -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I./src -I ./include ./src/QusideQRNG.c -L../lib -lqusideQRNGadmin -lqusideQRNG
	sudo mv ./libQusideQRNGjent.so /usr/lib

mock:
	gcc -Wno-incompatible-pointer-types -shared -o ./libQusideQRNGjent.so -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I./src -I ./include ./src/QusideQRNG.c -L../lib -lqusideQRNGminimal
	sudo mv ./libQusideQRNGjent.so /usr/lib


