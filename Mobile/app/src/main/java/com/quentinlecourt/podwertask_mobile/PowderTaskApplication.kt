package com.quentinlecourt.podwertask_mobile

import android.app.Application
import com.quentinlecourt.podwertask_mobile.data.api.RetrofitInstance

class PowderTaskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.initialize(this)
    }
}