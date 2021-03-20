package com.example.karaoke1


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.kakao.auth.ApiErrorCode
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException
import com.kakao.util.helper.Utility
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private var sessionCallback: SessionCallback?=null
    //private var sessionCallback: SessionCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sessionCallback=SessionCallback()
        Log.d("start activity", "YES")
        Session.getCurrentSession().addCallback(sessionCallback)
        //val intent=Intent(this,ConnectActivity::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        Session.getCurrentSession().removeCallback(sessionCallback)
    }

    private class SessionCallback : ISessionCallback{
        override fun onSessionOpened() {
            requestMe()
        }
        private fun requestMe(){
            UserManagement.getInstance()
                    .me(object : MeV2ResponseCallback() {
                        override fun onSessionClosed(errorResult: ErrorResult) {
                            Toast.makeText(MyApplication.applicationContext(), "세션이 닫혔습니다. 다시 시도해 주세요: " + errorResult!!.errorMessage, Toast.LENGTH_LONG).show()
                        }

                        override fun onFailure(errorResult: ErrorResult) {
                            if (errorResult!!.errorCode == ApiErrorCode.CLIENT_ERROR_CODE) {
                                Toast.makeText(MyApplication.applicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(MyApplication.applicationContext(), "로그인 도중 오류가 발생했습니다: " + errorResult.errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onSuccess(result: MeV2Response) {
                            Toast.makeText(MyApplication.applicationContext(), "Login success", Toast.LENGTH_LONG).show()
                            val intent=Intent(MyApplication.applicationContext(),HomeActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(MyApplication.applicationContext(),intent,null)
                        }
                    })
        }
        override fun onSessionOpenFailed(exception: KakaoException?) {
            Toast.makeText(MyApplication.applicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: " + exception.toString(), Toast.LENGTH_SHORT).show();
        }

        @Suppress("DEPRECATION")
        public class JsonLogin: AsyncTask<String, String, String>() {
            override fun doInBackground(vararg strings: String?): String? {
                val arr = arrayOfNulls<String>(strings.size)
                for (i in strings.indices) {
                    arr[i] = strings[i]
                }
                val ob = JSONObject()
                var con: HttpURLConnection? = null
                var reader: BufferedReader? = null
                try {
                    ob.accumulate("name", arr[0])
                    ob.accumulate("email", arr[1])
                    try {
                        val url = URL("http://175.118.28.138/kakao")
                        //URL url = new URL(urls[0]);
                        con = url.openConnection() as HttpURLConnection
                        con.requestMethod = "POST" //post 방식
                        con.setRequestProperty("Cache-Control", "no-cache") //캐시 설정
                        con.setRequestProperty("Content-Type", "application/json") // json형태로 전송
                        con.setRequestProperty("Accept", "text/html") //서버에 response 데이터를 html로 받음
                        con.doOutput = true //OutStream으로 post데이터를 넘겨주겠다는 의미
                        con.doInput = true //InputStream으로 서버의 응답을 받겠다는 의미
                        con.connect()

                        val outStream = con.outputStream //스트림 생성
                        val writer = BufferedWriter(OutputStreamWriter(outStream))
                        writer.write(ob.toString())
                        writer.flush()
                        writer.close()

                        val stream = con.inputStream
                        reader = BufferedReader(InputStreamReader(stream))

                        val buffer = StringBuffer()
                        var line: String? = ""
                        while (reader.readLine().also { line = it } != null) {
                            buffer.append(line)
                        }
                        return buffer.toString()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    //종료가 되면 disconnect메소드를 호출한다.
                    con?.disconnect()
                    try {
                        //버퍼를 닫아준다.
                        reader?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return null
            }

            //doInBackground메소드가 끝나면 여기로 와서 텍스트뷰의 값을 바꿔준다.
            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
            }
        }
    }
}