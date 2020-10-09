package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    public static String getByteArrayMD5(byte[] inputArr){
        try {
            // get MD5 digest
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            // The input String to Byte Array
            // Updates the digest using the specified byte.
            mDigest.update(inputArr);
            // Completes the hash computation by performing final operations such as padding.
            // The digest is reset after this call is made.
            byte[] resultArr = mDigest.digest();
            //
            return byteArrToHex(resultArr);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static String byteArrToHex(byte[] byteArr) {
        // Initialize the character array, used to store each hexadecimal string
        char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        // Initialize a char Array, used to form the result string
        char[] resultCharArr = new char[byteArr.length*2];
        // Traverse the byte array, converted into characters in a character array
        int index = 0;
        for (byte b : byteArr) {
            resultCharArr[index++] = hexDigits[b>>> 4 & 0xf];
            resultCharArr[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArr);
    }
}
