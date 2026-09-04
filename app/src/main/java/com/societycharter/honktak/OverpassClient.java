package com.societycharter.honktak;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Foreground-invoked read-only Overpass client; no polling or write methods. */
public final class OverpassClient {
    static final String[] ENDPOINTS = {"https://overpass.deflock.org/api/interpreter", "https://overpass-api.de/api/interpreter"};
    private OverpassClient() { }
    private static volatile HttpURLConnection activeConnection;
    public static void cancelActive() { HttpURLConnection c=activeConnection; if(c!=null)c.disconnect(); }
    public static List<OverpassImport.Camera> load(String query) throws Exception {
        Exception last = null;
        for (String endpoint : ENDPOINTS) {
            for (int attempt=0; attempt<2; attempt++) {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Import cancelled");
                try { return request(endpoint, query); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw e; }
                catch (Exception e) { last=e; }
                if (attempt==0) Thread.sleep(250L);
            }
        }
        throw last == null ? new IllegalStateException("No Overpass endpoint") : last;
    }
    static List<OverpassImport.Camera> request(String endpoint, String query) throws Exception {
        byte[] body=("data="+URLEncoder.encode(query,"UTF-8")).getBytes(StandardCharsets.UTF_8);
        HttpURLConnection c=(HttpURLConnection)new URL(endpoint).openConnection();
        activeConnection=c;
        try {
        c.setRequestMethod("POST"); c.setConnectTimeout(8_000); c.setReadTimeout(18_000);
        c.setDoOutput(true); c.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        c.setRequestProperty("User-Agent", "HonkTAK/0.2.12 (read-only OSM import; https://github.com/SocietyCharter/HonkTAK)");
        c.setFixedLengthStreamingMode(body.length);
        try (OutputStream output=c.getOutputStream()) { output.write(body); }
        if (c.getResponseCode()!=200) throw new java.io.IOException("Overpass HTTP "+c.getResponseCode());
        try (InputStream in=c.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[8192]; int total=0,n;
            while ((n=in.read(buffer))!=-1) { if(Thread.currentThread().isInterrupted()) throw new InterruptedException("Import cancelled"); total+=n; if(total>OverpassImport.MAX_RESPONSE_BYTES) throw new java.io.IOException("Overpass response too large"); out.write(buffer,0,n); }
            return OverpassImport.parse(out.toByteArray());
        }
        } finally { c.disconnect(); if(activeConnection==c)activeConnection=null; }
    }
}
