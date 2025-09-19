# paygo_tef
Este é um projeto para viabilizar a comunicação entre apps Flutter com a biblioteca 
PayGo Tef (Android).

## Pré-requisitos
1. É necessário solicitar o uso da área de testes aqui:
`(https://paygodev.readme.io/docs/passo-a-passo-homologa%C3%A7%C3%A3o)`

2. É necessário possuir um pinpad para capturar dados do cartão de crédito.

3. É necessário baixar o kit de integração disponível no topo desta página:
`(https://paygodev.readme.io/docs/kit-de-integra%C3%A7%C3%A3o)`
*Importante: Este plugin já conta com uma versão da biblioteca Interface automação. É provável 
que a versão deste kit de integração esteja em uma versão superior da biblioteca, o que não é 
um problema, pois você não irá utilizar este arquivo . Este plugin já tem uma cópia da última 
versão disponível quando foi desenvolvido. 
O arquivo que você precisa do kit, está dispoível para homologação na pasta `Desenvolvimento > Paygo Integrado CERT (apk)` . Já a versão de produção está na pasta > `Produção > Padrão` 

## Configurando seu app:
1. Referenciar o plugin no seu Pubspec.yaml dentro de dependencies.
2. No seu app, edite o arquivo build.gradle (ou build.gradle.ks) do caminho android/app/  com a seguinte informação:


```Java

android{

 repositories {
        flatDir {
            dirs project(':paygo_tef').file('libs')
        }

    }

} 


dependencies {
  
  //necesário para paygo_tef 
  implementation 'org.apache.commons:commons-lang3:3.9'
} 

```


3. Fazer o import da biblioteca no arquivo que for trabalhae:
`import 'package:paygo_tef/paygo_tef.dart';`

## Impressão de comprovantes
A biblioteca já disponibiliza a conversão de string para bytes, mas não imprime o comprovante
Para imprimir no Android, sugerimos a biblioteca `print_bluetooth_thermal`, passando os bytes para
a função `PrintBluetoothThermal.writeBytes(bytes)` 

## Exibição de comprovantes em tela
Dentro desta biblioteca, você encontrará na pasta `android/example/` o main.dart como exemplo de como exibir na tela o comprovante. Para testar, basta acessar a pasta e executar o comando `flutter run` ou `fvm flutter run`, casp utilize FVM.

## Enums
Os enums foram criados para fornecer uma forma confiável de capturar os valores recebidos no map da biblioteca.

## Enum de tamanho de impressão
Para gerar os bytes, é necessário informar para oconversor `ConvertStringHtmlToEscPosBytes` qual o tamanho da sua impressora de recibos. Os tamanhos disponíveis são 58mm e 80mm. Utilize os enums correspontentes: `PaygoTefPrintertypeEnum.m58mm` ou `PaygoTefPrintertypeEnum.m80mm`


No código de example existe este trecho:

```dart  

      //>>>respostaTransacao é a resposta da transação de venda


      Map<String, dynamic> dadosResp = {};
    if (respostaTransacao['map'] is Map<String, dynamic>) {
      dadosResp.addAll(respostaTransacao['map'] as Map<String, dynamic>);
    }

    bool bImprimirViaEstabelecimento = false;
    bool bImprimirViaCliente = true;
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
        
```

## Enums de Operação
A biblioteca tem todos os enums de operação da paygo, mas implementamos apenas as operações de `PaygoTefOperacaoTefEnum.VENDA`, `PaygoTefOperacaoTefEnum.REIMPRESSAO`, `PaygoTefOperacaoTefEnum.CANCELAMENTO`, `PaygoTefOperacaoTefEnum.RELATORIO_DETALHADO`, `PaygoTefOperacaoTefEnum.RELATORIO_RESUMIDO`, `PaygoTefOperacaoTefEnum.RELATORIO_SINTETICO` e `PaygoTefOperacaoTefEnum.VERSAO`

## Enums de modalidade
 A biblioteca tem todos es enums de modalidade, mas provavelmente você utilizará apenas  PAGAMENTO_CARTAO ou PAGAMENTO_CARTEIRA_VIRTUAL (no caso de aceitar pix)  
`PaygoTefModalidadesPgtoEnum.PAGAMENTO_CARTAO`, `PaygoTefModalidadesPgtoEnum.PAGAMENTO_CARTEIRA_VIRTUAL`, `PaygoTefModalidadesPgtoEnum.PAGAMENTO_CHEQUE`, `PaygoTefModalidadesPgtoEnum.PAGAMENTO_DINHEIRO`,

  
 ## Enums de Tipo Cartão
 Os enums são: `PaygoTefCartoesPgtoEnum.CARTAO_CREDITO`, `PaygoTefCartoesPgtoEnum.CARTAO_CREDITO`,  `PaygoTefCartoesPgtoEnum.CARTAO_DEBITO`, `PaygoTefCartoesPgtoEnum.CARTAO_DESCONHECIDO`, `PaygoTefCartoesPgtoEnum.ARTAO_PRIVATELABEL`,`PaygoTefCartoesPgtoEnum.CARTAO_VOUCHER`,

  

 ## Enums de Tipo Financiamento
 Os enums são:`PaygoTefFinanciamentosEnum.A_VISTA`,`PaygoTefFinanciamentosEnum.CREDITO_EMISSOR`,`PaygoTefFinanciamentosEnum.FINANCIAMENTO_NAO_DEFINIDO`,`PaygoTefFinanciamentosEnum.PARCELADO_EMISSOR`,`PaygoTefFinanciamentosEnum.PARCELADO_ESTABELECIMENTO`,
  `PaygoTefFinanciamentosEnum.PRE_DATADO`,




