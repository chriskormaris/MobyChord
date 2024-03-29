package gr.aueb.cs.grad.mobychord.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SimpleSHA1 {

    private String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public String SHA1(String text) {
        MessageDigest md = null;
        byte[] sha1hash;
        try {
            md = MessageDigest.getInstance("SHA-1");
            // sha1hash = new byte[40];
            md.update(text.getBytes(StandardCharsets.UTF_8), 0, text.length());
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

}
