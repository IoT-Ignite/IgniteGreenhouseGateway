package com.ardic.android.ignitegreenhouse.ignite;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.managers.DataManager;
import com.ardic.android.ignitegreenhouse.managers.MessageManager;
import com.ardic.android.ignitegreenhouse.managers.ThreadManager;
import com.ardic.android.ignitegreenhouse.operations.NodeThingOperations;
import com.ardic.android.ignitegreenhouse.operations.SensorTypeOperations;
import com.ardic.android.iotignite.callbacks.ConnectionCallback;
import com.ardic.android.iotignite.enumerations.NodeType;
import com.ardic.android.iotignite.enumerations.ThingCategory;
import com.ardic.android.iotignite.enumerations.ThingDataType;
import com.ardic.android.iotignite.exceptions.AuthenticationException;
import com.ardic.android.iotignite.exceptions.UnsupportedVersionException;
import com.ardic.android.iotignite.listeners.NodeListener;
import com.ardic.android.iotignite.listeners.ThingListener;
import com.ardic.android.iotignite.nodes.IotIgniteManager;
import com.ardic.android.iotignite.nodes.Node;
import com.ardic.android.iotignite.things.Thing;
import com.ardic.android.iotignite.things.ThingActionData;
import com.ardic.android.iotignite.things.ThingData;
import com.ardic.android.iotignite.things.ThingType;

import java.util.List;

public class IotIgniteHandler implements ConnectionCallback, NodeListener, ThingListener {

    private static final String TAG = IotIgniteHandler.class.getSimpleName();

    // Static singleton instance
    private static IotIgniteHandler INSTANCE = null;
    private static final long IGNITE_RECONNECT_INTERVAL = 10000L;


    private static final String CONFIG_NODE_ID = "Configurator";
    private static final String CONFIG_THING_ID = "Configurator Thing";

    private IotIgniteManager mIotIgniteManager;
    private boolean igniteConnected = false;
    private Context appContext;
    private Handler igniteWatchdog = new Handler();

    private Node mConfiguratorNode;
    private Thing mConfiguratorThing;

    private Node mRegisterNode;
    private Thing mRegisterThing;

    private Intent getConfIntent = new Intent(Constant.INTENT_FILTER_CONFIG);
    private Intent intents = new Intent(Constant.INTENT_FILTER_IGNITE_STATUS);

    private Thing findThing;


    private static final String INTENT_NODE_NAME = "getConfigPutNodeName";
    private static final String INTENT_THING_NAME = "getConfigPutThingName";
    private static final String INTENT_THING_FREQUENCY = "getConfigPutFrequency";

    private static final String CONFIGURATOR_TYPE_THING = "ARDIC Configurator";
    private static final String CONFIGURATOR_TYPE_VENDOR = "ARDIC";

    private static final String APPLICATION_DESTROYED_STRING = "Application Destroyed";

    private static final String THING_TYPE_FLOAT = "float";
    private static final String THING_TYPE_INTEGER = "integer";
    private static final String THING_TYPE_STRING = "string";

    private MessageManager mMessageManager;
    private DataManager mDataManager;

    private ThingType mConfiguratorThingType = new ThingType(
            CONFIGURATOR_TYPE_THING,
            CONFIGURATOR_TYPE_VENDOR,
            ThingDataType.STRING
    );


    private Runnable igniteWatchdogRunnable = new Runnable() {
        @Override
        public void run() {
            if (!igniteConnected) {
                rebuildIgnite();
                igniteWatchdog.postDelayed(this, IGNITE_RECONNECT_INTERVAL);
                if (Constant.DEBUG) {
                    Log.e(TAG, "Ignite is not connected. Trying to reconnect...");
                }
            } else {
                if (Constant.DEBUG) {
                    Log.e(TAG, "Ignite is already connected.");
                }
            }
        }
    };

    private IotIgniteHandler(Context context) {
        this.appContext = context;

    }

    public static synchronized IotIgniteHandler getInstance(Context appContext) {
        if (INSTANCE == null) {
            INSTANCE = new IotIgniteHandler(appContext);
        }
        return INSTANCE;
    }

    public void start() {
        startIgniteWatchdog();
    }

    @Override
    public void onConnected() {
        if (Constant.DEBUG) {
            Log.i(TAG, "Ignite Connected");
        }
        mMessageManager = MessageManager.getInstance(appContext);
        mDataManager = DataManager.getInstance(appContext);
        igniteWatchdog.removeCallbacks(igniteWatchdogRunnable);
        igniteConnected = true;

        intents.putExtra(Constant.IGNITE_STATUS_BROADCAST, true);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intents);
        if (Constant.DEBUG) {
            Log.i(TAG, "Ignite Send Broadcast (onConnected)");
        }

