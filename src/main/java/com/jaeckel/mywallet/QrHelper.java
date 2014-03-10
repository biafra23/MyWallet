package com.jaeckel.mywallet;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class QrHelper {


    static void encodeAndWriteQrCode(String uri, int widthHeight) {
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetEncoder encoder = charset.newEncoder();
        byte[] b = null;
        try {
            // Convert a string to ISO-8859-1 bytes in a ByteBuffer
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(uri));
            b = bbuf.array();
        } catch (CharacterCodingException e) {
            System.out.println(e.getMessage());
        }

        String data = null;
        try {
            data = new String(b, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

        // get a byte matrix for the data
        BitMatrix matrix = null;
        int h = widthHeight;
        int w = widthHeight;
        com.google.zxing.Writer writer = new QRCodeWriter();
        try {
            matrix = writer.encode(data,
                    com.google.zxing.BarcodeFormat.QR_CODE, w, h);
        } catch (com.google.zxing.WriterException e) {
            System.out.println(e.getMessage());
        }

        String filePath = "qr.png";
        File file = new File(filePath);
        try {
            MatrixToImageWriter.writeToFile(matrix, "PNG", file);
            System.out.println("printing to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
