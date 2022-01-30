package com.example.listview;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ShowToast")
public class MainActivity extends AppCompatActivity implements OnClickListener {
    private static String TAG = MainActivity.class.getSimpleName();
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private TextView et_content;
    private List<String> list;
    private String fanyi;
    private String result;
    private boolean fl;
    private Button bt_zhspeech;
    private SpeechSynthesizer mTts;// 语音合成
    private SpeechRecognizer mIat;// 语音听写
    private Button bt_enwrite;
    private RecognizerDialog iatDialog;//听写动画
    Map<String, String> map = new HashMap<String, String>();
    private ListView lv_list;
    static String flag;
    Button button;
    TextView textView;
    private View view;
    String TAG1 = "MainActivity";
    static String to;//目标译文 可变 zh中文 en英文
    private String[] voiceName = {"xiaoyan", "xiaoyu", "catherine", "henry",
            "vimary", "vixy", "xiaoqi", "vixf", "xiaomei", "xiaolin",
            "xiaorong", "xiaoqian", "xiaokun", "xiaoqiang", "vixying",
            "xiaoxin", "nannan", "vils"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_zhspeech = (Button) findViewById(R.id.button5);//中译英
        bt_enwrite = (Button) findViewById(R.id.button6);//英译中
        lv_list = (ListView) findViewById(R.id.list_view);
        bt_zhspeech.setOnClickListener(this);
        bt_enwrite.setOnClickListener(this);
        list = new ArrayList<>();
        // 初始化即创建语音配置对象，只有初始化后才可以使用MSC的各项服务
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=APPID");
        // 语音合成 1.创建SpeechSynthesizer对象, 第二个参数：本地合成时传InitListener
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this,
                mTtsInitListener);
        // 语音听写1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        mIat = SpeechRecognizer.createRecognizer(this, mTtsInitListener);
        // 1.创建SpeechRecognizer对象，第二个参数：本地听写时传InitListener
        iatDialog = new RecognizerDialog(this,
                mTtsInitListener);
    }

    public void starSpeech() {  //语音合成
        // 2.合成参数设置，详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        mTts.setParameter(SpeechConstant.VOICE_NAME, voiceName[5]);// 设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");// 设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "80");// 设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置云端
        // 设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        // 保存在SD卡需要在AndroidManifest.xml添加写SD卡权限
        // 如果不需要保存合成音频，注释该行代码
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        // 3.开始合成
        mTts.startSpeaking(result, mSynListener);
        // 合成监听器
    }

    /**
     * 初始化参数开始听写
     *
     * @Description:
     */
    private void starWrite() {
        // 2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
        // 语音识别应用领域（：iat，search，video，poi，music）
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        // 接收语言中文
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 接受的语言是普通话
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
        // 设置听写引擎（云端）
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        iatDialog.setListener(mRecognizerDialogListener);
        iatDialog.show();
        Toast.makeText(getApplication(), "请开始说话…", Toast.LENGTH_SHORT).show();
        // 3.开始听写
        //mIat.startListening(mRecoListener);
        // 听写监听器
    }

    /**
     * 语音听写监听
     */
    private RecognizerListener mRecoListener = new RecognizerListener() {
        // 听写结果回调接口(返回Json格式结果，用户可参见附录12.1)；
        // 一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
        // 关于解析Json的代码可参见MscDemo中JsonParser类；
        // isLast等于true时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results);
        }

        // 会话发生错误回调接口
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            if (error.getErrorCode() == 10118) {
                Toast.makeText(getApplicationContext(), "你好像没有说话哦",
                        Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getApplicationContext(), error.getPlainDescription(true),
                    Toast.LENGTH_SHORT).show();

        }// 获取错误码描述}

        // 开始录音
        public void onBeginOfSpeech() {
            Log.d(TAG, "开始说话");
            Toast.makeText(getApplicationContext(), "开始说话",
                    Toast.LENGTH_SHORT).show();
        }

        // 结束录音
        public void onEndOfSpeech() {
            Log.d(TAG, "说话结束");
            Toast.makeText(getApplicationContext(), "说话结束",
                    Toast.LENGTH_SHORT).show();
        }

        // 扩展用接口
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        //音量
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            // TODO Auto-generated method stub
            Log.d(TAG, "当前说话音量大小" + volume);

        }

    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            Toast.makeText(getApplication(), error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
        }

    };
    /**
     * 语音合成监听
     */
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        // 会话结束回调接口，没有错误时，error为null
        public void onCompleted(SpeechError error) {
            if (error != null) {
                Log.d("mySynthesiezer complete code:", error.getErrorCode()
                        + "");
            } else {
                Log.d("mySynthesiezer complete code:", "0");
            }
        }

        // 缓冲进度回调
        // percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }

        // 开始播放
        public void onSpeakBegin() {
        }

        // 暂停播放
        public void onSpeakPaused() {
        }

        // 播放进度回调
        // percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
        }

        // 恢复播放回调接口
        public void onSpeakResumed() {
        }

        // 会话事件回调接口
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
        }
    };

    /**
     * 初始化语音合成监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @SuppressLint("ShowToast")
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                // showTip("初始化失败,错误码：" + code);
                Toast.makeText(getApplicationContext(), "初始化失败,错误码：" + code,
                        Toast.LENGTH_SHORT).show();
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.button5://点击中译英
                flag = "中译英";
                fl = true;
                starWrite();
                break;
            case R.id.button6:
                flag = "英译中";
                fl = true;
                starWrite();
                break;
            default:
                break;
        }

    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        fanyi = resultBuffer.toString();
        if (flag == "中译英") {
            translate();
        } else {
            translate1();
        }
        if (fl == false) {
            list.add(fanyi);
            list.add(result);
            list.add("");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, list);
            lv_list.setAdapter(adapter);
            starSpeech();
        }
        fl = false;
    }

    public void translate() {
        //准备请求百度翻译接口需要的参数
        String word = fanyi;//需查询的单词 q
        String from = "auto";//源语种 en 英语 zh 中文
        //String中英文占用一个字节，中文占用两个字节，
        //利用String的这个存储特性可以用来判断String中有没有中文。
        to = "en";//含有汉字 中译英
        String appid = "appid";//appid 管理控制台有
        String salt = (int) (Math.random() * 100 + 1) + "";//随机数 这里范围是[0,100]整数 无强制要求
        String key = "secret key";//密钥 管理控制台有
        String string1 = appid + word + salt + key;// string1 = appid+q+salt+密钥
        String sign = MD5Utils.getMD5Code(string1);// 签名 = string1的MD5加密 32位字母小写
        Log.d(TAG1, "string1：" + string1);
        Log.d(TAG1, "sign: " + sign);
        Retrofit retrofitBaidu = new Retrofit.Builder()
                .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")
                .addConverterFactory(GsonConverterFactory.create()) // 设置数据解析器
                .build();
        BaiduTranslateService baiduTranslateService = retrofitBaidu.create(BaiduTranslateService.class);
        Call<RespondBean> call = baiduTranslateService.translate(word, from, to, appid, salt, sign);
        call.enqueue(new Callback<RespondBean>() {
            @Override
            public void onResponse(Call<RespondBean> call, Response<RespondBean> response) {
                //请求成功
                Log.d(TAG1, "onResponse: 请求成功");
                RespondBean respondBean = response.body();//返回的JSON字符串对应的对象
                result = respondBean.getTrans_result().get(0).getDst();//获取翻译的字符串String
                Log.d(TAG1, "英译中结果" + result);
            }

            @Override
            public void onFailure(Call<RespondBean> call, Throwable t) {
                //请求失败 打印异常
                Log.d(TAG1, "onResponse: 请求失败 " + t);
            }
        });
    }


    public void translate1() {
        //准备请求百度翻译接口需要的参数
        String word = fanyi;//需查询的单词 q
        String from = "auto";//源语种 en 英语 zh 中文
        //String中英文占用一个字节，中文占用两个字节，
        //利用String的这个存储特性可以用来判断String中有没有中文。
        to = "zh"; //没有汉字 英译中
        String appid = "appid";//appid 管理控制台有
        String salt = (int) (Math.random() * 100 + 1) + "";//随机数 这里范围是[0,100]整数 无强制要求
        String key = "密钥";//密钥 管理控制台有
        String string1 = appid + word + salt + key;// string1 = appid+q+salt+密钥
        String sign = MD5Utils.getMD5Code(string1);// 签名 = string1的MD5加密 32位字母小写
        Log.d(TAG1, "string1：" + string1);
        Log.d(TAG1, "sign: " + sign);
        Retrofit retrofitBaidu = new Retrofit.Builder()
                .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")
                .addConverterFactory(GsonConverterFactory.create()) // 设置数据解析器
                .build();
        BaiduTranslateService baiduTranslateService = retrofitBaidu.create(BaiduTranslateService.class);
        Call<RespondBean> call = baiduTranslateService.translate(word, from, to, appid, salt, sign);
        call.enqueue(new Callback<RespondBean>() {
            @Override
            public void onResponse(Call<RespondBean> call, Response<RespondBean> response) {
                //请求成功
                Log.d(TAG1, "onResponse: 请求成功");
                RespondBean respondBean = response.body();//返回的JSON字符串对应的对象
                result = respondBean.getTrans_result().get(0).getDst();//获取翻译的字符串String
                Log.d(TAG1, "英译中结果" + result);
                //textView.setText(result);
            }

            @Override
            public void onFailure(Call<RespondBean> call, Throwable t) {
                //请求失败 打印异常
                Log.d(TAG1, "onResponse: 请求失败 " + t);
            }
        });
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                view = View.inflate(MainActivity.this, R.layout.item, null);
            } else {
                view = convertView;
            }
            return view;
        }
    }
}
