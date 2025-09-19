package br.com.okjsolucoes.paygo_tef


import android.util.Log
import java.util.UUID

import br.com.setis.interfaceautomacao.EntradaTransacao
import br.com.setis.interfaceautomacao.ModalidadesPagamento
import br.com.setis.interfaceautomacao.ModalidadesTransacao
import br.com.setis.interfaceautomacao.Cartoes
import br.com.setis.interfaceautomacao.Operacoes
import br.com.setis.interfaceautomacao.Financiamentos
import java.util.Date

object EntradaTransacaoHelper {

    fun operacaoVenda(
        operacao: Operacoes = Operacoes.VENDA,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
        valorTotal: Long,
        modalidadePagamento: ModalidadesPagamento,
        tipoCartao: Cartoes,
        tipoFinanciamento: Financiamentos,
        nomeProvedor: String,
        numeroParcelas: Int = 1,
        codigoMoeda: Int = 986,
        documentoFiscal: String? = null,
        estabelecimentoCNPJouCPF: String? = null,
        campoLivre: String? = null
    ): EntradaTransacao {
        val entrada = EntradaTransacao(operacao, identificadorTransacaoAutomacao)

        entrada.informaValorTotal(valorTotal.toString())
        entrada.informaModalidadePagamento(modalidadePagamento)
        entrada.informaTipoCartao(tipoCartao)
        entrada.informaTipoFinanciamento(tipoFinanciamento)
        entrada.informaNomeProvedor(nomeProvedor)
        entrada.informaCodigoMoeda(codigoMoeda.toString())
         
        if (!documentoFiscal.isNullOrEmpty()) {
            entrada.informaDocumentoFiscal(documentoFiscal)
        }

        if (!estabelecimentoCNPJouCPF.isNullOrEmpty()) {
            entrada.informaEstabelecimentoCNPJouCPF(estabelecimentoCNPJouCPF)
        }

        if (!campoLivre.isNullOrEmpty()) {
            entrada.informaDadosAdicionaisAutomacao1(campoLivre)
        }

        return entrada
    }

    fun operacaoVersao(
        operacao: Operacoes = Operacoes.VERSAO,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
    ): EntradaTransacao {
        val entrada = EntradaTransacao(operacao, identificadorTransacaoAutomacao)
        return entrada
    }    

