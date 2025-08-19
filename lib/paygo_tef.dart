import 'package:paygo_tef/src/utils/decode_html_to_string_html.dart';

import 'paygo_tef_platform_interface.dart';
import 'src/enums/paygo_tef_cartoes_enum.dart';
import 'src/enums/paygo_tef_financiamentos_enum.dart';
import 'src/enums/paygo_tef_modalidades_pgto_enum.dart';
import 'src/enums/paygo_tef_operacoes_enum.dart';

export 'paygo_tef_platform_interface.dart';
export 'src/paygo_tef_method_channel.dart';
export 'src/enums/paygo_tef_cartoes_enum.dart';
export 'src/enums/paygo_tef_financiamentos_enum.dart';
export 'src/enums/paygo_tef_modalidades_pgto_enum.dart';
export 'src/enums/paygo_tef_operacoes_enum.dart';
export 'src/enums/paygo_tef_vias_impressao_enum.dart';
export 'src/enums/printer_type_enum.dart';
export 'src/utils/convert_string_html_to_escpos_bytes.dart';
export 'src/utils/decode_html_to_string_html.dart';
export 'src/utils/convert_base64_to_bitmap_escpos_bytes.dart';

class PaygoTef {
  static PaygoTefPlatform get _platform => PaygoTefPlatform.instance;

  /// Nome da chave no mapa de resposta contendo o comprovante completo em formato string html.
  static const keyComprovanteCompleto = 'comprovanteCompletoString';

  /// Nome da chave no mapa de resposta contendo o comprovante reduzido em formato string html.
  static const keyComprovanteReduzidoPortador = 'comprovanteReduzidoPortadorString';

  /// Nome da chave no mapa de resposta contendo o comprovante diferenciado loja em formato string html.
  static const keyComprovanteDifLoja = 'comprovanteDifLojaString';

  /// Nome da chave no mapa de resposta contendo o comprovante diferenciado portador em formato string html.
  ///
  ///   /// Utilize o utilitário [DecodeStringHtmlToBytes] para obter os bytes a partir desta string.
  ///
  /// Exemplo de uso:
  ///
  /// ...após receber a responseTransaction (Map<String, dynamic>) com o resultado de [enviarEntradaTransacaoVenda] :
  /// Dúvidas? Ver exemplo completo no docString de [enviarEntradaTransacaoVenda]
  /// Você deverá receber e tratar este comprovante assim:
  /// ```dart
  ///
  /// Map<String, dynamic> dadosResp = {};
  /// if (responseTransaction['map'] is Map<String, dynamic>) {
  ///   dadosResp.addAll(responseTransaction['map'] as Map<String, dynamic>);
  /// }
  ///
  ///  List<int> bytes = [];
  ///  if (dadosResp['status'] == 'success') {
  ///  //ler comprovantes para impressao
  ///  if (dadosResp[ PaygoTef.keyComprovanteGraficoPortadorBase64] != null) {
  ///    comprovanteGrafico = dadosResp[ PaygoTef.keyComprovanteGraficoPortadorBase64];
  ///    bytes = await DecodeStringHtmlToString().call(comprovanteGrafico!, PrintertypeEnum.m58mm);
  ///  }
  /// ```
  static const keyComprovanteDifPortador = 'comprovanteDifPortadorString';

