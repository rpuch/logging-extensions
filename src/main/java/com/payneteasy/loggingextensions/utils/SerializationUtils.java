package com.payneteasy.loggingextensions.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author rpuch
 */
public class SerializationUtils {
    public static byte[] serializeObjectToBytes(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        return baos.toByteArray();
    }
}
