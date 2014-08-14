package com.payneteasy.loggingextensions.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class SerializationUtilsTest {

    @Test
    public void testSerializeObjectToBytes() throws Exception {
        Container before = new Container(1, "abc");
        byte[] bytes = SerializationUtils.serializeObjectToBytes(before);
        Container after = (Container) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        Assert.assertEquals(before, after);
    }

    private static class Container implements Serializable {
        private final int a;
        private final String b;

        private Container(int a, String b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Container container = (Container) o;

            if (a != container.a) return false;
            if (b != null ? !b.equals(container.b) : container.b != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = a;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            return result;
        }
    }
}