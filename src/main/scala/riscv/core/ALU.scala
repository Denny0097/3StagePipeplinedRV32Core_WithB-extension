// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv.core

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import riscv.Parameters

object ALUFunctions extends ChiselEnum {
  val zero, add, sub, sll, slt, xor, or, and, srl, sra, sltu = Value
}

class ALU extends Module {
  val io = IO(new Bundle {
    val func = Input(ALUFunctions())

    val op1 = Input(UInt(Parameters.DataWidth))
    val op2 = Input(UInt(Parameters.DataWidth))

    val result = Output(UInt(Parameters.DataWidth))
  })


  val ShiftL  = Module(new ShiftLeft(32))
  val ShiftR  = Module(new ShiftRight(32))
  val Add     = Module(new Adder(32))
  val Invert  = Module(new InvertBits(32))
  val AND     = Module(new ANDBits(32))
  val OR      = Module(new ORBits(32))
  val XOR     = Module(new XORBits(32))
  val CLZ     = Module(new CountLeadingZeros(32))
  val CTZ     = Module(new CountTrailingZeros(32))
  val CPOP    = Module(new CountPopulation(32))
  val MAX     = Module(new MaxInstruction(32))
  val MAXU    = Module(new MaxUInstruction(32))
  val MIN     = Module(new MinInstruction(32))
  val MINU    = Module(new MinUInstruction(32))
  val SEXTB   = Module(new SextB(32))
  val SEXTH   = Module(new SextH(32))
  val ZEXTH   = Module(new ZextH(32))
  val ORCB    = Module(new OrcB(32))
  val REV8    = Module(new Rev8(32))


  io.result := 0.U
  switch(io.func) {
    is(ALUFunctions.add) {
      io.result := io.op1 + io.op2
    }
    is(ALUFunctions.sub) {
      io.result := io.op1 - io.op2
    }
    is(ALUFunctions.sll) {
      io.result := io.op1 << io.op2(4, 0)
    }
    is(ALUFunctions.slt) {
      io.result := io.op1.asSInt < io.op2.asSInt
    }
    is(ALUFunctions.xor) {
      io.result := io.op1 ^ io.op2
    }
    is(ALUFunctions.or) {
      io.result := io.op1 | io.op2
    }
    is(ALUFunctions.and) {
      io.result := io.op1 & io.op2
    }
    is(ALUFunctions.srl) {
      io.result := io.op1 >> io.op2(4, 0)
    }
    is(ALUFunctions.sra) {
      io.result := (io.op1.asSInt >> io.op2(4, 0)).asUInt
    }
    is(ALUFunctions.sltu) {
      io.result := io.op1 < io.op2
    }
    // Zba
    is(ZbaFunctions.sh1add) {
      ShiftL.io.A_in := op1
      ShiftL.io.bits := 1.U

      io.result := ShiftL.io.A_out + io.op2
    }

    // Zbb
    is(ZbbFunctions.clz)  {
      CLZ.io.A_in := op1
      
      io.result   := CLZ.io.A_out
    }

    // Zbc

    // Zbs
    is(ZbsFunctions.bext) {

      ShiftR.io.A_in := op1
      ShiftR.io.bits := op2 & 31.U  // Mask RS2 to 5 bits (valid range: 0-31)

      io.result := ShiftR.io.A_out & 1.U
    }
  }

}
