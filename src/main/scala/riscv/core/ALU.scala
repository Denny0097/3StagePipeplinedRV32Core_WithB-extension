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

  val signed_op1 = io.op1.asSInt
  val signed_op2 = io.op2.asSInt

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
    is(ALUFunctions.sh2add) {
      io.result := B_Extension.ShiftLeftB(io.op1, 2.U)+ io.op2
    }
    is(ALUFunctions.sh3add) {
      io.result := B_Extension.ShiftLeftB(io.op1, 3.U)+ io.op2
    }

    // Zbb
    is(ALUFunctions.andn) {
      io.result := io.op1 & ~(io.op2)
    }
    is(ALUFunctions.orn) {
      io.result := io.op1 | ~(io.op2)
    }
    is(ALUFunctions.xnor) {
      io.result := ~(io.op1) & io.op2
    }

    is(ALUFunctions.clz)  {
      io.result   :=  B_Extension.CountLeadingZeros(io.op1)
    }
    is(ALUFunctions.ctz)  {
      io.result   :=  B_Extension.CountTrailingZeros(io.op1)
    }
    is(ALUFunctions.cpop)  {
      io.result   :=  B_Extension.CountPop(io.op1)
    }

    is(ALUFunctions.max)  {
      io.result   :=  B_Extension.Max(signed_op1, signed_op1).asUInt
    }
    is(ALUFunctions.maxu)  {
      io.result   :=  B_Extension.UnsignMax(io.op1, io.op2)
    }
    is(ALUFunctions.min)  {
      io.result   :=  B_Extension.Min(signed_op1, signed_op1).asUInt
    }
    is(ALUFunctions.minu)  {
      io.result   :=  B_Extension.UnsignMin(io.op1, io.op2)
    }

    is(ALUFunctions.sextb)  {
      io.result   :=  B_Extension.signExtendedB(io.op1)
    }
    is(ALUFunctions.sexth)  {
      io.result   :=  B_Extension.signExtendedH(io.op1)
    }
    is(ALUFunctions.zexth)  {
      io.result   :=  B_Extension.zeroExtendedH(io.op1)
    }

    is(ALUFunctions.rol)  {
      io.result   :=  B_Extension.RotateLeft(io.op1, io.op2)
    }
    is(ALUFunctions.ror)  {
      io.result   :=  B_Extension.RotateRight(io.op1, io.op2)
    }
    is(ALUFunctions.rori)  {// op2 from shamt
      io.result   :=  B_Extension.RotateRight(io.op1, io.op2)
    }

    is(ALUFunctions.orcb)  {
      io.result   :=  B_Extension.BitWiseORCombineByte(io.op1)
    }
    is(ALUFunctions.rev8)  {
      io.result   :=  B_Extension.ByteReverseRegister(io.op1)
    }


    // Zbc
    is(ALUFunctions.clmul)  {
      io.result   :=B_Extension.CarryLessMultLow(io.op1, io.op2)
    }
    is(ALUFunctions.clmulh)  {
      io.result   :=B_Extension.CarryLessMultHigh(io.op1, io.op2)
    }
    is(ALUFunctions.clmulr)  {
      io.result   :=B_Extension.CarryLessMultReversed(io.op1, io.op2)
    }


    // Zbs
    is(ALUFunctions.bclr)  {
      io.result   :=B_Extension.SingleBitClear(io.op1, io.op2)
    }
    is(ALUFunctions.bclri)  {
      io.result   :=B_Extension.SingleBitClear(io.op1, io.op2)
    }
    is(ALUFunctions.bext)  {
      io.result   :=B_Extension.SingleBitExtract(io.op1, io.op2)
    }
    is(ALUFunctions.bexti)  {
      io.result   :=B_Extension.SingleBitExtract(io.op1, io.op2)
    }
    is(ALUFunctions.binv)  {
      io.result   :=B_Extension.SingleBitInvert(io.op1, io.op2)
    }
    is(ALUFunctions.binvi)  {
      io.result   :=B_Extension.SingleBitInvert(io.op1, io.op2)
    }
    is(ALUFunctions.bset)  {
      io.result   :=B_Extension.SingleBitSet(io.op1, io.op2)
    }
    is(ALUFunctions.bseti)  {
      io.result   :=B_Extension.SingleBitSet(io.op1, io.op2)
    }
  }
}
