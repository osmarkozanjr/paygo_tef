import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:flutter/services.dart';
import 'package:paygo_tef/paygo_tef.dart';
import 'package:image/image.dart' as img;
import 'package:html/parser.dart' as html_parser;
import 'package:html/dom.dart';

class ConvertStringHtmlToBitmapEscoposBytes {
  ConvertStringHtmlToBitmapEscoposBytes();

  Future<List<int>> call(String textHtmlString) async {
    // 1. Define fonte padrão (monoespaçada)
    try {
      // 1. Carrega a fonte bitmap
      final fontData = await rootBundle.load('assets/fonts/bitmap/PxPlus_IBM_VGA8.zip');

      final font = img.BitmapFont.fromZip(fontData.buffer.asUint8List());

      // 2. Pega o xAdvance do caractere espaço (id=32) ou, se não existir, pega de qualquer outro
      final spaceChar = font.characters[32]; // código ASCII do espaço
      final xAdvance = spaceChar?.xAdvance ?? font.characters.values.first.xAdvance;

      // 3. Parse HTML para texto limpo
      final parsedText = _parseHtmlToText(textHtmlString);

      // 4. Divide em linhas
      final lines = parsedText.split('\n');

      // 5. Calcula largura máxima com base na linha mais longa e no xAdvance da fonte
      final maxLineLength = lines.map((l) => l.length).fold<int>(0, (a, b) => a > b ? a : b);
      final imageWidth = (maxLineLength * xAdvance).clamp(1, 384); // evita largura zero e limita a 384px
      int offsetX = ((384 - imageWidth) / 2).round();
      offsetX = offsetX - 10;

      // 6. Calcula altura da imagem
      final imageHeight = (lines.length * font.lineHeight) + 50;

      // 7. Cria imagem
      final image = img.Image(width: imageWidth, height: imageHeight);

      // 8. Preenche fundo branco
      img.fill(image, color: image.getColor(255, 255, 255));

      // 9. Desenha texto
      img.drawString(image, parsedText, font: font, color: image.getColor(0, 0, 0), x: 0, y: 0);

      // 10. Gera bytes para impressão
      final profile = await CapabilityProfile.load();
      final generator = Generator(PaperSize.mm58, profile);

      List<int> bytes = [];
      bytes += generator.reset();
      bytes.addAll(generator.imageRaster(image, align: PosAlign.center));
      bytes += generator.feed(3);
      bytes += generator.cut();
      return bytes;
    } catch (e, s) {
      rethrow;
    }
  }

  String _parseHtmlToText(String html) {
    final document = html_parser.parse(html);
    final elements = document.body?.nodes ?? [];
    final List<String> textLines = [];

    for (var node in elements) {
      if (node.nodeType == Node.TEXT_NODE) {
        final text = node.text?.trim() ?? '';
        if (text.isNotEmpty) {
          textLines.add(text);
        }
        continue;
      }

      if (node is Element) {
        final text = node.text.replaceAll('\u00A0', ' '); //removi trim
        if (text.isNotEmpty) {
          switch (node.localName) {
            case 'b':
              // Texto em negrito - mantém o texto mas pode aplicar estilo diferente se necessário
              textLines.add(text);
              break;

            case 'center':
              // Texto centralizado - mantém o texto mas pode aplicar alinhamento se necessário
              textLines.add(text);
              break;

            case 'br':
              // Quebra de linha
              textLines.add('');
              break;

            case 'p':
              // Parágrafo - adiciona o texto e uma quebra de linha
              textLines.add(text);
              textLines.add('');
              break;

            default:
              // Outros elementos - adiciona o texto
              textLines.add(text);
              break;
          }
        }
      }
    }

    return textLines.join('\n');
  }
}
