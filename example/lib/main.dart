import 'package:flutter/material.dart';
import 'dart:async';
import 'package:paygo_tef/paygo_tef.dart';
import 'package:paygo_tef_example/dialog_html.dart';
import 'package:paygo_tef_example/keypad_numeric.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _amount = "0";
  String _result = "";
  String _recibo = "";

  final Map<String, dynamic> _automationData = {
    'nome': 'PayGo TEF Example',
    'versao': '1.0.0',
    'nomePdv': 'pdv_01',
  };

  final String cpfCnpjEstabelecimento = '**INFORMAR CNPJ CADASTRADO AQUI**'; // troque aqui pelo seu cnpj/cpf de
  //desenvolvedor em modo homologação e cnpj do cliente em produção;

  Future<void> _configurarDadosAutomacao() async {
    //toda transação com o paygo tef precisa antes de quaquer coisa
    //passar os dados da automação.
    try {
      await PaygoTef.enviarDadosAutomacao(
        nome: _automationData['nome'],
        versao: _automationData['versao'],
        nomePdv: _automationData['nomePdv'],
      );
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _result = "Falha ao configurar automação: $e";
      });
    }
  }

  Future<void> _fazerPagamento() async {
    await _configurarDadosAutomacao();

    if (!mounted) return;
    setState(() {
      _result = "Iniciando pagamento...";
      _recibo = "";
    });

    // Converta "R$ 12,34" para 1234 (multiplique x100)
    //para armazenar em variável int ou divida por 100 para
    //obter o valor com a casa dos centavos e exibir na tela.
    //Obs: No sandbox a REDE não aceita valores na casa dos
    //centavos  como R$12,34, apenas R$12,00.
    //Caso tentar realizar um pagamento assim, retornará "Não autorizado"
    //Em produção, a transação é aceita normalmente.

    final valueInCents = int.parse(_amount.replaceAll(RegExp(r'[^0-9]'), ''));

    if (valueInCents <= 0) {
      setState(() {
        _result = "Valor deve ser maior que zero.";
      });
      return;
    }
    //Caso ocorra o erro: Erro na automação: br.com.setis.interfaceautomacao.AplicacaoNaoInstaladaExcecao
    // verifique se preencheu corretamente o cnpj do estabelecimento e se tem instalado
    //no dispositivo o app do tef (PGIntegrado). Sem o app do tef não é possível testar.
    Map<String, dynamic> respostaTransacao = await PaygoTef.enviarEntradaTransacaoVenda(
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

    Map<String, dynamic> dadosResp = {};
    if (respostaTransacao['map'] is Map<String, dynamic>) {
      dadosResp.addAll(respostaTransacao['map'] as Map<String, dynamic>);
    }

    bool bImprimirViaEstabelecimento = false;
    bool bImprimirViaCliente = true;

    if (respostaTransacao['status'] == 'success') {
      try {
        //dentro do map de dadosResp[], estão diversas informações, incluindo os modelos de comprovante
        //utilize as constantes: [PaygoTef.keyComprovanteDifPortador], [PaygoTef.keyComprovanteDifLoja],
        // [keyComprovanteGrafLojistaBase64], [PaygoTef.keyComprovanteGraficoPortadorBase64] ou
        // [PaygoTef.keyComprovanteReduzidoPortador] para venda. Nem sempre todos estarão disponíveis.
        //OBS1:
        // O [PaygoTef.keyComprovanteCompleto] funciona somente para transação de relatórios, não aparecem na
        //venda.
        //OBS2:
        //O [PaygoTef.keyComprovanteHtmlStringTela] é usado para exibir comprovante em tela usando a biblioteca flutter_html

        //CAMPOS QUE SÃO RETORNADOS NA RESPOSTA DA VENDA
        //SALVE-OS NO SEU BANCO DE DADOS PARA USAR EM
        //UM POSSÍVEL CANCELAMENTO FUTURO.
        if (dadosResp['DataTransacao'] != null) {
          dadosResp['DataTransacao'];
        }
        if (dadosResp['referenciaLocalOriginal'] != null) {
          dadosResp['referenciaLocalOriginal'];
        }
        if (dadosResp['codigoAutorizacao'] != null) {
          dadosResp['codigoAutorizacao'];
        }
        if (dadosResp['nsuHost'] != null) {
          dadosResp['nsuHost'];
        }
        if (dadosResp['nsuLocal'] != null) {
          dadosResp['nsuLocal'];
        }
        if (dadosResp['nomeCartao'] != null) {
          dadosResp['nomeCartao'];
        }

        if (dadosResp['panCartao'] != null) {
          dadosResp['panCartao'];
        }
        if (dadosResp['aidCartao'] != null) {
          dadosResp['aidCartao'];
        }
        //////////////////////////////////////////

        ///COMPROVANTES
        Map<String, List<int>> printableBytesMap = {};
        List<int> bytes = [];

        //dando preferência para impressão gráfica
        if (dadosResp[PaygoTef.keyComprovanteGrafLojistaBase64] != null || dadosResp[PaygoTef.keyComprovanteGraficoPortadorBase64] != null) {
          if (dadosResp[PaygoTef.keyComprovanteGrafLojistaBase64] != null && (bImprimirViaEstabelecimento ?? false)) {
            bytes = await ConvertBase64ToBitmapEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteGrafLojistaBase64], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteGrafLojistaBase64] = bytes;
          }

          if (dadosResp[PaygoTef.keyComprovanteGraficoPortadorBase64] != null && (bImprimirViaCliente ?? false)) {
            bytes =
                await ConvertBase64ToBitmapEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteGraficoPortadorBase64], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteGraficoPortadorBase64] = bytes;
          }
        } else {
          //caso a impressão gráfica não esteja disponível para imprimir, a diferenciada estará
          if (dadosResp[PaygoTef.keyComprovanteDifLoja] != null && (bImprimirViaEstabelecimento ?? false)) {
            bytes = await ConvertStringHtmlToEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteDifLoja], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteDifLoja] = bytes;
          }

          if (dadosResp[PaygoTef.keyComprovanteDifPortador] != null && (bImprimirViaCliente ?? false)) {
            bytes = await ConvertStringHtmlToEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteDifPortador], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteDifPortador] = bytes;
          }
        }

        ///VOCÊ PRECISA IMPLEMENTAR UM PLUGIN DE IMPRESSÃO. SUGERIMOS O PrintBluetoothThermal PARA ANDROID
        for (final printBytes in printableBytesMap.values) {
          //TODO: Importar a biblioteca print_bluetooth_thermal para imprimir os printBytes na impressora
          //para imprimir no Android, a sujestão é utilizar a biblioteca print_bluetooth_thermal, passando os bytes para
          //a função PrintBluetoothThermal.writeBytes(bytes)
        }

        if (!mounted) return;
        setState(() {
          // dadosResp[PaygoTef.keyComprovanteHtmlStringTela] pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
          _recibo = dadosResp[PaygoTef.keyComprovanteHtmlStringTela] ?? '';

          _result = respostaTransacao['message'];
        });
        //EXIBINDO O COMPROVANTE EM TELA
        showRelatorio(_recibo);
        //
      } catch (err) {
        if (!mounted) return;
        setState(() {
          _result = '${respostaTransacao['message']}\n Err. Comprovante:  ${err.toString()}';
        });
      }
    } else {
      if (!mounted) return;
      if (dadosResp['status'] == 'pendent') {
        //existe uma transação anterior pendente que está sendo cancelada pela automação.
        setState(() {
          _result = '${respostaTransacao['message']} \n Estamos cancelando uma transação pendente.\n Realize uma nova tentativa.';
        });
      } else {
        //falhou
        setState(() {
          String err = respostaTransacao['message'];
          if (cpfCnpjEstabelecimento == '**INFORMAR CNPJ CADASTRADO AQUI**') {
            err += ' Você precisa informar o seu CNPJ registrado na paygo na variável cpfCnpjEstabelecimento!';
          }
          _result = err;
        });
      }
    }
  }

  Future<void> _setCancelamento() async {
    if (!mounted) return;
    setState(() {
      _result = "Iniciando cancelamento...";
      _recibo = "";
    });

    await _configurarDadosAutomacao();
    //TODO: criar uma tela para informar dados de cancelamento.),
    //Este é apenas um exemplo que como criar o rotina de cancelamento
    //Estamos usando dados fixos, mas você poderá capturar e armazenar
    //os dados na transação de venda e depois usá-los para cancelar uma venda.
    Map<String, dynamic> respostaTransacao = await PaygoTef.cancelarTransacaoVenda(
      identificadorTransacao: DateTime.now().millisecondsSinceEpoch.toString(),
      operacao: PaygoTefOperacaoTefEnum.CANCELAMENTO,
      valor: 1800,
      modalidadePagamento: PaygoTefModalidadesPgtoEnum.PAGAMENTO_CARTAO,
      tipoCartao: PaygoTefCartoesPgtoEnum.CARTAO_CREDITO,
      //tipoFinanciamento: state.tipoFinanciamento,
      nomeProvedor: 'DEMO',
      //parcelas: 1,
      estabelecimentoCNPJouCPF: cpfCnpjEstabelecimento, // Exemplo de CNPJ
      //documentoFiscal: '', // Exemplo de documento fiscal
      //campoLivre: 'Campo livre de teste', // Campo livre para dados adicionais
      nsuHost: '000412', //disponível no comprovante impresso como "Comprovante" ou via resposta da venda como nsuHost
      referenciaLocaloriginal:
          '00413', //disponível no comprovante impresso como "Ref" ou via resposta da venda como referenciaLocaloriginal (nem sempre é retornado)
      codigoAutorizacao: '00000414', //disponível no comprovante impresso como (consultar por AID no comprovante do PROVEDOR REDE)
      dataHoraTransacao: DateTime.parse('2025-08-29 15:52:00'),
    );

    Map<String, dynamic> dadosResp = {};
    if (respostaTransacao['map'] is Map<String, dynamic>) {
      dadosResp.addAll(respostaTransacao['map'] as Map<String, dynamic>);
    }

    if (!mounted) return;
    if (dadosResp['status'] == 'success') {
      setState(() {
        _result = respostaTransacao['message'];
      });
    } else {
      if (dadosResp['status'] == 'pendent') {
        //existe uma transação anterior pendente que está sendo cancelada pela automação.
        setState(() {
          _result = '${respostaTransacao['message']} \n Estamos cancelando uma transação pendente.\n Realize uma nova tentativa.';
        });
      } else {
        //falhou
        setState(() {
          _result = respostaTransacao['message'];
        });
      }
    }
  }

  Future<void> _setReimpressao() async {
    await _configurarDadosAutomacao();

    if (!mounted) return;
    setState(() {
      _result = "Iniciando reimpressão...";
      _recibo = "";
    });

    Map<String, dynamic> respostaTransacao = await PaygoTef.enviarEntradaTransacaoReimpressao(
      identificadorTransacao: DateTime.now().millisecondsSinceEpoch.toString(),
      operacao: PaygoTefOperacaoTefEnum.REIMPRESSAO,
    );

    Map<String, dynamic> dadosResp = {};
    if (respostaTransacao['map'] is Map<String, dynamic>) {
      dadosResp.addAll(respostaTransacao['map'] as Map<String, dynamic>);
    }

    bool bImprimirViaEstabelecimento = false;
    bool bImprimirViaCliente = true;

    if (respostaTransacao['status'] == 'success') {
      try {
        ///COMPROVANTES
        Map<String, List<int>> printableBytesMap = {};
        List<int> bytes = [];

        //dando preferência para impressão gráfica
        if (dadosResp[PaygoTef.keyComprovanteGrafLojistaBase64] != null || dadosResp[PaygoTef.keyComprovanteGraficoPortadorBase64] != null) {
          if (dadosResp[PaygoTef.keyComprovanteGrafLojistaBase64] != null && (bImprimirViaEstabelecimento ?? false)) {
            bytes = await ConvertBase64ToBitmapEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteGrafLojistaBase64], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteGrafLojistaBase64] = bytes;
          }

          if (dadosResp[PaygoTef.keyComprovanteGraficoPortadorBase64] != null && (bImprimirViaCliente ?? false)) {
            bytes =
                await ConvertBase64ToBitmapEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteGraficoPortadorBase64], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteGraficoPortadorBase64] = bytes;
          }
        } else {
          //caso a impressão gráfica não esteja disponível para imprimir, a diferenciada estará
          if (dadosResp[PaygoTef.keyComprovanteDifLoja] != null && (bImprimirViaEstabelecimento ?? false)) {
            bytes = await ConvertStringHtmlToEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteDifLoja], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteDifLoja] = bytes;
          }

          if (dadosResp[PaygoTef.keyComprovanteDifPortador] != null && (bImprimirViaCliente ?? false)) {
            bytes = await ConvertStringHtmlToEscPosBytes().call(dadosResp[PaygoTef.keyComprovanteDifPortador], PaygoTefPrintertypeEnum.m58mm);
            printableBytesMap[PaygoTef.keyComprovanteDifPortador] = bytes;
          }

          ///VOCÊ PRECISA IMPLEMENTAR UM PLUGIN DE IMPRESSÃO. SUGERIMOS O PrintBluetoothThermal PARA ANDROID
          for (final printBytes in printableBytesMap.values) {
            //TODO: Importar a biblioteca print_bluetooth_thermal para imprimir os printBytes na impressora
            //para imprimir no Android, a sujestão é utilizar a biblioteca print_bluetooth_thermal, passando os bytes para
            //a função PrintBluetoothThermal.writeBytes(bytes)
          }
        }
        if (!mounted) return;
        setState(() {
          // dadosResp[PaygoTef.keyComprovanteHtmlStringTela] pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
          _recibo = dadosResp[PaygoTef.keyComprovanteHtmlStringTela];
          _result = respostaTransacao['message'];
        });
      } catch (err) {
        if (!mounted) return;
        setState(() {
          _result = '${respostaTransacao['message']}\n Err. Comprovante:  ${err.toString()}';
        });
      }
    } else {
      if (!mounted) return;
      if (dadosResp['status'] == 'pendent') {
        //existe uma transação anterior pendente que está sendo cancelada pela automação.
        setState(() {
          _result = '${respostaTransacao['message']} \n Estamos cancelando uma transação pendente.\n Realize uma nova tentativa.';
        });
      } else {
        //falhou
        setState(() {
          _result = respostaTransacao['message'];
        });
      }
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

    if (dadosResp['status'] == 'success') {
      //O tipo de comprovante retornado para relatórios sempre
      //será com a constante [PaygoTef.keyComprovanteCompleto]
      if (dadosResp[PaygoTef.keyComprovanteHtmlStringTela] != null) {
        if (!mounted) return;
        setState(() {
          _recibo = dadosResp[PaygoTef.keyComprovanteHtmlStringTela];
          // decodedhtmlString pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
        });
      }
    }

    if (!mounted) return;
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

    String message = responseTransaction['message'] ?? 'Relat. detalhado.';

    if (dadosResp['status'] == 'success') {
      //O tipo de comprovante retornado para relatórios sempre
      //será com a constante [PaygoTef.keyComprovanteCompleto]
      if (dadosResp[PaygoTef.keyComprovanteHtmlStringTela] != null) {
        if (!mounted) return;
        setState(() {
          _recibo = dadosResp[PaygoTef.keyComprovanteHtmlStringTela];
          // decodedhtmlString pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
        });
      }
    }

    if (!mounted) return;
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

    String message = responseTransaction['message'] ?? 'Relat. detalhado.';

    if (dadosResp['status'] == 'success') {
      //O tipo de comprovante retornado para relatórios sempre
      //será com a constante [PaygoTef.keyComprovanteCompleto]
      if (dadosResp[PaygoTef.keyComprovanteHtmlStringTela] != null) {
        if (!mounted) return;
        setState(() {
          _recibo = dadosResp[PaygoTef.keyComprovanteHtmlStringTela];

          // decodedhtmlString pode ser exibido como um html
          //considere usar a biblioteca flutter_html e fazer o
          // import 'package:flutter_html/flutter_html.dart';
        });
      }
    }
    if (!mounted) return;
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
      _recibo = "";
    });
  }

  String _formatCurrency(String value) {
    final doubleValue = double.parse(value) / 100;
    return "R\$ ${doubleValue.toStringAsFixed(2).replaceAll('.', ',')}";
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('PayGo TEF Exemplo'),
        actions: [
          PopupMenuButton<String>(
            onSelected: (value) async {
              if (value == 'cancelamento') {
                _setCancelamento();
              }
              if (value == 'reimpressao') {
                _setReimpressao();
              }
              if (value == 'relatorio_detalhado') {
                await _setRelatorioDetalhado();
                showRelatorio(_recibo);
              }
              if (value == 'relatorio_resumido') {
                await _setRelatorioResumido();
                showRelatorio(_recibo);
              }
              if (value == 'relatorio_sintetico') {
                await _setRelatorioSintetico();
                showRelatorio(_recibo);
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
                  // Expanded(
                  //   child: SingleChildScrollView(
                  //     child: Text(_recibo),
                  //   ),
                  // ),
                ],
              ),
            ),
          ),
          // Keypad
          KeypadNumeric(
            onNumberTap: _onNumberTap,
            onBackspace: _onBackspace,
            onClear: _onClear,
            onConfirm: _fazerPagamento,
          ),
        ],
      ),
    );
  }

  showRelatorio(String htmlStringComprovante) {
    showDialog(
      context: context,
      builder: (_) => DialogHtml(
        htmlContent: htmlStringComprovante,
        tituloLabel: 'RELATORIO',
      ),

      // Aparece com efeito circular quando o status muda
    );
  }
}
