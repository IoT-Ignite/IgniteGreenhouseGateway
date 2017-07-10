package com.ardic.android.ignitegreenhouse.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ardic.android.ignitegreenhouse.DataBase.DevicesDataBase;
import com.ardic.android.ignitegreenhouse.ignite.IotIgniteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acel on 7/7/17.
 */

public class Configuration {

    private static final String TAG = Configuration.class.getSimpleName();

    private List<String> deviceIdList = new ArrayList();
    private List<String> deviceNameList = new ArrayList();

    private String getSensorDeviceId;

    private SharedPreferences sensors ;
    private SharedPreferences.Editor sensorsEditor;

    private static final String ADD_DEVICE_STRING = "addDevice";
    private static final String GET_NODE_ID_STRING = "nodeId";
    private static final String GET_THINGS_STRING = "things";
    private static final String GET_THING_CODE_STRING = "thingCode";
    private static final String GET_THING_LABEL_STRING = "thingId";

    private static final String CONFIGURATION_NODE_STRING="Configurator";
    private static final String CONFIGURATION_THING_STRING="Configurator Thing";

    private static final String RETURN_CREATE_MESSAGE="{\"returnAddDevice\":{\"createNode\":true,\"createThing\":true}}";

    private static final String PREFERENCES_ADD_SENSOR_NOT_GET="N/A";

    private Context mContext;

    private String getConfiguration = "{\"addDevice\":[{\"nodeId\":\"GreenHouse1\",\"things\":[{\"thingCode\":\"f4030687\",\"thingId\":\"Temperature\"},{\"thingCode\":\"10020001\",\"thingId\":\"Humidity\"}]},{\"nodeId\":\"GreenHouse2\",\"things\":[{\"thingCode\":\"1422348f\",\"thingId\":\"Temperature\"},{\"thingCode\":\"1a2c789a\",\"thingId\":\"Humidity\"}]}]}";
    //TODO : 2.faz olarak nod ismi aynı olanları thing olarak kaydetme. bu sayede sınıflama sağlanacak
    //TODO : Bu değerler buluttan config nodunun configthing thinkine acipn mesaj olarak gelecek ve preferense kayıt edilecek
    // TODO : Burada kayıt edilen yerden çekilecek

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


    public void receivedConfigMessage(String recivedNode ,String recivedThing, String receivedMessage ) {
        if (recivedNode.equals(CONFIGURATION_NODE_STRING)){
            if (recivedThing.equals(CONFIGURATION_THING_STRING)){
                addSensorJson(receivedMessage);
            }
        }

    }


    /**
     * For create new Node and Thing
     */
    private void addSensorJson(String getAddJson){
        try {
            JSONObject mGetConfigurationJson = new JSONObject(getConfiguration);


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

                        addJsonControl(addDeviceArray.getString(GET_NODE_ID_STRING), addThingArray.getString(GET_THING_CODE_STRING), addThingArray.getString(GET_THING_LABEL_STRING));
                    }
                }
                IotIgniteHandler.getInstance(mContext).sendConfiguratorThingMessage(RETURN_CREATE_MESSAGE);

                for (String getItems: DevicesDataBase.getInstance(mContext).getItem("GreenHouse1")) {
                    Log.i(TAG,"GET DATABASE : "+ getItems);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
//DevicesDataBase mDevicesDataBase=new DevicesDataBase(mContext);
    private void addJsonControl(String getNode, String getThing, String getThingLabel){
        Log.e(TAG,"Node : " + getNode +
                  "\nThing : " + getThing +
                  "\nThing Label : " +getThingLabel);
        DevicesDataBase.getInstance(mContext).insertDevices(getNode,getThing,getThingLabel);




    }


    /*if (!sensors.getString("saveAddSensor",PREFERENCES_ADD_SENSOR_NOT_GET).equals(PREFERENCES_ADD_SENSOR_NOT_GET)) {
                    Log.i(TAG, "Preferences : " + sensors.getString("saveAddSensor", "N/A"));
}*/


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
