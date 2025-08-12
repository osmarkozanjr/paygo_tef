import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:html/parser.dart' as html_parser;
import 'package:html/dom.dart';
import 'package:paygo_tef/paygo_tef.dart';
import 'package:paygo_tef/src/utils/convert_string_html_to_bitmap_escopos_bytes.dart';

import '../enums/printer_type_enum.dart';

///Em caso de impressora 80mm usar conversão de string html diretamente para bytes
/// com [ConvertStringHtmlToEscPosBytes]
/// Caso a impressora seja 58mm, precisa ainda converter para bitmap de modo a compactar o
/// texto na largura máxima do comprovante (384px). Para isso usar [ConvertStringHtmlToBitmapEscoposBytes]
///
///
class ConvertStringHtmlToEscPosBytes {
  ConvertStringHtmlToEscPosBytes();
  List<int> bytes = [];
  Future<List<int>> call(String htmlDecodedString, PrintertypeEnum printerType) async {
    if (printerType == PrintertypeEnum.m58mm) {
      bytes = await ConvertStringHtmlToBitmapEscoposBytes().call(htmlDecodedString);
    } else {
      final profile = await CapabilityProfile.load();
      final generator = Generator(PaperSize.mm80, profile);
      bytes += generator.reset();
      bytes += generator.setGlobalFont(PosFontType.fontA);

      bytes += _parseHtmlLineEscPos(htmlDecodedString, generator);

      bytes += generator.cut();
    }
    return bytes;
  }

  List<int> _parseHtmlLineEscPos(String html, Generator generator) {
    final List<int> cmds = [];
    final document = html_parser.parse(html);
    final elements = document.body?.nodes ?? [];

    for (var node in elements) {
      if (node.nodeType == Node.TEXT_NODE) {
        cmds.addAll(generator.text(node.text ?? ''));
        continue;
      }

      if (node is Element) {
        final text = node.text.replaceAll('\u00A0', ' ');
        switch (node.localName) {
          case 'b':
            cmds.addAll(generator.text(text, styles: const PosStyles(bold: true)));
            break;

          case 'center':
            cmds.addAll(generator.text(text, styles: const PosStyles(align: PosAlign.center)));
            break;

          case 'br':
            cmds.addAll(generator.feed(1));
            break;

          case 'p':
            cmds.addAll(generator.text(text));
            break;

          case 'b' && 'center': // caso futuro mais complexo
            cmds.addAll(generator.text(text, styles: const PosStyles(bold: true, align: PosAlign.center)));
            break;

          default:
            cmds.addAll(generator.text(text));
            break;
        }
      }
    }

    return cmds;
  }
}
