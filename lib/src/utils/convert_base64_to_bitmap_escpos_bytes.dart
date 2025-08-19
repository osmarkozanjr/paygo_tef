import 'dart:convert';

import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:paygo_tef/paygo_tef.dart';
import 'package:image/image.dart' as img;
import 'package:flutter/foundation.dart';

class ConvertBase64ToBitmapEscPosBytes {
  ConvertBase64ToBitmapEscPosBytes();
  //xyz
  Future<List<int>> call(String base64Image, PaygoTefPrintertypeEnum printerType) async {
    if (base64Image == null || base64Image == '') {
      throw Exception('Err. ConvertBase64ToBitmapEscPosBytes \n base64Image não pode ser vazio!');
    }
    try {
      // 1. Decode base64 para Uint8List
      final imageBytes = base64Decode(base64Image.replaceAll('\n', ''));

      // 2. Decodifica para imagem da lib `image`
      final image = img.decodeImage(imageBytes);

      if (image == null) {
        throw Exception('Falha ao decodificar a imagem');
      }

      // var file = File('teste_comprovante.jpg');
      // final jpgBytes = img.encodeJpg(image);
      // await file.writeAsBytes(jpgBytes);

      // 3. Gerar comandos de impressão
      final profile = await CapabilityProfile.load();
      final generator = Generator(printerType == PaygoTefPrintertypeEnum.m58mm ? PaperSize.mm58 : PaperSize.mm80, profile);
      List<int> bytes = [];

      bytes += generator.reset();
      bytes += generator.imageRaster(image, align: PosAlign.center);
      bytes += generator.feed(2);
      bytes += generator.cut();

      return bytes;
    } catch (e, s) {
      const red = '\x1B[31m';
      const reset = '\x1B[0m';
      debugPrint('${red} Erro: $e$reset');
      debugPrintStack(stackTrace: s);
      rethrow;
    }
  }
}
