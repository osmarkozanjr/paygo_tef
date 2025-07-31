package br.infosolutions.iceasaplus.paygo_tef

import br.com.setis.interfaceautomacao.DadosAutomacao
import br.com.setis.interfaceautomacao.Personalizacao

object DadosAutomacaoHelper {
    fun criar(
            nome: String,
            versao: String,
            nomePdv: String,
            suportaTroco: Boolean = false,
            capturaPinPad: Boolean = true,
            requerConfirmacao: Boolean = true,
            requisitaSenha: Boolean = true,
            personalizacao: Personalizacao? = null
    ): DadosAutomacao {
        return DadosAutomacao(
                nome, // nomeAutomacao
                versao, // versaoAutomacao
                nomePdv, // nomePdv
                suportaTroco,
                capturaPinPad,
                requerConfirmacao,
                requisitaSenha,
                personalizacao // null se não tiver
        )
    }
}
