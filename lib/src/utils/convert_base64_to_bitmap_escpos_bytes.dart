import 'dart:convert';

import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:paygo_tef/paygo_tef.dart';
import 'package:image/image.dart' as img;

class ConvertBase64ToBitmapEscPosBytes {
  ConvertBase64ToBitmapEscPosBytes();

  Future<List<int>> call(String base64Image, PrintertypeEnum printerType) async {
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
    final generator = Generator(printerType == PrintertypeEnum.m58mm ? PaperSize.mm58 : PaperSize.mm80, profile);
    List<int> bytes = [];

    bytes += generator.reset();
    bytes += generator.imageRaster(image, align: PosAlign.center);
    bytes += generator.feed(2);
    bytes += generator.cut();

    return bytes;
  }
}
