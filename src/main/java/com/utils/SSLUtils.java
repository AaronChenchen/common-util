package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;

public class SSLUtils {
    public static SSLConnectionSocketFactory createSSLConnectionSocketFactory(File file, String password)
            throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(file);
        try {
            trustStore.load(instream, password.toCharArray());
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        return sslsf;
    }

    public static SSLConnectionSocketFactory createSSLConnectionSocketFactory(InputStream in, String password) {
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(in, password.toCharArray());
            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                    .build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" },
                    null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            return sslsf;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static SSLConnectionSocketFactory createSSLConnectionSocketFactory(String file) {
        InputStream in = null;
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            in = SSLUtils.class.getResourceAsStream(file);
            trustStore.load(in, "123456".toCharArray());
            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                    .build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" },
                    null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            return sslsf;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static SSLContext gettrustAllSSLContext() {
        try {
            return new SSLContextBuilder().loadTrustMaterial(null, new TrustAllStrategy()).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class TrustAllStrategy implements TrustStrategy {
        // 信任所有
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }
    }

    // public final static SSLConnectionSocketFactory TRUAT_ALL_SSLSF = new
    // SSLConnectionSocketFactory(
    // gettrustAllSSLContext(), new AllowAllHostnameVerifier());
    public final static SSLConnectionSocketFactory TRUAT_ALL_SSLSF = new SSLConnectionSocketFactory(
            gettrustAllSSLContext(), new AllowAllHostnameVerifier());
}
