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
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.UUID
import kotlinx.coroutines.*

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
                    val dados =
                        DadosAutomacaoHelper.criar(nome, versao, nomePdv)
                    Log.d(
                        "PaygoTefPlugin",
                        "Inicializando Transacoes com dados da automacao"
                    )
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
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val identificadorTransacao =
                            call.argument<String>(
                                "identificadorTransacao"
                            )
                                ?: UUID.randomUUID().toString()
                        val operacaoStr =
                            call.argument<String>("operacao")
                                ?: Operacoes.VENDA.name
                        val operacao = Operacoes.valueOf(operacaoStr)
                        val modalidadePagamentoStr =
                            call.argument<String>("modalidadePagamento")
                                ?: ModalidadesPagamento
                                    .PAGAMENTO_CARTAO
                                    .name
                        val modalidadePagamento =
                            ModalidadesPagamento.valueOf(
                                modalidadePagamentoStr
                            )
                        val tipoCartaoStr =
                            call.argument<String>("tipoCartao")
                                ?: Cartoes.CARTAO_CREDITO.name
                        val tipoCartao = Cartoes.valueOf(tipoCartaoStr)
                        val tipoFinanciamentoStr =
                            call.argument<String>("tipoFinanciamento")
                                ?: Financiamentos.A_VISTA.name
                        val tipoFinanciamento =
                            Financiamentos.valueOf(tipoFinanciamentoStr)
                        val nomeProvedor =
                            call.argument<String>("nomeProvedor") ?: ""
                        val valor = call.argument<Int>("valor") ?: 0
                        val parcelas = call.argument<Int>("parcelas") ?: 1
                        val estabelecimentoCNPJouCPF =
                            call.argument<String>(
                                "estabelecimentoCNPJouCPF"
                            )
                                ?: null // Default CNPJ
                        val documentoFiscal =
                            call.argument<String>("documentoFiscal")
                                ?: null
                        val campoLivre =
                            call.argument<String>("campoLivre") ?: null

                        if (operacao == Operacoes.VENDA) {
                            val entrada =
                                EntradaTransacaoHelper
                                    .operacaoVenda(
                                        identificadorTransacaoAutomacao =
                                        identificadorTransacao,
                                        operacao = operacao,
                                        modalidadePagamento =
                                        modalidadePagamento,
                                        tipoCartao =
                                        tipoCartao,
                                        tipoFinanciamento =
                                        tipoFinanciamento,
                                        nomeProvedor =
                                        nomeProvedor,
                                        valorTotal =
                                        valor.toLong(),
                                        numeroParcelas =
                                        parcelas,
                                        codigoMoeda =
                                        986, // Código padrão para Real Brasileiro
                                        estabelecimentoCNPJouCPF =
                                        estabelecimentoCNPJouCPF,
                                        documentoFiscal =
                                        documentoFiscal,
                                        campoLivre =
                                        campoLivre,
                                    )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao (operacaoVenda) com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida =
                                    TransacoesHelper
                                        .realizarTransacao(
                                            entrada
                                        )

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida =
                                        SaidaTransacaoHelper
                                            .extrairDados(
                                                saida
                                            )

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                    """.trimIndent()
                                    )

                                    if (dadosSaida[
                                            "necessitaConfirmacao"] as
                                                Boolean
                                    ) {
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
                                            TransacoesHelper
                                                .confirmarTransacao(
                                                    confirmacao
                                                )
                                            Log.d(
                                                "PaygoTefPlugin",
                                                "Transação confirmada automaticamente."
                                            )
                                        } catch (
                                            e:
                                            Exception
                                        ) {
                                            Log.e(
                                                "PaygoTefPlugin",
                                                "Erro ao confirmar transação automaticamente: ${e.message ?: e.toString()}"
                                            )

                                            //
                                            // result.error(
                                            //
                                            // "SAIDA_TRANSACAO_ERROR",
                                            //
                                            //
                                            //
                                            //
                                            //
                                            // "Erro
                                            // ao
                                            // confirmar
                                            // transação: ${e.message ?:
                                            // e.toString()}",
                                            //
                                            //
                                            //
                                            //
                                            //
                                            // null
                                            //
                                            //
                                            //
                                            //
                                            //    )
                                        }
                                    }

                                    if (saida.existeTransacaoPendente()
                                    ) {
                                        val pendente =
                                            saida.obtemDadosTransacaoPendente()
                                        val confirmacao =
                                            Confirmacoes()
                                                .informaIdentificadorConfirmacaoTransacao(
                                                    idConfirmacaoTransacao
                                                )
                                                .informaStatusTransacao(
                                                    StatusTransacao
                                                        .CONFIRMADO_AUTOMATICO
                                                )
                                        if (pendente != null
                                        ) {

                                            try {
                                                TransacoesHelper
                                                    .resolverPendencia(
                                                        pendente,
                                                        confirmacao
                                                    )
                                                Log.d(
                                                    "PaygoTefPlugin",
                                                    "Pendência resolvida com sucesso."
                                                )
                                            } catch (
                                                e:
                                                Exception
                                            ) {
                                                Log.e(
                                                    "PaygoTefPlugin",
                                                    "Erro ao resolver pendência: ${e.message ?: e.toString()}  "
                                                )
                                            }
                                        }
                                    }
                                    // aqui não posso usar
                                    // dadosSaida["necessitaConfirmacao"] as
                                    // Boolean
                                    // pois estou capturando
                                    // novo resultado após
                                    // confirmação ou
                                    // pendente
                                    // eventualmente serem
                                    // acionados, mudando o
                                    // status inicial
                                    if (saida.obtemResultadoTransacao() ==
                                        0
                                    ) {
                                        // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteDiferenciadoLoja()
                                                ?: emptyList()
                                        Log.e(
                                            "PaygoTefPlugin",
                                            "Comprovante diferenciado loja é: $listComprovanteDifLoja"
                                        )

                                        val listComprovanteDifPortador:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteDiferenciadoPortador()
                                                ?: emptyList()
                                        Log.e(
                                            "PaygoTefPlugin",
                                            "Comprovante diferenciado portador é: $listComprovanteDifPortador"
                                        )

                                        val stringComprovanteGrafLojista:
                                                String =
                                            saida.obtemComprovanteGraficoLojista()
                                                ?: ""
                                        Log.e(
                                            "PaygoTefPlugin",
                                            "Comprovante gráfico do lojista é: $stringComprovanteGrafLojista"
                                        )

                                        val stringComprovanteGrafPortador:
                                                String =
                                            saida.obtemComprovanteGraficoPortador()
                                                ?: ""
                                        Log.e(
                                            "PaygoTefPlugin",
                                            "Comprovante gráfico do portador é: $stringComprovanteGrafPortador"
                                        )

                                        val listComprovanteCompleto:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteCompleto()
                                                ?: emptyList()

                                        Log.e(
                                            "PaygoTefPlugin",
                                            "Comprovante completo é: $listComprovanteCompleto"
                                        )

                                        val listComprovanteReduzidoPortador:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                                ?: emptyList()

                                        Log.e(
                                            "PaygoTefPlugin",
                                            "Comprovante Reduzido é: $listComprovanteReduzidoPortador}"
                                        )

                                        ///
                                        Log.e(
                                            "TIPO",
                                            "Tipo listComprovanteDifLoja: ${listComprovanteDifLoja::class.java.name}"
                                        )
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "sucess",
                                                "mensagem" to
                                                        "Operação VENDA realizada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                                // impressão do comprovante
                                                "comprovanteGraficoPortador" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteCompletoList" to
                                                        listComprovanteCompleto,
                                                "comprovanteReduzidoPortadorList" to
                                                        listComprovanteReduzidoPortador,
                                                "comprovanteDifLojaList" to
                                                        listComprovanteDifLoja,
                                                "comprovanteGrafLojista" to
                                                        stringComprovanteGrafLojista,
                                                "comprovanteDifPortadorList" to
                                                        listComprovanteDifPortador,
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "error",
                                                "mensagem" to
                                                        "Operação VENDA falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    }
                                } else {
                                    withContext(
                                        Dispatchers.Main
                                    ) {
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
                                    "Erro ao realizar transação de Venda >>Catch exception",
                                    e
                                )
                                withContext(Dispatchers.Main) {
                                    result.error(
                                        "SAIDA_TRANSACAO_ERROR",
                                        e.message
                                            ?: "${e.toString()}", // "Erro ao receber saida
                                        // da transação de
                                        // Versão.
                                        // Saída Nula",
                                        e.stackTraceToString()
                                    )
                                }
                            }
                        } else if (operacao == Operacoes.EXIBE_PDC) {

                            val entrada =
                                EntradaTransacaoHelper
                                    .operacaoExibePdc(
                                        identificadorTransacaoAutomacao =
                                        identificadorTransacao,
                                        operacao = operacao,
                                    )

                            try {
                                val saida =
                                    TransacoesHelper
                                        .realizarTransacao(
                                            entrada
                                        )

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida =
                                        SaidaTransacaoHelper
                                            .extrairDados(
                                                saida
                                            )

                                    if (saida.obtemResultadoTransacao() ==
                                        0
                                    ) {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "sucess",
                                                "mensagem" to
                                                        "Operação EXIBE_PDC realizada com sucesso.",
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )
                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "error",
                                                "mensagem" to
                                                        "Operação EXIBE_PDC falhou!",
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )
                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    }
                                } else {
                                    withContext(
                                        Dispatchers.Main
                                    ) {
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
                                    "Erro ao realizar transação de EXIBE_PDC >>Catch exception",
                                    e
                                )
                                result.error(
                                    "SAIDA_TRANSACAO_ERROR",
                                    e.message
                                        ?: "${e.toString()}",
                                    e.stackTraceToString()
                                )
                            }
                        } else if (operacao == Operacoes.VERSAO) {
                            val entrada =
                                EntradaTransacaoHelper
                                    .operacaoVersao(
                                        identificadorTransacaoAutomacao =
                                        identificadorTransacao,
                                        operacao = operacao,
                                    )

                            try {
                                val saida =
                                    TransacoesHelper
                                        .realizarTransacao(
                                            entrada
                                        )

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()

                                    val dadosSaida =
                                        SaidaTransacaoHelper
                                            .extrairDados(
                                                saida
                                            )

                                    val versoes =
                                        TransacoesHelper
                                            .obterVersoes()

                                    val versaoBiblioteca =
                                        versoes?.obtemVersaoBiblioteca()
                                            ?: "N/A"
                                    val versaoAPK =
                                        versoes?.obtemVersaoApk()
                                            ?: "N/A"
                                    if (saida.obtemResultadoTransacao() ==
                                        0
                                    ) {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "sucess",
                                                "mensagem" to
                                                        "Operação VERSAO realizada com sucesso",
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "versaoApk" to
                                                        versaoAPK,
                                                "versaoLib" to
                                                        versaoBiblioteca,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )
                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "error",
                                                "mensagem" to
                                                        "Operação VERSAO falhou!",
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "versaoApk" to
                                                        versaoAPK,
                                                "versaoLib" to
                                                        versaoBiblioteca,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )
                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    }
                                } else {
                                    withContext(
                                        Dispatchers.Main
                                    ) {
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
                                    "Erro ao realizar transação de Versao >>Catch exception",
                                    e
                                )
                                result.error(
                                    "SAIDA_TRANSACAO_ERROR",
                                    e.message
                                        ?: "${e.toString()}", // "Erro ao receber saida da
                                    // transação de Versão.
                                    // Saída
                                    // Nula",
                                    e.stackTraceToString()
                                )
                            }
                        } else if (operacao == Operacoes.REIMPRESSAO) {
                            val entrada =
                                EntradaTransacaoHelper
                                    .operacaoReimprimeUltComprovante(
                                        identificadorTransacaoAutomacao =
                                        identificadorTransacao,
                                        operacao = operacao,

                                        )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao (operacaoReimpressao) com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida =
                                    TransacoesHelper
                                        .realizarTransacao(
                                            entrada
                                        )

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida =
                                        SaidaTransacaoHelper
                                            .extrairDados(
                                                saida
                                            )

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: Entrada de transação (operacaoReimpressao) criada com sucesso
                                        - mensagem_saida: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                     """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() ==
                                        0
                                    ) {
                                        // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja:
                                                List<
                                                        String> =
                                            (saida.obtemComprovanteDiferenciadoLoja()
                                                ?: "") as
                                                    List<
                                                            String>
                                        val stringComprovanteGrafLojista:
                                                String =
                                            saida.obtemComprovanteGraficoLojista()
                                                ?: ""
                                        val stringComprovanteGrafPortador:
                                                String =
                                            saida.obtemComprovanteGraficoPortador()
                                        val listComprovanteCompleto:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteCompleto()
                                        val listComprovanteReduzidoPortador:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "sucess",
                                                "mensagem" to
                                                        "Operação CANCELAMENTO criada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                                // impressão do comprovante
                                                "comprovanteGraficoPortador" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteCompletoList" to
                                                        listComprovanteCompleto,
                                                "comprovanteReduzidoPortadorList" to
                                                        listComprovanteReduzidoPortador,
                                                "comprovanteDifLojaList" to
                                                        listComprovanteDifLoja,
                                                "comprovanteGrafLojista" to
                                                        stringComprovanteGrafLojista,
                                                ///
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "error",
                                                "mensagem" to
                                                        "Operação CANCELAMENTO falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    }
                                } else {
                                    withContext(
                                        Dispatchers.Main
                                    ) {
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
                                    "Erro ao realizar transação de Cancelamento >>Catch exception",
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

                        } else if (operacao == Operacoes.CANCELAMENTO) {
                            val entrada =
                                EntradaTransacaoHelper
                                    .operacaoCancelamento(
                                        identificadorTransacaoAutomacao =
                                        identificadorTransacao,
                                        operacao = operacao,
                                        modalidadePagamento =
                                        modalidadePagamento,
                                        tipoCartao =
                                        tipoCartao,
                                        tipoFinanciamento =
                                        tipoFinanciamento,
                                        nomeProvedor =
                                        nomeProvedor,
                                        valorTotal =
                                        valor.toLong(),
                                        numeroParcelas =
                                        parcelas,
                                        codigoMoeda =
                                        986, // Código padrão para Real Brasileiro
                                        estabelecimentoCNPJouCPF =
                                        estabelecimentoCNPJouCPF,
                                        documentoFiscal =
                                        documentoFiscal,
                                        campoLivre =
                                        campoLivre,
                                    )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao (operacaoCancelamento) com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida =
                                    TransacoesHelper
                                        .realizarTransacao(
                                            entrada
                                        )

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida =
                                        SaidaTransacaoHelper
                                            .extrairDados(
                                                saida
                                            )

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: Entrada de transação (operacaoCancelamento) criada com sucesso
                                        - mensagem_saida: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                     """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() ==
                                        0
                                    ) {
                                        // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja:
                                                List<
                                                        String> =
                                            (saida.obtemComprovanteDiferenciadoLoja()
                                                ?: "") as
                                                    List<
                                                            String>
                                        val stringComprovanteGrafLojista:
                                                String =
                                            saida.obtemComprovanteGraficoLojista()
                                                ?: ""
                                        val stringComprovanteGrafPortador:
                                                String =
                                            saida.obtemComprovanteGraficoPortador()
                                        val listComprovanteCompleto:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteCompleto()
                                        val listComprovanteReduzidoPortador:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "sucess",
                                                "mensagem" to
                                                        "Operação CANCELAMENTO criada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                                // impressão do comprovante
                                                "comprovanteGraficoPortador" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteCompletoList" to
                                                        listComprovanteCompleto,
                                                "comprovanteReduzidoPortadorList" to
                                                        listComprovanteReduzidoPortador,
                                                "comprovanteDifLojaList" to
                                                        listComprovanteDifLoja,
                                                "comprovanteGrafLojista" to
                                                        stringComprovanteGrafLojista,
                                                ///
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "error",
                                                "mensagem" to
                                                        "Operação CANCELAMENTO falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    }
                                } else {
                                    withContext(
                                        Dispatchers.Main
                                    ) {
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
                                    "Erro ao realizar transação de Cancelamento >>Catch exception",
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

                        } else if (operacao == Operacoes.RELATORIO_RESUMIDO) {
                            val entrada =
                                EntradaTransacaoHelper
                                    .operacaoCancelamento(
                                        identificadorTransacaoAutomacao =
                                        identificadorTransacao,
                                        operacao = operacao,
                                        modalidadePagamento =
                                        modalidadePagamento,
                                        tipoCartao =
                                        tipoCartao,
                                        tipoFinanciamento =
                                        tipoFinanciamento,
                                        nomeProvedor =
                                        nomeProvedor,
                                        valorTotal =
                                        valor.toLong(),
                                        numeroParcelas =
                                        parcelas,
                                        codigoMoeda =
                                        986, // Código padrão para Real Brasileiro
                                        estabelecimentoCNPJouCPF =
                                        estabelecimentoCNPJouCPF,
                                        documentoFiscal =
                                        documentoFiscal,
                                        campoLivre =
                                        campoLivre,
                                    )

                            Log.d(
                                "PaygoTefPlugin",
                                "Realizando Transacao (operacaoRelatorioResumido) com identificador: $identificadorTransacao"
                            )
                            try {
                                val saida =
                                    TransacoesHelper
                                        .realizarTransacao(
                                            entrada
                                        )

                                if (saida != null) {
                                    val idConfirmacaoTransacao =
                                        saida.obtemIdentificadorConfirmacaoTransacao()
                                    val dadosSaida =
                                        SaidaTransacaoHelper
                                            .extrairDados(
                                                saida
                                            )

                                    Log.d(
                                        "PaygoTefPlugin",
                                        """
                                        ➤ Saída da transação:
                                        - Identificador: $identificadorTransacao
                                        - idConfirmacao: $idConfirmacaoTransacao
                                        - Mensagem: Entrada de transação (operacaoRelatorioResumido) criada com sucesso
                                        - mensagem_saida: ${dadosSaida["mensagemResultado"]}
                                        - Resultado: ${dadosSaida["resultadoTransacao"]}
                                        - Necessita Confirmação: ${dadosSaida["necessitaConfirmacao"]}
                                     """.trimIndent()
                                    )

                                    if (saida.obtemResultadoTransacao() ==
                                        0
                                    ) {
                                        // impressão do
                                        // comprovante
                                        val listComprovanteDifLoja:
                                                List<
                                                        String> =
                                            (saida.obtemComprovanteDiferenciadoLoja()
                                                ?: "") as
                                                    List<
                                                            String>
                                        val stringComprovanteGrafLojista:
                                                String =
                                            saida.obtemComprovanteGraficoLojista()
                                                ?: ""
                                        val stringComprovanteGrafPortador:
                                                String =
                                            saida.obtemComprovanteGraficoPortador()
                                        val listComprovanteCompleto:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteCompleto()
                                        val listComprovanteReduzidoPortador:
                                                List<
                                                        String> =
                                            saida.obtemComprovanteReduzidoPortador()
                                        ////
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "sucess",
                                                "mensagem" to
                                                        "Operação Relatório Resumido criada com sucesso.",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                                // impressão do comprovante
                                                "comprovanteGraficoPortador" to
                                                        stringComprovanteGrafPortador,
                                                "comprovanteCompletoList" to
                                                        listComprovanteCompleto,
                                                "comprovanteReduzidoPortadorList" to
                                                        listComprovanteReduzidoPortador,
                                                "comprovanteDifLojaList" to
                                                        listComprovanteDifLoja,
                                                "comprovanteGrafLojista" to
                                                        stringComprovanteGrafLojista,
                                                ///
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    } else {
                                        val retorno =
                                            mapOf(
                                                "status" to
                                                        "error",
                                                "mensagem" to
                                                        "Operação CANCELAMENTO falhou!",
                                                "identificadorTransacao" to
                                                        entrada.obtemIdTransacaoAutomacao(),
                                                "valorTotal" to
                                                        entrada.obtemValorTotal(),
                                                "modalidadePagamento" to
                                                        entrada.obtemModalidadePagamento()
                                                            .name,
                                                "numeroParcelas" to
                                                        entrada.obtemNumeroParcelas(),
                                                "codigoMoeda" to
                                                        entrada.obtemCodigoMoeda(),
                                                "documentoFiscal" to
                                                        entrada.obtemDocumentoFiscal(),
                                                "estabelecimento" to
                                                        entrada.obtemEstabelecimentoCNPJouCPF(),
                                                "campoLivre" to
                                                        entrada.obtemDadosAdicionaisAutomacao1(),
                                                "idTransacao" to
                                                        identificadorTransacao,
                                                "idConfirmacaoTransacao" to
                                                        idConfirmacaoTransacao,
                                                "mensagem_saida" to
                                                        dadosSaida[
                                                            "mensagemResultado"],
                                                "resultadoTransacao" to
                                                        dadosSaida[
                                                            "resultadoTransacao"],
                                            )

                                        withContext(
                                            Dispatchers
                                                .Main
                                        ) {
                                            result.success(
                                                retorno
                                            )
                                        }
                                    }
                                } else {
                                    withContext(
                                        Dispatchers.Main
                                    ) {
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
                                    "Erro ao realizar transação de Relatório Resumido >>Catch exception",
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
                        } else {
                            withContext(Dispatchers.Main) {
                                result.notImplemented()
                            }
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
