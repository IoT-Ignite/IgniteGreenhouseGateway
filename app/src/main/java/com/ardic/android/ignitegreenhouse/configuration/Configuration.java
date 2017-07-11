package com.ardic.android.ignitegreenhouse.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by acel on 7/7/17.
 */

public class Configuration {

    private static final String TAG = Configuration.class.getSimpleName();

    private List<String> deviceIdList = new ArrayList();
    private List<String> deviceNameList = new ArrayList();

    private String getSensorDeviceId;

    private SharedPreferences sensors;
    private SharedPreferences.Editor sensorsEditor;

    private static final String ADD_DEVICE_STRING = "addDevice";
    private static final String GET_NODE_ID_STRING = "nodeId";
    private static final String GET_THINGS_STRING = "things";
    private static final String GET_THING_CODE_STRING = "thingCode";
    private static final String GET_THING_LABEL_STRING = "thingId";

    private static final String CONFIGURATION_NODE_STRING = "Configurator";
    private static final String CONFIGURATION_THING_STRING = "Configurator Thing";

    private static final String RETURN_CREATE_MESSAGE_TRUE = "{\"returnAddDevice\":{\"createNode\":true,\"createThing\":true}}";
    private static final String RETURN_CREATE_MESSAGE_FALSE = "{\"returnAddDevice\": {\"createNode\": false,\"createThing\": false,\"descriptions\":\"The received data have already been registered. Please delete first !\"}}";
    private static final String PREFERENCES_ADD_SENSOR_NOT_GET = "N/A";

    private Context mContext;

    private static Configuration INSTANCE = null;

    private Configuration(Context context) {
        mContext = context;

        sensors = PreferenceManager.getDefaultSharedPreferences(mContext);
        sensorsEditor = sensors.edit();
    }

    public static synchronized Configuration getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new Configuration(context);
        }
        return INSTANCE;
    }


    public Boolean matchDevice(String deviceId) {
        getSensorDeviceId = deviceId;
        if (deviceIdList.contains(deviceId)) {
            return true;
        } else {
            return false;
        }
    }


    public void receivedConfigMessage(String recivedNode, String recivedThing, String receivedMessage) {
        if (recivedNode.equals(CONFIGURATION_NODE_STRING)) {
            if (recivedThing.equals(CONFIGURATION_THING_STRING)) {
                addSensorJson(receivedMessage);
            }
        }

    }


    /**
     * For create new Node and Thing
     */
    private void addSensorJson(String getAddJson) {
        try {
            JSONObject mGetConfigurationJson = new JSONObject(getAddJson);

            if (mGetConfigurationJson != null && mGetConfigurationJson.has(ADD_DEVICE_STRING)) {

                int getAddDataSize = mGetConfigurationJson.getJSONArray(ADD_DEVICE_STRING).length();

                for (int nodeNumber = 0; nodeNumber < getAddDataSize; nodeNumber++) {
                    JSONObject addDeviceArray = new JSONObject(String.valueOf(mGetConfigurationJson.getJSONArray(ADD_DEVICE_STRING).get(nodeNumber)));
                    Log.e(TAG, "GET Node Id : " + addDeviceArray.getString(GET_NODE_ID_STRING));
                    int getAddThingSize = addDeviceArray.getJSONArray(GET_THINGS_STRING).length();

                    for (int thingNumber = 0; thingNumber < getAddThingSize; thingNumber++) {
                        JSONObject addThingArray = new JSONObject(String.valueOf(addDeviceArray.getJSONArray(GET_THINGS_STRING).get(thingNumber)));
                        Log.e(TAG, "GET Thing Code : " + addThingArray.getString(GET_THING_CODE_STRING));
                        Log.e(TAG, "GET Thing Label : " + addThingArray.getString(GET_THING_LABEL_STRING));

                        //TODO : Boolean kontrol yapıp geri mesaj gönderilecek
                        addJsonControl(addDeviceArray.getString(GET_NODE_ID_STRING), addThingArray.getString(GET_THING_CODE_STRING), addThingArray.getString(GET_THING_LABEL_STRING));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addJsonControl(String getNode, String getThing, String getThingLabel) {
        Log.e(TAG, "Node : " + getNode +
                "\nThing : " + getThing +
                "\nThing Label : " + getThingLabel);

        if (!sensors.contains(getNode + ":" + getThingLabel)) {
            sensorsEditor.putString(getNode + ":" + getThingLabel, getThing);
            sensorsEditor.commit();
            Log.i(TAG, getNode + " Node has been added " + getThing + " with the number " + getThingLabel + " thing.");
            IotIgniteHandler.getInstance(mContext).registerNodes(getNode);
            IotIgniteHandler.getInstance(mContext).registerThings(getThingLabel, "Seratonin", "Seraton", "d");
            IotIgniteHandler.getInstance(mContext).sendConfiguratorThingMessage(RETURN_CREATE_MESSAGE_TRUE);
        } else {
            //todo: json olarak çek ve içine "node" ile "thing" i göm
            IotIgniteHandler.getInstance(mContext).sendConfiguratorThingMessage( RETURN_CREATE_MESSAGE_FALSE);
            Log.i(TAG, "The received data have already been registered. Please delete first !");
        }
    }


    public String getSavedDevices(String getNode, String getThingLabel) {
        return sensors.getString(getNode + ":" + getThingLabel, PREFERENCES_ADD_SENSOR_NOT_GET);

    }

    public Map<String, ?> getSavedAllDevices() {
        return sensors.getAll();
    }

    public void removeSavedDevices(String getNode, String getThingLabel) {
        sensorsEditor.remove(getNode + ":" + getThingLabel);
        sensorsEditor.commit();
        Log.e(TAG, "Removed Node : " + getNode +
                "\nThing : " + getThingLabel);
    }

    // Todo : Boolean kontrol ekle
    public void removeSavedNode(String getNode) {
        Map<String, ?> getAllSensor = sensors.getAll();

        Set keys = getAllSensor.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String[] split = key.split(":");
            if (split[0].equals(getNode)) {
                sensorsEditor.remove(key);
                sensorsEditor.commit();
                Log.e(TAG, "Removed Node : " + getNode);
            }
        }
    }

    public void removeSavedAllDevices() {
        sensorsEditor.clear();
        sensorsEditor.commit();
        Log.e(TAG, "Removed All Saved ...");
    }


    public String getDeviceCodeThing(String deviceCode) {
        Map<String, ?> getAllSensor = sensors.getAll();
        Set keys = getAllSensor.keySet();

        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) getAllSensor.get(key);
            if (value.equals(deviceCode)) {
                return key;
            }
        }
        return null;
    }


//TODO : Eğer mesajı alırda işleyemezse geri hata döndürecek
//TODO : Mesajı işlerse ilgili nodun thingine true değeri döndürecek

}

        /*
        //TODO : List Denemesi
        deviceIdList.add("f4030687");
        deviceNameList.add("Temperature");

        deviceIdList.add("10020001");
        deviceNameList.add("Humidity");
        Log.i(TAG,"Get Device String" + deviceIdList);

        // TODO : Map Denemesi
        map1.put("name", "Josh");
        Log.e(TAG,"Mappp : " +map1.get("name"));
        */
