auto:
	gcc -Wno-incompatible-pointer-types -shared -o ./libQusideQRNG.so -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I./src -I ./include ./src/QusideQRNG.c -L../lib -lqusideQRNGadmin
	sudo mv ./libQusideQRNG.so /usr/lib

mock:
	gcc -Wno-incompatible-pointer-types -shared -o ./libQusideQRNG.so -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I./src -I ./include ./src/QusideQRNG.c -L../lib -lqusideQRNGminimal
	sudo mv ./libQusideQRNG.so /usr/lib


