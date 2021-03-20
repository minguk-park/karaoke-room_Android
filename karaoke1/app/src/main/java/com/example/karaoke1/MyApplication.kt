package com.example.karaoke1

import android.app.Application
import android.bluetooth.BluetoothGatt
import android.content.Context
import com.kakao.auth.*


class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        instance=this
        KakaoSDK.init(KakaoSDKAdapter())
    }

    override fun onTerminate() {
        super.onTerminate()
        instance = null
    }

    class KakaoSDKAdapter : KakaoAdapter() {
        override fun getSessionConfig(): ISessionConfig {
            return object : ISessionConfig {
                override fun getAuthTypes(): Array<AuthType> {
                    return arrayOf(AuthType.KAKAO_LOGIN_ALL)
                }

                override fun isUsingWebviewTimer(): Boolean {
                    return false
                }

                override fun isSecureMode(): Boolean {
                    return false
                }

                override fun getApprovalType(): ApprovalType? {
                    return ApprovalType.INDIVIDUAL
                }

                override fun isSaveFormData(): Boolean {
                    return true
                }
            }
        }

        // Application이 가지고 있는 정보를 얻기 위한 인터페이스
        override fun getApplicationConfig(): IApplicationConfig {
            return IApplicationConfig { getGlobalApplicationContext() }
        }
    }


    companion object{
        private var instance:MyApplication?=null
        lateinit var mGatt: BluetoothGatt
        fun applicationContext():Context{
            return instance!!.applicationContext
        }
        fun getGlobalApplicationContext(): MyApplication? {
            checkNotNull(instance) { "This Application does not inherit com.kakao.GlobalApplication" }
            return instance
        }

    }
}