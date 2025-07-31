import 'paygo_tef_platform_interface.dart';
import 'src/enums/paygo_tef_cartoes_enum.dart';
import 'src/enums/paygo_tef_financiamentos_enum.dart';
import 'src/enums/paygo_tef_modalidades_pgto_enum.dart';
import 'src/enums/paygo_tef_operacoes_enum.dart';

export 'paygo_tef_platform_interface.dart';
export 'src/paygo_tef_method_channel.dart';

class PaygoTef {
  static PaygoTefPlatform get _platform => PaygoTefPlatform.instance;

  Future<String?> getPlatformVersion() {
    return PaygoTefPlatform.instance.getPlatformVersion();
  }

  Future<String> testTransaction(String valor) {
    return _platform.testTransaction(valor);
  }

  Future<String> enviarDadosAutomacao({required String nome, required String versao, required String nomePdv}) {
    return _platform.enviarDadosAutomacao(nome: nome, versao: versao, nomePdv: nomePdv);
  }

  Future<Map<String, dynamic>> enviarEntradaTransacaoVenda({
    required String identificadorTransacao,
    required OperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
    required int valor,
    required ModalidadesPgtoEnum modalidadePagamento,
    required CartoesPgtoEnum tipoCartao, //enum dentro de paygo_tef_cartoes_enum.dart
    required FinanciamentosEnum tipoFinanciamento, //enum dentro de paygo_tef_financiamentos_enum.dart
    required String nomeProvedor,
    required int parcelas,
    String estabelecimentoCNPJouCPF = '',
    String documentoFiscal = '',
    String campoLivre = '',
  }) {
    return _platform.enviarEntradaTransacaoVenda(
      identificadorTransacao: identificadorTransacao,
      operacao: operacao,
      valor: valor,
      modalidadePagamento: modalidadePagamento,
      tipoCartao: tipoCartao,
      tipoFinanciamento: tipoFinanciamento,
      nomeProvedor: nomeProvedor,
      parcelas: parcelas,
      estabelecimentoCNPJouCPF: estabelecimentoCNPJouCPF,
      documentoFiscal: documentoFiscal,
      campoLivre: campoLivre,
    );
  }

  Future<Map<String, dynamic>> enviarEntradaTransacaoVersao({
    required String identificadorTransacao,
    required OperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.enviarEntradaTransacaoVersao(identificadorTransacao: identificadorTransacao, operacao: operacao);
  }

  Future<Map<String, dynamic>> cancelarTransacaoVenda({
    required String identificadorTransacao,
    required OperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
    required int valor,
    required ModalidadesPgtoEnum modalidadePagamento,
    required CartoesPgtoEnum tipoCartao, //enum dentro de paygo_tef_cartoes_enum.dart
    required FinanciamentosEnum tipoFinanciamento, //enum dentro de paygo_tef_financiamentos_enum.dart
    required String nomeProvedor,
    required int parcelas,
    String estabelecimentoCNPJouCPF = '',
    String documentoFiscal = '',
    String campoLivre = '',
  }) {
    return _platform.cancelarTransacaoVenda(
      identificadorTransacao: identificadorTransacao,
      operacao: operacao,
      valor: valor,
      modalidadePagamento: modalidadePagamento,
      tipoCartao: tipoCartao,
      tipoFinanciamento: tipoFinanciamento,
      nomeProvedor: nomeProvedor,
      parcelas: parcelas,
      estabelecimentoCNPJouCPF: estabelecimentoCNPJouCPF,
      documentoFiscal: documentoFiscal,
      campoLivre: campoLivre,
    );
  }

  Future<Map<String, dynamic>> exibePontoDeCapturaInstalado({
    required OperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.exibePontoDeCapturaInstalado(operacao: operacao);
  }
}
