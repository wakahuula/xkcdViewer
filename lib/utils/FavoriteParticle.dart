import 'dart:math';

import 'package:flutter/material.dart';
import 'package:pimp_my_button/pimp_my_button.dart';

class FavoriteParticle extends Particle {
  @override
  void paint(Canvas canvas, Size size, progress, seed) {
    Random random = Random(seed);
    int randomMirrorOffset = random.nextInt(8) + 1;

    CompositeParticle(
      children: [
        CircleMirror(
          numberOfParticles: 6,
          child: AnimatedPositionedParticle(
            begin: Offset(0.0, 10.0),
            end: Offset(0.0, 20.0),
            child: FadingRect(width: 4.0, height: 14.0, color: Colors.white),
          ),
          initialRotation: -pi / randomMirrorOffset,
        ),
        CircleMirror.builder(
          numberOfParticles: 4,
          particleBuilder: (index) {
            return IntervalParticle(
                child: AnimatedPositionedParticle(
                  begin: Offset(0.0, 14.0),
                  end: Offset(0.0, 30.0),
                  child: FadingTriangle(
                      baseSize: 6.0 + random.nextDouble() * 2.0,
                      heightToBaseFactor: 1.0 + random.nextDouble(),
                      variation: random.nextDouble(),
                      color: Colors.white),
                ),
                interval: Interval(0.0, 0.8));
          },
          initialRotation: -pi / randomMirrorOffset + 8,
        ),
      ],
    ).paint(canvas, size, progress, seed);
  }
}
