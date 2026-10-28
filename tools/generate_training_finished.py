"""Generate the short, non-telephone training completion chime."""

import math
import struct
import wave
from pathlib import Path


SAMPLE_RATE = 44_100
NOTES = ((523.25, 0.15), (659.25, 0.15), (783.99, 0.34))
GAP_SECONDS = 0.055
OUTPUT = Path(__file__).resolve().parents[1] / "app/src/main/res/raw/training_finished.wav"


def note_sample(frequency: float, time: float, duration: float) -> float:
    attack = min(1.0, time / 0.008)
    release = min(1.0, (duration - time) / 0.045)
    decay = math.exp(-3.2 * time / duration)
    phase = 2 * math.pi * frequency * time
    timbre = math.sin(phase) + 0.22 * math.sin(2 * phase) + 0.07 * math.sin(3 * phase)
    return 0.55 * attack * release * decay * timbre


def main() -> None:
    samples: list[int] = []
    for index, (frequency, duration) in enumerate(NOTES):
        count = round(duration * SAMPLE_RATE)
        for sample_index in range(count):
            value = note_sample(frequency, sample_index / SAMPLE_RATE, duration)
            samples.append(round(max(-1.0, min(1.0, value)) * 32767))
        if index < len(NOTES) - 1:
            samples.extend([0] * round(GAP_SECONDS * SAMPLE_RATE))

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(OUTPUT), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(SAMPLE_RATE)
        output.writeframes(struct.pack(f"<{len(samples)}h", *samples))


if __name__ == "__main__":
    main()
