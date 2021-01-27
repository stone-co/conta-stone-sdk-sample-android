package co.stone.sdk.sample

import android.util.Log
import co.stone.logger.Logger

internal class InternalLogger : Logger {
    override fun d(message: String) {
        Log.d("SampleSdk", message)
    }

    override fun e(message: String, e: Throwable?) {
        Log.e("SampleSdk", message, e)
    }

    override fun i(message: String) {
        Log.i("SampleSdk", message)
    }

    override fun v(message: String) {
        Log.v("SampleSdk", message)
    }

    override fun w(message: String) {
        Log.w("SampleSdk", message)
    }
}