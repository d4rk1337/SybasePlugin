package de.hsheilbronn.cordova.sybaseplugin;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ianywhere.ultralitejni16.ConfigFileAndroid;
import com.ianywhere.ultralitejni16.Connection;
import com.ianywhere.ultralitejni16.DatabaseManager;
import com.ianywhere.ultralitejni16.SyncParms;
import com.ianywhere.ultralitejni16.SyncResult;
import com.ianywhere.ultralitejni16.ULjException;

public class Sybase extends CordovaPlugin {

	private static final String TAG = "Sybase";
	private static final String DATABASE_FILE = "remote.udb";

	private static final String ACTION_SYNC = "sync";

	/*
	 * Cordova member
	 */
	private CallbackContext callbackContext;

	/*
	 * Sybase member
	 */
	private Connection mConn = null;
	private ConfigFileAndroid mDB = null;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		boolean result = true;

		// Save callbackContext for ActivityResult or other life cycle methods.
		this.callbackContext = callbackContext;

		// Open database
		this.GetDatabase(DATABASE_FILE);

		// Execute
		if (ACTION_SYNC.equals(action)) {
			this.sync.execute("");
		} else {
			result = false;
		}

		// Close database
		this.CloseDatabase();

		// Handle result
		if (result) {
			callbackContext.success();
			return true;
		} else {
			return false;
		}
	}

	public Connection GetDatabase(String strFilename) {

		try {
			mDB = DatabaseManager.createConfigurationFileAndroid(strFilename, this.webView.getContext());
			mConn = DatabaseManager.connect(mDB);
		} catch (ULjException exceptionConnect) {
			if (mDB != null) {
				try {
					mConn = DatabaseManager.createDatabase(mDB);
				} catch (ULjException exceptionCreateDatabase) {
					Log.e(TAG, exceptionCreateDatabase.getMessage());
				}
			}
			Log.e(TAG, exceptionConnect.getMessage());
			exceptionConnect.printStackTrace();

			this.callbackContext.error("Error during synchronization: " + exceptionConnect.getMessage());
		}
		return mConn;
	}

	public void CloseDatabase() {
		try {
			mConn.release();
		} catch (ULjException exception) {
			Log.e(TAG, exception.getMessage());
			exception.printStackTrace();

			this.callbackContext.error("Error during synchronization: " + exception.getMessage());
		}
	}

	private void sync() {
		try {
			// SyncParms syncParms = mConn.createSyncParms(SyncParms.HTTP_STREAM, "u1", "Demo_AddressMandantText");
			// SyncParms syncParms = mConn.createSyncParms("u1", "Demo_AddressMandantText");
			SyncParms syncParms = mConn.createSyncParms("Demo", "Demo_AddressText");
			syncParms.getStreamParms().setHost("192.168.178.34");
			syncParms.getStreamParms().setPort(5555);
			syncParms.setUserName("Demo");
			syncParms.setPassword("123");

			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

			Log.e("SYNC", "##########################################################");
			Log.e("SYNC", "### Sync gestartet: " + format.format(new Date(System.currentTimeMillis())));
			long timestampStart = System.currentTimeMillis();
			Log.e("SYNC", "##########################################################");

			mConn.synchronize(syncParms);

			Log.e("SYNC", "##########################################################");
			Log.e("SYNC", "### Sync beendet: " + format.format(new Date(System.currentTimeMillis())));
			long timestampEnd = System.currentTimeMillis();
			Log.e("SYNC", "##########################################################");

			SyncResult result = syncParms.getSyncResult();

			Log.e("SYNC", "##########################################################");
			Log.e("SYNC", "### Sync-Ergebnis:");
			Log.e("SYNC", "###");
			Log.e("SYNC", "### Gesendet:  " + calculateSize(result.getSentByteCount()));
			Log.e("SYNC", "### Empfangen: " + calculateSize(result.getReceivedByteCount()));
			Log.e("SYNC", "### Tabellen:  " + result.getTotalTableCount());
			Log.e("SYNC", "### Zeit:  " + ((double) (timestampEnd - timestampStart)) / 1000.0);
			Log.e("SYNC", "##########################################################");
			// Log.e("SYNC",
			// "*** Synchronized *** bytes sent="
			// + result.getSentByteCount() + ", bytes received="
			// + result.getReceivedByteCount()
			// + ", rows received=" + result.getReceivedRowCount()
			// + "; TableCount: " + result.getTotalTableCount());

			// mConn.release();

			// updateList();

		} catch (ULjException exception) {
			Log.e(TAG, exception.getMessage());
			exception.printStackTrace();

			this.callbackContext.error("Error during synchronization: " + exception.getMessage());
		}
	}

	private String calculateSize(long bytes) {
		double kbyte = 0.0;
		double mbyte = 0.0;

		kbyte = ((double) bytes) / 1024.0;
		mbyte = kbyte / 1024.0;

		return "" + mbyte + " MB, " + kbyte + " KB, " + bytes + " Bytes";
	}
	
	/*
	 * AsyncTasks
	 */
	private AsyncTask<String, String, String> sync = new AsyncTask<String, String, String>() {

		@Override
		protected void onPreExecute() {
			Toast.makeText(webView.getContext(), "Sync started", Toast.LENGTH_LONG).show();

		}

		@Override
		protected String doInBackground(String... params) {
			sync();
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(webView.getContext(), "Sync ended (" + ")", Toast.LENGTH_LONG).show();
		}

	};

}