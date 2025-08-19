import 'package:flutter/material.dart';
import 'dart:async';
import 'package:paygo_tef/paygo_tef.dart';
import 'package:paygo_tef_example/keypad_numeric.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _amount = "0";
  String _result = "";
  String _receipt = "";

  final Map<String, dynamic> _automationData = {
    'nome': 'PayGo TEF Example',
    'versao': '1.0.0',
    'nomePdv': 'pdv_01',
  };

  final String cpfCnpjEstabelecimento = '**INFORMAR CNPJ CADASTRADO AQUI**'; // troque aqui pelo seu cnpj/cpf de
  //desenvolvedor em modo homologação e cnpj do cliente em produção;

  Future<void> _configureAutomation() async {
    //toda transação com o paygo tef precisa antes de quaquer coisa
    //passar os dados da automação.
    try {
      await PaygoTef.enviarDadosAutomacao(
        nome: _automationData['nome'],
        versao: _automationData['versao'],
        nomePdv: _automationData['nomePdv'],
      );
    } catch (e) {
      setState(() {
        _result = "Falha ao configurar automação: $e";
      });
    }
  }

  Future<void> _makePayment() async {
    await _configureAutomation();

    setState(() {
      _result = "Iniciando pagamento...";
      _receipt = "";
    });

    try {
      // Convert amount from "R$ 12,34" to 1234 //No sandbox a REDE não
      //aceita valores na casa dos centavos  como R$12,34, apenas R$12,00
      //em produção, normal, basta enviar todo como centavos.
      final valueInCents = int.parse(_amount.replaceAll(RegExp(r'[^0-9]'), ''));

      if (valueInCents <= 0) {
        setState(() {
          _result = "Valor deve ser maior que zero.";
        });
        return;
      }
      //caso ocorra o erro: Erro na automação: br.com.setis.interfaceautomacao.AplicacaoNaoInstaladaExcecao
      // verifique se preencheu corretamente o cnpj do estabelecimento e se tem instalado
      //no dispositivo o app do tef (PGIntegrado). Sem o app do tef não é possível testar.
      Map<String, dynamic> responseTransaction = await PaygoTef.enviarEntradaTransacaoVenda(
        identificadorTransacao: DateTime.now().millisecondsSinceEpoch.toString(),
        operacao: PaygoTefOperacaoTefEnum.VENDA,
        valor: valueInCents,
        modalidadePagamento: PaygoTefModalidadesPgtoEnum.PAGAMENTO_CARTAO,
        tipoCartao: PaygoTefCartoesPgtoEnum.CARTAO_CREDITO,
        tipoFinanciamento: PaygoTefFinanciamentosEnum.A_VISTA,
        nomeProvedor: 'DEMO',
        parcelas: 1,
        estabelecimentoCNPJouCPF: cpfCnpjEstabelecimento, // Adicione um CNPJ/CPF válido para testes
        documentoFiscal: '',
        campoLivre: 'Teste...',
      );

      Map<String, dynamic> data = {};
      if (responseTransaction['map'] is Map<String, dynamic>) {
        data.addAll(responseTransaction['map'] as Map<String, dynamic>);
      }

      String message = responseTransaction['message'] ?? 'Transação finalizada.';
      String? receiptHtml;
      String? decodedHtmlString = '';
      List<int> bytes = [];

      if (data['status'] == 'success') {
        //dentro do map de data[], estão diversas informações, incluindo os modelos de comprovante
        //utilize as constantes [PaygoTef.keyComprovanteDifPortador], [PaygoTef.keyComprovanteDifLoja],
        // [keyComprovanteGrafLojistaBase64], [PaygoTef.keyComprovanteGraficoPortadorBase64] ou
        // [PaygoTef.keyComprovanteReduzidoPortador] para venda. Nem sempre todos estarão disponíveis.
        // O [PaygoTef.keyComprovanteCompleto] funciona somente para transação de relatórios, não aparecem na
        //venda.
        receiptHtml = data[PaygoTef.keyComprovanteDifPortador] ?? data[PaygoTef.keyComprovanteDifLoja];
        if (receiptHtml != null) {
          decodedHtmlString = await DecodeHtmlToStringHtml().call(receiptHtml);
          //para obter a versão que a impressora térmica consegue imprimir, utilize adicionalmente
          //este passo: converter em bytes com [ConvertStringHtmlToEscPosBytes]
          bytes = await ConvertStringHtmlToEscPosBytes().call(decodedHtmlString, PaygoTefPrintertypeEnum.m58mm);
          //para imprimir no Android, a sujestão é utilizar a biblioteca print_bluetooth_thermal, passando os bytes para
          //a função PrintBluetoothThermal.writeBytes(bytes)
          setState(() {
            _receipt = decodedHtmlString!;
            // decodedhtmlString pode ser exibido como um html
            //considere usar a biblioteca flutter_html e fazer o
            // import 'package:flutter_html/flutter_html.dart';
          });
        }
      }

      setState(() {
        _result = message;
      });
    } catch (e) {
      setState(() {
        _result = "Erro durante o pagamento: $e";
      });
    }
  }

  Future<void> _setReimpressao() async {
    await _configureAutomation();

    setState(() {
      _result = "Iniciando reimpressão...";
      _receipt = "";
    });

    try {
      Map<String, dynamic> responseTransaction = await PaygoTef.enviarEntradaTransacaoReimpressao(
        identificadorTransacao: DateTime.now().millisecondsSinceEpoch.toString(),
        operacao: PaygoTefOperacaoTefEnum.REIMPRESSAO,
      );

      Map<String, dynamic> data = {};
      if (responseTransaction['map'] is Map<String, dynamic>) {
        data.addAll(responseTransaction['map'] as Map<String, dynamic>);
      }

      String message = responseTransaction['message'] ?? 'Reimpressão finalizada.';
      String? receiptHtml;
      String? decodedHtmlString = '';
      List<int> bytes = [];

      if (data['status'] == 'success') {
        receiptHtml = data[PaygoTef.keyComprovanteDifPortador] ?? data[PaygoTef.keyComprovanteDifLoja];
        if (receiptHtml != null) {
          decodedHtmlString = await DecodeHtmlToStringHtml().call(receiptHtml);
          //para obter a versão que a impressora térmica consegue imprimir, utilize adicionalmente
          //este passo: converter em bytes com [ConvertStringHtmlToEscPosBytes]
          bytes = await ConvertStringHtmlToEscPosBytes().call(decodedHtmlString, PaygoTefPrintertypeEnum.m58mm);
          //para scannear impressoras bluetooth, a sugestão é utilizar a biblioteca easy_blue_printer
          //para imprimir no Android, a sujestão é utilizar a biblioteca print_bluetooth_thermal, passando os bytes para
          //a função PrintBluetoothThermal.writeBytes(bytes)
          setState(() {
            _receipt = decodedHtmlString!;
            // decodedhtmlString pode ser exibido como um html
            //considere usar a biblioteca flutter_html e fazer o
            // import 'package:flutter_html/flutter_html.dart';
          });
        }
      }

      setState(() {
        _result = message;
      });
    } catch (e) {
      setState(() {
        _result = "Erro durante a reimpressão: $e";
      });
    }
  }

  Future<void> _setRelatorioDetalhado() async {
    Map<String, dynamic> responseTransaction = await PaygoTef.enviarEntradaRelatorioDetalhado(
      identificadorTransacao: DateTime.now().millisecondsSinceEpoch.toString(),
      operacao: PaygoTefOperacaoTefEnum.RELATORIO_DETALHADO,
    );

    Map<String, dynamic> dadosResp = {};
    if (responseTransaction['map'] is Map<String, dynamic>) {
      dadosResp.addAll(responseTransaction['map'] as Map<String, dynamic>);
    }

    String message = responseTransaction['message'] ?? 'Relat. detalhado.';
    String? decodedHtmlString = '';
    String? receiptHtml;

    if (dadosResp['status'] == 'success') {
      //O tipo de comprovante retornado para relatórios sempre
      //será com a constante [PaygoTef.keyComprovanteCompleto]
      if (dadosResp[PaygoTef.keyComprovanteCompleto] != null) {
        receiptHtml = dadosResp[PaygoTef.keyComprovanteCompleto];
        decodedHtmlString = await DecodeHtmlToStringHtml().call(receiptHtml!);
        setState(() {
          _receipt = decodedHtmlString!;
          // decodedhtmlString pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
        });
      }
    }

    setState(() {
      _result = message;
    });
  }

  Future<void> _setRelatorioResumido() async {
    Map<String, dynamic> responseTransaction = await PaygoTef.enviarEntradaRelatorioResumido(
      identificadorTransacao: DateTime.now().millisecondsSinceEpoch.toString(),
      operacao: PaygoTefOperacaoTefEnum.RELATORIO_RESUMIDO,
    );

    Map<String, dynamic> dadosResp = {};
    if (responseTransaction['map'] is Map<String, dynamic>) {
      dadosResp.addAll(responseTransaction['map'] as Map<String, dynamic>);
    }

    String message = responseTransaction['message'] ?? 'Relat. resumido.';
    String? decodedHtmlString = '';
    String? receiptHtml;

    if (dadosResp['status'] == 'success') {
      //O tipo de comprovante retornado para relatórios sempre
      //será com a constante [PaygoTef.keyComprovanteCompleto]
      if (dadosResp[PaygoTef.keyComprovanteCompleto] != null) {
        receiptHtml = dadosResp[PaygoTef.keyComprovanteCompleto];
        decodedHtmlString = await DecodeHtmlToStringHtml().call(receiptHtml!);
        setState(() {
          _receipt = decodedHtmlString!;
          // decodedhtmlString pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
        });
      }
    }

    setState(() {
      _result = message;
    });
  }

  Future<void> _setRelatorioSintetico() async {
    Map<String, dynamic> responseTransaction = await PaygoTef.enviarEntradaRelatorioSintetico(
      identificadorTransacao: DateTime.now().millisecondsSinceEpoch.toString(),
      operacao: PaygoTefOperacaoTefEnum.RELATORIO_SINTETICO,
    );

    Map<String, dynamic> dadosResp = {};
    if (responseTransaction['map'] is Map<String, dynamic>) {
      dadosResp.addAll(responseTransaction['map'] as Map<String, dynamic>);
    }

    String message = responseTransaction['message'] ?? 'Relat. sintético.';
    String? decodedHtmlString = '';
    String? receiptHtml;

    if (dadosResp['status'] == 'success') {
      //O tipo de comprovante retornado para relatórios sempre
      //será com a constante [PaygoTef.keyComprovanteCompleto]
      if (dadosResp[PaygoTef.keyComprovanteCompleto] != null) {
        receiptHtml = dadosResp[PaygoTef.keyComprovanteCompleto];
        decodedHtmlString = await DecodeHtmlToStringHtml().call(receiptHtml!);
        setState(() {
          _receipt = decodedHtmlString!;
          // decodedhtmlString pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
        });
      }
    }
    setState(() {
      _result = message;
    });
  }

  void _onNumberTap(String number) {
    setState(() {
      if (_amount == "0") {
        _amount = number;
      } else {
        _amount += number;
      }
    });
  }

  void _onBackspace() {
    setState(() {
      if (_amount.length > 1) {
        _amount = _amount.substring(0, _amount.length - 1);
      } else {
        _amount = "0";
      }
    });
  }

  void _onClear() {
    setState(() {
      _amount = "0";
      _result = "";
      _receipt = "";
    });
  }

  String _formatCurrency(String value) {
    final doubleValue = double.parse(value) / 100;
    return "R\$ ${doubleValue.toStringAsFixed(2).replaceAll('.', ',')}";
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('PayGo TEF Exemplo'),
          actions: [
            PopupMenuButton<String>(
              onSelected: (value) {
                if (value == 'cancelamento') {
                  //  _setCancelamento();
                }
                if (value == 'reimpressao') {
                  _setReimpressao();
                }
                if (value == 'relatorio_detalhado') {
                  _setRelatorioDetalhado();
                }
                if (value == 'relatorio_resumido') {
                  _setRelatorioResumido();
                }
                if (value == 'relatorio_sintetico') {
                  _setRelatorioSintetico();
                }
              },
              itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
                const PopupMenuItem<String>(
                  value: 'cancelamento',
                  child: Text('Cancelamento'),
                ),
                const PopupMenuItem<String>(
                  value: 'reimpressao',
                  child: Text('Reimpressão'),
                ),
                const PopupMenuItem<String>(
                  value: 'relatorio_detalhado',
                  child: Text('Relat. Detalhado'),
                ),
                const PopupMenuItem<String>(
                  value: 'relatorio_resumido',
                  child: Text('Relat. Resumido'),
                ),
                const PopupMenuItem<String>(
                  value: 'relatorio_sintetico',
                  child: Text('Relat. Sintético'),
                ),
                const PopupMenuItem<String>(
                  value: 'exibe_pdc',
                  child: Text('Exibe Ponto de captura'),
                ),
              ],
              icon: const Icon(Icons.settings),
            ),
          ],
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Display
            Expanded(
              child: Container(
                color: Colors.grey[200],
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    // Amount
                    Align(
                      alignment: Alignment.centerRight,
                      child: Text(
                        _formatCurrency(_amount),
                        style: const TextStyle(
                          fontSize: 48,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    // Result
                    Text(
                      _result,
                      style: TextStyle(
                        fontSize: 16,
                        color: _result.contains("Erro") ? Colors.red : Colors.black,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 8),
                    // Receipt
                    Expanded(
                      child: SingleChildScrollView(
                        child: Text(_receipt),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            // Keypad
            KeypadNumeric(
              onNumberTap: _onNumberTap,
              onBackspace: _onBackspace,
              onClear: _onClear,
              onConfirm: _makePayment,
            ),
          ],
        ),
      ),
    );
  }
}
