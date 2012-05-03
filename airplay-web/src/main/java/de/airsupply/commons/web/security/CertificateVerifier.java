package de.airsupply.commons.web.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CertificateVerifier {

	private static class SavingTrustManager implements X509TrustManager {

		private X509Certificate[] chain;

		private final X509TrustManager trustManager;

		SavingTrustManager(X509TrustManager trustManager) {
			this.trustManager = trustManager;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			trustManager.checkServerTrusted(chain, authType);
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

	}

	private static final String CACERTS_FILENAME = "cacerts";

	private static final String PASSPHRASE = "changeit";

	private final String host;

	private KeyStore keyStore;

	private final Log log = LogFactory.getLog(getClass());

	private final int port;

	private SavingTrustManager trustManager;

	public CertificateVerifier(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private String getMessage(X509Certificate certificate, String alias) {
		return new StringBuilder().append("Added certificate to keystore 'cacerts' using alias '" + alias + "'")
				.append("\n").append("Issuer:	" + certificate.getIssuerDN()).append("\n")
				.append("Name:	" + certificate.getSigAlgName()).append("\n")
				.append("OID:	" + certificate.getSigAlgOID()).append("\n").append("Type:	" + certificate.getType())
				.toString();
	}

	private SavingTrustManager getTrustManager(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);

		TrustManager[] trustManagers = tmf.getTrustManagers();
		for (TrustManager current : trustManagers) {
			if (current instanceof X509TrustManager) {
				X509TrustManager defaultTrustManager = (X509TrustManager) current;
				return new SavingTrustManager(defaultTrustManager);
			}
		}
		return null;
	}

	private File getTrustStoreFile() {
		File directory = new File(getTrustStoreLocation());
		File file = new File(directory, CACERTS_FILENAME);
		return file;
	}

	private String getTrustStoreLocation() {
		return System.getProperty("java.home") + File.separatorChar + "lib" + File.separatorChar + "security";
	}

	public boolean isTrusted() {
		try {
			if (keyStore == null) {
				keyStore = loadKeyStore();
			}
			if (trustManager == null) {
				trustManager = getTrustManager(keyStore);
			}

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { trustManager }, null);
			SSLSocketFactory factory = context.getSocketFactory();

			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
			socket.setSoTimeout(10000);
			socket.startHandshake();
			socket.close();
			return true;
		} catch (Exception exception) {
			log.error(exception.getMessage(), exception);
			return false;
		}
	}

	private KeyStore loadKeyStore() throws IOException, GeneralSecurityException {
		InputStream inputStream = new FileInputStream(getTrustStoreFile());

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(inputStream, PASSPHRASE.toCharArray());

		inputStream.close();
		return keyStore;
	}

	private void saveKeyStore() throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException {
		OutputStream outputStream = new FileOutputStream(getTrustStoreFile());
		keyStore.store(outputStream, PASSPHRASE.toCharArray());
		outputStream.close();
	}

	public void trustCertificate() throws IllegalArgumentException {
		try {
			if (!isTrusted()) {
				X509Certificate[] chain = trustManager.chain;
				if (chain == null) {
					throw new IllegalArgumentException("Could not obtain server certificate chain");
				}

				for (int i = 0; i < chain.length; i++) {
					X509Certificate certificate = chain[i];

					String alias = host + "-" + (i + 1);
					keyStore.setCertificateEntry(alias, certificate);

					String message = getMessage(certificate, alias);
					log.info(message);
				}
				saveKeyStore();
			}
		} catch (GeneralSecurityException exception) {
			log.error(exception.getMessage(), exception);
			throw new IllegalArgumentException(exception);
		} catch (IOException exception) {
			log.error(exception.getMessage(), exception);
			throw new IllegalArgumentException(exception);
		}

	}

}
