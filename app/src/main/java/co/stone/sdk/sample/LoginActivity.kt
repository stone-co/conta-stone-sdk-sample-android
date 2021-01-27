package co.stone.sdk.sample

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import co.stone.auth.authFlow.AuthFlowMode
import co.stone.cactus.utils.ktx.toast
import co.stone.conta.AuthAndVerificationResult
import co.stone.conta.VerificationLaunchMode
import co.stone.conta.VerificationParams
import co.stone.conta.internal.UserVerificationResult
import co.stone.sdk.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val LOGIN_RC = 0x1425

        fun launch(source: Activity): Intent = Intent(source, LoginActivity::class.java)
    }

    private val contaStoneSdk by lazy {
        (applicationContext as SampleApp).contaStoneSdk
    }

    private val trash by lazy {
        CompositeDisposable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupLogin()
    }

    private fun setupLogin() {
        /**
         * If the user is already logged and has no Kyc pendencies a session is stored and is possible to proceed without start login flow again
         * */
        proceedToLogin.setOnClickListener {
            handleLoading(show = true)
            trash += isAlreadyLoggedAndCanProceed()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { canProceed ->
                        handleLoading(false)
                        when (canProceed) {
                            true -> launchOptions()
                            false -> proceedToAuthAndVerificationFlow()
                        }
                    },
                    onError = {
                        toast("Error while checking user pendencies")
                        handleLoading(false)
                    }
                )
        }

    }

    private fun handleLoading(show: Boolean) {
        loading.isVisible = show
        proceedToLogin.isVisible = show.not()
    }

    private fun proceedToAuthAndVerificationFlow() {
        contaStoneSdk.startAuthAndVerificationFlowForResult(
            context = this,
            params = VerificationParams(
                launchMode = VerificationLaunchMode.StartingApp,
                authMode = AuthFlowMode.RegisteredUser
            ),
            requestCode = LOGIN_RC
        )
    }

    private fun isAlreadyLoggedAndCanProceed() = Observable.create<Boolean> { emitter ->
        contaStoneSdk.checkUserPendencies(
            onSuccess = { result ->
                when (result) {
                    UserVerificationResult.HasNoPendencies -> emitter.onNext(true)
                    else -> emitter.onNext(false)
                }
            },
            onError = { emitter.tryOnError(it) }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_RC) {
            val result = contaStoneSdk.parseAuthAndVerificationResult(data)

            when (result) {
                is AuthAndVerificationResult.Ok -> toast("User is authenticated").also {
                    launchOptions()
                }
                is AuthAndVerificationResult.MissingData -> toast("some information is missing")
                is AuthAndVerificationResult.UserCancelled -> toast("User cancelled flow")
                is AuthAndVerificationResult.Error -> toast("Error during login ${result.error}")
                is AuthAndVerificationResult.UserLoggedOut -> toast("User requests logout")
                is AuthAndVerificationResult.BlockedUser -> toast("User is blocked")
            }
        }
    }

    private fun launchOptions() {
        startActivity(OptionsActivity.launch(this))
    }
}