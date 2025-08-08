import 'package:esc_pos_utils_plus/esc_pos_utils_plus.dart';
import 'package:html/parser.dart' as html_parser;
import 'package:html/dom.dart';

class ConvertListStringHtmlToBytesPrint {
  ConvertListStringHtmlToBytesPrint();

  Future<List<int>> call(String htmlDecodedString) async {
    List<int> bytes = [];

    const optionprinttype = '58 mm';
    final profile = await CapabilityProfile.load();
    final generator = Generator(optionprinttype == '58 mm' ? PaperSize.mm58 : PaperSize.mm80, profile);
    bytes += generator.reset();
    bytes += generator.setGlobalFont(PosFontType.fontA);

    bytes += _parseHtmlLine(htmlDecodedString, generator);

    bytes += generator.cut();
    return bytes;
  }

  List<int> _parseHtmlLine(String html, Generator generator) {
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
