package br.com.okjsolucoes.paygo_tef

import android.util.Log
import br.com.setis.interfaceautomacao.SaidaTransacao
import br.com.setis.interfaceautomacao.ViasImpressao

object SaidaTransacaoHelper {

    fun extrairDados(saida: SaidaTransacao): Map<String, Any?> {
        return try {
            val viasImpressaoDisponiveis: ViasImpressao = saida.obtemViasImprimir()
                
           // val viaEnum = ViasImpressao.valueOf( viasImpressaoDisponiveis.name)

            val timeStampTransacaoOriginal: Long?  =  saida.obtemDataHoraTransacaoOriginal()?.time
            val timeStampTransacao: Long? = saida.obtemDataHoraTransacao()?.time

            val mensagemResultado = saida.obtemMensagemResultado()?.replace("\n", " ")
            val resultado = mapOf(
                "mensagemResultado" to mensagemResultado,
                "resultadoTransacao" to saida.obtemResultadoTransacao(),
                "necessitaConfirmacao" to saida.obtemInformacaoConfirmacao(),
                "idConfirmacaoTransacao" to saida.obtemIdentificadorConfirmacaoTransacao(),
                "viasImpressaoDisponveis" to viasImpressaoDisponiveis.name,
                "referenciaLocalOriginal" to saida.obtemReferenciaLocalOriginal(),
                "codigoAutorizacao" to saida.obtemCodigoAutorizacao(),
                "codigoAutorizacaoOriginal" to saida.obtemCodigoAutorizacaoOriginal(),
                "nsuHost" to saida.obtemNsuHost(),
                "nsuHostOriginal" to saida.obtemNsuHostOriginal(),
                "nsuLocal" to saida.obtemNsuLocal(),
                "nsuLocalIOriginal" to saida.obtemNsuLocalOriginal(),
                "nomeCartao" to saida.obtemNomeCartaoPadrao(),
                "panCartao" to  saida.obtemPanMascaradoPadrao(),
                "aidCartao" to saida.obtemAidCartao(),
                "timeStampTransacao" to timeStampTransacao,
                "timeStampTransacaoOriginal" to timeStampTransacaoOriginal,


            )
            Log.d("SaidaTransacaoHelper", "Dados extraídos com sucesso: $resultado")
            resultado
        } catch (e: Exception) {
            Log.e("SaidaTransacaoHelper", "Erro ao extrair dados da SaidaTransacao", e)
            throw e
        }
    }
}
