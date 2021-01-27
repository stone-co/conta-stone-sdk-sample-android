# Conta Stone Sdk Sample
Stable version: ![version](https://img.shields.io/badge/version-3.0.87-blue)

 A Conta Stone SDK é responsável por todo o fluxo de login e Know Your Customer da [Stone Openbank API](https://docs.openbank.stone.com.br/docs/overview-guides).

## Contato
 
 Se durante a integração encontrar algum problema ou ponto de melhoria abra uma [issue](https://github.com/stone-co/conta-stone-sdk-sample-android/issues) neste repositório.

## Como funciona
A Conta Stone SDK é o ponto de entrada para acesso às nossas SDKs de autenticação, aprovação e verificação de [KYC](https://en.wikipedia.org/wiki/Know_your_customer) (_Know your constumer_).

- A SDK de autenticação é responsável por executar todo o processo de autenticação seguindo as diretrizes do [oAuth2](https://oauth.net/2/).

- A SDK de aprovação é responsável pelo processo de aprovação e rejeição de transações.

- A SDK de KYC é responsável por capturar as informações da usuária que está realizando cadastro na conta.

## Como integrar com a Conta Stone SDK

Antes de começar a integração é necessário obter uma chave de acesso ao repositório, pois trata-se de um repositório privado. Para obter esta chave entre em contato com a gente. 

No arquivo `build.gradle` do projeto adicione a URL do repositório e substitua o `{access-key}` pela chave fornecida.

```
maven { url "https://packagecloud.io/priv/${access-key}/Stone/stoneid/maven2"}
```
Também é preciso adicionar o Jetpack, pois usamos algumas libs que estão disponíveis neste repositório
```
maven { url 'https://jitpack.io' }
```

Importe a dependência da Conta Stone SDK

```
implementation 'co.stone:conta:${latest_version}'
```

A Conta Stone SDK utiliza o Firebase para implementar alguns de seus serviços, logo é necessário ter um projeto no firebase e integrá-lo no seu app. Para mais informações sobre como adicionar o Firebase ao seu projeto acesse este [link](https://firebase.google.com/docs/android/setup).

Sincronize o projeto e pronto, já é possível utilizar a Conta Stone SDK!

### Inicializando a SDK
Uma vez que a dependência foi importada o passo seguinte da integração é inicializar a SDK no seu app, para isso é necessário especificar os parâmetros abaixo.

```
private val environment = Environment.Sandbox

 ContaStone.initialize(
            application = application,
            environment = environment,
            appInfo = AppInfo(
                name = "Conta Stone Sample App",
                applicationId = BuildConfig.APPLICATION_ID,
                buildId = BuildConfig.BUILD_TYPE,
                version = BuildConfig.VERSION_NAME
            ),
            authFlowUIConfig = AuthFlowUIConfig(themeId = R.style.Theme_ContaStoneSdkSample),
            clientId = "myapp@example.com.br",
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
```

`application`: Trata-se da instância do Application do seu app.

`environment`: Existem 3 ambientes para os quais a sdk pode apontar: `Homolog`, `Sandbox` e `Production` para fins de teste use o `Environment.Sandbox`

`appInfo`: Informações referentes ap app como nome, versão e buildId.

`client_id`: Identificador fornecido pelo time de suporte OpenBank.

`authFlowUIConfig`: Com esta config é possível passar o tema da sua aplicação para a SDK, assim as cores principais do estilo do seu app serão aplicadas nas telas internas da SDK. 

`httpClientConfig`: Trata-se de configurações HTTP customizadas do cliente, como `interceptors`, `connectionTimeoutMs`, `readTimeoutMs`, `writeTimeoutMs`, `networkInterceptors`.

`tokenKeyMasterUri`: Uma URI para a chave mestra no formato `android-keystore://`. Essa chave será utilizada para acessar o Keystore do android e salvar o token da usuária de forma segura. 

`deepLinkUris`: Uris usadas para fazer a navegação para Activities externas a sdk. Exemplo, a `uriLogout` se refere a Activity que a usuária deve ser redirecionada depois que ela for deslogada do app, essa navegação é feita por deeplink.

### Iniciando fluxo de Autenticação e verificação de KYC

Para iniciar o fluxo de autenticação e verificação de KYC é necessário chamar o método abaixo.

```
contaStoneSdk.startAuthAndVerificationFlowForResult(
            context = this,
            params = VerificationParams(
                launchMode = VerificationLaunchMode.StartingApp,
                authMode = AuthFlowMode.RegisteredUser
            ),
            requestCode = LOGIN_RC
        )
```

O atributo `params` recebe dois argumentos: O modo de inicialização, o qual define como que o fluxo deve ser iniciado e o `authMode` que neste caso sempre vai ser `AuthFlowMode.RegisteredUser`. O modo de inicialização pode ser: 

`StartingApp` - Inicia todo o fluxo de autenticação e check de KYC

`AccountSelectionRequest` - Exibe a tela de troca de conta e lida com a escolha da usuária nos casos em que a usuária em questão possui mais de uma conta de pagamento. 

`PushNotificationReceived` - Deve ser iniciado ao receber um push notification referente ao processo de abertura de conta.

`NewAccountCreated` - Deve ser chamado após a criação de uma nova conta de pagamento para que seja verificado se esta conta não possui nenhuma pendência em seu cadastro. 

Este método inicia a Activity principal da SDK e executa os fluxos internos de autenticação e verificação de KYC. Quando o processo é finalizado a Conta Stone SDK retorna um `Result` através do `onActivityResult` da Activity que deve ser tratado pelo app.

### Lidando com o resultado da sdk

Ao finalizar o fluxo de autenticação e verificação a SDK emite um resultado para o app informando o desfecho do fluxo. Segue abaixo um exemplo de como tratar o resultado emitido pela `conta-stone-sdk` e o que cada um significa. 
```
 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == LOGIN_RC) {
             val result = contaStoneSdk.parseAuthAndVerificationResult(data)
 
             when (result) {
                 is AuthAndVerificationResult.Ok -> toast("User is authenticated")
                 is AuthAndVerificationResult.MissingData -> toast("some information is missing")
                 is AuthAndVerificationResult.UserCancelled -> toast("User cancelled flow")
                 is AuthAndVerificationResult.Error -> toast("Error during login ${result.error}")
                 is AuthAndVerificationResult.UserLoggedOut -> toast("User requests logout")
                 is AuthAndVerificationResult.BlockedUser -> toast("User is blocked")
             }
         }
     }
    }
```

### Como fazer logout da usuária

Para deslogar a usuária podemos fazer uma chamada para o método de logout seguindo o exemplo abaixo.

```
contaStoneSdk.logout {
            if (it != null) {
                toast("Error on logout. Try again!")
            } else toast("user successfully logged out")
        }
```
Quando a exception é nula o logout ocorreu com sucesso, quando não, algum erro ocorreu durante o fluxo.

### Obtendo o Client autenticado

A authSDK é responsável por todo o processo de autenticação incluindo a adição do token no header das chamadas para a API do Stone Openbank. Para isso a SDK fornece um `OkHttpClient` configurado que pode ser utilizado nas chamadas HTTP do seu app. Este client possui [certificate pinning](https://www.owasp.org/index.php/Certificate_and_Public_Key_Pinning) com os certificados da API do Stone OpenBank e é possível acessá-lo chamando o método `contaStone.auth().client()`. 

```
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
                    toast("Error fetching institutions $e")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnMainThread {
                    toast("Request executed successfully")
                }
            }
        }

        client.newCall(request).enqueue(callback)
    }
```
### Como inicializar o Aprovador
Para conseguir iniciar o aprovador é necessário fazer a chamada conforme o exemplo abaixo
```
 contaStoneSdk.startApproverForResult(
                        source = this,
                        requestCode = APPROVER_RQ,
                        params = ApproverParams(loggedAccount, ApproverLaunchMode.SDKLaunchMode)
                    )
```

Para recuperar o `loggedAccount` a Conta Stone SDK disponibiliza a sessão da usuária logada com todas as informações que o `LoggedAccount` precisa.

```
contaStoneSdk.getSession(
                onComplete = { result ->
                    when (result) {
                        is SessionResult.Success -> {
                                currentAccount?.paymentAccount?.let {
                                        LoggedAccountInfo(
                                            id = it.id,
                                            accountNumber = it.accountCode,
                                            accountOwner = currentAccount?.owner?.name.orEmpty(),
                                            accountOwnerDocument = currentAccount?.owner?.document.orEmpty(),
                                            bankName = "Stone Pagamentos S.A",
                                            bankNumber = "197",
                                            branchNumber = "0001",
                                            userLoggedName = profile.fullName
                                        )
                                    }
                                }
                        is SessionResult.HasNoActiveSession -> redirectToLogin()
                    }
                },
                onError = {
                    toast("Error trying to get session")
                }
            )
```