import 'package:html/dom.dart';
import 'package:html/parser.dart' as html_parser;

class DecodeHtmlToStringHtml {
  Future<String> call(String urlEncodedText) async {
    if (urlEncodedText == null || urlEncodedText == '') {
      throw Exception('Err. DecodeHtmlToStringHtml \n urlEncodedText não pode ser vazio!');
    }
    try {
      // Decode URL
      final decoded = Uri.decodeFull(urlEncodedText);

      // Quebra em linhas e limpa
      final lines = decoded.split('\n').map((line) => line).where((line) => line.isNotEmpty);

      // Gera HTML com <p> para cada linha
      final htmlStringBuff = StringBuffer('<div style="font-family: monospace;">');
      for (var line in lines) {
        // htmlStringBuff.writeln('<p>${line.replaceAll(' ', '&nbsp;')}</p>');
        htmlStringBuff.writeln('<p>${line}</p>');
      }
      htmlStringBuff.write('</div>');
      Document document = html_parser.parse(htmlStringBuff.toString());
      // Parse para Document
      return htmlStringBuff.toString(); // document.body?.text ?? ''; //
    } catch (e, s) {
      rethrow;
    }
  }
}
