package com.dustcheck.min.dustcheck;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

    ImageView LocBtn;
    TextView Loc;

    // tmX / tmY / pageNo = 1 / numOfRows = 10 / ServiceKey / _returnType = json
    String urlA = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?";

    // stationName / dataTerm = daily / pageNo = 1 / numOfrows = 10 / ServiceKey / ver = 1.3 / _returnType = json
    String urlB = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?";
    String ServiceKey = "dm9KUBV6wyt1FoXqfkVcM%2Fb4jEj%2FLNQqUhWAhSJq7NfsO39dixj%2BpJiSx9o50%2B40Frgi4qJx88RP4mAMX9%2BT9w%3D%3D";

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean isGetLocation = false;

    Location locations;

    LocationManager locationManager;

    double tmX = 0;
    double tmY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocBtn = (ImageView) findViewById(R.id.locationBtn);
        Loc = (TextView) findViewById(R.id.loc);

        LocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            GeoPoint geoPoint = new GeoPoint(lat, lng);
            GeoPoint trans = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, geoPoint);
            //geoTrans.convert
            //Loc.setText("latitude: "+ trans.getX() +", longitude: "+ trans.getY());
            tmX = trans.getX();
            tmY = trans.getY();

            ContentValues c1 = new ContentValues();

            DecimalFormat df = new DecimalFormat("#.#");
            String tm_X = df.format(tmX);
            String tm_Y = df.format(tmY);
            c1.put("tmX", tm_X);
            c1.put("tmY", tm_Y);
            c1.put("pageNo", 1);
            c1.put("numOfRows", 10);
            c1.put("ServiceKey", ServiceKey);
            c1.put("_returnType", "json");

            // stationName / dataTerm = daily / pageNo = 1 / numOfRows = 10 / ServiceKey / ver = 1.3 / _returnType = json
            //ContentValues c2 = new ContentValues();
            //c2.put("tmX", tmX);
            //c2.put("dataTerm", "daily");
            //c2.put("pageNo", 1);
            //c2.put("numOfRows", 10);
            //c2.put("ServiceKey", ServiceKey);
            //c2.put("ver", 1.3);
            //c1.put("_returnType", "json");

            NetworkTask networkTask = new NetworkTask(urlA, c1);
            networkTask.execute();

            //networkTask = new NetworkTask(urlA, c2);
            //networkTask.execute();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Loc.setText("onStatusChanged");
        }

        public void onProviderEnabled(String provider) {
            Loc.setText("onProviderEnabled");
        }

        public void onProviderDisabled(String provider) {
            Loc.setText("onProviderDisabled");
        }
    };

    public Location getLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(
                        getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                        getApplicationContext(), android.Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET}, 1);

            return null;
        }

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
                isGetLocation = true;

                if (isGPSEnabled) {
                    if (locations == null) {
                        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BY_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                        locations = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (locations != null) {
                            double lng = locations.getLatitude();
                            double lat = locations.getLatitude();

                            GeoPoint geoPoint = new GeoPoint(lat, lng);
                            GeoPoint trans = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, geoPoint);
                            //geoTrans.convert
                            //Loc.setText("latitude: " + trans.getX() + ", longitude: " + trans.getY());
                            tmX = trans.getX();
                            tmY = trans.getY();
                        }
                    }
                }
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("위치 기능 요청")
                        .setMessage("해당 기능을 이용하기 위해서는, 위치 기능을 사용해야 합니다.")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

        //locationManager.removeUpdates(locationListener);
        return locations;
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            HttpTask requestHttpURLConnection = new HttpTask();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.

            Loc.setText(s);
        }
    }
}
