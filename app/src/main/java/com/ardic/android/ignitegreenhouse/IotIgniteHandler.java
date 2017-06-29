package com.ardic.android.ignitegreenhouse;

import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.ardic.android.iotignite.callbacks.ConnectionCallback;
import com.ardic.android.iotignite.enumerations.NodeType;
import com.ardic.android.iotignite.enumerations.ThingCategory;
import com.ardic.android.iotignite.enumerations.ThingDataType;
import com.ardic.android.iotignite.exceptions.UnsupportedVersionException;
import com.ardic.android.iotignite.listeners.NodeListener;
import com.ardic.android.iotignite.listeners.ThingListener;
import com.ardic.android.iotignite.nodes.IotIgniteManager;
import com.ardic.android.iotignite.nodes.Node;
import com.ardic.android.iotignite.things.Thing;
import com.ardic.android.iotignite.things.ThingActionData;
import com.ardic.android.iotignite.things.ThingData;
import com.ardic.android.iotignite.things.ThingType;

public class IotIgniteHandler implements ConnectionCallback, NodeListener, ThingListener {

    private static final String TAG = IotIgniteHandler.class.getSimpleName();

    // Static singleton instance
    private static IotIgniteHandler INSTANCE = null;
    private static final long IGNITE_RECONNECT_INTERVAL = 10000L;

    private static final String NODE_ID = "WeMos D1";
    private static final String THING_ID = "LM-35 Temperature";

    private IotIgniteManager mIotIgniteManager;
    private boolean igniteConnected = false;
    private Context appContext;
    private Handler igniteWatchdog = new Handler();

    private Node mySampleNode;
    private Thing mySampleThing;

    private ThingType sampleThingType = new ThingType(
            /** Define Type of your Thing */
            "My Sample Thing Type",

            /** Set your things vendor. It's useful if you are using real sensors
             * This is important for separating identical sensors manufactured by different vendors.
             * For example accelerometer sensor produced by Bosch data sampling is
             * different than Samsung's.*/
            "My Sample Vendor",

            /** Set thing data type.
             */
            ThingDataType.FLOAT
    );

    private Runnable igniteWatchdogRunnable = new Runnable() {
        @Override
        public void run() {

            if(!igniteConnected){
                rebuildIgnite();
                igniteWatchdog.postDelayed(this,IGNITE_RECONNECT_INTERVAL);
                Log.e(TAG,"Ignite is not connected. Trying to reconnect...");
            }else {
                Log.e(TAG,"Ignite is already connected.");
            }
        }
    };

    private IotIgniteHandler(Context context){
        this.appContext = context;
    }

    public static synchronized IotIgniteHandler getInstance(Context appContext){

        if(INSTANCE == null){
            INSTANCE = new IotIgniteHandler(appContext);
        }
        return INSTANCE;
    }

    public void start(){
        startIgniteWatchdog();
    }

    public boolean sendData(float temperature) {
        if (mySampleThing != null && mySampleThing.isRegistered()) {
            ThingData data = new ThingData();
            data.addData(temperature);
            return mySampleThing.sendData(data);
        } return false;
    }

