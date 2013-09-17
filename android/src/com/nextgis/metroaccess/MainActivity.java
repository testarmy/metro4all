/******************************************************************************
 * Project:  Metro Access
 * Purpose:  Routing in subway for disabled.
 * Author:   Baryshnikov Dmitriy (aka Bishop), polimax@mail.ru
 ******************************************************************************
*   Copyright (C) 2013 NextGIS
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ****************************************************************************/
package com.nextgis.metroaccess;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.MenuItem;

import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.VariableGraph;
import edu.asu.emit.qyan.alg.model.Vertex;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

//https://code.google.com/p/k-shortest-paths/

public class MainActivity extends SherlockActivity implements OnNavigationListener{
	final static String TAG = "metroaccess";	
	final static String sUrl = "http://gis-lab.info/data/zp-gis/data/ma/";
	final static String META = "meta.json";
	final static String BUNDLE_MSG_KEY = "msg";
	final static String BUNDLE_PAYLOAD_KEY = "json";
	final static String BUNDLE_ERRORMARK_KEY = "error";
	final static String BUNDLE_EVENTSRC_KEY = "eventsrc";
	final static String BUNDLE_ENTRANCE_KEY = "in";
	final static String BUNDLE_PATHCOUNT_KEY = "pathcount";
	final static String BUNDLE_PATH_KEY = "path_";
	final static String BUNDLE_STATIONMAP_KEY = "stationmap";
	final static String BUNDLE_STATIONID_KEY = "stationid";
	final static String BUNDLE_PORTALID_KEY = "portalid";
	
	final static String REMOTE_METAFILE = "remotemeta.json";
	final static String ROUTE_DATA_DIR = "rdata";
	
	final static String CURRENT_METRO_SEL = "metro_selection";
	
	final static String CSV_CHAR = ";";
	
	public final static int MENU_SEARCH = 3;
	public final static int MENU_SETTINGS = 4;
	public final static int MENU_ABOUT = 5;

	public final static int DEPARTURE_RESULT = 1;
	public final static int ARRIVAL_RESULT = 2;

	public final static int MAX_RECENT_ITEMS = 10;
	
	//public final static int GET_META = 0;
	
	private static Handler moGetJSONHandler; 
	protected HashMap<Integer, JSONObject> mmoRouteMetadata;
	protected static String msRDataPath;
	
	protected HashMap<Integer, StationItem> mmoStations;
	protected List<Pair<Integer, Integer>> maoDepRecentIds, maoArrRecentIds;

	protected Graph mGraph;
	
	protected Button mSearchButton;
	protected Button mSelectFromStationButton;
	protected Button mSelectToStationButton;
	protected MenuItem mSearchMenuItem;
	
	protected int mnDepartureStationId, mnArrivalStationId;
	protected int mnDeparturePortalId, mnArrivalPortalId;
	protected TextView mtvDepartureStationName, mtvArrivalStationName; 
	protected TextView mtvDeparturePortalName, mtvArrivalPortalName;
	
	protected boolean mbDirected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mbDirected = false;

		mnDepartureStationId = mnArrivalStationId = -1;
		mnDeparturePortalId = mnArrivalPortalId = -1;
		
		maoDepRecentIds = new ArrayList<Pair<Integer, Integer>>();
		maoArrRecentIds = new ArrayList<Pair<Integer, Integer>>();
        // initialize the default settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
					
