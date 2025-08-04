# Instruções para usar o plugin paygo_tef

## Problema com AAR

O plugin `paygo_tef` usa a biblioteca `PaygoTef-InterfaceAutomacao-v2.1.0.6.aar` que não pode ser incluída diretamente no plugin devido a limitações do Android Gradle Plugin.

## Solução

O projeto que usa este plugin deve incluir o AAR em seu próprio `build.gradle`.

### Passo 1: Copiar o AAR
Copie o arquivo `PaygoTef-InterfaceAutomacao-v2.1.0.6.aar` do diretório `android/libs/` do plugin para o diretório `android/app/libs/` do seu projeto.

### Passo 2: Adicionar dependência
No arquivo `android/app/build.gradle` do seu projeto, adicione:

```gradle
dependencies {
    // ... outras dependências ...
    implementation files('libs/PaygoTef-InterfaceAutomacao-v2.1.0.6.aar')
}
```

### Passo 3: Configurar repositório (se necessário)
No arquivo `android/build.gradle` do seu projeto, certifique-se de que o repositório flatDir está configurado:

```gradle
allprojects {
    repositories {
        // ... outros repositórios ...
        flatDir {
            dirs 'libs'
        }
    }
}
```

## Exemplo completo

```gradle
// android/app/build.gradle
dependencies {
    implementation files('libs/PaygoTef-InterfaceAutomacao-v2.1.0.6.aar')
    // ... outras dependências ...
}
```

Após essas configurações, o plugin funcionará corretamente com todas as classes da biblioteca Paygo TEF disponíveis. 
Após essas configurações, o plugin funcionará corretamente com todas as classes da biblioteca Paygo TEF disponíveis. 