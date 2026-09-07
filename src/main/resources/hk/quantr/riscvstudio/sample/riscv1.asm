# RISC-V Assembler & Simulator
# Sample program: print a string via ecall

        .section .text
        .globl  _start

_start:
        j       main

main:
        la      a0, msg_hello
        li      a7, 4
        ecall
        li      a0, 0
        li      a7, 10
        ecall

loop:
        j       loop

        .section .data
msg_hello:
        .asciz  "Hello, RISC-V!"
msg_exit:
        .asciz  "Goodbye."
