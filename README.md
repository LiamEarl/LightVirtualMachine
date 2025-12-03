Lightweight Virtual Machine

This project features a compact virtual machine designed around a tightly constrained architecture:

65 KB of RAM

65 KB of virtual disk storage

65 KB of VRAM powering a 256×256-pixel display

Despite its small footprint, the VM simulates a surprisingly rich hardware environment, including:

A software-implemented cache

A register set

Variable-length instructions modeled after real processor designs

<img width="515" height="396" src="https://github.com/user-attachments/assets/00bf62fe-4395-43b9-a992-f6dd5b2c6078" alt="Virtual Machine Screenshot"/>

To make development more approachable, the machine includes its own assembly-style programming language, allowing you to write readable instructions rather than raw binary opcodes.
Just edit code.txt, and run the Compiler program. (The compiler code is not my best work)
The virtual disk comes preloaded with a demo program — Pong.
On startup, the VM automatically loads Pong into RAM and executes it, giving you an immediate interactive demonstration of the system’s capabilities.

<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/2e78cfd7-9704-4a53-bc56-a1da56979c5a" />