		moGetJSONHandler = new Handler() {
            public void handleMessage(Message msg) {
            	super.handleMessage(msg);
            	
            	Bundle resultData = msg.getData();
            	boolean bHaveErr = resultData.getBoolean(BUNDLE_ERRORMARK_KEY);
            	int nEventSource = resultData.getInt(BUNDLE_EVENTSRC_KEY);
            	String sPayload = resultData.getString(BUNDLE_PAYLOAD_KEY);
            	if(bHaveErr){
            		Toast.makeText(MainActivity.this, resultData.getString(BUNDLE_MSG_KEY), Toast.LENGTH_LONG).show();
	            	switch(nEventSource){
	            	case 1://get remote meta
	            		File file = new File(getExternalFilesDir(null), REMOTE_METAFILE);
	            		sPayload = readFromFile(file);
	            		break;
            		default:
            			return;
	            	}
            	}

            	switch(nEventSource){
            	case 1://get remote meta
            		if(IsRoutingDataExist()){
            			//check if updates available
            			CheckUpdatesAvailable(sPayload);
            		}
            		else{
            			AskForDownloadData(sPayload);
            		}
            		break;
            	case 2://create meta.json in routing data folder
                    String sPath = resultData.getString("path");
                    String sName = resultData.getString("name");
                    String sLocalName = resultData.getString("locname");
                    int nVer = resultData.getInt("ver");
                    boolean bDirected = resultData.getBoolean("directed");

                    JSONObject oJSONRoot = new JSONObject();
                    try {
						oJSONRoot.put("name", sName);
						oJSONRoot.put("name_" + Locale.getDefault().getLanguage(), sLocalName);
						oJSONRoot.put("ver", nVer);
						oJSONRoot.put("directed", bDirected);
					} catch (JSONException e) {
						e.printStackTrace();
					}
                    
                    String sJSON = oJSONRoot.toString();
                    File file = new File(sPath, META);
                    if(writeToFile(file, sJSON)){
                    	//store data
                    	//create sqlite db
                    	//Creating and saving the graph
                    }
                    
                    if(IsRoutingDataExist())
                    	LoadInterface();
            		break;
            	}
            }
        };		
        
		mmoStations = new HashMap<Integer, StationItem>();
		mmoRouteMetadata = new HashMap<Integer, JSONObject>();        
		
