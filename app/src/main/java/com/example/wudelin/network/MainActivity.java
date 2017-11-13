package com.example.wudelin.network;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button xmlPull;
    private Button xmlSAX;
    private Button jsonObject;
    private Button jsonGson;
    private static TextView responseText;
    private static final String xmlAddress = "http://114.67.224.207:8080/Okhttp/get_data.xml";
    private static final String jsonAddress = "http://114.67.224.207:8080/Okhttp/get_data.json";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xmlPull = this.findViewById(R.id.xml_pull);
        xmlSAX = this.findViewById(R.id.xml_sax);
        jsonObject = this.findViewById(R.id.json_jsonobject);
        jsonGson = this.findViewById(R.id.json_gson);
        responseText = this.findViewById(R.id.response_text);
        xmlPull.setOnClickListener(this);
        xmlSAX.setOnClickListener(this);
        jsonObject.setOnClickListener(this);
        jsonGson.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.xml_pull:{
                HttpUtil.sendHttpRequest(xmlAddress, new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        parseXMLWithPull(response);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            }break;
            case R.id.xml_sax:{
                HttpUtil.sendHttpRequest(xmlAddress, new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        parseXMLWithSAX(response);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

            }break;
            case R.id.json_jsonobject:{
                HttpUtil.sendHttpRequest(jsonAddress, new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        parseJSONWithJSONObject(response);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }break;
            case R.id.json_gson:{
               HttpUtil.sendOkHttpRequest(jsonAddress, new okhttp3.Callback() {
                   @Override
                   public void onFailure(Call call, IOException e) {

                   }

                   @Override
                   public void onResponse(Call call, Response response) throws IOException {
                       String text = response.body().string();
                       //Log.d("MainActivity", "onResponse: "+text);
                       parseJSONWithGSON(text);
                   }
               });
            }break;
            default:break;
        }
    }

    private void parseJSONWithGSON(String response) {
        Gson gson = new Gson();
        List<App> list = gson.fromJson(response,new TypeToken<List<App>>(){}.getType());
        StringBuilder stringBuilder = new StringBuilder();
        for(App app:list){
            stringBuilder.append(app.getId()+" "+app.getName()+" "+app.getName()+"\n");
        }
        Message message = new Message();
        message.what = 2;
        Bundle bundle = new Bundle();
        bundle.putString("response",stringBuilder.toString());
        message.setData(bundle);
        myHandler.sendMessage(message);
        stringBuilder.setLength(0);

    }

    private void parseJSONWithJSONObject(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0;i<jsonArray.length();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                stringBuilder.append(jsonObject.getString("id")+"\n");
                stringBuilder.append(jsonObject.getString("name")+"\n");
                stringBuilder.append(jsonObject.getString("version")+"\n");
            }
            Message message = new Message();
            message.what = 1;
            Bundle bundle = new Bundle();
            bundle.putString("response",stringBuilder.toString());
            message.setData(bundle);
            myHandler.sendMessage(message);
            stringBuilder.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseXMLWithSAX(String response) {

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();
            ContentHandler contentHandler = new ContentHandler();
            reader.setContentHandler(contentHandler);
            reader.parse(new InputSource(new StringReader(response)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    private static Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:{
                   Bundle bundle = msg.getData();
                   String response = bundle.getString("response");
                   responseText.append(response+"\n");
                } break;
                case 1:{
                    Bundle bundle = msg.getData();
                    String response = bundle.getString("response");
                    responseText.append(response+"\n");
                } break;
                case 2:{
                    Bundle bundle = msg.getData();
                    String response = bundle.getString("response");
                    responseText.append(response+"\n");
                } break;
                default:break;
            }
        }
    };
    private void parseXMLWithPull(String response) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser pullParser = factory.newPullParser();
            pullParser.setInput(new StringReader(response));
            int evenType = pullParser.getEventType();
            String id = "";
            String name = "";
            while(evenType!=XmlPullParser.END_DOCUMENT){
                String nodeName = pullParser.getName();
                switch (evenType){
                    case XmlPullParser.START_TAG:{
                        if("id".equals(nodeName))
                            id = pullParser.nextText();
                        else if("name".equals(nodeName))
                            name = pullParser.nextText();
                    }break;
                    case XmlPullParser.END_TAG:{
                        if("app".equals(nodeName)){
                            Message msg = myHandler.obtainMessage();
                            msg.what = 0; //消息标识
                            Bundle bundle = new Bundle();
                            bundle.putString("response",id+" "+name);
                            msg.setData(bundle);
                            myHandler.sendMessage(msg); //发送消息
                        }
                    }break;
                    default:break;
                }
                evenType = pullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
