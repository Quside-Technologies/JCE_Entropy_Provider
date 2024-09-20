#!/bin/sh
# note: replace "=" with "==" to really override java.security


java -cp .:lib/bc-fips-2.0.0.jar:lib/QusideProvider.jar:lib/bcutil-fips-2.0.3.jar -Djava.security.properties=java.security QusideQrngCheck

