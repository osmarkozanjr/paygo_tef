import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import '../paygo_tef_platform_interface.dart';

import 'enums/paygo_tef_cartoes_enum.dart';
import 'package:uuid/uuid.dart';
import 'enums/paygo_tef_operacoes_enum.dart';
import 'enums/paygo_tef_modalidades_pgto_enum.dart';
import 'enums/paygo_tef_financiamentos_enum.dart';

//zyz
const String _channelName = 'br.com.okjsolucoes.paygo_tef';

/// An implementation of [PaygoTefPlatform] that uses method channels.
class MethodChannelPaygoTef extends PaygoTefPlatform {
  /// The method channel used to interact with the native platform.
  ///

  final methodChannel = const MethodChannel(_channelName);

  @visibleForTesting
  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String> testTransaction(String valor) async {
    try {
      final String resultado = await methodChannel.invokeMethod('testTransaction', {'valor': valor});
      return resultado;
    } on PlatformException catch (e) {
      return 'Falha na transação: ${e.message}';
    }
  }

  @override
  Future<String> enviarDadosAutomacao({required String nome, required String versao, required String nomePdv}) async {
    try {
      final String result = await methodChannel.invokeMethod('dadosAutomacao', {'nome': nome, 'versao': versao, 'nomePdv': nomePdv});
      return result;
    } on PlatformException catch (e) {
      return 'Erro na automação: ${(e.message ?? e.toString())}';
    } on MissingPluginException catch (e) {
      return 'Erro na automação: ${(e.message ?? e.toString())}';
    } on Exception catch (e) {
      return 'Erro na automação: ${e.toString()}';
    }
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaTransacaoVenda({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
    required int valor,
    required PaygoTefModalidadesPgtoEnum modalidadePagamento,
    required PaygoTefCartoesPgtoEnum tipoCartao, //enum dentro de paygo_tef_cartoes_enum.dart
    required PaygoTefFinanciamentosEnum tipoFinanciamento, //enum dentro de paygo_tef_financiamentos_enum.dart
    required String nomeProvedor,
    required int parcelas,
    String estabelecimentoCNPJouCPF = '',
    String documentoFiscal = '',
    String campoLivre = '',
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();

      if (operacao == PaygoTefOperacaoTefEnum.VENDA) {
        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {
          'identificadorTransacao': idTransacao,
          'operacao': operacao.name,
          'valor': valor,
          'modalidadePagamento': modalidadePagamento.name,
          'tipoCartao': tipoCartao.name,
          'tipoFinanciamento': tipoFinanciamento.name,
          'nomeProvedor': nomeProvedor,
          'parcelas': parcelas,
          'estabelecimentoCNPJouCPF': estabelecimentoCNPJouCPF,
          'documentoFiscal': documentoFiscal,
          'campoLivre': campoLivre,
        });
        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {
                'status': resultMap['status'],
                'map': resultMap,
                'message': resultMap['mensagem_saida'],
              }
            : {
                'status': 'error',
                'map': resultMap,
                'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação',
              };
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {
        'status': 'error',
        'map': null,
        'message': 'Erro na automação: ${(e.message ?? e.toString())}',
        'code': e.code,
        'details': e.details,
      };
    } on MissingPluginException catch (e) {
      return {
        'status': 'error',
        'map': null,
        'message': 'Erro na automação: ${(e.message ?? e.toString())}',
      };
    } on Exception catch (e) {
      return {
        'status': 'error',
        'map': null,
        'message': 'Erro na automação: ${e.toString()}',
      };
    }
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaTransacaoVersao({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();

      if (operacao == PaygoTefOperacaoTefEnum.VERSAO) {
        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {'identificadorTransacao': idTransacao, 'operacao': operacao.name});

        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {
                'status': resultMap['status'],
                'map': resultMap,
                'message': resultMap['mensagem_saida'],
              }
            : {
                'status': 'error',
                'map': resultMap,
                'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação',
              };
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {
        'status': 'error',
        'map': null,
        'message': 'Erro na automação: ${(e.message ?? e.toString())}',
        'code': e.code,
        'details': e.details,
      };
    } on MissingPluginException catch (e) {
      return {
        'status': 'error',
        'map': null,
        'message': 'Erro na automação: ${(e.message ?? e.toString())}',
      };
    } on Exception catch (e) {
      return {
        'status': 'error',
        'map': null,
        'message': 'Erro na automação: ${e.toString()}',
      };
    }
  }

  @override
  Future<Map<String, dynamic>> cancelarTransacaoVenda({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
    required int valor,
    required PaygoTefModalidadesPgtoEnum modalidadePagamento,
    required PaygoTefCartoesPgtoEnum tipoCartao, //enum dentro de paygo_tef_cartoes_enum.dart
    //required PaygoTefFinanciamentosEnum tipoFinanciamento, //enum dentro de paygo_tef_financiamentos_enum.dart
    required String nomeProvedor,
    //required int parcelas,
    String estabelecimentoCNPJouCPF = '',
    //String documentoFiscal = '',
    //String campoLivre = '',
    String? nsuHost,
    String? referenciaLocaloriginal,
    String? codigoAutorizacao,
    required DateTime dataHoraTransacao,
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();

      if (operacao == PaygoTefOperacaoTefEnum.CANCELAMENTO) {
        final millisDataHoraTransacaoOriginal = dataHoraTransacao.millisecondsSinceEpoch;

        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {
          'identificadorTransacao': idTransacao,
          'operacao': operacao.name,
          'valor': valor,
          'modalidadePagamento': modalidadePagamento.name,
          'tipoCartao': tipoCartao.name,
          //'tipoFinanciamento': tipoFinanciamento.name,
          'nomeProvedor': nomeProvedor,
          //'parcelas': parcelas,
          'estabelecimentoCNPJouCPF': estabelecimentoCNPJouCPF,
          'nsuHostTransacao': nsuHost,
          'referenciaLocaloriginal': referenciaLocaloriginal,
          'codigoAutorizacao': codigoAutorizacao,
          'timeStampTransacao': millisDataHoraTransacaoOriginal,
          //'documentoFiscal': documentoFiscal,
          //'campoLivre': campoLivre,
        });
        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {'status': resultMap['status'], 'map': resultMap, 'message': resultMap['mensagem_saida']}
            : {'status': 'error', 'map': resultMap, 'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação'};
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}', 'code': e.code, 'details': e.details};
    } on MissingPluginException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}'};
    } on Exception catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${e.toString()}'};
    }
  }

  @override
  Future<Map<String, dynamic>> exibePontoDeCapturaInstalado({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();
      if (operacao == PaygoTefOperacaoTefEnum.EXIBE_PDC) {
        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {
          'identificadorTransacao': idTransacao,
          'operacao': operacao.name,
        });
        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {'status': resultMap['status'], 'map': resultMap, 'message': resultMap['mensagem_saida']}
            : {'status': 'error', 'map': resultMap, 'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação'};
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}', 'code': e.code, 'details': e.details};
    } on MissingPluginException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}'};
    } on Exception catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${e.toString()}'};
    }
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaTransacaoReimpressao({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();
      if (operacao == PaygoTefOperacaoTefEnum.REIMPRESSAO) {
        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {
          'identificadorTransacao': idTransacao,
          'operacao': operacao.name,
        });
        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {'status': resultMap['status'], 'map': resultMap, 'message': resultMap['mensagem_saida']}
            : {'status': 'error', 'map': resultMap, 'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação'};
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}', 'code': e.code, 'details': e.details};
    } on MissingPluginException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}'};
    } on Exception catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${e.toString()}'};
    }
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaRelatorioResumido({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();
      if (operacao == PaygoTefOperacaoTefEnum.RELATORIO_RESUMIDO) {
        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {
          'identificadorTransacao': idTransacao,
          'operacao': operacao.name,
        });
        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {'status': resultMap['status'], 'map': resultMap, 'message': resultMap['mensagem_saida']}
            : {'status': 'error', 'map': resultMap, 'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação'};
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}', 'code': e.code, 'details': e.details};
    } on MissingPluginException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}'};
    } on Exception catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${e.toString()}'};
    }
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaRelatorioSintetico({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();
      if (operacao == PaygoTefOperacaoTefEnum.RELATORIO_SINTETICO) {
        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {
          'identificadorTransacao': idTransacao,
          'operacao': operacao.name,
        });
        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {'status': resultMap['status'], 'map': resultMap, 'message': resultMap['mensagem_saida']}
            : {'status': 'error', 'map': resultMap, 'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação'};
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}', 'code': e.code, 'details': e.details};
    } on MissingPluginException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}'};
    } on Exception catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${e.toString()}'};
    }
  }

  @override
  Future<Map<String, dynamic>> enviarEntradaRelatorioDetalhado({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) async {
    try {
      final String idTransacao = identificadorTransacao.isNotEmpty ? identificadorTransacao : const Uuid().v4();
      if (operacao == PaygoTefOperacaoTefEnum.RELATORIO_DETALHADO) {
        final rawResult = await methodChannel.invokeMethod('entradaTransacao', {
          'identificadorTransacao': idTransacao,
          'operacao': operacao.name,
        });
        final resultMap = (rawResult as Map?)?.map((key, value) => MapEntry(key.toString(), value));
        return resultMap != null
            ? {'status': resultMap['status'], 'map': resultMap, 'message': resultMap['mensagem_saida']}
            : {'status': 'error', 'map': resultMap, 'message': resultMap?['mensagem_saida'] ?? 'Sem resposta da automação'};
      } else {
        throw Exception('Operação inválida para este método. Use o método correto!');
      }
    } on PlatformException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}', 'code': e.code, 'details': e.details};
    } on MissingPluginException catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${(e.message ?? e.toString())}'};
    } on Exception catch (e) {
      return {'status': 'error', 'map': null, 'message': 'Erro na automação: ${e.toString()}'};
    }
  }
}
