package br.com.okjsolucoes.paygo_tef

import android.util.Log
import br.com.setis.interfaceautomacao.SaidaTransacao

object SaidaTransacaoHelper {

    fun extrairDados(saida: SaidaTransacao): Map<String, Any?> {
        return try {
            val resultado = mapOf(
                "mensagemResultado" to saida.obtemMensagemResultado(),
                "resultadoTransacao" to saida.obtemResultadoTransacao(),
                "necessitaConfirmacao" to saida.obtemInformacaoConfirmacao(),
                "idConfirmacaoTransacao" to saida.obtemIdentificadorConfirmacaoTransacao()
            )
            Log.d("SaidaTransacaoHelper", "Dados extraídos com sucesso: $resultado")
            resultado
        } catch (e: Exception) {
            Log.e("SaidaTransacaoHelper", "Erro ao extrair dados da SaidaTransacao", e)
            throw e
        }
    }
}
