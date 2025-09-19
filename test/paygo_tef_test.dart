
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:paygo_tef/paygo_tef.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const MethodChannel channel = MethodChannel('br.com.okjsolucoes.paygo_tef');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      switch (methodCall.method) {
        case 'dadosAutomacao':
          return 'OK';
        case 'entradaTransacao':
          return {'status': 'success', 'message': 'Transação realizada com sucesso'};
        default:
          return null;
      }
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('enviarDadosAutomacao should return OK', () async {
    final result = await PaygoTef.enviarDadosAutomacao(
      nome: 'App Teste',
      versao: '1.0.0',
      nomePdv: 'PDV01',
    );

    expect(result, 'OK');
  });

  test('enviarEntradaTransacaoVenda should return success', () async {
    final result = await PaygoTef.enviarEntradaTransacaoVenda(
      identificadorTransacao: '12345',
      operacao: PaygoTefOperacaoTefEnum.VENDA,
      valor: 1000,
      modalidadePagamento: PaygoTefModalidadesPgtoEnum.PAGAMENTO_CARTAO,
      tipoCartao: PaygoTefCartoesPgtoEnum.CARTAO_CREDITO,
      tipoFinanciamento: PaygoTefFinanciamentosEnum.A_VISTA,
      nomeProvedor: 'DEMO',
      parcelas: 1,
    );

    expect(result['status'], 'success');
  });

  test('cancelarTransacaoVenda should return success', () async {
    final result = await PaygoTef.cancelarTransacaoVenda(
      identificadorTransacao: '12345',
      operacao: PaygoTefOperacaoTefEnum.CANCELAMENTO,
      valor: 1000,
      modalidadePagamento: PaygoTefModalidadesPgtoEnum.PAGAMENTO_CARTAO,
      tipoCartao: PaygoTefCartoesPgtoEnum.CARTAO_CREDITO,
      nomeProvedor: 'DEMO',
      dataHoraTransacao: DateTime.now(),
    );

    expect(result['status'], 'success');
  });

  test('enviarEntradaTransacaoReimpressao should return success', () async {
    final result = await PaygoTef.enviarEntradaTransacaoReimpressao(
      identificadorTransacao: '12345',
      operacao: PaygoTefOperacaoTefEnum.REIMPRESSAO,
    );

    expect(result['status'], 'success');
  });

  test('enviarEntradaRelatorioDetalhado should return success', () async {
    final result = await PaygoTef.enviarEntradaRelatorioDetalhado(
      identificadorTransacao: '12345',
      operacao: PaygoTefOperacaoTefEnum.RELATORIO_DETALHADO,
    );

    expect(result['status'], 'success');
  });

  test('enviarEntradaRelatorioResumido should return success', () async {
    final result = await PaygoTef.enviarEntradaRelatorioResumido(
      identificadorTransacao: '12345',
      operacao: PaygoTefOperacaoTefEnum.RELATORIO_RESUMIDO,
    );

    expect(result['status'], 'success');
  });

  test('enviarEntradaRelatorioSintetico should return success', () async {
    final result = await PaygoTef.enviarEntradaRelatorioSintetico(
      identificadorTransacao: '12345',
      operacao: PaygoTefOperacaoTefEnum.RELATORIO_SINTETICO,
    );

    expect(result['status'], 'success');
  });
}
