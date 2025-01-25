
package riscv.core

import chisel3._
import chisel3.util._
import riscv.Parameters

// object ZbaFunctions extends ChiselEnum {
//   val sh1add, sh2add, sh3add = Value
// }

// object ZbbFunctions extends ChiselEnum {
//   val   andn, orn, xnor, 
//         clz, ctz, cpop, 
//         max, maxu, min, minu,
//         sextb, sexth, zexth,
//         rol, ror, rori,
//         orcb, rev8  = Value
// }

// object ZbcFunctions extends ChiselEnum {
//   val clmul, clmulh, clmulr = Value
// }

// object ZbsFunctions extends ChiselEnum {
//   val bclr, bclri, bext, bexti, binv, binvi, bset, bseti = Value
// }

object B_Extension{

  def ShiftRightB(op1: UInt, bits: UInt): UInt = (op1 >> bits).asUInt
  def ShiftLeftB(op1: UInt, bits: UInt): UInt = (op1 << bits).asUInt
  
  def InvertBits(op1: UInt): UInt = (~(op1)).asUInt
  def CountLeadingZeros(op1: UInt): UInt = PriorityEncoder(Reverse(op1))
  def CountTrailingZeros(op1: UInt): UInt = PriorityEncoder(op1)
  def CountPop(op1: UInt): UInt = op1.asBools.map(b => Mux(b, 1.U, 0.U)).reduce(_ + _)
  def Max(op1: SInt, op2: SInt): SInt = Mux(op1 < op2, op2, op1).asSInt
  def UnsignMax(op1: UInt, op2: UInt): UInt = Mux(op1 < op2, op2, op1)
  def Min(op1: SInt, op2: SInt): SInt = Mux(op1 < op2, op1, op2).asSInt
  def UnsignMin(op1: UInt, op2: UInt): UInt = Mux(op1 < op2, op1, op2)
  def signExtendedB(op1: UInt): UInt = Cat(Fill(24, op1(7)), op1(7, 0))
  def signExtendedH(op1: UInt): UInt = Cat(Fill(16, op1(15)), op1(15, 0))
  def zeroExtendedH(op1: UInt): UInt = Cat(0.U(16.W), op1(15, 0))
  def RotateLeft(op1: UInt, op2: UInt): UInt = {
    val shiftAmount = op2(4, 0) 
    Cat(op1 << shiftAmount, op1 >> (32.U - shiftAmount))
  }
    def RotateRight(op1: UInt, op2: UInt): UInt = {
    val shiftAmount = op2(4, 0) 
    Cat(op1 >> shiftAmount, op1 << (32.U - shiftAmount))
  }
  // def RotateRight(op1: UInt, op2:UInt): UInt = Cat(op1 >> op2, op1 << ("b11111".U - op2))
  def BitWiseORCombineByte(op1: UInt): UInt = {
    // Number of bytes in the input register (assuming 32-bit input)
    val numBytes = 32 / 8
    val inputBytes = Wire(Vec(numBytes, UInt(8.W)))  // Use Wire to allocate memory for the Vec
    // Extract each byte from the input UInt
    for (i <- 0 until numBytes) {
      inputBytes(i) := op1(8 * (i + 1) - 1, 8 * i)
    }
    // OR combine all bytes using reduce
    inputBytes.reduce(_ | _)
  }
  def ByteReverseRegister(op1: UInt): UInt = {
    // 計算字節數量
    val numBytes = 32 / 8
    // 提取每個字節
    val byteOrder = (0 until numBytes).map(i => op1(8 * (i + 1) - 1, 8 * i))
    // 反轉字節順序並拼接
    val reverseOrder = byteOrder.reverse
    // 將反轉後的字節轉換為UInt並返回
    reverseOrder.foldLeft(0.U)((acc, byte) => (acc << 8) | byte)
  }
   

