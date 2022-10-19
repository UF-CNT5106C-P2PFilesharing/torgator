package Logging;

import java.nio.ByteBuffer;

public class Utils {

    /**
     * This method is used to convert integer to byte array
     * @param value - value to convert
     * @return byte array value of integer
     */
    public static byte[] convertIntToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    /**
     * This method is used to convert byte array to integer
     * @param dataInBytes - value to convert
     * @return integer value of byte array
     */
    public static int convertByteArrayToInt(byte[] dataInBytes) {
        return ByteBuffer.wrap(dataInBytes).getInt();
    }
}
