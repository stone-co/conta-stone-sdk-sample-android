package co.stone.sdk.sample

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.GridLayoutManager
import co.stone.auth.authFlow.AuthFlowMode
import co.stone.auth.httpClient.HttpService
import co.stone.cactus.utils.ktx.toast
import co.stone.conta.*
import co.stone.conta.internal.authAndVerification.useCases.SessionResult
import co.stone.conta.internal.authAndVerification.useCases.UserSession
import co.stone.sdk.R
import co.stone.sdk.sample.entries.ActionEntry
import co.stone.sdk.sample.entries.LoadingEntry
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_options.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class OptionsActivity : AppCompatActivity() {

    private val contaStoneSdk by lazy {
        (applicationContext as SampleApp).contaStoneSdk
    }

    private val adapter by lazy {
        GroupAdapter<GroupieViewHolder>().apply {
            spanCount = SINGLE_COLUMN
        }
    }

    private val layoutManager by lazy {
        GridLayoutManager(this, DOUBLE_COLUMNS).apply {
            spanSizeLookup = adapter.spanSizeLookup
        }
    }

    private val trash by lazy {
        CompositeDisposable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        setupViews()
    }

    private fun setupViews() {
        optionsRv.apply {
            adapter = this@OptionsActivity.adapter
            layoutManager = this@OptionsActivity.layoutManager
        }

        logoutBt.setOnClickListener {
            showLoading()
            logout()
        }

        populate()
    }

    private fun populate() {
        adapter.clear()
        adapter.addAll(options)
    }

    private fun handleClick(tag: ActionTag) {
        when (tag) {
            ActionTag.Approver -> launchApprover()
            ActionTag.ChangeAccount -> launchAccountSwitch()
            ActionTag.RegisterNewPin -> launchPinRegisterFlow()
            ActionTag.ChangeAnExistingPin -> launchChangePinFlow()
            ActionTag.EnableOtp -> launchOtpFlow()
            ActionTag.AuthenticatedRequest -> performAuthenticatedRequest()
        }
    }

    /*
    * HttpService provides the environments url
    * */
    private fun performAuthenticatedRequest() {
        val httpService = HttpService.BankingGatewaySandbox
        val client = contaStoneSdk.auth().getOkHttpClient(httpService)

        val request = Request.Builder()
            .url("${httpService.url}/api/v1/institutions")
            .get()
            .build()

        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnMainThread {
                    populate()
                    toast("Error fetching institutions $e")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnMainThread {
                    populate()
                    toast("Request executed successfully")
                }
            }
        }

        client.newCall(request).enqueue(callback)
        showLoading()
    }

    private fun showLoading(){
        adapter.clear()
        adapter.add(LoadingEntry())
    }

    private fun launchOtpFlow() {
        contaStoneSdk.startOtpFlowForResult(
            source = this,
            requestCode = OTP_FLOW_RQ
        )
    }

    private fun launchPinRegisterFlow() {
        contaStoneSdk.startPinFlow(
            source = this,
            requestCode = PIN_FLOW_RQ,
            params = PinParams(PinIntent.RegisterANewPin)
        )
    }

    private fun launchChangePinFlow() {
        contaStoneSdk.startPinFlow(
            source = this,
            requestCode = PIN_FLOW_RQ,
            params = PinParams(PinIntent.ChangeAnExistentPin)
        )
    }

    private fun launchAccountSwitch() {
        contaStoneSdk.startAuthAndVerificationFlowForResult(
            this,
            requestCode = ACCOUNT_SWITCH_RQ,
            params = VerificationParams(
                launchMode = VerificationLaunchMode.AccountSelectionRequest,
                authMode = AuthFlowMode.RegisteredUser
            )
        )
    }

    private fun launchApprover() {
        trash += retrieveLoggedAccount()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { loggedAccount ->
                    contaStoneSdk.startApproverForResult(
                        source = this,
                        requestCode = APPROVER_RQ,
                        params = ApproverParams(loggedAccount, ApproverLaunchMode.SDKLaunchMode)
                    )
                },
                onError = { toast("Error trying to retrieve user logged account info") }
            )
    }

    private fun retrieveLoggedAccount(): Observable<LoggedAccountInfo> =
        Observable.create<LoggedAccountInfo> { emitter ->
            contaStoneSdk.getSession(
                onComplete = { result ->
                    when (result) {
                        is SessionResult.Success -> emitter.onNext(result.session.toLoggedAccountInfo())
                        is SessionResult.HasNoActiveSession -> redirectToLogin()
                    }
                },
                onError = {
                    emitter.tryOnError(it)
                }
            )
        }

    private fun UserSession.toLoggedAccountInfo() = currentAccount?.paymentAccount?.let {
        LoggedAccountInfo(
            id = it.id,
            accountNumber = it.accountCode,
            accountOwner = currentAccount?.owner?.name.orEmpty(),
            accountOwnerDocument = currentAccount?.owner?.document.orEmpty(),
            bankName = StoneOpenBank.institutionName,
            bankNumber = StoneOpenBank.institutionCode,
            branchNumber = StoneOpenBank.agencyCode,
            userLoggedName = profile.fullName
        )
    } ?: throw IllegalStateException("An account must be selected")

    private fun redirectToLogin() {
        startActivity(
            LoginActivity.launch(this)
        ).also { finish() }
    }

    private fun logout() {
        contaStoneSdk.logout {
            if (it != null) {
                populate()
                toast("Error on logout. Try again!")
            } else startActivity(LoginActivity.launch(this))
        }
    }

    private val options = listOf(
        ActionEntry(
            iconRes = R.drawable.ic_money,
            labelRes = R.string.approver_action_label,
            actionTag = ActionTag.Approver,
            onClick = ::handleClick
        ),
        ActionEntry(
            iconRes = R.drawable.ic_transfer,
            labelRes = R.string.change_account_action_label,
            actionTag = ActionTag.ChangeAccount,
            onClick = ::handleClick
        ),
        ActionEntry(
            iconRes = R.drawable.ic_transfer,
            labelRes = R.string.register_pin_action_label,
            actionTag = ActionTag.RegisterNewPin,
            onClick = ::handleClick
        ),
        ActionEntry(
            iconRes = R.drawable.ic_transfer,
            labelRes = R.string.change_pin_account_action_label,
            actionTag = ActionTag.ChangeAnExistingPin,
            onClick = ::handleClick
        ),
        ActionEntry(
            iconRes = R.drawable.ic_transfer,
            labelRes = R.string.enable_otp_action_label,
            actionTag = ActionTag.EnableOtp,
            onClick = ::handleClick
        ),
        ActionEntry(
            iconRes = R.drawable.ic_transfer,
            labelRes = R.string.authenticated_request_action_label,
            actionTag = ActionTag.AuthenticatedRequest,
            onClick = ::handleClick
        )
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = contaStoneSdk.parseAuthAndVerificationResult(data)

        when (requestCode) {
            APPROVER_RQ -> toast("Approver flow result $result")
            ACCOUNT_SWITCH_RQ -> toast("Account Switch result $result")
            PIN_FLOW_RQ -> toast("Pin Flow Result result $result")
            OTP_FLOW_RQ -> toast("Otp Flow Result result $result")
        }
    }

    private fun runOnMainThread(body: () -> Unit) {
        Handler(Looper.getMainLooper()).post { body() }
    }

    companion object {
        const val SINGLE_COLUMN = 1
        const val DOUBLE_COLUMNS = 2
        const val APPROVER_RQ = 0x252
        const val ACCOUNT_SWITCH_RQ = 0x123
        const val PIN_FLOW_RQ = 0x114
        const val OTP_FLOW_RQ = 0x225

        fun launch(source: Activity) = Intent(source, OptionsActivity::class.java)
    }
}