		//check for data exist
		if(IsRoutingDataExist()){
			//else check for updates
			CheckForUpdates();		
		}
		else{
			//ask to download data
			GetRoutingData();
		}      

	}
	
	private void LoadInterface(){
		ArrayList<String> items = new ArrayList<String>();
		for (int i = 0; i < mmoRouteMetadata.size(); i++) {
			JSONObject oJSON = mmoRouteMetadata.get(i);
			try{
	        	String sName = oJSON.getString("name_" + Locale.getDefault().getLanguage());
	        	items.add(sName);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		
	    ActionBar actionBar = getSupportActionBar();
		Context context = actionBar.getThemedContext();		
		ArrayAdapter<CharSequence> adapter= new ArrayAdapter<CharSequence>(context, R.layout.sherlock_spinner_dropdown_item, items.toArray(new String[items.size()]));
		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.views, R.layout.sherlock_spinner_dropdown_item);
		adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
	    
	    actionBar.setDisplayShowTitleEnabled(false);
	    actionBar.setNavigationMode(com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_LIST);
	    actionBar.setListNavigationCallbacks((SpinnerAdapter)adapter, this);
	    
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    int nCurrentMetro = prefs.getInt(CURRENT_METRO_SEL, 0);
	    actionBar.setSelectedNavigationItem(nCurrentMetro);
	    
	    try {
	    	JSONObject mmeta = mmoRouteMetadata.get(nCurrentMetro);
			msRDataPath = mmeta.getString("path");

			if(mmeta.has("directed")){
				mbDirected = mmeta.getBoolean("directed");
			}
					
			setContentView(R.layout.activity_main);
	
			mGraph = new VariableGraph();

		    //fill with station list
		    File station_file = new File(msRDataPath, "stations.csv");
			if (station_file != null) {
	        	InputStream in;
				in = new BufferedInputStream(new FileInputStream(station_file));
	       	
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	
		        String line = reader.readLine();
		        while ((line = reader.readLine()) != null) {
		             String[] RowData = line.split(CSV_CHAR);
		             
		             if(RowData.length != 3){
		     	    	 Toast.makeText(MainActivity.this, getString(R.string.sInvalidCSVData) + "stations.csv", Toast.LENGTH_LONG).show();
		            	 return;
		             }
		             
					 String sName = RowData[0];
					 int nLine = Integer.parseInt(RowData[1]);
					 int nID = Integer.parseInt(RowData[2]);
	 					 
					 mGraph.add_vertex(new Vertex(nID));
					 StationItem st = new StationItem(nID, sName, nLine, 0);
	 				     
				     mmoStations.put(nID, st);
		        }
			        
		        reader.close();
		        if (in != null) {
		        	in.close();
		    	} 
			}
	
			File portals_file = new File(msRDataPath, "portals.csv");
			if (portals_file != null) {
			   	InputStream in;
				in = new BufferedInputStream(new FileInputStream(portals_file));
	
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	
		        String line = reader.readLine();
		        while ((line = reader.readLine()) != null) {
		             String[] RowData = line.split(CSV_CHAR);
		             
		             if(RowData.length < 4){
		     	    	 Toast.makeText(MainActivity.this, getString(R.string.sInvalidCSVData) + "portals.csv", Toast.LENGTH_LONG).show();
		            	 return;
		             }
		             
					 int nID = Integer.parseInt(RowData[0]);
					 String sName = RowData[1];
					 int nStationId = Integer.parseInt(RowData[2]);
					 int nDirection = 0;
					 if(RowData[3].equals("in")){
						 nDirection = 1;
					 }
					 else if(RowData[3].equals("out")){
						 nDirection = 2;
					 }
					 else{
					 	nDirection = 3;
					 }						
					 
					 int min_width = 0;
					 int min_step = 0;
					 int min_step_ramp = 0;
					 int lift = 0;
					 int lift_minus_step = 0;
					 int min_rail_width = 0;
					 int max_rail_width = 0;
					 int max_angle = 0;
					 
					 if(RowData.length > 13)
					 {
						 String tmp = RowData[6];
						 min_width = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
						 tmp = RowData[7];
						 min_step = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
						 tmp = RowData[8];
						 min_step_ramp = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
						 tmp = RowData[9];
						 lift = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
						 tmp = RowData[10];
						 lift_minus_step = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
						 tmp = RowData[11];
						 min_rail_width = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
						 tmp = RowData[12];
						 max_rail_width = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
						 tmp = RowData[13];
						 max_angle = tmp.length() == 0 ? 0 : Integer.parseInt(tmp);
					 }
					 int [] detailes = {min_width, min_step, min_step_ramp, lift, lift_minus_step, min_rail_width, max_rail_width, max_angle};
					 PortalItem pt = new PortalItem(nID, sName, nStationId, nDirection, detailes);
	
					 mmoStations.get(nStationId).AddPortal(pt);
					 
					 Log.d(TAG, "#" + nID);
		        }
		        
		        reader.close();
			    if (in != null) {
			       	in.close();
			   	} 
			}	
		
		
			//fill routes
			File file_route = new File(msRDataPath, "graph.csv");
			if (file_route != null) {
	        	InputStream in;
				in = new BufferedInputStream(new FileInputStream(file_route));
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		        String line = reader.readLine();
		        while ((line = reader.readLine()) != null) {
		             String[] RowData = line.split(CSV_CHAR);
		             
		             if(RowData.length != 5){
		     	    	 Toast.makeText(MainActivity.this, getString(R.string.sInvalidCSVData) + "graph.csv", Toast.LENGTH_LONG).show();
		            	 return;
		             }
		             
					 int nFromId = Integer.parseInt(RowData[0]);
					 int nToId = Integer.parseInt(RowData[1]);
					 int nCost = Integer.parseInt(RowData[4]);
	 					 
					 Log.d("Route", ">" + nFromId + "-" + nToId + ":" + nCost);
					 mGraph.add_edge(nFromId, nToId, nCost);
					 if(!mbDirected){
						 mGraph.add_edge(nToId, nFromId, nCost);
					 }
		        }
		        reader.close();
		        if (in != null) {
		        	in.close();
		    	}
			}
	    }
	    catch (IOException ex) {
	    	ex.printStackTrace();
		}
		catch(IllegalArgumentException ex){
			ex.printStackTrace();
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	    
		mSearchButton = (Button) findViewById(R.id.btSearch);
		mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	onSearch();
             }
        });  
		mSearchButton.setEnabled(false);
		
		//from station
		mSelectFromStationButton = (Button) findViewById(R.id.btSetDepart);
		mSelectFromStationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	onSelectDepatrure();
             }
        });  		
		//to station
		mSelectToStationButton = (Button) findViewById(R.id.btSelArrival);
		mSelectToStationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	onSelectArrival();
             }
        });
		
		mtvDepartureStationName = (TextView) findViewById(R.id.fromstationname);
		mtvArrivalStationName = (TextView) findViewById(R.id.tostationname);
		mtvDeparturePortalName = (TextView) findViewById(R.id.fromentrancename);
		mtvArrivalPortalName =  (TextView) findViewById(R.id.toentrancename);
		
		UpdateUI();
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		mSearchMenuItem = menu.add(com.actionbarsherlock.view.Menu.NONE, MENU_SEARCH, com.actionbarsherlock.view.Menu.NONE, R.string.sSearch)
		.setIcon(R.drawable.ic_action_search);
		mSearchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mSearchMenuItem.setEnabled(false);

		menu.add(com.actionbarsherlock.view.Menu.NONE, MENU_SETTINGS, com.actionbarsherlock.view.Menu.NONE, R.string.sSettings)
       .setIcon(R.drawable.ic_action_settings)
       .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);		
		
		menu.add(com.actionbarsherlock.view.Menu.NONE, MENU_ABOUT, com.actionbarsherlock.view.Menu.NONE, R.string.sAbout)
		.setIcon(R.drawable.ic_action_about)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);	
		  
		return true;