  /// Nome da chave no mapa de resposta contendo o comprovante diferenciado portador em formato String base64.
  ///
  /// Utilize o utilitário [ConvertBase64ToBitmapEscPosBytes] para obter os bytes a partir desta string.
  ///
  /// Exemplo de uso:
  ///
  /// ...após receber a responseTransaction (Map<String, dynamic>) com o resultado de [enviarEntradaTransacaoVenda] :
  /// Dúvidas? Ver exemplo completo no docString de [enviarEntradaTransacaoVenda]
  /// Você deverá receber e tratar este comprovante assim:
  /// ```dart
  ///
  /// Map<String, dynamic> dadosResp = {};
  /// if (responseTransaction['map'] is Map<String, dynamic>) {
  ///   dadosResp.addAll(responseTransaction['map'] as Map<String, dynamic>);
  /// }
  ///
  ///  List<int> bytes = [];
  ///  if (dadosResp['status'] == 'success') {
  ///  //ler comprovantes para impressao
  ///  if (dadosResp[ PaygoTef.keyComprovanteGraficoPortadorBase64] != null) {
  ///    comprovanteGrafico = dadosResp[ PaygoTef.keyComprovanteGraficoPortadorBase64];
  ///    bytes = await ConvertBase64ToBitmapEscPosBytes().call(comprovanteGrafico!, PrintertypeEnum.m58mm);
  ///  }
  /// ```
  static const keyComprovanteGraficoPortadorBase64 = 'comprovanteGraficoPortadorBase64';

  /// Nome da chave no mapa de resposta contendo o comprovante diferenciado portador em formato String base64.
  ///
  /// Utilize o utilitário [ConvertBase64ToBitmapEscPosBytes] para obter os bytes a partir desta string.
  ///
  /// Exemplo de uso:
  ///
  /// ...após receber a responseTransaction (Map<String, dynamic>) com o resultado de [enviarEntradaTransacaoVenda] :
  /// Dúvidas? Ver exemplo completo no docString de [enviarEntradaTransacaoVenda]
  /// Você deverá receber e tratar este comprovante assim:
  /// ```dart
  ///
  /// Map<String, dynamic> dadosResp = {};
  /// if (responseTransaction['map'] is Map<String, dynamic>) {
  ///   dadosResp.addAll(responseTransaction['map'] as Map<String, dynamic>);
  /// }
  ///
  ///  List<int> bytes = [];
  ///  if (dadosResp['status'] == 'success') {
  ///  //ler comprovantes para impressao
  ///  if (dadosResp[ PaygoTef.keyComprovanteGrafLojistaBase64] != null) {
  ///    comprovanteGrafico = dadosResp[ PaygoTef.keyComprovanteGrafLojistaBase64];
  ///    bytes = await ConvertBase64ToBitmapEscPosBytes().call(comprovanteGrafico!, PrintertypeEnum.m58mm);
  ///  }
  /// ```
  static const keyComprovanteGrafLojistaBase64 = 'comprovanteGrafLojistaBase64';

  static Future<String?> getPlatformVersion() {
    return PaygoTefPlatform.instance.getPlatformVersion();
  }

  static Future<String> testTransaction(String valor) {
    return _platform.testTransaction(valor);
  }

  static Future<String> enviarDadosAutomacao({required String nome, required String versao, required String nomePdv}) {
    return _platform.enviarDadosAutomacao(nome: nome, versao: versao, nomePdv: nomePdv);
  }

  /// Exemplo de uso:
  /// ```dart
  ///
  ///  Map<String, dynamic> responseTransaction = await PaygoTef.enviarEntradaTransacaoVenda(
  ///     identificadorTransacao: idTransacao,
  ///     operacao: OperacaoTefEnum.VENDA,
  ///     valor: 85000, //85039, //R$850,39 em centavos. nao existe ponto ou virgula
  ///     modalidadePagamento: ModalidadesPgtoEnum.PAGAMENTO_CARTAO,
  ///     tipoCartao: state.tipoCartao == 'crédito' ? CartoesPgtoEnum.CARTAO_CREDITO : CartoesPgtoEnum.CARTAO_DEBITO,
  ///     tipoFinanciamento: state.tipoFinanciamento,
  ///     nomeProvedor: 'DEMO', //'REDE', //
  ///     parcelas: 1,
  ///     estabelecimentoCNPJouCPF: '51897233000135', // Seu cnpj informado ao solicitar sandbox da paygo | cnpj do cliente quando em produção
  ///     documentoFiscal: '', // Exemplo de documento fiscal
  ///     campoLivre: 'Campo livre de teste', // Campo livre para dados adicionais
  ///   );
  ///
  ///  Map<String, dynamic> dadosResp = {};
  ///  if (responseTransaction['map'] is Map<String, dynamic>) {
  ///    dadosResp.addAll(responseTransaction['map'] as Map<String, dynamic>);
  ///  }
  ///
  ///  if (dadosResp['status'] == 'success') {
  ///  //ler comprovantes para impressao
  ///  }
  ///  ```

