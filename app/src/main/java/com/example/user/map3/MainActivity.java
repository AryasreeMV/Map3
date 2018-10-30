package com.example.user.map3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GPSTracker mGPS = new GPSTracker(this);

        TextView text = (TextView) findViewById(R.id.txtloc);
        if(mGPS.canGetLocation ){
            mGPS.getLocation();
            text.setText("Lat"+mGPS.getLatitude()+"Lon"+mGPS.getLongitude());
        }else{
            text.setText("Unabletofind");
            System.out.println("Unable");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