  //Zbc
  // clmul..
  def CarryLessMultLow(op1: UInt, op2: UInt): UInt = {
    val xlen = op1.getWidth
    // Build partial products
    val partials = (0 until xlen).map { i =>
      // If op2(i) is 1, then partial product is (op1 << i), otherwise 0
      Mux(op2(i), op1 << i, 0.U(xlen.W))
    }
    // XOR them all together
    partials.reduce(_ ^ _)
  }


  def CarryLessMultHigh(op1: UInt, op2: UInt): UInt = {
    val xlen = op1.getWidth
    val partials = (1 until xlen - 1).map { i =>
      Mux(op2(i), op1 >> (xlen - i), 0.U(xlen.W))
    }
    partials.reduce(_ ^ _)
  }


  def CarryLessMultReversed(op1: UInt, op2: UInt): UInt = {
    val xlen = op1.getWidth
    val partials = (0 until xlen).map { i =>
      Mux(op2(i), op1 >> (xlen - i - 1), 0.U(xlen.W))
    }
    partials.reduce(_ ^ _)
  }



  // Zbs
  // Clear bit at index op2(4,0)
  def SingleBitClear(op1: UInt, op2: UInt): UInt = {
    val shiftAmt = op2(4, 0)  // 0..31
    op1 & ~(1.U(32.W) << shiftAmt)
  }

  // Extract bit at index op2(4,0) as a Bool
  def SingleBitExtract(op1: UInt, op2: UInt): Bool = {
    val shiftAmt = op2(4, 0)
    (op1 >> shiftAmt)(0)
  }

  // Invert bit at index op2(4,0)
  def SingleBitInvert(op1: UInt, op2: UInt): UInt = {
    val shiftAmt = op2(4, 0)
    op1 ^ (1.U(32.W) << shiftAmt)
  }

  // Set bit at index op2(4,0)
  def SingleBitSet(op1: UInt, op2: UInt): UInt = {
    val shiftAmt = op2(4, 0)
    op1 | (1.U(32.W) << shiftAmt)
  }  
}


// // Taking OR of every byte individually and then combining the result
// class OrcB(N: Int) extends Module {
//   val io = IO(new Bundle {
//     val A_in = Input(UInt(N.W)) 
//     val A_out = Output(UInt(N.W))
//   })

//   // Number of bytes in the input register
//   val numBytes = N / 8
//   val inputBytes = Wire(Vec(numBytes, UInt(8.W)))

//   // Extract each byte
//   for (i <- 0 until numBytes) {
//     inputBytes(i) := io.A_in(8 * (i + 1) - 1, 8 * i)
//   }

//   // OR combine each byte
//   val outputBytes = Wire(Vec(numBytes, UInt(8.W)))

//   for (i <- 0 until numBytes) {
//     // Check if any bit in the byte is set
//     when(inputBytes(i) === 0.U) {
//       outputBytes(i) := 0x00.U
//     }.otherwise {
//       outputBytes(i) := 0xFF.U
//     }
//   }
  
//   // Combine the processed bytes into the output register
//   io.A_out := outputBytes.asUInt
// }

// // Reversing the order of bytes
// class Rev8(N: Int) extends Module {
//   val io = IO(new Bundle {
//     val A_in = Input(UInt(N.W))  
//     val A_out = Output(UInt(N.W)) 
//   })

//   // Calculate the number of bytes in the register
//   val numBytes = N / 8

//   // Reverse the bytes
//   val ByteOrder = Wire(Vec(numBytes, UInt(8.W)))
//   for (i <- 0 until numBytes) {
//     ByteOrder(i) := io.A_in((i + 1) * 8 - 1, i * 8) // Extract byte i
//   }

//   // Concatenate reversed bytes and assign to ReverseOrder
//   val ReverseOrder = ByteOrder.reverse

//   // Convert to Vec and then to UInt
//   io.A_out := VecInit(ReverseOrder).asUInt 
// }
