package br.ansp.sistema;

import java.io.IOException;

import java.util.Arrays;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

/**
 * @author Antonio
 *
 */
public class RackDisplayActivity extends ActionBarActivity {
	private TextView rackName;
	private String rackCode;
	private TextView scannedView;
	private PatrimonioDBHelper dbHelper;
	private SharedPreferences sharedPrefs;
	private final String SCANNED_VIEW = "scannedView";
	private final String SAVE_OK = "Item salvo";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rack_display);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		Intent intent = getIntent();
		rackCode = intent.getStringExtra(MainActivity.RACK_CODE);
		dbHelper = new PatrimonioDBHelper(this);
		sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		rackName = (TextView) findViewById(R.id.rack_name);

		ConnectivityManager connMgr = (ConnectivityManager) 
	            getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected()) {
	        new ExecuteXMLRPCCall(rackName).execute("patrimonio", rackCode);
	    } else {
	    	rackName.setText(rackCode);
	    }
	}


	private class ExecuteXMLRPCCall extends AsyncTask<String, Void, Object[]> {
		/*
		 * Execute calls to Sistema ANSP through XML-RPC. The calls are asynchronous.
		 */
		private View output;
		
		public ExecuteXMLRPCCall(View v) {
			this.output = v;
		}
        @Override
        protected Object[] doInBackground(String... params) {
        	XMLRPCClient client = new XMLRPCClient("http://sistema.ansp.br/xml_rpc_srv");
            // params comes from the execute() call: params[0] is the url.
            try {
            	if (params.length > 2) {
            		return (Object[]) client.call(params[0], params[1], params[2], params[3]);
            	} else {
            		return (Object[]) client.call(params[0], params[1]);
            	}
            } catch (XMLRPCException e) {
            	e.printStackTrace();
            } catch (Exception e) {
            	String x = e.toString();
            }
            return params;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Object[] result) {
        	if (result.length > 1) {
        		TextView ot = (TextView) this.output;
        		ot.setText((String) result[0]);
		        ImageView img = (ImageView) findViewById(R.id.check);
		        Boolean ok = (Boolean) result[1];
		        if (ok) {
		        	img.setImageResource(R.drawable.check);
		        } else {
		        	img.setImageResource(R.drawable.cross);
		        }
		        img.setVisibility(View.VISIBLE);
        	} else {
        		TextView rackName = (TextView) this.output;
        		TextView rackId = (TextView) findViewById(R.id.rack_id);
        		rackId.setText(rackCode);
        		rackName.setText((String) result[0]);
        	}
       }

	}
	
	public void scanEquipamento(View v){
		/*
		 * Uses Barcode Scanner to scan an equipment position or id
		 */
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(SCANNED_VIEW, v.getId());
		editor.commit();
		IntentIntegrator scanIntegrator = new IntentIntegrator(this);
		scanIntegrator.initiateScan();
	}

	public void save(View v) {
		/*
		 * Saves this equipment's position in a SQLite Database
		 */
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv;
		
		EditText pos = (EditText) findViewById(R.id.posicao);
		String posicao = pos.getText().toString();
		EditText equip_id = (EditText) findViewById(R.id.equipamento_id);
		String equipamento_id = equip_id.getText().toString();
		TextView equip = (TextView) findViewById(R.id.equipamento);
		String equipamento = equip.getText().toString();
		cv = new ContentValues();
		cv.put(PatrimonioDBHelper.ID, equipamento_id);
		cv.put(PatrimonioDBHelper.POSICAO, posicao);
		cv.put(PatrimonioDBHelper.RACK_ID, rackCode);
		cv.put(PatrimonioDBHelper.APELIDO, equipamento);
		long id = db.insert(PatrimonioDBHelper.TABLE1_NAME, null, cv);
		
		Toast toast = Toast.makeText(getApplicationContext(), SAVE_OK, Toast.LENGTH_LONG);
		toast.show();
	}

	public void clear(View v) {
		/*
		 * Clears the fields to allow scanning a new equipment
		 */
		TextView equip = (TextView) findViewById(R.id.equipamento);
		EditText pos = (EditText) findViewById(R.id.posicao);
		EditText eid = (EditText) findViewById(R.id.equipamento_id);
		ImageView check = (ImageView) findViewById(R.id.check);
		eid.setText("");
		pos.setText("");
		equip.setText("");
		check.setVisibility(android.view.View.INVISIBLE);
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/*
		 * Gets the result from Barcode Scanner and puts it in the right field
		 */
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		scannedView = (TextView) findViewById(sharedPrefs.getInt(SCANNED_VIEW,0));
		if (scanningResult != null) {
			String scanContent = scanningResult.getContents();
			String formatName = scanningResult.getFormatName();
			if (formatName == "EAN_8") {
				int format[] = {1,2,3,4,5,6,7,8};
				for (int i = 0; i < 8; i++) {
					format[i] = scanContent.charAt(i) - 48;
				}
				int s1 = 3*(format[0]+format[2]+format[4]+format[6]);
				int s2 = format[1]+format[3]+format[5];
				s1 = s1+s2;
				s2 = s1 % 10;
				s1 = 10-s1;
				if (s1 == 10) {
					s1 = 0;
				}
				if (s1 == format[7]) {
					scanContent = scanContent.substring(0, 7);
				}
			}
			
			String fieldName = getResources().getResourceEntryName(scannedView.getId());
			
			if (scanContent != null && fieldName.equals("equipamento_texto")) {
		        EditText equip_id = (EditText) findViewById(R.id.equipamento_id);
				equip_id.setText(scanContent);
				EditText posicao = (EditText) findViewById(R.id.posicao);
				TextView equipamento = (TextView) findViewById(R.id.equipamento);
				ConnectivityManager connMgr = (ConnectivityManager) 
						getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					new ExecuteXMLRPCCall(equipamento).execute("patrimonio_contem", rackCode, scanContent, posicao.getText().toString());
				} 
			} else {
				EditText posicao = (EditText) findViewById(R.id.posicao);
			   	posicao.setText(scanContent);
			}
		}
		else{
		    Toast toast = Toast.makeText(getApplicationContext() , 
		        "No scan data received!", Toast.LENGTH_SHORT);
		    toast.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rack_display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_rack_display,
					container, false);
			return rootView;
		}
	}

}
