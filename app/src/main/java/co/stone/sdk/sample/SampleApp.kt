package co.stone.sdk.sample

import android.app.Application
import co.stone.auth.AppInfo
import co.stone.auth.DeepLinkUris
import co.stone.auth.authFlow.AuthFlowUIConfig
import co.stone.auth.httpClient.HttpClientConfig
import co.stone.conta.ContaStone
import co.stone.conta.Environment
import co.stone.sdk.BuildConfig
import co.stone.sdk.R
import java.net.URI

class SampleApp : Application() {
    /**
     * There's three different environments:
     * - Environment.Homologue
     * - Environment.Sandbox
     * - Environment.Production
     *
     * For testing purposes choose Environment.Sandbox
     * */
    private val environment = Environment.Sandbox

    val contaStoneSdk by lazy {
        ContaStone.initialize(
            application = this,
            environment = environment,
            appInfo = AppInfo(
                name = "Conta Stone Sample App",
                applicationId = BuildConfig.APPLICATION_ID,
                buildId = BuildConfig.BUILD_TYPE,
                version = BuildConfig.VERSION_NAME
            ),
            authFlowUIConfig = AuthFlowUIConfig(themeId = R.style.Theme_ContaStoneSdkSample),
            clientId = "myapp@example.com.br", // TODO Replace to your client id
            deepLinkUris = DeepLinkUris(
                uriLogout = "sample://uri.logout",
                uriChat = "sample://uri.chat",
                uriDashboard = "sample//uri.dashboard",
                uriHelp = "sample://uri.help",
                uriKyc = "sample://uri.kyc",
                uriUpdateApp = "sample://uri.update.app"
            ),
            httpClientConfig = HttpClientConfig(),
            logger = InternalLogger(),
            tokenKeyMasterUri =  URI("android-keystore://stone-mobile")
        )
    }
}