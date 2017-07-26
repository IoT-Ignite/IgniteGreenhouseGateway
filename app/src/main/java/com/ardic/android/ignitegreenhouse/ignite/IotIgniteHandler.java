package com.ardic.android.ignitegreenhouse.ignite;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.constants.Constant;
import com.ardic.android.ignitegreenhouse.managers.MessageManager;
import com.ardic.android.ignitegreenhouse.managers.ThreadManager;
import com.ardic.android.ignitegreenhouse.utils.NodeThingUtils;
import com.ardic.android.ignitegreenhouse.utils.SensorTypeUtils;
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

import java.util.Collections;
import java.util.List;

public class IotIgniteHandler implements ConnectionCallback, NodeListener, ThingListener {

    private static final String TAG = IotIgniteHandler.class.getSimpleName();

    // Static singleton instance
    private static IotIgniteHandler instance = null;
    private static final long IGNITE_RECONNECT_INTERVAL = 10000L;

    private boolean igniteConnected = false;
    private Context appContext;
    private Handler igniteWatchdog = new Handler();

    /**
     * Node & Thing
     */
    private Node mConfiguratorNode;
    private Thing mConfiguratorThing;

    private Node mRegisterNode;
    private Thing mRegisterThing;

    private Thing findThing;

    private Intent getConfIntent = new Intent(Constant.INTENT_FILTER_CONFIG);
    private Intent intents = new Intent(Constant.INTENT_FILTER_IGNITE_STATUS);

    private MessageManager mMessageManager;

    private ThingType mConfiguratorThingType = new ThingType(
            Constant.CONFIGURATOR_TYPE_THING,
            Constant.CONFIGURATOR_TYPE_VENDOR,
            ThingDataType.STRING
    );


    private Runnable igniteWatchdogRunnable = new Runnable() {
        @Override
        public void run() {
            if (!igniteConnected) {
                try {
                    new IotIgniteManager.Builder().setConnectionListener(IotIgniteHandler.this).setContext(appContext).build();
                } catch (UnsupportedVersionException e) {
                    if (Constant.DEBUG)
                        Log.e(TAG, "rebuildIgnite UnsupportedVersionException :" + e);
                }
                igniteWatchdog.postDelayed(this, IGNITE_RECONNECT_INTERVAL);
                if (Constant.DEBUG)
                    Log.e(TAG, "Ignite is not connected. Trying to reconnect...");
            } else {
                if (Constant.DEBUG)
                    Log.e(TAG, "Ignite is already connected.");
            }
        }
    };

    /**
     * Connect to iot ignite
     * <p>
     * private void rebuildIgnite() {
     * try {
     * mIotIgniteManager = new IotIgniteManager.Builder()
     * .setConnectionListener(this)
     * .setContext(appContext)
     * .build();
     * getAllNode();
     * } catch (UnsupportedVersionException e) {
     * if (Constant.DEBUG) {
     * Log.e(TAG, "rebuildIgnite UnsupportedVersionException :" + e);
     * }
     * }
     * }
     */

    private IotIgniteHandler(Context context) {
        this.appContext = context;

    }