//		return super.onCreateOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            return false;
        case MENU_SEARCH:
        	onSearch();
        	return true;
        case MENU_SETTINGS:
            // app icon in action bar clicked; go home
            Intent intentSet = new Intent(this, PreferencesActivity.class);
            intentSet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentSet);
            return true;
        case MENU_ABOUT:
            Intent intentAbout = new Intent(this, AboutActivity.class);
            intentAbout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentAbout);
            return true;	  
        }
		return super.onOptionsItemSelected(item);
	}	
	
	//check if data for routing is downloaded
	protected boolean IsRoutingDataExist(){	
		if(mmoRouteMetadata.isEmpty()){			
			File f = new File(getExternalFilesDir(ROUTE_DATA_DIR).getPath());
			File[] files = f.listFiles();
			int nCounter = 0;
			for (File inFile : files) {
			    if (inFile.isDirectory()) {
			        File metafile = new File(inFile, META);
			        if(metafile.isFile()){
			        	String sJSON = readFromFile(metafile);
			        	JSONObject oJSON;
						try {
							oJSON = new JSONObject(sJSON);
				        	oJSON.put("path", inFile.getPath());
				        	mmoRouteMetadata.put(nCounter, oJSON);
				        	nCounter++;
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			    }
			}		
		}
		return !mmoRouteMetadata.isEmpty();
	}	
	
	protected void CheckForUpdates(){
		MetaDownloader uploader = new MetaDownloader(MainActivity.this, getResources().getString(R.string.sDownLoading), moGetJSONHandler, false);
		uploader.execute(sUrl + META);				
	}
	
	protected void GetRoutingData(){
		MetaDownloader loader = new MetaDownloader(MainActivity.this, getResources().getString(R.string.sDownLoading), moGetJSONHandler, true);
		loader.execute(sUrl + META);		
	}
	
	protected void CheckUpdatesAvailable(String sJSON){
		//ask user for download
		try{
			JSONObject oJSONMetaRemote = new JSONObject(sJSON);
			
			//save remote meta to file
			if(oJSONMetaRemote != null ){
				File file = new File(getExternalFilesDir(null), REMOTE_METAFILE);
				writeToFile(file, sJSON);
			}			
			
			final JSONArray jsonArray = oJSONMetaRemote.getJSONArray("packages");

			ArrayList<String> items = new ArrayList<String>();

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String sLocaleKeyName = "name_" + Locale.getDefault().getLanguage();
				String sEnName = jsonObject.getString("name");
				String sName = jsonObject.getString(sLocaleKeyName);	
				if(sName.length() == 0)
					sName = sEnName;
				int nVer = jsonObject.getInt("ver");
				for(int j = 0; j < mmoRouteMetadata.size(); j++)
				{
					String sRemoteEnName = mmoRouteMetadata.get(j).getString("name");
					if(sRemoteEnName.equals(sEnName)){
						if(nVer > mmoRouteMetadata.get(j).getInt("ver")) {
							items.add(sName);
						}
					}
				}
			}

			int count = items.size();
			if(count < 1){
				LoadInterface();
				return;
			}
			final boolean[] checkedItems = new boolean[count];

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.sUpdateAvaliable)
			.setCancelable(false)
			.setMultiChoiceItems(items.toArray(new String[items.size()]), checkedItems,
					new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checkedItems[which] = isChecked;
				}
			})
			.setPositiveButton(R.string.sDownload,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					for (int i = 0; i < checkedItems.length; i++) {
						if (checkedItems[i]){
							try {
								JSONObject jsonObject = jsonArray.getJSONObject(i);
								//download and unzip
								int nVer = jsonObject.getInt("ver");
								String sPath = jsonObject.getString("path");
								String sName = jsonObject.getString("name");
								String sLocName = sName;
								if(jsonObject.has("name_" + Locale.getDefault().getLanguage())){
									sLocName = jsonObject.getString("name_" + Locale.getDefault().getLanguage());
								}
								boolean bDirected = false;
								if(jsonObject.has("directed")){
									bDirected = jsonObject.getBoolean("directed");
								}
								if(sLocName.length() == 0){
									sLocName = sName;
								}
								DataDownloader uploader = new DataDownloader(MainActivity.this, sPath, sName, sLocName, nVer, bDirected, getResources().getString(R.string.sDownLoading), moGetJSONHandler);
								uploader.execute(sUrl + sPath + ".zip");

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			})

			.setNegativeButton(R.string.sCancel,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();

				}
			});
			builder.create();
			builder.show();		    
	    }
	    catch (Exception e) {
	    	Toast.makeText(MainActivity.this, R.string.sNetworkInvalidData, Toast.LENGTH_LONG).show();
		}	    	
		
	}

	protected void AskForDownloadData(String sJSON){
		//ask user for download
	    try{
	    	JSONObject oJSONMetaRemote = new JSONObject(sJSON);
	    	
			//save remote meta to file
			if(oJSONMetaRemote != null ){
				File file = new File(getExternalFilesDir(null), REMOTE_METAFILE);
				writeToFile(file, sJSON);
			}
			
		    final JSONArray jsonArray = oJSONMetaRemote.getJSONArray("packages");
		    
		    ArrayList<String> items = new ArrayList<String>();
		    
		    for (int i = 0; i < jsonArray.length(); i++) {
		    	  JSONObject jsonObject = jsonArray.getJSONObject(i);
		    	  String sLocaleKeyName = "name_" + Locale.getDefault().getLanguage();
		    	  String sName = jsonObject.getString(sLocaleKeyName);	
		    	  if(sName.length() == 0)
		    		  sName = jsonObject.getString("name");
		    	  items.add(sName);
		    }
		    
		    int count = items.size();
		    final boolean[] checkedItems = new boolean[count];
		    
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.sSelectDataToDownload)
				   .setCancelable(false)
				   .setMultiChoiceItems(items.toArray(new String[items.size()]), checkedItems,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which, boolean isChecked) {
									checkedItems[which] = isChecked;
								}
							})
					.setPositiveButton(R.string.sDownload,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									for (int i = 0; i < checkedItems.length; i++) {
										if (checkedItems[i]){
											try {
												JSONObject jsonObject = jsonArray.getJSONObject(i);
												//download and unzip
												int nVer = jsonObject.getInt("ver");
												String sPath = jsonObject.getString("path");
												String sName = jsonObject.getString("name");
												String sLocName = sName;
												if(jsonObject.has("name_" + Locale.getDefault().getLanguage())){
													sLocName = jsonObject.getString("name_" + Locale.getDefault().getLanguage());
												}
												boolean bDirected = false;
												if(jsonObject.has("directed")){
													bDirected = jsonObject.getBoolean("directed");
												}
												
												if(sLocName.length() == 0){
													sLocName = sName;
												}
												DataDownloader uploader = new DataDownloader(MainActivity.this, sPath, sName, sLocName, nVer, bDirected, getResources().getString(R.string.sDownLoading), moGetJSONHandler);
												uploader.execute(sUrl + sPath + ".zip");
												
											} catch (JSONException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
								}
							})

					.setNegativeButton(R.string.sCancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();

								}
							});
			builder.create();
			builder.show();
	    	
	    } 
	    catch (Exception e) {
	    	Toast.makeText(MainActivity.this, R.string.sNetworkInvalidData, Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean writeToFile(File filePath, String sData){
		try{
			FileOutputStream os = new FileOutputStream(filePath, false);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os);
	        outputStreamWriter.write(sData);
	        outputStreamWriter.close();
	        return true;
		}
		catch(IOException e){
			return false;
		}		
	}

	private String readFromFile(File filePath) {

	    String ret = "";

	    try {
	    	FileInputStream inputStream = new FileInputStream(filePath);

	        if ( inputStream != null ) {
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = "";
	            StringBuilder stringBuilder = new StringBuilder();

	            while ( (receiveString = bufferedReader.readLine()) != null ) {
	                stringBuilder.append(receiveString);
	            }

	            inputStream.close();
	            ret = stringBuilder.toString();
	        }
	    }
	    catch (FileNotFoundException e) {
	    	Toast.makeText(MainActivity.this, getString(R.string.sFileNotFound) + ": " + e.toString(), Toast.LENGTH_LONG).show();
	    } catch (IOException e) {
	    	Toast.makeText(MainActivity.this, getString(R.string.sCannotReadFile) + ": " + e.toString(), Toast.LENGTH_LONG).show();
	    }

	    return ret;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onPause() {
	    ActionBar actionBar = getSupportActionBar();
		if(ActionBar.NAVIGATION_MODE_LIST == actionBar.getNavigationMode()){
			final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		    
			edit.putInt(CURRENT_METRO_SEL, actionBar.getSelectedNavigationIndex());
			
			edit.putInt("dep_"+BUNDLE_STATIONID_KEY, mnDepartureStationId);
			edit.putInt("arr_"+BUNDLE_STATIONID_KEY, mnArrivalStationId);
			edit.putInt("dep_"+BUNDLE_PORTALID_KEY, mnDeparturePortalId);
			edit.putInt("arr_"+BUNDLE_PORTALID_KEY, mnArrivalPortalId);
			
			int nbeg = maoDepRecentIds.size() < MAX_RECENT_ITEMS ? 0 : maoDepRecentIds.size() - MAX_RECENT_ITEMS;
			int nsize = maoDepRecentIds.size() - nbeg;
			int counter = 0;
			for(int i = nbeg; i < nsize; i++){
				edit.putInt("recent_dep_"+BUNDLE_STATIONID_KEY+counter, maoDepRecentIds.get(i).first);
				edit.putInt("recent_dep_"+BUNDLE_PORTALID_KEY+counter, maoDepRecentIds.get(i).second);
				
				counter++;
			}
			edit.putInt("recent_dep_counter",counter);
			
			nbeg = maoArrRecentIds.size() < MAX_RECENT_ITEMS ? 0 : maoArrRecentIds.size() - MAX_RECENT_ITEMS;
			nsize = maoArrRecentIds.size() - nbeg;
			counter = 0;
			for(int i = nbeg; i < nsize; i++){
				edit.putInt("recent_arr_"+BUNDLE_STATIONID_KEY+counter, maoArrRecentIds.get(i).first);
				edit.putInt("recent_arr_"+BUNDLE_PORTALID_KEY+counter, maoArrRecentIds.get(i).second);
				
				counter++;
			}
			edit.putInt("recent_arr_counter",counter);
			
			edit.commit();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	    ActionBar actionBar = getSupportActionBar();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(ActionBar.NAVIGATION_MODE_LIST == actionBar.getNavigationMode()){
		    int nCurrentMetro = prefs.getInt(CURRENT_METRO_SEL, 0);
		    actionBar.setSelectedNavigationItem(nCurrentMetro);
		}
	    mnDepartureStationId = prefs.getInt("dep_"+BUNDLE_STATIONID_KEY, -1);
	    mnArrivalStationId = prefs.getInt("arr_"+BUNDLE_STATIONID_KEY, -1);
	    mnDeparturePortalId = prefs.getInt("dep_"+BUNDLE_PORTALID_KEY, -1);
	    mnArrivalPortalId = prefs.getInt("arr_"+BUNDLE_PORTALID_KEY, -1);

	    UpdateUI();
	}
	
	protected void 	onSelectDepatrure(){
	    Intent intent = new Intent(this, SelectStationActivity.class);
	    Bundle bundle = new Bundle();
	    bundle.putInt(BUNDLE_EVENTSRC_KEY, DEPARTURE_RESULT);
        bundle.putSerializable(BUNDLE_STATIONMAP_KEY, mmoStations);
        bundle.putBoolean(BUNDLE_ENTRANCE_KEY, true);
	    intent.putExtras(bundle);
	    startActivityForResult(intent, DEPARTURE_RESULT);	
	}
	
	protected void 	onSelectArrival(){
	    Intent intent = new Intent(this, SelectStationActivity.class);
	    Bundle bundle = new Bundle();
	    bundle.putInt(BUNDLE_EVENTSRC_KEY, ARRIVAL_RESULT);
        bundle.putSerializable(BUNDLE_STATIONMAP_KEY, mmoStations);
        bundle.putBoolean(BUNDLE_ENTRANCE_KEY, false);
	    intent.putExtras(bundle);
	    startActivityForResult(intent, ARRIVAL_RESULT);			
	}
	
	@Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (data == null) {
	    	return;
	    }
	    
	    if (resultCode != RESULT_OK) {
	    	return;
	    }
	    
    	int nStationId = data.getIntExtra(BUNDLE_STATIONID_KEY, -1);
    	int nPortalId = data.getIntExtra(BUNDLE_PORTALID_KEY, -1);
    	
    	
		final SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		
	    switch(requestCode){
	    case DEPARTURE_RESULT:
	    	maoDepRecentIds.add(Pair.create(nStationId, nPortalId));
	       	mnDepartureStationId = nStationId;	    	
	    	mnDeparturePortalId = nPortalId;
			edit.putInt("dep_"+BUNDLE_STATIONID_KEY, mnDepartureStationId);
			edit.putInt("dep_"+BUNDLE_PORTALID_KEY, mnDeparturePortalId);
	       	break;
	    case ARRIVAL_RESULT:
	    	maoArrRecentIds.add(Pair.create(nStationId, nPortalId));
	    	mnArrivalStationId = nStationId;
	    	mnArrivalPortalId = nPortalId;
			edit.putInt("arr_"+BUNDLE_STATIONID_KEY, mnArrivalStationId);
			edit.putInt("arr_"+BUNDLE_PORTALID_KEY, mnArrivalPortalId);
	    	break;
    	default:
    		break;
	    }

	    edit.commit();
	    
	    UpdateUI();
	}	
	
	protected void UpdateUI(){
    	StationItem dep_sit = mmoStations.get(mnDepartureStationId);
    	if(dep_sit != null && mtvDepartureStationName != null){    		
    		mtvDepartureStationName.setText(dep_sit.GetName());
    		PortalItem pit = dep_sit.GetPortal(mnDeparturePortalId);
    		if(pit != null && mtvDeparturePortalName != null){
    			mtvDeparturePortalName.setText(pit.GetName());
    		}
    	}

    	StationItem arr_sit = mmoStations.get(mnArrivalStationId);
    	if(arr_sit != null && mtvArrivalStationName != null){
    		mtvArrivalStationName.setText(arr_sit.GetName());
    		PortalItem pit = arr_sit.GetPortal(mnArrivalPortalId);
    		if(pit != null && mtvArrivalPortalName != null){
    			mtvArrivalPortalName.setText(pit.GetName());
    		}
    	}

	    if(mnDepartureStationId != mnArrivalStationId && mnArrivalStationId != -1 && mnArrivalStationId != -1){
	    	if(mSearchButton != null) 
	    		mSearchButton.setEnabled(true);
	    }
	    else{
	    	if(mSearchButton != null) 
	    		mSearchButton.setEnabled(false);
	    }
	}
	
	protected void onSearch(){
		//BellmanFordShortestPath
		/*List<DefaultWeightedEdge> path = BellmanFordShortestPath.findPathBetween(mGraph, stFrom.getId(), stTo.getId());
		if(path != null){
			for(DefaultWeightedEdge edge : path) {
                	Log.d("Route", mmoStations.get(mGraph.getEdgeSource(edge)) + " - " + mmoStations.get(mGraph.getEdgeTarget(edge)) + " " + edge);
                }
		}*/
		//DijkstraShortestPath
		/*List<DefaultWeightedEdge> path = DijkstraShortestPath.findPathBetween(mGraph, stFrom.getId(), stTo.getId());
		if(path != null){
			for(DefaultWeightedEdge edge : path) {
                	Log.d("Route", mmoStations.get(mGraph.getEdgeSource(edge)) + " - " + mmoStations.get(mGraph.getEdgeTarget(edge)) + " " + edge);
                }
		}*/	
        //KShortestPaths
		/*
		KShortestPaths<Integer, DefaultWeightedEdge> kPaths = new KShortestPaths<Integer, DefaultWeightedEdge>(mGraph, stFrom.getId(), 2);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths = null;
        try {
            paths = kPaths.getPaths(stTo.getId());
            for (GraphPath<Integer, DefaultWeightedEdge> path : paths) {
                for (DefaultWeightedEdge edge : path.getEdgeList()) {
                	Log.d("Route", mmoStations.get(mGraph.getEdgeSource(edge)) + " - " + mmoStations.get(mGraph.getEdgeTarget(edge)) + " " + edge);
                }
                Log.d("Route", "Weight: " + path.getWeight());
            }
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        }*/
		
		//YenTopKShortestPathsAlg
		YenTopKShortestPathsAlg yenAlg = new YenTopKShortestPathsAlg(mGraph);
		List<Path> shortest_paths_list = yenAlg.get_shortest_paths(mGraph.get_vertex(mnDepartureStationId), mGraph.get_vertex(mnArrivalStationId), 3);
		
		if(shortest_paths_list.size() == 0){
			Toast.makeText(this, R.string.sCannotGetPath, Toast.LENGTH_SHORT).show();
		}
		else {
	        Intent intentView = new Intent(MainActivity.this, com.nextgis.metroaccess.StationListView.class);
	        //intentView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		
	        int nCounter = 0;
	        Bundle bundle = new Bundle();
	        bundle.putInt("dep_" + BUNDLE_PORTALID_KEY, mnDeparturePortalId);
	        bundle.putInt("arr_" + BUNDLE_PORTALID_KEY, mnArrivalPortalId);			

	        for (Path path : shortest_paths_list) {
				ArrayList<Integer> IndexPath = new  ArrayList<Integer>();
				Log.d(TAG, "Route# " + nCounter);
	            for (BaseVertex v : path.get_vertices()) {
	            	IndexPath.add(v.get_id());
	            	Log.d(TAG, "<" + mmoStations.get(v.get_id()));
	            }
	            intentView.putIntegerArrayListExtra(BUNDLE_PATH_KEY + nCounter, IndexPath);
	            nCounter++;
	        }	        
	        
	        bundle.putInt(BUNDLE_PATHCOUNT_KEY, nCounter);
	        bundle.putSerializable(BUNDLE_STATIONMAP_KEY, mmoStations);
			
			intentView.putExtras(bundle);
	        
	        MainActivity.this.startActivity(intentView);
	       
		}
	} 
}