        updateListener();

        registerConfigurator();

        ThreadManager.getInstance(appContext).threadManager();

    }

    @Override
    public void onDisconnected() {
        if (Constant.DEBUG) {
            Log.i(TAG, "Ignite Disconnected");
        }
        ThreadManager.getInstance(appContext).killAllThread();

        // start watchdog again here.
        igniteConnected = false;
        startIgniteWatchdog();
        intents.putExtra(Constant.IGNITE_STATUS_BROADCAST, false);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intents);
    }

    /**
     * Node Remove
     */
    @Override
    public void onNodeUnregistered(String s) {
        // mMessageManager.removeSavedNode(s);
    }

    @Override
    public void onConfigurationReceived(Thing thing) {

        /**
         * Thing configuration messages will be handled here.
         * For example data sending frequency or custom configuration may be in the incoming thing object.
         */
        getConfIntent.putExtra(INTENT_NODE_NAME, thing.getNodeID());
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

        getConfIntent.putExtra(INTENT_THING_NAME, thing.getThingID());
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

        getConfIntent.putExtra(INTENT_THING_FREQUENCY, thing.getThingConfiguration().getDataReadingFrequency());
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);


    }

    @Override
    public void onActionReceived(String s, String s1, ThingActionData thingActionData) {
        /**
         * Thing action message will be handled here. Call thingActionData.getMessage()
         */
        mMessageManager.receivedConfigMessage(s, s1, thingActionData.getMessage());
        if (Constant.DEBUG) {
            Log.i(TAG, "Action Node : " + s);
            Log.i(TAG, "Action Thing : " + s1);
            Log.i(TAG, "Action Message : " + thingActionData.getMessage());
        }
    }

    @Override
    public void onThingUnregistered(final String s, final String s1) {

        /**
         * If your thing object is unregistered from outside world, you will receive this
         * information callback.
         */
        if (!s.equals(CONFIG_NODE_ID) && !s1.equals(CONFIG_THING_ID)) {
            Thread removeThingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeThingOperations.getInstance(appContext).removeSavedThing(s, s1);
                }
            });
            removeThingThread.run();

            Thread killAllThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ThreadManager.getInstance(appContext).killThread(s, s1);
                }
            });
            killAllThread.run();

        }
        registerConfigurator();

        if (Constant.DEBUG) {
            Log.e(TAG, "Unregister : " + s + ":" + s1);
        }
    }


    /**
     * Connect to iot ignite
     */
    private void rebuildIgnite() {
        try {
            mIotIgniteManager = new IotIgniteManager.Builder()
                    .setConnectionListener(this)
                    .setContext(appContext)
                    .build();
        } catch (UnsupportedVersionException e) {
            if (Constant.DEBUG) {
                Log.e(TAG, "UnsupportedVersionException :" + e);
            }
        }
    }


    /**
     * remove previous callback and setup new watchdog
     */
    private void startIgniteWatchdog() {
        igniteWatchdog.removeCallbacks(igniteWatchdogRunnable);
        igniteWatchdog.postDelayed(igniteWatchdogRunnable, IGNITE_RECONNECT_INTERVAL);

    }

    /**
     * Set all things and nodes connection to offline.
     * When the application close or destroyed.
     */
    public void shutdown() {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (getEveryThing(mNode) != null) {
                    for (Thing mThing : getEveryThing(mNode)) {
                        mThing.setConnected(false, APPLICATION_DESTROYED_STRING);
                        mNode.setConnected(false, APPLICATION_DESTROYED_STRING);
                    }
                }
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }


    /**
     * Configurator Node
     */
    private boolean registerConfiguratorNode() {
        mConfiguratorNode = IotIgniteManager.NodeFactory.createNode(
                CONFIG_NODE_ID,
                CONFIG_NODE_ID,
                NodeType.GENERIC,
                null,
                this
        );

        if (mConfiguratorNode != null) {
            if (Constant.DEBUG) {
                Log.i(TAG, mConfiguratorNode.getNodeID() + " created.");
                Log.i(TAG, mConfiguratorNode.getNodeID() + " is registering...");
            }
            if (mConfiguratorNode.isRegistered() || mConfiguratorNode.register()) {
                ;
                /**
                 * Register node here. If registration is successful, make it online.
                 */
                if (Constant.DEBUG) {
                    Log.i(TAG, mConfiguratorNode.getNodeID() + " is registered successfully. Setting connection true");
                }
                mConfiguratorNode.setNodeListener(this);
                mConfiguratorNode.setConnected(true, "");
                return true;
            }

        }
        return false;
    }

    /**
     * Configurator Thing
     */
    private boolean registerConfiguratorThing() {
        if (mConfiguratorNode != null && mConfiguratorNode.isRegistered()) {
            mConfiguratorThing = mConfiguratorNode.createThing(
                    CONFIG_THING_ID,
                    mConfiguratorThingType,
                    ThingCategory.EXTERNAL,
                    true,
                    this,
                    null
            );
        }

        if (mConfiguratorThing != null) {
            if (Constant.DEBUG) {
                Log.i(TAG, "Creating Thing ");
            }

            if (mConfiguratorThing.isRegistered() || mConfiguratorThing.register()) {
                if (Constant.DEBUG) {
                    Log.i(TAG, "Thing[" + mConfiguratorThing.getThingID() + "]  is registered.");
                }
                mConfiguratorThing.setConnected(true, "");
                mConfiguratorThing.setThingListener(this);
                return true;
            }
            mConfiguratorThing.setThingListener(this);
        }
        return false;
    }

    private void registerConfigurator() {
        if (registerConfiguratorNode() && registerConfiguratorThing()) {

            if (Constant.DEBUG) {
                Log.i(TAG, "Configurator Node and Configurator Thing Created");
            }
        }
    }

    /**
     * Register Nodes
     */
    public boolean registerNode(String getNode) {
        mRegisterNode = IotIgniteManager.NodeFactory.createNode(
                getNode,
                getNode,
                NodeType.GENERIC,
                null,
                this
        );
        if (mRegisterNode != null) {
            if (Constant.DEBUG) {
                Log.i(TAG, mRegisterNode.getNodeID() + " created.");
                Log.i(TAG, mRegisterNode.getNodeID() + " is registering...");
            }
            if (mRegisterNode.isRegistered() || mRegisterNode.register()) {

                /**
                 * Register node here. If registration is successful, make it online.
                 */
                if (Constant.DEBUG) {
                    Log.i(TAG, mRegisterNode.getNodeID() + " is registered successfully. Setting connection true");
                }
                mRegisterNode.setConnected(true, "");
                return true;
            }
        }
        return false;
    }

    /**
     * Register Thing
     */
    public boolean registerThing(String getThingLabel) {
        ThingDataType getDataType = ThingDataType.STRING;

        String[] getSensorType = SensorTypeOperations.getInstance(appContext).getSensorType(NodeThingOperations.getInstance(appContext).getSavedThing(mRegisterNode.getNodeID(), getThingLabel));
        String thingTypeString = null;
        String thingVendorString = null;
        String thingType;

        if (getSensorType != null) {
            thingTypeString = getSensorType[0];
            thingVendorString = getSensorType[1];
            thingType = getSensorType[2];
            if (thingType == THING_TYPE_FLOAT) {
                getDataType = ThingDataType.FLOAT;
            } else if (thingType == THING_TYPE_INTEGER) {
                getDataType = ThingDataType.INTEGER;
            } else if (thingType == THING_TYPE_STRING) {
                getDataType = ThingDataType.STRING;
            }
        }

        if (mRegisterNode != null && mRegisterNode.isRegistered()) {
            if (!TextUtils.isEmpty(thingTypeString) && !TextUtils.isEmpty(thingVendorString)) {
                mRegisterThing = mRegisterNode.createThing(
                        getThingLabel,
                        new ThingType(
                                thingTypeString,
                                thingVendorString,
                                getDataType
                        ),
                        ThingCategory.EXTERNAL,
                        true,
                        this,
                        null
                );
            }
        }

        if (mRegisterThing != null) {
            if (Constant.DEBUG) {
                Log.i(TAG, "Creating Thing ");
            }
            if (mRegisterThing.isRegistered() || mRegisterThing.register()) {
                if (Constant.DEBUG) {
                    Log.i(TAG, "Thing[" + mRegisterThing.getThingID() + "]  is registered.");
                }
                mRegisterThing.setConnected(true, "");
                return true;
            }
        }
        return false;
    }


    /**
     * Send Configurator
     */
    public boolean sendConfiguratorThingMessage(String sendMessage) {
        if (igniteConnected && mConfiguratorThing != null && mConfiguratorThing.isRegistered()) {
            ThingData data = new ThingData();
            data.addData(sendMessage);
            return mConfiguratorThing.sendData(data);
        }
        return false;
    }

    /**
     * Send Data
     */
    public void sendData(final String nodeName, final String thingName, final String value) {
        Thread sendDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(nodeName) && !TextUtils.isEmpty(thingName) && (getThingList(nodeName, thingName) != null)) {
                    findThing = getThingList(nodeName, thingName);
                } else if (!NodeThingOperations.getInstance(appContext).getSavedThing(nodeName, thingName).equals(Constant.PREFERENCES_ADD_SENSOR_NOT_GET) && getThingList(nodeName, thingName) == null) {
                    if (Constant.DEBUG) {
                        Log.e(TAG, "Not Find Cloud : Node : " + nodeName + " : " + thingName);
                    }
                    registerNode(nodeName);
                    registerThing(thingName);
                    registerConfigurator();
                } else if (!(getThingList(nodeName, thingName) == null) && !getThingList(nodeName, thingName).isRegistered()) {
                    if (Constant.DEBUG) {
                        Log.e(TAG, "Not Register Node : " + nodeName + " - Thing : " + thingName);
                    }
                    registerNode(nodeName);
                    registerThing(thingName);
                }

                if (igniteConnected && findThing != null && findThing.isRegistered()) {
                    ThingData data = new ThingData();
                    data.addData(Double.parseDouble(value));
                    if (Constant.DEBUG) {
                        Log.e(TAG, "Send Node : " + nodeName +
                                "\nThing : " + thingName +
                                "\nValue : " + value);
                    }
                    findThing.sendData(data);
                }
            }
        });
        sendDataThread.run();
    }

    /**
     * Listener information, "synchronized"
     */
    private synchronized List<Thing> getEveryThing(Node mNode) {
        if (mNode != null && mNode.isRegistered() && mNode.getEveryThing().size() != 0) {
            return mNode.getEveryThing();
        }
        return null;
    }

    /**
     * Deletes all registered "node" and "thing"
     */
    public void clearAllThing() {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (getEveryThing(mNode) != null) {
                    for (Thing mThing : getEveryThing(mNode)) {
                        mThing.unregister();
                        if (Constant.DEBUG) {
                            Log.e(TAG, "Thing List : " + mNode.getEveryThing());
                        }
                    }
                }
                mNode.unregister();
            }

            registerConfigurator();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns "thing" information back to "node" and "thing"
     */
    public Thing getThingList(String nodeName, String thingName) {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (getEveryThing(mNode) != null) {
                    for (Thing mThing : getEveryThing(mNode)) {
                        if (mNode.getNodeID().equals(nodeName) && mThing.getThingID().equals(thingName)) {
                            return mThing;
                        }
                    }
                }
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Register things and assign listener
     */
    public void updateListener() {
        Thread updateListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Node mNode : IotIgniteManager.getNodeList()) {
                        mNode.setNodeListener(IotIgniteHandler.this);
                        mNode.register();
                        if (getEveryThing(mNode) != null) {
                            for (Thing mThing : getEveryThing(mNode)) {
                                mThing.setThingListener(IotIgniteHandler.this);
                                if (registerNode(mThing.getNodeID()) && registerThing(mThing.getThingID())) {
                                    if (Constant.DEBUG) {
                                        Log.i(TAG, mThing.getNodeID() + "  Node and " + mThing.getThingID() + " Thing Control");
                                    }
                                }

                                getConfIntent.putExtra(INTENT_NODE_NAME, mThing.getNodeID());
                                LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

                                getConfIntent.putExtra(INTENT_THING_NAME, mThing.getThingID());
                                LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

                                getConfIntent.putExtra(INTENT_THING_FREQUENCY, mThing.getThingConfiguration().getDataReadingFrequency());
                                LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);
                            }
                        }
                    }
                    registerConfigurator();
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                }
            }
        });
        updateListenerThread.run();

    }

    /**
     * Returns the configuration information of that thing in the "thing" information given in "Node: Thing" format
     */
    public long getConfigurationTime(String nodeThing) {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (getEveryThing(mNode) != null) {
                    for (Thing mThing : getEveryThing(mNode)) {
                        if (nodeThing.equals(mThing.getNodeID() + ":" + mThing.getThingID())) {
                            if (Constant.DEBUG) {
                                Log.i(TAG, "Configuration desired : " + nodeThing);
                            }
                            return mThing.getThingConfiguration().getDataReadingFrequency();
                        }
                    }
                }
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        return -5;
    }
}