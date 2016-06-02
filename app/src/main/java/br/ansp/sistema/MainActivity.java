package br.ansp.sistema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends ActionBarActivity {
	private Button scanBtn;
	private TextView formatTxt, contentTxt;
	private PatrimonioDBHelper dbHelper;
	public final static String RACK_CODE = "br.ansp.sistema.RACK_CODE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		dbHelper = new PatrimonioDBHelper(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}

	public void showData() {
		/* 
		 * Displays the data in the database so the user can see if everything is ok
		 */
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cur = db.query(true, PatrimonioDBHelper.TABLE1_NAME, new String [] {PatrimonioDBHelper.RACK_ID}, null, null, null, null, null, null);
		cur.moveToFirst();
		int index = cur.getColumnIndex(PatrimonioDBHelper.RACK_ID);
		TableLayout table = (TableLayout) findViewById(R.id.table_show);
		table.setVisibility(android.view.View.VISIBLE);
		for (int i=table.getChildCount()-1; i>1; i--){
			table.removeViewAt(i);
		}
		while(cur.isAfterLast() == false) {
			int rack_id = cur.getInt(index);
			String s_rack_id = ((Integer) rack_id).toString();
			Cursor cur2 = db.query(PatrimonioDBHelper.TABLE1_NAME, new String [] {PatrimonioDBHelper.APELIDO, PatrimonioDBHelper.ID, PatrimonioDBHelper.POSICAO}, PatrimonioDBHelper.RACK_ID+" = "+s_rack_id,null,null,null,null);
			cur2.moveToFirst();
			int idx_apelido, idx_pos, idx_id;
			idx_apelido = cur2.getColumnIndex(PatrimonioDBHelper.APELIDO);
			idx_pos = cur2.getColumnIndex(PatrimonioDBHelper.POSICAO);
			idx_id = cur2.getColumnIndex(PatrimonioDBHelper.ID);
			while(cur2.isAfterLast() == false){
				String apelido = cur2.getString(idx_apelido);
				int id = cur2.getInt(idx_id);
				TableRow tr = new TableRow(this);
				TextView trid = new TextView(this);
				TextView tap = new TextView(this);
				TextView tpos = new TextView(this);
				TextView tid = new TextView(this);
				trid.setText(s_rack_id);
				String pos = cur2.getString(idx_pos);
				tap.setText(apelido);
				tid.setText(((Integer)id).toString());
				tpos.setText(pos);
				tr.addView(trid);
				tr.addView(tid);
				tr.addView(tap);
				tr.addView(tpos);
				table.addView(tr);
				cur2.moveToNext();
			}
			cur.moveToNext();
		}
	}
		
	public void share() {
		/*
		 * Creates a csv from the data on the internal database. This csv may be shared with any app that supports csv.
		 */
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	SQLiteDatabase db = dbHelper.getReadableDatabase();
	    	Cursor cur = db.query(PatrimonioDBHelper.TABLE1_NAME, new String [] {PatrimonioDBHelper.RACK_ID, PatrimonioDBHelper.ID, PatrimonioDBHelper.POSICAO}, null, null, null, null, PatrimonioDBHelper.RACK_ID);
	    	int idx_rid = cur.getColumnIndex(PatrimonioDBHelper.RACK_ID);
	    	int idx_id = cur.getColumnIndex(PatrimonioDBHelper.ID);
	    	int idx_pos = cur.getColumnIndex(PatrimonioDBHelper.POSICAO);
	    	File path = Environment.getExternalStoragePublicDirectory(
	                Environment.DIRECTORY_PICTURES);
	        FileWriter fileWrite;
	        File file;

	    	try {
	    		file = new File(path, "racks.csv");
	    		fileWrite = new FileWriter(file);
	    	} catch (IOException e) {
	    		Toast toast = Toast.makeText(getApplicationContext(), "Problema ao criar o arquivo", Toast.LENGTH_SHORT);
	    		toast.show();
	    		return;
	    	}
	    	cur.moveToFirst();
	    	String csv = String.format("\"%s\";\"%s\";\"%s\"\n", PatrimonioDBHelper.RACK_ID, PatrimonioDBHelper.ID, PatrimonioDBHelper.POSICAO);
	    	try {
	    		fileWrite.append(csv);
	    	} catch (IOException e) {
	    		Toast toast = Toast.makeText(getApplicationContext(), "Problema ao escrever no arquivo", Toast.LENGTH_SHORT);
	    		toast.show();
	    		return;
	    	}

	    	while (cur.isAfterLast() == false) {
	    		csv = String.format("\"%d\";\"%d\";\"%s\"\n", cur.getInt(idx_rid), cur.getInt(idx_id), cur.getString(idx_pos));
	    		try {
	    			fileWrite.append(csv);
	    		} catch (IOException e) {
	    			Toast toast = Toast.makeText(getApplicationContext(), "Problema ao escrever no arquivo", Toast.LENGTH_SHORT);
	    			toast.show();
	    			return;
	    		}
	    		cur.moveToNext();
	    	}

	    	try {
				fileWrite.flush();
		    	fileWrite.close();
		    } catch (IOException e) {
    			Toast toast = Toast.makeText(getApplicationContext(), "Problema ao escrever no arquivo", Toast.LENGTH_SHORT);
    			toast.show();
    			return;
			}

	    	Intent sendIntent = new Intent();
	    	sendIntent.setAction(Intent.ACTION_SEND);
	    	sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
	    	sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Rack positions");
	    	sendIntent.putExtra(Intent.EXTRA_TEXT, "CSV com as posições dos racks");
	    	sendIntent.setType("text/csv");
	    	startActivity(sendIntent);
	    }
	}
	
	public void initializeDB() {
		/*
		 * Clear the internal database
		 */
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		dbHelper.onUpgrade(db, 0, 0);
		TableLayout table = (TableLayout) findViewById(R.id.table_show);
		table.setVisibility(android.view.View.INVISIBLE);	
	}
	
	public void startScan() {
		/*
		 * Starts intent to open Barcode Scanner
		 */
		IntentIntegrator scanIntegrator = new IntentIntegrator(this);
		scanIntegrator.initiateScan();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/*
		 * Treats the returning of the scanning
		 */
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanningResult != null) {
			String scanContent = scanningResult.getContents();
			
			if (scanContent != null){
				Intent rackIntent = new Intent(this, RackDisplayActivity.class);
				rackIntent.putExtra(RACK_CODE, scanContent);
				startActivity(rackIntent);
			}
		}
		else{
		    Toast toast = Toast.makeText(getApplicationContext() , 
		        "No scan data received!", Toast.LENGTH_SHORT);
		    toast.show();
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_settings:
				return true;
			case R.id.action_scan:
				startScan();
				return true;
			case R.id.action_share:
				share();
				return true;
			case R.id.action_show:
				showData();
				return true;
			case R.id.action_initialize:
				initializeDB();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
