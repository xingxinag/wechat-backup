package com.example.myxposedmodule.utils;

import android.util.Base64;
import java.io.*;
import java.security.SecureRandom;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CompressionUtils {
    private static final int BUFFER_SIZE = 8192;
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    
    public static void compressAndEncrypt(File sourceDir, File destFile, String password) throws Exception {
        // 生成加密密钥
        byte[] salt = generateSalt();
        SecretKey key = generateKey(password, salt);
        byte[] iv = generateIV();
        
        try (FileOutputStream fos = new FileOutputStream(destFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            
            // 写入salt和iv
            bos.write(salt);
            bos.write(iv);
            
            // 创建加密cipher
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            
            // 创建加密输出流
            try (CipherOutputStream cos = new CipherOutputStream(bos, cipher);
                 ZipOutputStream zos = new ZipOutputStream(cos)) {
                
                // 设置压缩级别
                zos.setLevel(Deflater.BEST_COMPRESSION);
                zipDirectory(sourceDir, "", zos);
            }
        }
    }
    
    private static void zipDirectory(File dir, String path, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        File[] files = dir.listFiles();
        
        if (files != null) {
            for (File file : files) {
                String entryPath = path + file.getName();
                if (file.isDirectory()) {
                    zos.putNextEntry(new ZipEntry(entryPath + "/"));
                    zipDirectory(file, entryPath + "/", zos);
                } else {
                    zos.putNextEntry(new ZipEntry(entryPath));
                    try (FileInputStream fis = new FileInputStream(file);
                         BufferedInputStream bis = new BufferedInputStream(fis)) {
                        int count;
                        while ((count = bis.read(buffer)) != -1) {
                            zos.write(buffer, 0, count);
                        }
                    }
                }
                zos.closeEntry();
            }
        }
    }
    
    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    
    private static byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    
    private static SecretKey generateKey(String password, byte[] salt) throws Exception {
        // 使用PBKDF2或其他密钥派生函数
        // 这里简化处理，实际应用中应该使用更安全的方法
        byte[] key = (password + Base64.encodeToString(salt, Base64.NO_WRAP)).getBytes();
        return new SecretKeySpec(key, "AES");
    }
} 