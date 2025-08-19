# paygo_tef

Este é um projeto para viabilizar a comunicação entre apps Flutter com a biblioteca 
PayGo Tef (Android).

## Pré-requisitos

1. É necessário solicitar o uso da área de testes aqui:
(https://paygodev.readme.io/docs/passo-a-passo-homologa%C3%A7%C3%A3o)

2. É necessário possuir um pinpad para capturar dados do cartão de crédito.

2. É necessário baixar o kit de integração disponível no topo desta página:
(https://paygodev.readme.io/docs/kit-de-integra%C3%A7%C3%A3o)
*Importante: Este plugin já conta com uma versão da biblioteca Interface automação. É provável 
que a versão deste kit de integração esteja em uma versão superior da biblioteca, o que não é 
um problema, pois você não irá utilizar este arquivo . Este plugin já tem uma cópia da última 
versão disponível quando foi desenvolvido. 
O arquivo que você precisa do kit, está dispoível para homologação na pasta > Desenvolvimento > Paygo Integrado CERT (apk) . A versã ode produção está na pasta > Produção > Padrão 

## Configurando seu app:

1. Referenciar o plugin no seu Pubspec.yaml
2. No build.gradle (ou build.gradle.ks) do caminho android/app/build.gradle adicione em dependencies:
dependencies{
    //importante: Verificar a versão que vem junto com este plugin paygoTef
    implementation(name: 'PaygoTef-InterfaceAutomacao-v2.1.0.6', ext: 'aar') 
    

}

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/to/develop-plugins),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

