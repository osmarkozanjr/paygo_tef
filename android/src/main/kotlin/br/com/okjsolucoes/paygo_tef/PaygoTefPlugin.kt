package br.com.okjsolucoes.paygo_tef

// IMPORTS DA BIBLIOTECA PAYGO TEF (CONFORME SEU JAVADOC)
// IMPORTS DO PROJETO QUE COMUNICAM COM IMPORTS DA BIBLIOTECA PAYGO TEF
import android.content.Context
import android.util.Log
import br.com.setis.interfaceautomacao.Cartoes
import br.com.setis.interfaceautomacao.Confirmacoes
import br.com.setis.interfaceautomacao.Financiamentos
import br.com.setis.interfaceautomacao.ModalidadesPagamento
import br.com.setis.interfaceautomacao.Operacoes
import br.com.setis.interfaceautomacao.StatusTransacao
import br.com.setis.interfaceautomacao.ViasImpressao
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.*
import java.util.Date
import java.util.UUID

// Classe do plugin que implementa o handler do MethodChannel
class PaygoTefPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    companion object {
        private const val CHANNEL = "br.com.okjsolucoes.paygo_tef"
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "br.com.okjsolucoes.paygo_tef")
        channel.setMethodCallHandler(this)
        context = binding.applicationContext

        Log.d("PaygoTefPlugin", "Plugin registrado com FlutterPlugin")
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        Log.d("PaygoTefPlugin", "Plugin desregistrado")
    }

    override fun onMethodCall(call: MethodCall, result: Result) {

        when (call.method) {
            "testTransaction" -> {
                val valor = call.argument<String>("valor")
                Log.d("PaygoTefPlugin", "Valor recebido: $valor")

                try {
                    // Simulação de chamada SDK Paygo TEF
                    val resposta =
                        "Transação (SIMULADA) Paygo TEF de R$ $valor iniciada com sucesso! \n Pronto para receber o pagamento!"
                    result.success(resposta)
                } catch (e: Exception) {
                    Log.e("PaygoTefPlugin", "Erro ao iniciar transação", e)
                    result.error(
                        "PAYGO_TEF_ERROR",
                        "Erro ao iniciar transação Paygo TEF: ${e.message}",
                        e.stackTraceToString()
                    )
                }
            }

            "dadosAutomacao" -> {
                val nome = call.argument<String>("nome") ?: "ICEASA"
                val versao = call.argument<String>("versao") ?: "1.0.0"
                val nomePdv = call.argument<String>("nomePdv") ?: "PDV01"

                try {
                    val dados = DadosAutomacaoHelper.criar(nome, versao, nomePdv)
                    Log.d("PaygoTefPlugin", "Inicializando Transacoes com dados da automacao")
                    TransacoesHelper.inicializar(dados, context)
                    Log.d(
                        "PaygoTefPlugin",
                        "DadosAutomacao criados: ${nome}, ${versao}, ${nomePdv}"
                    )
                    result.success(
                        "Dados da automação criados com sucesso. Transação Iniciada!"
                    ) // result.success(true)
                } catch (e: Exception) {
                    Log.e("PaygoTefPlugin", "Erro ao criar dados automacao", e)
                    result.error(
                        "DADOS_AUTOMACAO_ERROR",
                        "Erro ao criar dados automação: ${e.message}",
                        e.stackTraceToString()
                    )
                }
            }

            "entradaTransacao" -> {
                // Extrair operacaoStr antes do try-catch para usar nos logs
                val operacaoStr = call.argument<String>("operacao") ?: Operacoes.VENDA.name

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val identificadorTransacao =
                            call.argument<String>("identificadorTransacao")
                                ?: UUID.randomUUID().toString()
                        val operacao = Operacoes.valueOf(operacaoStr)
                        val modalidadePagamentoStr =
                            call.argument<String>("modalidadePagamento")
                                ?: ModalidadesPagamento.PAGAMENTO_CARTAO.name
                        val modalidadePagamento =
                            ModalidadesPagamento.valueOf(modalidadePagamentoStr)
                        val tipoCartaoStr =
                            call.argument<String>("tipoCartao") ?: Cartoes.CARTAO_CREDITO.name
                        val tipoCartao = Cartoes.valueOf(tipoCartaoStr)
                        val tipoFinanciamentoStr =
                            call.argument<String>("tipoFinanciamento")
                                ?: Financiamentos.A_VISTA.name
                        val tipoFinanciamento = Financiamentos.valueOf(tipoFinanciamentoStr)
                        val nomeProvedor = call.argument<String>("nomeProvedor") ?: ""
                        val valor = call.argument<Int>("valor") ?: 0
                        val parcelas = call.argument<Int>("parcelas") ?: 1
                        val estabelecimentoCNPJouCPF =
                            call.argument<String>("estabelecimentoCNPJouCPF")
                                ?: null // Default CNPJ
                        val documentoFiscal = call.argument<String>("documentoFiscal") ?: null
                        val campoLivre = call.argument<String>("campoLivre") ?: null
                        val nsuTransacaoOriginal = call.argument<String>("nsuTransacaoOriginal") ?: null
                        val referenciaLocaloriginal = call.argument<String>("referenciaLocaloriginal") ?: null

                        val codigoAutorizacaoOriginal = call.argument<String>("codigoAutorizacaoOriginal") ?: null

                        val timeStampTransacaoOriginal: Long? = call.argument("timeStampTransacaoOriginal")
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        if (operacao == Operacoes.VENDA) {
                            val entrada =
                                EntradaTransacaoHelper.operacaoVenda(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                    modalidadePagamento = modalidadePagamento,
                                    tipoCartao = tipoCartao,
                                    tipoFinanciamento = tipoFinanciamento,
                                    nomeProvedor = nomeProvedor,
                                    valorTotal = valor.toLong(),
                                    numeroParcelas = parcelas,
                                    codigoMoeda = 986, // Código padrão para Real Brasileiro
                                    estabelecimentoCNPJouCPF = estabelecimentoCNPJouCPF,
                                    documentoFiscal = documentoFiscal,
                                    campoLivre = campoLivre,
                                )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao $operacaoStr com identificador: $identificadorTransacao"
                            )
                            try {

                                val saida = TransacoesHelper.realizarTransacao(entrada)



                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ #VXDSaída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )

                                    //EXISTE TRANSAÇÃO ANTERIOR PENDENTE?
                                    if (saida.existeTransacaoPendente()) {
                                        val confirmacao =
                                            Confirmacoes()
                                                .informaIdentificadorConfirmacaoTransacao(
                                                    idConfirmacaoTransacao
                                                )
                                                .informaStatusTransacao(
                                                    StatusTransacao
                                                        .DESFEITO_MANUAL
                                                )

                                        val tansacaoPendenteDados = saida.obtemDadosTransacaoPendente()

                                         if (tansacaoPendenteDados != null) {

                                             try {
                                                 TransacoesHelper.resolverPendencia(
                                                     tansacaoPendenteDados,
                                                     confirmacao
                                                 )
                                                 Log.d(
                                                     "PaygoTefPlugin",
                                                     "Pendência cancelada com sucesso."
                                                 )
                                             } catch (e: Exception) {
                                                 Log.e(
                                                     "PaygoTefPlugin",
                                                     "Erro ao resolver pendência: ${e.message ?: e.toString()}  "
                                                 )
                                             }
                                         }

                                        Log.d(
                                            "PaygoTefPlugin",
                                            "Transação está pendente de confirmação. Dados da pendência: }"
                                        )
                                        Log.d(
                                            "PaygoTefPlugin",
                                            "desfazendo transação) ${tansacaoPendenteDados.toString()}"
                                        )
                                        val retorno =
                                            mapOf(
                                                "status" to "pendent",
                                                "mensagem" to "Operação $operacaoStr aguarda confirmação!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                "nsuHostTransacaoPendente" to tansacaoPendenteDados?.obtemNsuHost(),
                                                "nsuLocalTransacaoPendente" to tansacaoPendenteDados?.obtemNsuLocal(),
                                                "nsuTransacaoPendente" to tansacaoPendenteDados?.obtemNsuTransacao(),
                                                "idEstabelecimentoPendente" to tansacaoPendenteDados?.obtemIdentificadorEstabelecimento(),
                                                )


                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                        // val confirmacao =
                                        //     Confirmacoes()
                                        //         .informaIdentificadorConfirmacaoTransacao(
                                        //             idConfirmacaoTransacao
                                        //         )
                                        //         .informaStatusTransacao(
                                        //             StatusTransacao
                                        //                 .CONFIRMADO_AUTOMATICO
                                        //         )
                                        // if (tansacaoPendenteDados != null) {

                                        //     try {
                                        //         TransacoesHelper.resolverPendencia(
                                        //             tansacaoPendenteDados,
                                        //             confirmacao
                                        //         )
                                        //         Log.d(
                                        //             "PaygoTefPlugin",
                                        //             "Pendência resolvida com sucesso."
                                        //         )
                                        //     } catch (e: Exception) {
                                        //         Log.e(
                                        //             "PaygoTefPlugin",
                                        //             "Erro ao resolver pendência: ${e.message ?: e.toString()}  "
                                        //         )
                                        //     }
                                        // }
                                    }else{
                                        //NÃO EXISTE TRANSAÇÃO PENDENTE

                                        if (dadosSaida["necessitaConfirmacao"] as Boolean) {
                                            val confirmacao =
                                                Confirmacoes()
                                                    .informaIdentificadorConfirmacaoTransacao(
                                                        idConfirmacaoTransacao
                                                    )
                                                    .informaStatusTransacao(
                                                        StatusTransacao
                                                            .CONFIRMADO_AUTOMATICO
                                                    )

                                            try {
                                                TransacoesHelper.confirmarTransacao(confirmacao)
                                                Log.d(
                                                    "PaygoTefPlugin",
                                                    "\uD83C\uDDE7\uD83C\uDDF7#VXDTransação confirmada automaticamente."
                                                )
                                            } catch (e: Exception) {
                                                Log.e(
                                                    "PaygoTefPlugin",
                                                    "Erro ao confirmar transação automaticamente: ${e.message ?: e.toString()}"
                                                )


                                            }
                                        }



                                        if (saida.obtemResultadoTransacao() == 0) {
                                            // impressão do
                                            // comprovante
                                            val listComprovanteDifLoja: List<String> =
                                                saida.obtemComprovanteDiferenciadoLoja()
                                                    ?: emptyList()
                                            // Log.e(
                                            //     "PaygoTefPlugin",
                                            //     "Comprovante diferenciado loja é: $listComprovanteDifLoja"
                                            // )

                                            val listComprovanteDifPortador: List<String> =
                                                saida.obtemComprovanteDiferenciadoPortador()
                                                    ?: emptyList()
//                                        Log.e(
//                                            "PaygoTefPlugin",
//                                            "Comprovante diferenciado portador é: ${listComprovanteDifPortador.firstOrNull() ?: ""}"
//                                        )



                                            val stringComprovanteGrafLojista: String? =
                                                saida.obtemComprovanteGraficoLojista() ?: null
                                            // Log.e(
                                            //     "PaygoTefPlugin",
                                            //     "Comprovante gráfico do lojista é: $stringComprovanteGrafLojista"
                                            // )

                                            val stringComprovanteGrafPortador: String? =
                                                saida.obtemComprovanteGraficoPortador() ?: null
                                            // Log.e(
                                            //     "PaygoTefPlugin",
                                            //     "Comprovante gráfico do portador é: $stringComprovanteGrafPortador"
                                            // )

                                            val listComprovanteCompleto: List<String> =
                                                saida.obtemComprovanteCompleto() ?: emptyList()

                                            // Log.e(
                                            //     "PaygoTefPlugin",
                                            //     "Comprovante completo é: $listComprovanteCompleto"
                                            // )

                                            val listComprovanteReduzidoPortador: List<String> =
                                                saida.obtemComprovanteReduzidoPortador()
                                                    ?: emptyList()

                                            // Log.e(
                                            //     "PaygoTefPlugin",
                                            //     "Comprovante Reduzido é: $listComprovanteReduzidoPortador"
                                            // )

                                            val retorno =
                                                mapOf(
                                                    "status" to "success",
                                                    "mensagem" to
                                                            "Operação $operacaoStr realizada com sucesso.",
                                                    "identificadorTransacao" to
                                                            entrada.obtemIdTransacaoAutomacao(),
                                                    "valorTotal" to entrada.obtemValorTotal(),
                                                    "modalidadePagamento" to
                                                            entrada.obtemModalidadePagamento()
                                                                .name,
                                                    "numeroParcelas" to
                                                            entrada.obtemNumeroParcelas(),
                                                    "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                    "documentoFiscal" to
                                                            entrada.obtemDocumentoFiscal(),
                                                    "estabelecimento" to
                                                            entrada.obtemEstabelecimentoCNPJouCPF(),
                                                    "campoLivre" to
                                                            entrada.obtemDadosAdicionaisAutomacao1(),
                                                    "idTransacao" to identificadorTransacao,
                                                    "idConfirmacaoTransacao" to
                                                            idConfirmacaoTransacao,
                                                    "mensagem_saida" to
                                                            dadosSaida["mensagemResultado"],
                                                    "resultadoTransacao" to
                                                            dadosSaida["resultadoTransacao"],
                                                    // impressão do comprovante
                                                    "comprovanteGraficoPortadorBase64" to
                                                            stringComprovanteGrafPortador,
                                                    "comprovanteGrafLojistaBase64" to
                                                            stringComprovanteGrafLojista,
                                                    "comprovanteCompletoString" to
                                                            listComprovanteCompleto.firstOrNull(),
                                                    "comprovanteReduzidoPortadorString" to
                                                            listComprovanteReduzidoPortador.firstOrNull(),
                                                    "comprovanteDifLojaString" to
                                                            listComprovanteDifLoja.firstOrNull(),
                                                    "comprovanteDifPortadorString" to
                                                            listComprovanteDifPortador.firstOrNull(),
                                                    "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                                )

                                            withContext(Dispatchers.Main) { result.success(retorno) }
                                        } else {
                                            val retorno =
                                                mapOf(
                                                    "status" to "error",
                                                    "mensagem" to "Operação $operacaoStr falhou!",
                                                    "identificadorTransacao" to
                                                            entrada.obtemIdTransacaoAutomacao(),
                                                    "valorTotal" to entrada.obtemValorTotal(),
                                                    "modalidadePagamento" to
                                                            entrada.obtemModalidadePagamento()
                                                                .name,
                                                    "numeroParcelas" to
                                                            entrada.obtemNumeroParcelas(),
                                                    "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                    "documentoFiscal" to
                                                            entrada.obtemDocumentoFiscal(),
                                                    "estabelecimento" to
                                                            entrada.obtemEstabelecimentoCNPJouCPF(),
                                                    "campoLivre" to
                                                            entrada.obtemDadosAdicionaisAutomacao1(),
                                                    "idTransacao" to identificadorTransacao,
                                                    "idConfirmacaoTransacao" to
                                                            idConfirmacaoTransacao,
                                                    "mensagem_saida" to
                                                            dadosSaida["mensagemResultado"],
                                                    "resultadoTransacao" to
                                                            dadosSaida["resultadoTransacao"],

                                                    )

                                            withContext(Dispatchers.Main) { result.success(retorno) }
                                        }
                                    }



                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr >>Catch exception",
                                    e
                                )
                                withContext(Dispatchers.Main) {
                                    result.error(
                                        "SAIDA_TRANSACAO_ERROR",
                                        e.message
                                            ?: "${e.toString()}",
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        } else if (operacao == Operacoes.EXIBE_PDC) {

                            val entrada =
                                EntradaTransacaoHelper.operacaoExibePdc(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                )

                            try {
                                val saida = TransacoesHelper.realizarTransacao(entrada)

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() == 0) {
                                        val retorno =
                                            mapOf(
                                                "status" to "success",
                                                "mensagem" to
                                                        "Operação $operacaoStr realizada com sucesso.",
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                            )
                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to "error",
                                                "mensagem" to "Operação $operacaoStr falhou!",
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                            )
                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr >>Catch exception",
                                    e
                                )
                                result.error(
                                    "SAIDA_TRANSACAO_ERROR",
                                    e.message ?: "${e.toString()}",
                                    e.stackTraceToString()
                                )
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        } else if (operacao == Operacoes.VERSAO) {

                            val entrada =
                                EntradaTransacaoHelper.operacaoVersao(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                )

                            try {
                                val saida = TransacoesHelper.realizarTransacao(entrada)

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()

                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )
                                    val versoes = TransacoesHelper.obterVersoes()

                                    val versaoBiblioteca = versoes?.obtemVersaoBiblioteca() ?: "N/A"
                                    val versaoAPK = versoes?.obtemVersaoApk() ?: "N/A"
                                    if (saida.obtemResultadoTransacao() == 0) {
                                        val retorno =
                                            mapOf(
                                                "status" to "success",
                                                "mensagem" to
                                                        "Operação $operacaoStr realizada com sucesso",
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "versaoApk" to versaoAPK,
                                                "versaoLib" to versaoBiblioteca,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                            )
                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to "error",
                                                "mensagem" to "Operação $operacaoStr falhou!",
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "versaoApk" to versaoAPK,
                                                "versaoLib" to versaoBiblioteca,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                            )
                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr >>Catch exception",
                                    e
                                )
                                result.error(
                                    "SAIDA_TRANSACAO_ERROR",
                                    e.message ?: "${e.toString()}", // "Erro ao receber saida da
                                    // transação de Versão.
                                    // Saída
                                    // Nula",
                                    e.stackTraceToString()
                                )
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        } else if (operacao == Operacoes.REIMPRESSAO) {

                            val entrada =
                                EntradaTransacaoHelper.operacaoReimprimeUltComprovante(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao $operacaoStr com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida = TransacoesHelper.realizarTransacao(entrada)

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() == 0) {
                                        // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja: List<String> =
                                            saida.obtemComprovanteDiferenciadoLoja()
                                                ?: emptyList()
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante diferenciado loja é: $listComprovanteDifLoja"
                                        // )

                                        val listComprovanteDifPortador: List<String> =
                                            saida.obtemComprovanteDiferenciadoPortador()
                                                ?: emptyList()
//                                        Log.e(
//                                            "PaygoTefPlugin",
//                                            "Comprovante diferenciado portador é: ${listComprovanteDifPortador.firstOrNull() ?: ""}"
//                                        )
                                       
                                        

                                        val stringComprovanteGrafLojista: String? =
                                            saida.obtemComprovanteGraficoLojista() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do lojista é: $stringComprovanteGrafLojista"
                                        // )

                                        val stringComprovanteGrafPortador: String? =
                                            saida.obtemComprovanteGraficoPortador() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do portador é: $stringComprovanteGrafPortador"
                                        // )

                                        val listComprovanteCompleto: List<String> =
                                            saida.obtemComprovanteCompleto() ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante completo é: $listComprovanteCompleto"
                                        // )

                                        val listComprovanteReduzidoPortador: List<String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                                ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante Reduzido é: $listComprovanteReduzidoPortador"
                                        // )

                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to "success",
                                                "mensagem" to
                                                        "Operação $operacaoStr realizada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                // impressão do comprovante
                                                // impressão do comprovante
                                                "comprovanteGraficoPortadorBase64" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteGrafLojistaBase64" to
                                                        stringComprovanteGrafLojista,
                                                "comprovanteCompletoString" to
                                                        listComprovanteCompleto.firstOrNull(),
                                                "comprovanteReduzidoPortadorString" to
                                                        listComprovanteReduzidoPortador.firstOrNull(),
                                                "comprovanteDifLojaString" to
                                                        listComprovanteDifLoja.firstOrNull(),
                                                "comprovanteDifPortadorString" to
                                                        listComprovanteDifPortador.firstOrNull(),
                                                "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                                ///
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to "error",
                                                "mensagem" to
                                                        "Operação $operacaoStr falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr >>Catch exception",
                                    e
                                )
                                withContext(Dispatchers.Main) {
                                    result.error(
                                        "SAIDA_TRANSACAO_ERROR",
                                        e.message ?: "${e.toString()}",
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        } else if (operacao == Operacoes.CANCELAMENTO) {

                            val dataHoraTransacaoOriginal : Date? = timeStampTransacaoOriginal?.let { millis ->
                                Date(millis)
                            }

                            val entrada =
                                EntradaTransacaoHelper.operacaoCancelamento(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                    modalidadePagamento = modalidadePagamento,
                                    tipoCartao = tipoCartao,
                                    tipoFinanciamento = tipoFinanciamento,
                                    nomeProvedor = nomeProvedor,
                                    valorTotal = valor.toLong(),
                                    numeroParcelas = parcelas,
                                    codigoMoeda = 986, // Código padrão para Real Brasileiro
                                    estabelecimentoCNPJouCPF = estabelecimentoCNPJouCPF,
                                    documentoFiscal = documentoFiscal,
                                    campoLivre = campoLivre,
                                    nsuTransacaoOriginal = nsuTransacaoOriginal,
                                    referenciaLocaloriginal = referenciaLocaloriginal,
                                    codigoAutorizacaoOriginal = codigoAutorizacaoOriginal,
                                    dataHoraTransacaoOriginal = dataHoraTransacaoOriginal,
                                )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao $operacaoStr com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida = TransacoesHelper.realizarTransacao(entrada)

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() == 0) {
                                        // impressão do
                                        // comprovante
                                   
                                        val listComprovanteDifLoja: List<String> =
                                            saida.obtemComprovanteDiferenciadoLoja()
                                                ?: emptyList()
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante diferenciado loja é: $listComprovanteDifLoja"
                                        // )

                                        val listComprovanteDifPortador: List<String> =
                                            saida.obtemComprovanteDiferenciadoPortador()
                                                ?: emptyList()
//                                        Log.e(
//                                            "PaygoTefPlugin",
//                                            "Comprovante diferenciado portador é: ${listComprovanteDifPortador.firstOrNull() ?: ""}"
//                                        )
                                       
                                        

                                        val stringComprovanteGrafLojista: String? =
                                            saida.obtemComprovanteGraficoLojista() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do lojista é: $stringComprovanteGrafLojista"
                                        // )

                                        val stringComprovanteGrafPortador: String? =
                                            saida.obtemComprovanteGraficoPortador() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do portador é: $stringComprovanteGrafPortador"
                                        // )

                                        val listComprovanteCompleto: List<String> =
                                            saida.obtemComprovanteCompleto() ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante completo é: $listComprovanteCompleto"
                                        // )

                                        val listComprovanteReduzidoPortador: List<String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                                ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante Reduzido é: $listComprovanteReduzidoPortador"
                                        // )

                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to "success",
                                                "mensagem" to
                                                        "Operação $operacaoStr realizada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                // impressão do comprovante
                                                // impressão do comprovante
                                                "comprovanteGraficoPortadorBase64" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteGrafLojistaBase64" to
                                                        stringComprovanteGrafLojista,
                                                "comprovanteCompletoString" to
                                                        listComprovanteCompleto.firstOrNull(),
                                                "comprovanteReduzidoPortadorString" to
                                                        listComprovanteReduzidoPortador.firstOrNull(),
                                                "comprovanteDifLojaString" to
                                                        listComprovanteDifLoja.firstOrNull(),
                                                "comprovanteDifPortadorString" to
                                                        listComprovanteDifPortador.firstOrNull(),
                                                "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                                ///
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to "error",
                                                "mensagem" to
                                                        "Operação $operacaoStr  falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr >>Catch exception",
                                    e
                                )
                                withContext(Dispatchers.Main) {
                                    result.error(
                                        "SAIDA_TRANSACAO_ERROR",
                                        e.message ?: "${e.toString()}",
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        } else if (operacao == Operacoes.RELATORIO_RESUMIDO) {
                            val entrada =
                                EntradaTransacaoHelper.operacaoRelatorioResumido(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao $operacaoStr com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida = TransacoesHelper.realizarTransacao(entrada)

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() == 0) {
                               // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja: List<String> =
                                            saida.obtemComprovanteDiferenciadoLoja()
                                                ?: emptyList()
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante diferenciado loja é: $listComprovanteDifLoja"
                                        // )

                                        val listComprovanteDifPortador: List<String> =
                                            saida.obtemComprovanteDiferenciadoPortador()
                                                ?: emptyList()
//                                        Log.e(
//                                            "PaygoTefPlugin",
//                                            "Comprovante diferenciado portador é: ${listComprovanteDifPortador.firstOrNull() ?: ""}"
//                                        )
                                       
                                        

                                        val stringComprovanteGrafLojista: String? =
                                            saida.obtemComprovanteGraficoLojista() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do lojista é: $stringComprovanteGrafLojista"
                                        // )

                                        val stringComprovanteGrafPortador: String? =
                                            saida.obtemComprovanteGraficoPortador() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do portador é: $stringComprovanteGrafPortador"
                                        // )

                                        val listComprovanteCompleto: List<String> =
                                            saida.obtemComprovanteCompleto() ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante completo é: $listComprovanteCompleto"
                                        // )

                                        val listComprovanteReduzidoPortador: List<String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                                ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante Reduzido é: $listComprovanteReduzidoPortador"
                                        // )

                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to "success",
                                                "mensagem" to
                                                        "Operação $operacaoStr realizada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                // impressão do comprovante
                                                // impressão do comprovante
                                                "comprovanteGraficoPortadorBase64" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteGrafLojistaBase64" to
                                                        stringComprovanteGrafLojista,
                                                "comprovanteCompletoString" to
                                                        listComprovanteCompleto.firstOrNull(),
                                                "comprovanteReduzidoPortadorString" to
                                                        listComprovanteReduzidoPortador.firstOrNull(),
                                                "comprovanteDifLojaString" to
                                                        listComprovanteDifLoja.firstOrNull(),
                                                "comprovanteDifPortadorString" to
                                                        listComprovanteDifPortador.firstOrNull(),
                                                ///
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to "error",
                                                "mensagem" to
                                                        "Operação $operacaoStr falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr  >>Catch exception",
                                    e
                                )
                                withContext(Dispatchers.Main) {
                                    result.error(
                                        "SAIDA_TRANSACAO_ERROR",
                                        e.message ?: "${e.toString()}",
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        } else if (operacao == Operacoes.RELATORIO_SINTETICO) {
                            val entrada =
                                EntradaTransacaoHelper.operacaoRelatorioSintetico(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao $operacaoStr com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida = TransacoesHelper.realizarTransacao(entrada)

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() == 0) {
                                        // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja: List<String> =
                                            saida.obtemComprovanteDiferenciadoLoja()
                                                ?: emptyList()
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante diferenciado loja é: $listComprovanteDifLoja"
                                        // )

                                        val listComprovanteDifPortador: List<String> =
                                            saida.obtemComprovanteDiferenciadoPortador()
                                                ?: emptyList()
//                                        Log.e(
//                                            "PaygoTefPlugin",
//                                            "Comprovante diferenciado portador é: ${listComprovanteDifPortador.firstOrNull() ?: ""}"
//                                        )
                                       
                                        

                                        val stringComprovanteGrafLojista: String? =
                                            saida.obtemComprovanteGraficoLojista() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do lojista é: $stringComprovanteGrafLojista"
                                        // )

                                        val stringComprovanteGrafPortador: String? =
                                            saida.obtemComprovanteGraficoPortador() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do portador é: $stringComprovanteGrafPortador"
                                        // )

                                        val listComprovanteCompleto: List<String> =
                                            saida.obtemComprovanteCompleto() ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante completo é: $listComprovanteCompleto"
                                        // )

                                        val listComprovanteReduzidoPortador: List<String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                                ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante Reduzido é: $listComprovanteReduzidoPortador"
                                        // )

                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to "success",
                                                "mensagem" to
                                                        "Operação $operacaoStr realizada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                // impressão do comprovante
                                                // impressão do comprovante
                                                "comprovanteGraficoPortadorBase64" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteGrafLojistaBase64" to
                                                        stringComprovanteGrafLojista,
                                                "comprovanteCompletoString" to
                                                        listComprovanteCompleto.firstOrNull(),
                                                "comprovanteReduzidoPortadorString" to
                                                        listComprovanteReduzidoPortador.firstOrNull(),
                                                "comprovanteDifLojaString" to
                                                        listComprovanteDifLoja.firstOrNull(),
                                                "comprovanteDifPortadorString" to
                                                        listComprovanteDifPortador.firstOrNull(),
                                                "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                                ///
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to "error",
                                                "mensagem" to
                                                        "Operação $operacaoStr falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr >>Catch exception",
                                    e
                                )
                                withContext(Dispatchers.Main) {
                                    result.error(
                                        "SAIDA_TRANSACAO_ERROR",
                                        e.message ?: "${e.toString()}",
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////  
                        } else if (operacao == Operacoes.RELATORIO_DETALHADO) {
                            val entrada =
                                EntradaTransacaoHelper.operacaoRelatorioSintetico(
                                    identificadorTransacaoAutomacao =
                                    identificadorTransacao,
                                    operacao = operacao,
                                )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao $operacaoStr com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida = TransacoesHelper.realizarTransacao(entrada)

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida = SaidaTransacaoHelper.extrairDados(saida)

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                        - Vias de impressão: ${dadosSaida["viasImpressaoDisponveis"]}
                                    """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() == 0) {
                                        // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja: List<String> =
                                            saida.obtemComprovanteDiferenciadoLoja()
                                                ?: emptyList()
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante diferenciado loja é: $listComprovanteDifLoja"
                                        // )

                                        val listComprovanteDifPortador: List<String> =
                                            saida.obtemComprovanteDiferenciadoPortador()
                                                ?: emptyList()
//                                        Log.e(
//                                            "PaygoTefPlugin",
//                                            "Comprovante diferenciado portador é: ${listComprovanteDifPortador.firstOrNull() ?: ""}"
//                                        )
                                       
                                        

                                        val stringComprovanteGrafLojista: String? =
                                            saida.obtemComprovanteGraficoLojista() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do lojista é: $stringComprovanteGrafLojista"
                                        // )

                                        val stringComprovanteGrafPortador: String? =
                                            saida.obtemComprovanteGraficoPortador() ?: null
                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante gráfico do portador é: $stringComprovanteGrafPortador"
                                        // )

                                        val listComprovanteCompleto: List<String> =
                                            saida.obtemComprovanteCompleto() ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante completo é: $listComprovanteCompleto"
                                        // )

                                        val listComprovanteReduzidoPortador: List<String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                                ?: emptyList()

                                        // Log.e(
                                        //     "PaygoTefPlugin",
                                        //     "Comprovante Reduzido é: $listComprovanteReduzidoPortador"
                                        // )

                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to "success",
                                                "mensagem" to
                                                        "Operação $operacaoStr realizada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                                // impressão do comprovante
                                                // impressão do comprovante
                                                "comprovanteGraficoPortadorBase64" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteGrafLojistaBase64" to
                                                        stringComprovanteGrafLojista,
                                                "comprovanteCompletoString" to
                                                        listComprovanteCompleto.firstOrNull(),
                                                "comprovanteReduzidoPortadorString" to
                                                        listComprovanteReduzidoPortador.firstOrNull(),
                                                "comprovanteDifLojaString" to
                                                        listComprovanteDifLoja.firstOrNull(),
                                                "comprovanteDifPortadorString" to
                                                        listComprovanteDifPortador.firstOrNull(),
                                                "viasImpressao" to dadosSaida["viasImpressaoDisponveis"],
                                                ///
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to "error",
                                                "mensagem" to
                                                        "Operação $operacaoStr falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida["mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida["resultadoTransacao"],
                                            )

                                        withContext(Dispatchers.Main) { result.success(retorno) }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        result.error(
                                            "SAIDA_TRANSACAO_ERROR",
                                            "Erro receber saida da transação. Saída Nula",
                                            null
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "PaygoTefPlugin",
                                    "Erro ao realizar transação de $operacaoStr >>Catch exception",
                                    e
                                )
                                withContext(Dispatchers.Main) {
                                    result.error(
                                        "SAIDA_TRANSACAO_ERROR",
                                        e.message ?: "${e.toString()}",
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        ///////////////////////////////////////////////   
                        /////////////////////////////////////////////// 
                        ///////////////////////////////////////////////      
                        } else {
                            withContext(Dispatchers.Main) { result.notImplemented() }
                        }
                    } catch (e: Exception) {
                        Log.e("PaygoTefPlugin", "Erro na transação", e)
                        withContext(Dispatchers.Main) {
                            result.error(
                                "TRANSACAO_EXCEPTION",
                                e.message ?: "${e.toString()}",
                                e.stackTraceToString()
                            )
                        }
                    }
                }
            }
        }
    }
}
