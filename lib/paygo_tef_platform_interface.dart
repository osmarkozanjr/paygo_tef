import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'src/enums/paygo_tef_cartoes_enum.dart';
import 'src/enums/paygo_tef_financiamentos_enum.dart';
import 'src/paygo_tef_method_channel.dart';

import 'src/enums/paygo_tef_modalidades_pgto_enum.dart';
import 'src/enums/paygo_tef_operacoes_enum.dart';

abstract class PaygoTefPlatform extends PlatformInterface {
  /// Constructs a PaygoTefPlatform.
  PaygoTefPlatform() : super(token: _token);

  static final Object _token = Object();

  static PaygoTefPlatform _instance = MethodChannelPaygoTef();

  /// The default instance of [PaygoTefPlatform] to use.
  ///
  /// Defaults to [MethodChannelPaygoTef].
  static PaygoTefPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their owne
  /// platform-specific class that extends [PaygoTefPlatform] when
  /// they register themselves.
  static set instance(PaygoTefPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String> testTransaction(String valor) {
    throw UnimplementedError('testTransaction() has not been implemented.');
  }

  Future<String> enviarDadosAutomacao({required String nome, required String versao, required String nomePdv}) {
    throw UnimplementedError('enviarDadosAutomacao() has not been implemented.');
  }

  Future<Map<String, dynamic>> enviarEntradaTransacaoVersao({
    required String identificadorTransacao,
    required OperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    throw UnimplementedError('enviarEntradaTransacaoVersao() has not been implemented.');
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
    throw UnimplementedError('enviarEntradaTransacaoVenda() has not been implemented.');
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
    throw UnimplementedError('cancelarTransacaoVenda() has not been implemented.');
  }

  Future<Map<String, dynamic>> exibePontoDeCapturaInstalado({
     required String identificadorTransacao,
    required OperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) async {
    throw UnimplementedError('exibePontoDeCapturaInstalado() has not been implemented.');
  }

    Future<Map<String, dynamic>> enviarEntradaTransacaoReimpressao({
       required String identificadorTransacao,
    required OperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    throw UnimplementedError('enviarEntradaTransacaoVersao() has not been implemented.');
  }
}