    @Override
    public void onConnected() {
        Log.i(TAG,"Ignite Connected");
        // cancel watchdog //
        igniteWatchdog.removeCallbacks(igniteWatchdogRunnable);
        igniteConnected = true;

        Log.i(TAG,"Creating Node: " + NODE_ID);

        mySampleNode = IotIgniteManager.NodeFactory.createNode(
                /*Unique ID of Node*/
                NODE_ID,
                /* Node label may be unique. */
                NODE_ID,
                /*Node Type is definition for node. If your node is a physical device, you can state it here.
                * Supported Node Types
                * GENERIC : Default node type. If you do not want to do type based things, this will work for you.
                * RASPBERRY_PI: Defines node as a Raspberry Pi.
                * If your node is Raspberry Pi and you are going to deal with "RasPi" specific things like RaspiCam, choose this one.
                * ARDUINO_YUN:  Defines node as Arduino Yun. Use it for Arduino Yun specific things like Bridge etc. */
                NodeType.GENERIC,
                /** Reserved for later use. Pass null for now.*/
                null,
                /*Node Listener : Callback for node unregistration. Nodes can be unregistered from enterprise.iot-ignite.com remotely.
                * If your node is unregistered from there but not by your code, will receive a callback here. */
                this
        );

        /**
         * Check node object and register it.
         */

        if(mySampleNode != null ) {

            /**
             * Node is not null and not registered. Register first and set connection online.
             * If you don't set connection true you can not send data over it.
             *
             */

            Log.i(TAG, mySampleNode.getNodeID() + " created.");
            if (!mySampleNode.isRegistered()) {

                Log.i(TAG, mySampleNode.getNodeID() + " is registering...");

                /**
                 * Register node here. If registration is successful, make it online.
                 */
                if (mySampleNode.register()) {

                    Log.i(TAG, mySampleNode.getNodeID() + " is registered successfully. Setting connection true");
                    mySampleNode.setConnected(true, "");
                }
            } else {

                /**
                 * Node is already registered. Set connection to true.
                 */
                Log.i(TAG, mySampleNode.getNodeID() + " has already registered. Setting connection true");

                mySampleNode.setConnected(true, "");
            }
        }

        /**
         * As node is registered, it is time to bound a thing to our node.
         */

        if(mySampleNode != null && mySampleNode.isRegistered()){

            mySampleThing = mySampleNode.createThing(

                    /*Thing ID : Must be unique*/
                    THING_ID,

                    /*Define your thing type here. Use ThingType object.
                    * Thing Type objects give information about what type of sensor/actuator you are using.*/
                    sampleThingType,

                    /** You can categorize your thing. EXTERNAL, BUILTIN or UNDEFINED */
                    ThingCategory.EXTERNAL,

                    /**If your thing going to to same action for example opening something or triggering relay,
                     * Set this true. When set it true your things can receive action messages over listener callback.
                     * Otwervise if your thing is only generating data. Set this false.*/
                    true,

                    /** Thing Listener : Callback for thing objects. Listener has three callbacks:
                     * - onConfigurationReceived() : Occurs when configuration setted by IoT-Ignite.
                     * - onActionReceived(): If your thing set as actuator action message will handle here.
                     * - onThingUnregistered(): If your thing unregister from IoT-Ignite you will receive this callback.*/
                    this,

                    /** Reserved for later uses. Pass null for now. */
                    null
            );


        }

        if(mySampleThing != null){

            /**
             * Check thing object. If registered make it online.
             * If thing is new and not registered previously, register it first
             * then make it online.
             */

            if(mySampleThing.isRegistered() || mySampleThing.register()){

                /**
                 * Thing is registered. Set connection to true.
                 */
                Log.i(TAG,"Thing["+mySampleThing.getThingID()+"]  is registered.");

                mySampleThing.setConnected(true,"");
            }
        }
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG,"Ignite Disconnected");
        // start watchdog again here.
        igniteConnected = false;
        startIgniteWatchdog();
    }

    /**
     * Connect to iot ignite
     */

    private void rebuildIgnite(){
        try {
            mIotIgniteManager = new IotIgniteManager.Builder()
                    .setConnectionListener(this)
                    .setContext(appContext)
                    .build();
        } catch (UnsupportedVersionException e) {
            Log.e(TAG, "UnsupportedVersionException :" + e);
        }
    }

    /**
     * remove previous callback and setup new watchdog
     */

    private void startIgniteWatchdog(){
        igniteWatchdog.removeCallbacks(igniteWatchdogRunnable);
        igniteWatchdog.postDelayed(igniteWatchdogRunnable,IGNITE_RECONNECT_INTERVAL);

    }

    @Override
    public void onNodeUnregistered(String s) {

    }

    /**
     * Set all things and nodes connection to offline.
     * When the application close or destroyed.
     */


    public void shutdown(){

        if(mySampleNode!=null){
            if(mySampleThing != null){
                mySampleThing.setConnected(false,"Application Destroyed");
            }
            mySampleNode.setConnected(false,"Application Destroyed");
        }
    }

    @Override
    public void onConfigurationReceived(Thing thing) {

        /**
         * Thing configuration messages will be handled here.
         * For example data sending frequency or custom configuration may be in the incoming thing object.
         */

    }

    @Override
    public void onActionReceived(String s, String s1, ThingActionData thingActionData) {

        /**
         * Thing action message will be handled here. Call thingActionData.getMessage()
         */

    }

    @Override
    public void onThingUnregistered(String s, String s1) {

        /**
         * If your thing object is unregistered from outside world, you will receive this
         * information callback.
         */
    }
}