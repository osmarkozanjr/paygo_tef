import 'package:flutter/material.dart';

class KeypadNumeric extends StatelessWidget {
  final void Function(String)? onNumberTap;
  final VoidCallback? onBackspace;
  final VoidCallback? onClear;
  final VoidCallback? onConfirm;

  const KeypadNumeric({
    super.key,
    this.onNumberTap,
    this.onBackspace,
    this.onClear,
    this.onConfirm,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: MediaQuery.of(context).size.height * 0.33,
      width: double.infinity,
      child: Padding(
        padding: const EdgeInsets.all(10.0),
        child: Row(
          children: [
            // Numeric keys (2/3 width)
            Expanded(
              flex: 2,
              child: Column(
                children: [
                  Expanded(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        _buildNumberButton(onNumberTap, '1'),
                        const SizedBox(width: 10),
                        _buildNumberButton(onNumberTap, '2'),
                        const SizedBox(width: 10),
                        _buildNumberButton(onNumberTap, '3'),
                      ],
                    ),
                  ),
                  const SizedBox(height: 10),
                  Expanded(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        _buildNumberButton(onNumberTap, '4'),
                        const SizedBox(width: 10),
                        _buildNumberButton(onNumberTap, '5'),
                        const SizedBox(width: 10),
                        _buildNumberButton(onNumberTap, '6'),
                      ],
                    ),
                  ),
                  const SizedBox(height: 10),
                  Expanded(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        _buildNumberButton(onNumberTap, '7'),
                        const SizedBox(width: 10),
                        _buildNumberButton(onNumberTap, '8'),
                        const SizedBox(width: 10),
                        _buildNumberButton(onNumberTap, '9'),
                      ],
                    ),
                  ),
                  const SizedBox(height: 10),
                  Expanded(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        _buildNumberButton(onNumberTap, '0', isZero: true),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 10),
            // Action keys (1/3 width)
            Expanded(
              flex: 1,
              child: Column(
                children: [
                  Expanded(
                    flex: 1,
                    child: _buildActionButton(
                      padding: const EdgeInsets.only(bottom: 4.5),
                      icon: Icons.close,
                      color: Colors.red,
                      onTap: onClear,
                    ),
                  ),
                  Expanded(
                    flex: 1,
                    child: _buildActionButton(
                      padding: const EdgeInsets.only(top: 4.5),
                      icon: Icons.arrow_back,
                      color: Colors.yellow,
                      onTap: onBackspace,
                    ),
                  ),
                  const SizedBox(height: 10),
                  Expanded(
                    flex: 2, // Ocupa o espaço de duas linhas
                    child: _buildActionButton(
                      icon: Icons.check,
                      color: Colors.green,
                      onTap: onConfirm,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNumberButton(
    final void Function(String)? onNumberTap,
    String number, {
    bool isZero = false,
  }) {
    return Expanded(
      flex: isZero ? 3 : 1,
      child: ElevatedButton(
        onPressed: () => onNumberTap?.call(number),
        style: ElevatedButton.styleFrom(
          backgroundColor: Colors.grey[200],
          foregroundColor: Colors.black,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(4),
          ),
        ),
        child: Text(
          number,
          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _buildActionButton({
    EdgeInsetsGeometry padding = EdgeInsets.zero,
    required IconData icon,
    required Color color,
    required VoidCallback? onTap,
  }) {
    return SizedBox.expand(
      child: Padding(
        padding: padding,
        child: ElevatedButton(
          onPressed: onTap,
          style: ElevatedButton.styleFrom(
            backgroundColor: color,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(4),
            ),
          ),
          child: Icon(icon, color: Colors.white, size: 32),
        ),
      ),
    );
  }
}
