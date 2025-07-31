import 'package:flutter_test/flutter_test.dart';
import 'package:paygo_tef/paygo_tef.dart';
import 'package:paygo_tef/paygo_tef_platform_interface.dart';
import 'package:paygo_tef/src/enums/paygo_tef_cartoes_enum.dart';
import 'package:paygo_tef/src/enums/paygo_tef_financiamentos_enum.dart';
import 'package:paygo_tef/src/enums/paygo_tef_modalidades_pgto_enum.dart';
import 'package:paygo_tef/src/enums/paygo_tef_operacoes_enum.dart';
import 'package:paygo_tef/src/paygo_tef_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockPaygoTefPlatform with MockPlatformInterfaceMixin implements PaygoTefPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<String> testTransaction(String valor) {
    // TODO: implement testTransaction
    throw UnimplementedError();
  }

  @override
  Future<String> enviarDadosAutomacao({required String nome, required String versao, required String nomePdv}) {
    // TODO: implement enviarDadosAutomacao
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaTransacaoVenda({
    required String identificadorTransacao,
    required OperacaoTefEnum operacao,
    required int valor,
    required ModalidadesPgtoEnum modalidadePagamento,
    required CartoesPgtoEnum tipoCartao,
    required FinanciamentosEnum tipoFinanciamento,
    required String nomeProvedor,
    required int parcelas,
    String estabelecimentoCNPJouCPF = '',
    String documentoFiscal = '',
    String campoLivre = '',
  }) {
    // TODO: implement enviarEntradaTransacaoVenda
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaTransacaoVersao({required String identificadorTransacao, required OperacaoTefEnum operacao}) {
    // TODO: implement enviarEntradaTransacaoVersao
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> cancelarTransacaoVenda({
    required String identificadorTransacao,
    required OperacaoTefEnum operacao,
    required int valor,
    required ModalidadesPgtoEnum modalidadePagamento,
    required CartoesPgtoEnum tipoCartao,
    required FinanciamentosEnum tipoFinanciamento,
    required String nomeProvedor,
    required int parcelas,
    String estabelecimentoCNPJouCPF = '',
    String documentoFiscal = '',
    String campoLivre = '',
  }) {
    // TODO: implement cancelarTransacaoVenda
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> exibePontoDeCapturaInstalado({required OperacaoTefEnum operacao}) {
    // TODO: implement exibePontoDeCapturaInstalado
    throw UnimplementedError();
  }
}

void main() {
  final PaygoTefPlatform initialPlatform = PaygoTefPlatform.instance;

  test('$MethodChannelPaygoTef is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPaygoTef>());
  });

  test('getPlatformVersion', () async {
    PaygoTef paygoTefPlugin = PaygoTef();
    MockPaygoTefPlatform fakePlatform = MockPaygoTefPlatform();
    PaygoTefPlatform.instance = fakePlatform;

    expect(await paygoTefPlugin.getPlatformVersion(), '42');
  });
}
