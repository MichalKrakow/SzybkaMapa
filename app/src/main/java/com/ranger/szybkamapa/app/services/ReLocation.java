package com.ranger.szybkamapa.app.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Michal on 2014-06-13.
 */
public class ReLocation extends Service implements LocationListener, GpsStatus.Listener {

    private LocationManager locationManager;
    private Location location;
    private GpsStatus gpsStatus;

    private NotificationManager mNotificationManager;
    private int counter = 0, incrementBy = 1;
    private static boolean isRunning = false;

    private List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    private int mValue = 0; // Holds last value set by a client.
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    public static final int MSG_GPS_LOCATION = 5;
    public static final int MSG_GPS_LOCATION_OBJECT = 6;
    public static final int MSG_GPS_SATELLITE = 7;

    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler()); // Target we publish for clients to send messages to IncomingHandler.

    private static final String LOGTAG = "[USŁUGA]";

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(LOGTAG, "Service Started.");
        showNotification();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000,300f, this);

        gpsStatus = locationManager.getGpsStatus(null);

        locationManager.addGpsStatusListener(this);

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
        return START_STICKY; // Run until explicitly stopped.
    }

    private void showNotification() {
        /*
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_label), text, contentIntent);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(R.string.service_started, notification);
        */
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    private void sendMessageToUI(int intvaluetosend) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {
                // Send data as an Integer
                messenger.send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));

                /*
                // Send data as a String
                Bundle bundle = new Bundle();
                bundle.putString("str1", "ab" + intvaluetosend + "cd");
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(bundle);
                messenger.send(msg);
                */

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }
    private void sendLocationObject(Location loc){
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {

                Bundle bundle = new Bundle();
                bundle.putParcelable("loc",loc);
                Message msg = Message.obtain(null, MSG_GPS_LOCATION_OBJECT);
                msg.setData(bundle);
                messenger.send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }
    private void sendStatelliteToUi(String sat) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {
                // Send data as a String
                Bundle bundle = new Bundle();
                bundle.putString("sat", sat);
                Message msg = Message.obtain(null, MSG_GPS_SATELLITE);
                msg.setData(bundle);
                messenger.send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }
    private void sendLocationToUi(String loc) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {
                // Send data as a String
                Bundle bundle = new Bundle();
                bundle.putString("gps", loc);
                Message msg = Message.obtain(null, MSG_GPS_LOCATION);
                msg.setData(bundle);
                messenger.send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        try
        {
            locationManager.removeUpdates(this);
            locationManager.removeGpsStatusListener(this);
            locationManager=null;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //mNotificationManager.cancel(R.string.service_started); // Cancel the persistent notification.
        Log.i(LOGTAG, "Service Stopped.");
        isRunning = false;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        gpsStatus = locationManager.getGpsStatus(gpsStatus);
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                //Log.i(LOGTAG, "GPS STARTED");
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                //Log.i(LOGTAG, "GPS STOPPED");
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                //Log.i(LOGTAG, "GPS GET FIX");
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                //Log.i(LOGTAG, "GPS GET FIX");
                sendStatelliteToUi(getSatStatus());
                break;
        }

    }


    private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG,"handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_INT_VALUE:
                    incrementBy = msg.arg1;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        /*
        String comma = location.getLatitude() + ","
                       + location.getLongitude()
                       + "," + location.getAltitude()
                       + "," + location.getAccuracy();
        sendLocationToUi(comma);
        */
        sendLocationObject(location);
        Log.d(LOGTAG, "onLocationChanged");
        //comma = null;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        //Log.d(LOGTAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String s) {
        //Log.d(LOGTAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        //Log.d(LOGTAG, "onProviderDisabled");
    }
    private String getSatStatus(){
        Iterable<GpsSatellite> sats = gpsStatus.getSatellites();

        int count, used;
        used = count = 0;

        for(GpsSatellite sat : sats){
            count++;

            if(sat.usedInFix())
                used++;

        }
        return used + "/" + count;
    }
    private void getSatData(){
        Iterable<GpsSatellite> sats = gpsStatus.getSatellites();

        for(GpsSatellite sat : sats){
            StringBuilder sb = new StringBuilder();
            sb.append(sat.getPrn());
            sb.append("\t");
            sb.append(sat.getElevation());
            sb.append("\t");
            sb.append(sat.getAzimuth());
            sb.append("\t");
            sb.append(sat.getSnr());

            try {
                Log.v(LOGTAG, sb.toString());
            } catch (Exception e) {
                Log.w(LOGTAG,e.getMessage());
            }
        }

    }


}
