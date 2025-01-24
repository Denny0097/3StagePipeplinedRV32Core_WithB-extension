// mycpu is freely redistributable under the MIT License. See the file
// "LICENSE" for information on usage and redistribution of this file.

package riscv.core

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import riscv.Parameters

import riscv.core._

object ALUFunctions extends ChiselEnum {
  val zero, add, sub, sll, slt, xor, or, and, srl, sra, sltu = Value
  // Zba 
  val sh1add, sh2add, sh3add = Value
  // Zbb
  val andn, orn, xnor, 
      clz, ctz, cpop, 
      max, maxu, min, minu,
      sextb, sexth, zexth,
      rol, ror, rori,
      orcb, rev8  = Value
  // Zbc
  val clmul, clmulh, clmulr = Value
  // Zbs
  val bclr, bclri, bext, bexti, binv, binvi, bset, bseti = Value
}

class ALU extends Module {
  val io = IO(new Bundle {
    val func = Input(ALUFunctions())

    val op1 = Input(UInt(Parameters.DataWidth))
    val op2 = Input(UInt(Parameters.DataWidth))

    val result = Output(UInt(Parameters.DataWidth))
  })


  // val ShiftR  = Module(new ShiftRightB(32))
  // val ShiftL  = Module(new ShiftLeftB(32))
  // val CLZ     = Module(new CountLeadingZeros(32))
  // val CTZ     = Module(new CountTrailingZeros(32))
  // val SEXTB   = Module(new SextB(32))
  // val SEXTH   = Module(new SextH(32))
  // val ZEXTH   = Module(new ZextH(32))
  // val ORCB    = Module(new OrcB(32))
  // val REV8    = Module(new Rev8(32))


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
    is(ALUFunctions.sh1add) {
      io.result := B_Extension.ShiftLeftB(io.op1, 1.U)+ io.op2
    }
    // is(ALUFunctions.sh2add) {
    //   ShiftL.io.A_in := io.op1
    //   ShiftL.io.bits := 2.U

    //   io.result := ShiftL.io.A_out + io.op2
    // }
    // is(ALUFunctions.sh3add) {
    //   ShiftL.io.A_in := io.op1
    //   ShiftL.io.bits := 3.U

    //   io.result := ShiftL.io.A_out + io.op2
    // }

    // Zbb
    is(ALUFunctions.clz)  {
      io.result   :=  B_Extension.CountLeadingZeros(io.op1)
    }

    // Zbc

    // Zbs
    // is(ALUFunctions.bext) {

    //   ShiftR.io.A_in := io.op1
    //   ShiftR.io.bits := io.op2 & 31.U  // Mask RS2 to 5 bits (valid range: 0-31)

    //   io.result := ShiftR.io.A_out & 1.U
    // }
  }

}
