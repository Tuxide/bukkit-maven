package com.erdfelt.bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipeHandler extends Thread {
    private static final int   BUFSIZE = 1024;
    private final InputStream  in;
    private final OutputStream out;

    public PipeHandler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[BUFSIZE];
            for (int n = 0; n != -1; n = in.read(buffer)) {
                out.write(buffer, 0, n);
                out.flush();
            }
        } catch (IOException e) {
            if (!e.getMessage().equalsIgnoreCase("Stream closed")) {
                e.printStackTrace();
            }
        }
    }
}