package dev.aikido.agent_api.helpers;

import java.io.*;
import java.util.Optional;

public class Serializer {
    public static byte[] serializeData(Object data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(data);
        return bos.toByteArray();
    }
    public static <T> T deserializeData(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (T) ois.readObject();
    }
}