    public static synchronized IotIgniteHandler getInstance(Context appContext) {
        if (instance == null) {
            instance = new IotIgniteHandler(appContext);
        }
        return instance;
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
        igniteWatchdog.removeCallbacks(igniteWatchdogRunnable);
        igniteConnected = true;

        intents.putExtra(Constant.INTENT_FILTER_IGNITE_STATUS_VALUE_NAME, true);
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
        intents.putExtra(Constant.INTENT_FILTER_IGNITE_STATUS_VALUE_NAME, false);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intents);
    }

    /**
     * Node Remove
     */
    @Override
    public void onNodeUnregistered(String s) {
        NodeThingUtils.getInstance(appContext).removeSavedNode(s);
        unRegister(s);
    }

    @Override
    public void onConfigurationReceived(Thing thing) {

        /**
         * Thing configuration messages will be handled here.
         * For example data sending frequency or custom configuration may be in the incoming thing object.
         */
        getConfIntent.putExtra(Constant.INTENT_NODE_NAME, thing.getNodeID());
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

        getConfIntent.putExtra(Constant.INTENT_THING_NAME, thing.getThingID());
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

        getConfIntent.putExtra(Constant.INTENT_THING_FREQUENCY, thing.getThingConfiguration().getDataReadingFrequency());
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
        if (!s.equals(Constant.CONFIGURATION_NODE_NAME) && !s1.equals(Constant.CONFIGURATION_THING_NAME)) {
            Thread removeThingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeThingUtils.getInstance(appContext).removeSavedThing(s, s1);
                }
            });
            removeThingThread.start();

            Thread killAllThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ThreadManager.getInstance(appContext).killThread(s, s1);
                }
            });
            killAllThread.start();

        }
        registerConfigurator();

        if (Constant.DEBUG) {
            Log.e(TAG, "Unregister : " + s + ":" + s1);
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
     * Configurator Node
     */
    private boolean registerConfiguratorNode() {
        mConfiguratorNode = IotIgniteManager.NodeFactory.createNode(
                Constant.CONFIGURATION_NODE_NAME,
                Constant.CONFIGURATION_NODE_NAME,
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
                    Constant.CONFIGURATION_THING_NAME,
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
        if (registerConfiguratorNode() && registerConfiguratorThing() && Constant.DEBUG) {
            Log.i(TAG, "Configurator Node and Configurator Thing Created");
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
        registerThingType(getThingLabel);

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

    private void registerThingType(String getThingLabel){
        ThingDataType getDataType = ThingDataType.STRING;
        if (mRegisterNode != null) {
            String[] getSensorType = SensorTypeUtils.getInstance(appContext).getSensorType(NodeThingUtils.getInstance(appContext).getSavedThing(mRegisterNode.getNodeID(), getThingLabel));

            String thingTypeString = null;
            String thingVendorString = null;
            String thingType;

            if (getSensorType != null) {
                thingTypeString = getSensorType[0];
                thingVendorString = getSensorType[1];
                thingType = getSensorType[2];
                if (thingType == Constant.THING_TYPE_FLOAT) {
                    getDataType = ThingDataType.FLOAT;
                } else if (thingType == Constant.THING_TYPE_INTEGER) {
                    getDataType = ThingDataType.INTEGER;
                } else if (thingType == Constant.THING_TYPE_STRING) {
                    getDataType = ThingDataType.STRING;
                }
            }

            if (mRegisterNode.isRegistered()
                    && !TextUtils.isEmpty(thingTypeString)
                    && !TextUtils.isEmpty(thingVendorString)) {

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
                sendDataControl(nodeName,thingName);
                if (igniteConnected && findThing != null && findThing.isRegistered()) {
                    ThingData data = new ThingData();
                    data.addData(Double.parseDouble(value));
                    if (Constant.DEBUG)
                        Log.e(TAG, "Send Node : " + nodeName + "\nThing : " + thingName +"\nValue : " + value);
                    findThing.sendData(data);
                }
            }
        });
        sendDataThread.start();
    }

    private void sendDataControl(String nodeName, String thingName){
        if (!TextUtils.isEmpty(nodeName) && !TextUtils.isEmpty(thingName) && (getThingList(nodeName, thingName) != null)) {
            findThing = getThingList(nodeName, thingName);
        } else if (!NodeThingUtils.getInstance(appContext).getSavedThing(nodeName, thingName).equals(Constant.PREFERENCES_ADD_SENSOR_NOT_GET) && getThingList(nodeName, thingName) == null) {
            if (Constant.DEBUG) {
                Log.e(TAG, "Not Find Cloud : Node : " + nodeName + " : " + thingName);
            }
            registration(nodeName,thingName);
        } else if (getThingList(nodeName, thingName) != null && !getThingList(nodeName, thingName).isRegistered()) {
            if (Constant.DEBUG) {
                Log.e(TAG, "Not Register Node : " + nodeName + " - Thing : " + thingName);
            }
            registration(nodeName,thingName);
        }
    }

    private void registration(String nodeName, String thingName){
        registerNode(nodeName);
        registerThing(thingName);
        registerConfigurator();
    }
    /**
     * Listener information, "synchronized"
     */
    private synchronized List<Thing> getEveryThing(Node mNode) {
        if (mNode != null && mNode.isRegistered() && !mNode.getEveryThing().isEmpty()) {
            return mNode.getEveryThing();
        }
        return Collections.emptyList();
    }


    /**
     * Returns "thing" information back to "node" and "thing"
     */
    public Thing getThingList(String nodeName, String thingName) {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (!getEveryThing(mNode).isEmpty() && returnThing(mNode, nodeName, thingName) != null) {
                    return returnThing(mNode, nodeName, thingName);
                }
            }
        } catch (AuthenticationException e) {
            Log.e(TAG, "getThingList Error : " + e);
        }
        return null;
    }

    private Thing returnThing(Node mNode, String nodeName, String thingName) {
        for (Thing mThing : getEveryThing(mNode)) {
            if (mNode != null && mNode.getNodeID().equals(nodeName) && mThing.getThingID().equals(thingName)) {
                return mThing;
            }
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
                        if (!getEveryThing(mNode).isEmpty()) {
                            updateListener(mNode);
                        }
                    }
                    registerConfigurator();
                } catch (AuthenticationException e) {
                    Log.e(TAG, "updateListener Error : " + e);
                }
            }
        });
        updateListenerThread.start();
    }

    private void updateListener(Node mNode) {
        for (Thing mThing : getEveryThing(mNode)) {
            mThing.setThingListener(IotIgniteHandler.this);
            if (registerNode(mThing.getNodeID())
                    && registerThing(mThing.getThingID())
                    && Constant.DEBUG) {
                Log.i(TAG, mThing.getNodeID() + "  Node and " + mThing.getThingID() + " Thing Control");
            }

            getConfIntent.putExtra(Constant.INTENT_NODE_NAME, mThing.getNodeID());
            LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

            getConfIntent.putExtra(Constant.INTENT_THING_NAME, mThing.getThingID());
            LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);

            getConfIntent.putExtra(Constant.INTENT_THING_FREQUENCY, mThing.getThingConfiguration().getDataReadingFrequency());
            LocalBroadcastManager.getInstance(appContext).sendBroadcast(getConfIntent);
        }
    }

    /**
     * Returns the configuration information of that thing in the "thing" information given in
     * "Node: Thing" format
     */
    public long getConfigurationTime(String nodeThing) {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (getEveryThing(mNode).isEmpty() && getConfigurationTime(mNode, nodeThing) != -5) {
                    return getConfigurationTime(mNode, nodeThing);
                }
            }
        } catch (AuthenticationException e) {
            Log.e(TAG, "Get Configuration Time Error : " + e);
        }
        return -5;
    }

    private long getConfigurationTime(Node mNode, String nodeThing) {
        for (Thing mThing : getEveryThing(mNode)) {
            if (nodeThing.equals(mThing.getNodeID() + ":" + mThing.getThingID())) {
                if (Constant.DEBUG) {
                    Log.i(TAG, "Configuration desired : " + nodeThing);
                }
                return mThing.getThingConfiguration().getDataReadingFrequency();
            }
        }
        return -5;
    }


    /**
     * Fuse unRegister
     */
    public void unRegister(String nodeId) {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (getEveryThing(mNode) != null && mNode.getNodeID().equals(nodeId) && mNode.isRegistered()) {
                    mNode.unregister();
                }
            }
        } catch (AuthenticationException e1) {
            Log.e(TAG, "unRegister Error (Node): " + e1);
        }
    }

    public void unRegister(String nodeId, String thingId) {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (getEveryThing(mNode) != null) {
                    unRegister(mNode, nodeId, thingId);
                }
            }
        } catch (AuthenticationException e1) {
            Log.e(TAG, "Un Register Error (Node - Thing) : " + e1);
        }
    }

    private void unRegister(Node mNode, String nodeId, String thingId) {
        for (Thing mThing : getEveryThing(mNode)) {
            if (mNode != null && mNode.getNodeID().equals(nodeId) && mThing.getThingID().equals(thingId)) {
                mThing.unregister();
            }
        }
    }


    /**
     * Set all things and nodes connection to offline.
     * When the application close or destroyed.
     */
    public void shutdown() {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (mNode != null) {
                    aRegistrationsOperations(mNode, 1);
                    mNode.setConnected(false, Constant.APPLICATION_DESTROYED_STRING);
                }
            }
        } catch (AuthenticationException e) {
            Log.e(TAG, "shutdown Error : " + e);
        }
    }

    /**
     * Deletes all registered "node" and "thing"
     */
    public void clearAllThing() {
        try {
            for (Node mNode : IotIgniteManager.getNodeList()) {
                if (mNode != null) {
                    aRegistrationsOperations(mNode, 0);
                    mNode.setConnected(false, Constant.APPLICATION_DESTROYED_STRING);
                    mNode.unregister();
                }
            }
            registerConfigurator();
        } catch (AuthenticationException e) {
            Log.e(TAG, "clearAllThing Error : " + e);
        }
    }

    private void aRegistrationsOperations(Node mNode, int operationNumber) {
        List<Thing> thingList = getEveryThing(mNode);
        if (thingList != null && thingList.isEmpty()) {
            for (Thing mThing : getEveryThing(mNode)) {
                if (operationNumber == 0) {
                    mThing.setConnected(false, Constant.APPLICATION_DESTROYED_STRING);
                    mThing.unregister();
                } else if (operationNumber == 1) {
                    mThing.setConnected(false, Constant.APPLICATION_DESTROYED_STRING);
                }
            }
        }
    }

}