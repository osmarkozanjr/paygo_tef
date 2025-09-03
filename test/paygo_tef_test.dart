import 'package:flutter_test/flutter_test.dart';
import 'package:paygo_tef/paygo_tef.dart';
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
    required PaygoTefOperacaoTefEnum operacao,
    required int valor,
    required PaygoTefModalidadesPgtoEnum modalidadePagamento,
    required PaygoTefCartoesPgtoEnum tipoCartao,
    required PaygoTefFinanciamentosEnum tipoFinanciamento,
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
  Future<Map<String, dynamic>> enviarEntradaTransacaoVersao({required String identificadorTransacao, required PaygoTefOperacaoTefEnum operacao}) {
    // TODO: implement enviarEntradaTransacaoVersao
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> cancelarTransacaoVenda({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao,
    required int valor,
    required PaygoTefModalidadesPgtoEnum modalidadePagamento,
    required PaygoTefCartoesPgtoEnum tipoCartao,
    // required PaygoTefFinanciamentosEnum tipoFinanciamento,
    required String nomeProvedor,
    // required int parcelas,
    String estabelecimentoCNPJouCPF = '',
    // String documentoFiscal = '',
    // String campoLivre = '',
    String? nsuTransacaoOriginal,
    String? referenciaLocaloriginal,
    String? codigoAutorizacaoOriginal,
    required DateTime dataHoraTransacaoOriginal,
  }) {
    // TODO: implement cancelarTransacaoVenda
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> exibePontoDeCapturaInstalado({required String identificadorTransacao, required PaygoTefOperacaoTefEnum operacao}) {
    // TODO: implement exibePontoDeCapturaInstalado
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaTransacaoReimpressao(
      {required String identificadorTransacao, required PaygoTefOperacaoTefEnum operacao}) {
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaRelatorioResumido({required String identificadorTransacao, required PaygoTefOperacaoTefEnum operacao}) {
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaRelatorioSintetico({required String identificadorTransacao, required PaygoTefOperacaoTefEnum operacao}) {
    throw UnimplementedError();
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaRelatorioDetalhado({required String identificadorTransacao, required PaygoTefOperacaoTefEnum operacao}) {
    throw UnimplementedError();
  }
}

void main() {
  final PaygoTefPlatform initialPlatform = PaygoTefPlatform.instance;

  test('$MethodChannelPaygoTef is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPaygoTef>());
  });

  test('getPlatformVersion', () async {
    MockPaygoTefPlatform fakePlatform = MockPaygoTefPlatform();
    PaygoTefPlatform.instance = fakePlatform;

    expect(await PaygoTef.getPlatformVersion(), '42');
  });
}
