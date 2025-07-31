package br.infosolutions.iceasaplus.paygo_tef

import java.util.UUID
import android.util.Log
import android.content.Context

import br.com.setis.interfaceautomacao.*

object TransacoesHelper {

    private var transacoes: Transacoes? = null

    fun inicializar(dadosAutomacao: DadosAutomacao, context: Context) {
        transacoes = Transacoes.obtemInstancia(dadosAutomacao, context)
        Log.d("TransacoesHelper", "Transacoes inicializado com sucesso")
    }

    fun realizarTransacao(entrada: EntradaTransacao): SaidaTransacao? {
        return try {
             Log.e("TransacoesHelper", "Vai realizar a transação")
            transacoes?.realizaTransacao(entrada)
        } catch (e: AplicacaoNaoInstaladaExcecao) {
            Log.e("TransacoesHelper", "Aplicação não instalada: \${e.message}")
            throw e
        } catch (e: QuedaConexaoTerminalExcecao) {
            Log.e("TransacoesHelper", "Queda de conexão com o terminal: \${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("TransacoesHelper", "Erro ao realizar transação: ${e.message}")
            throw e
        }
    }

    fun confirmarTransacao(confirmacao: Confirmacao) {
        try {
            transacoes?.confirmaTransacao(confirmacao)
        } catch (e: Exception) {
            Log.e("TransacoesHelper", "Erro ao confirmar transação: ${e.message}")
            throw e
        }
    }

    fun resolverPendencia(pendente: TransacaoPendenteDados, confirmacao: Confirmacao) {
        try {
            transacoes?.resolvePendencia(pendente, confirmacao)
        } catch (e: Exception) {
            Log.e("TransacoesHelper", "Erro ao resolver pendência: ${e.message}")
            throw e
        }
    }

    fun obterVersoes(): Versoes? {
        return transacoes?.obtemVersoes()
    }
}