    fun operacaoCancelamento(
        operacao: Operacoes = Operacoes.CANCELAMENTO,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
        valorTotal: Long,
        modalidadePagamento: ModalidadesPagamento,
        tipoCartao: Cartoes,
        tipoFinanciamento: Financiamentos,
        nomeProvedor: String,
        numeroParcelas: Int = 1,
        codigoMoeda: Int = 986,
        documentoFiscal: String? = null,
        estabelecimentoCNPJouCPF: String? = null,
        campoLivre: String? = null,
        nsuHostTransacao: String? = null,
        referenciaLocaloriginal: String? = null,
        codigoAutorizacao: String? =  null,
        dataHoraTransacao: Date? = null
    ): EntradaTransacao {
        val entrada = EntradaTransacao(operacao, identificadorTransacaoAutomacao)
        Log.d("PaygoTefPlugin", "Operação: ${operacao.toString()}")
        Log.d("PaygoTefPlugin", "nome provedor: $nomeProvedor")
        Log.d("PaygoTefPlugin", "dataHoraTransacao: $dataHoraTransacao")
        Log.d("PaygoTefPlugin", "estabelecimentoCNPJouCPF: $estabelecimentoCNPJouCPF")
        Log.d("PaygoTefPlugin", "valorTotal: $valorTotal")
        Log.d("PaygoTefPlugin", "modalidade pgto: ${modalidadePagamento.toString()}")
        Log.d("PaygoTefPlugin", "tipoCartao: ${tipoCartao.toString()}")
        Log.d("PaygoTefPlugin", "codigo referencia enviado: $referenciaLocaloriginal")

        entrada.informaValorTotal(valorTotal.toString())
        entrada.informaModalidadePagamento(modalidadePagamento)
        entrada.informaTipoCartao(tipoCartao)
        entrada.informaTipoFinanciamento(tipoFinanciamento)
        entrada.informaNomeProvedor(nomeProvedor)
        entrada.informaCodigoMoeda(codigoMoeda.toString())
        if (!codigoAutorizacao.isNullOrEmpty()) {
            Log.d("PaygoTefPlugin", "Vai executar informaCodigoAutorizacao com valor: $codigoAutorizacao")
            var returnCod = entrada.informaCodigoAutorizacaoOriginal(codigoAutorizacao)
            Log.d("PaygoTefPlugin", "print no retorno de informaCodigoAutorizacaoOriginal: $returnCod")
            var outroReturn = entrada.obtemCodigoAutorizacaoOriginal()
            Log.d("PaygoTefPlugin", "print no retorno de obtemCodigoAutorizacaoOriginal: $outroReturn")
        }
        if (!nsuHostTransacao.isNullOrEmpty()) {
            Log.d("PaygoTefPlugin", "Vai executar informaNsuTransacaoOriginal com valor: $nsuHostTransacao")
            var returnCod = entrada.informaNsuTransacaoOriginal(nsuHostTransacao)
            Log.d("PaygoTefPlugin", "print no retorno de informaNsuTransacaoOriginal: $returnCod")
            var outroReturn = entrada.obtemNsuTransacaoOriginal()
            Log.d("PaygoTefPlugin", "print no retorno de obtemNsuTransacaoOriginal: $outroReturn")
        }
        if (!referenciaLocaloriginal.isNullOrEmpty()) {
            Log.d("PaygoTefPlugin", "Vai executar informaReferenciaLocalOriginal com valor: $referenciaLocaloriginal")
            var returnCod =  entrada.informaReferenciaLocalOriginal(referenciaLocaloriginal)
            Log.d("PaygoTefPlugin", "print no retorno de informaReferenciaLocalOriginal: $returnCod")
            var outroReturn = entrada.obtemReferenciaLocalOriginal()
            Log.d("PaygoTefPlugin", "print no retorno de obtemReferenciaLocalOriginal: $outroReturn")
        }
        


        entrada.informaDataHoraTransacaoOriginal(dataHoraTransacao)
         
        if (!documentoFiscal.isNullOrEmpty()) {
            entrada.informaDocumentoFiscal(documentoFiscal)
        }

        if (!estabelecimentoCNPJouCPF.isNullOrEmpty()) {
            entrada.informaEstabelecimentoCNPJouCPF(estabelecimentoCNPJouCPF)
        }

        if (!campoLivre.isNullOrEmpty()) {
            entrada.informaDadosAdicionaisAutomacao1(campoLivre)
        }

        return entrada
    }

    fun operacaoExibePdc(
        operacao: Operacoes = Operacoes.EXIBE_PDC,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
    ): EntradaTransacao {
        val entrada = EntradaTransacao(operacao, identificadorTransacaoAutomacao)
        return entrada
    }

    fun operacaoReimprimeUltComprovante(
        operacao: Operacoes = Operacoes.REIMPRESSAO,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
    ): EntradaTransacao{
        val entrada = EntradaTransacao(operacao,identificadorTransacaoAutomacao)
        return entrada
    }
    fun operacaoRelatorioResumido(
        operacao: Operacoes = Operacoes.RELATORIO_RESUMIDO,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
    ): EntradaTransacao{
        val entrada = EntradaTransacao(operacao, identificadorTransacaoAutomacao)
        return entrada
    }
    fun operacaoRelatorioSintetico(
        operacao: Operacoes = Operacoes.RELATORIO_SINTETICO,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
    ): EntradaTransacao{
        val entrada = EntradaTransacao(operacao, identificadorTransacaoAutomacao)
        return entrada
    }
    fun operacaoRelatorioDetalhado(
        operacao: Operacoes = Operacoes.RELATORIO_DETALHADO,
        identificadorTransacaoAutomacao: String = java.util.UUID.randomUUID().toString(),
    ): EntradaTransacao{
        val entrada = EntradaTransacao(operacao, identificadorTransacaoAutomacao)
        return entrada
    }
    
}
