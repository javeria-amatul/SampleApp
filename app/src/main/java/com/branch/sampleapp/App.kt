package com.branch.sampleapp

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import io.branch.referral.Branch
import java.util.*

class App : Application(), LifecycleObserver {

    var TAG = "App"

    init {
        instance = this
    }

    companion object {
        private var sessionId: String? = null

        fun getAppSessionId(): String {
            if (sessionId == null) sessionId = UUID.randomUUID().toString()
            return sessionId!!
        }

        fun clearAppSessionId() {
            this.sessionId = null
        }

        private var instance: App? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun application(): Application {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        setUpBranchIO()
    }

    private fun setUpBranchIO() {
        try {
            //BranchIO
            // Branch logging for debugging
            if (BuildConfig.DEBUG) {
                Branch.enableLogging()
                Branch.enableTestMode()
            } else {
                Branch.disableTestMode()
            }
            // Branch object initialization
            Branch.getAutoInstance(this)
        } catch (ex: Exception) {
            Log.e(TAG, ex.toString())
        }
    }
}