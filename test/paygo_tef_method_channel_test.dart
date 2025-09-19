import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:paygo_tef/src/paygo_tef_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelPaygoTef platform = MethodChannelPaygoTef();
  const MethodChannel channel = MethodChannel('br.com.okjsolucoes.paygo_tef');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      if (methodCall.method == 'getPlatformVersion') {
        return '42';
      }
      return null;
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
