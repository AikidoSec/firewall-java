package dev.aikido.agent_api.helpers.extraction;

import java.nio.charset.StandardCharsets;

public class ByteArrayHelper {
    private ByteArrayHelper() {}

    public record CommandData(String command, byte[] data) {}

    public static CommandData splitByteArray(byte[] byteArray, byte separator) {
        int separatorIndex = -1;

        // Find the index of the separator
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] == separator) {
                separatorIndex = i;
                break;
            }
        }

        // If the separator is not found, return null
        if (separatorIndex == -1) {
            return null;
        }

        // Split the byte array into command and data
        String command = new String(byteArray, 0, separatorIndex, StandardCharsets.UTF_8);
        byte[] data = new byte[byteArray.length - separatorIndex - 1];
        System.arraycopy(byteArray, separatorIndex + 1, data, 0, data.length);

        return new CommandData(command, data);
    }

    public static byte[] joinByteArrays(byte[] array1, byte[] array2) {
        byte[] joinedArray = new byte[array1.length + array2.length];

        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);

        return joinedArray;
    }
}
