package util;

import java.security.*;
import java.util.Base64;

public class Crypto {
    public static String sign (String data, PrivateKey privateKey){
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            // 将签名字节数组转换为十六进制字符串表示
            StringBuilder hexString = new StringBuilder();
            for (byte b : signature.sign()) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify (String data,String signature, PublicKey publicKey){
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes());
            // 将十六进制字符串的签名转换为字节数组
            byte[] signatureBytes = new byte[signature.length() / 2];
            for (int i = 0; i < signature.length(); i += 2) {
                String hex = signature.substring(i, i + 2);
                byte b = (byte) Integer.parseInt(hex, 16);
                signatureBytes[i / 2] = b;
            }
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
