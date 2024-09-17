package com.quside;
import java.security.Provider;

import java.util.Collections;

public class QusideProvider extends Provider {

    private static final String INFO = "Quside QRNG Provider";
    private static final double VERSION = 0.1;
    public static final String NAME = "Quside";

    public QusideProvider() {
        super(NAME, VERSION, INFO);
        putService(new QesService(this));
        putService(new QrngService(this));
    }

    public static String getInfoString() {
        return INFO;
    }

    public synchronized final Service getService(String type, String algorithm) {
        if ("SecureRandom.QES".equals(type + "." + algorithm)) {
            return new QesService(this);
        }

        if ("SecureRandom.QRNG".equals(type + "." + algorithm)) {
            return new QrngService(this);
        }
        return null;
    }

    private static class QesService extends Service {
        public QesService(Provider provider) {
            super(provider, "SecureRandom", "QES", QusideProvider.class.getName() + "$QesService", Collections.emptyList(), Collections.emptyMap());
        }

        public Object newInstance(Object constructorParameter) {
            return new QusideQesSpi();
        }
    }

    private static class QrngService extends Service {
        public QrngService(Provider provider) {
            super(provider, "SecureRandom", "QRNG", QusideProvider.class.getName() + "$QrngService", Collections.emptyList(), Collections.emptyMap());
        }

        public Object newInstance(Object constructorParameter) {
            QusideEntropySource qes = new QusideEntropySource();
            return new QusideQrngSpi(qes);
        }
    }

}