  static Future<Map<String, dynamic>> enviarEntradaTransacaoVenda({
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
    String? encodedString;
    String? decodedString;
    var response = await _platform.enviarEntradaTransacaoVenda(
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

    //decodificando url-encoded de comprovante completo
    if (response['map'] is Map<String, dynamic>) {
      Map<String, dynamic> responseMap = response['map'];
      encodedString = responseMap[PaygoTef.keyComprovanteCompleto];
      if (encodedString != null && encodedString != '') {
        decodedString = await DecodeHtmlToStringHtml().call(encodedString);
        responseMap[PaygoTef.keyComprovanteCompleto] = decodedString;
      }
      //decodificando url-encoded de comprovante reduzido
      encodedString = responseMap[PaygoTef.keyComprovanteReduzidoPortador];
      if (encodedString != null && encodedString != '') {
        decodedString = await DecodeHtmlToStringHtml().call(encodedString);
        responseMap[PaygoTef.keyComprovanteReduzidoPortador] = decodedString;
      }
      //decodificando url-encoded de comprovante diferenciado loja
      encodedString = responseMap[PaygoTef.keyComprovanteDifLoja];
      if (encodedString != null && encodedString != '') {
        decodedString = await DecodeHtmlToStringHtml().call(encodedString);
        responseMap[PaygoTef.keyComprovanteDifLoja] = decodedString;
      }
      //decodificando url-encoded de comprovante diferenciado portador
      encodedString = responseMap[PaygoTef.keyComprovanteDifPortador];
      if (encodedString != null && encodedString != '') {
        decodedString = await DecodeHtmlToStringHtml().call(encodedString);
        responseMap[PaygoTef.keyComprovanteDifPortador] = decodedString;
      }
    }
    return response;
  }

  static Future<Map<String, dynamic>> enviarEntradaTransacaoVersao({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.enviarEntradaTransacaoVersao(identificadorTransacao: identificadorTransacao, operacao: operacao);
  }

  static Future<Map<String, dynamic>> cancelarTransacaoVenda({
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

  static Future<Map<String, dynamic>> exibePontoDeCapturaInstalado({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.exibePontoDeCapturaInstalado(
      identificadorTransacao: identificadorTransacao,
      operacao: operacao,
    );
  }

  static Future<Map<String, dynamic>> enviarEntradaTransacaoReimpressao({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.enviarEntradaTransacaoReimpressao(
      identificadorTransacao: identificadorTransacao,
      operacao: operacao,
    );
  }

  static Future<Map<String, dynamic>> enviarEntradaRelatorioResumido({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.enviarEntradaRelatorioResumido(
      identificadorTransacao: identificadorTransacao,
      operacao: operacao,
    );
  }

  static Future<Map<String, dynamic>> enviarEntradaRelatorioSintetico({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.enviarEntradaRelatorioSintetico(
      identificadorTransacao: identificadorTransacao,
      operacao: operacao,
    );
  }

  static Future<Map<String, dynamic>> enviarEntradaRelatorioDetalhado({
    required String identificadorTransacao,
    required PaygoTefOperacaoTefEnum operacao, //enum dentro de paygo_tef_operacoes_enum.dart
  }) {
    return _platform.enviarEntradaRelatorioDetalhado(
      identificadorTransacao: identificadorTransacao,
      operacao: operacao,
    );
  }
}
