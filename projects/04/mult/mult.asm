// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)


//initialize sum as zero
@sum
M=0
//create r1 variable to handle second register
@R1
D=M
//initialize count as the value of r1
@count
M=D

//add r0 to sum r1 times
(LOOP)
//end condition is when count = 0
@count
D=M
@END
D;JEQ
//looped code block begins; gets value of r0
@R0
D=M
//adds r0 to sum
@sum
M=M+D
//decrement count by one to move toward end condition (count=0)
@count
M=M-1
@LOOP
0;JMP
(END)

//set sum to M (which was incremented in loop)
@sum
D=M
//set R2 to M
@R2
M=D